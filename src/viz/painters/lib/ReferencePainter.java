package viz.painters.lib;

import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import viz.ProViz;
import viz.animation.motion.Direction;
import viz.animation.motion.Motion;
import viz.animation.motion.Move;
import viz.animation.motion.move.LinearPath;
import viz.animation.motion.move.Path;
import viz.painters.Painter;
import viz.painters.VizPainterManager;
import viz.painters.graphics.StraightArrow;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Paints this painter's variable and draws an arrow to an aliasing painter. This shows
 * that this variable is one of the aliases to the target object.
 * 
 * @author JW
 */
public class ReferencePainter extends VariablePainter {

	//private Hashtable<Painter, StraightArrow> arrows = new Hashtable<Painter, StraightArrow>();
	private StraightArrow arrow = new StraightArrow();
	private boolean isArrowMoving = false;
	private Painter aliasingPainter = null;

	public ReferencePainter(IVizVariable vvar, VizCanvas canvas) throws Exception {
		super(vvar, canvas);
		this.setShouldCreateFieldPainters(false);
		this.setLocation(10, 10);
	}

	/* (non-Javadoc)
	 * @see viz.painters.lib.VariablePainter#addToCanvas_userImp()
	 */
	@Override
	public void addToCanvas_userImp() {
		this.aliasingPainter = null;
		super.addToCanvas_userImp();
		IVizVariable var = this.getVariable();
		if (var.isObject() && !var.isNull()) {
			int x = this.getLocation().x + this.getWidth() / 2;
			int y = this.getLocation().y + this.getHeight() / 2;
			arrow.setSource(x, y);
			List<Painter> list = ProViz.getVPM().getAliasingPainters(var.getUniqueObjectID());
			//synchronized (this) {
				for (Painter painter : list) {
					if (painter != this) {
						//arrows.put(painter, new StraightArrow(x, y, painter.getLocation().x, painter.getLocation().y));
						arrow.setDestination(painter.getLocation().x, painter.getLocation().y);
						this.aliasingPainter = painter;
						break;
					}
				}
			//}
			if (this.aliasingPainter != null) {
				this.getCanvas().addPainterToBePainted(this);
				this.getCanvas().repaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.lib.VariablePainter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		super.paint_userImp();
		this.isArrowMoving = false;
	}

	/**
	 * If this painter's variable is assigned to another object, animation will be used
	 * to move this painter and its arrow to point to the new object.
	 * @see viz.painters.lib.VariablePainter#handleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		super.handleChange(change, source);
		if (change == Change.TO_NULL) {
	//Removes the arrow
			this.getCanvas().removePainterToBePainted(this);
			this.getCanvas().repaint();
		}
		else if (change == Change.NULL_TO_OBJ) {
	//Draws the arrow
			this.getCanvas().addPainterToBePainted(this);
			this.getCanvas().repaint();
		}
		else if (change != Change.VALUE_CHANGED) {
			//synchronized (this) {
				this.isArrowMoving = true;
				VizPainterManager vpm = ProViz.getVPM();
				List<Painter> list = vpm.getAliasingPainters(this.getVariable().getUniqueObjectID());
				for (Painter painter : list) {
					if (painter != this) {
						if (this.aliasingPainter == null) {
							this.aliasingPainter = painter;
	//Draws the arrow
							arrow.setDestination(painter.getLocation().x, painter.getLocation().y);
							this.isArrowMoving = false;
							this.getCanvas().addPainterToBePainted(this);
							this.getCanvas().repaint();
						}
						else {
							this.aliasingPainter = painter;
							int px = painter.getLocation().x - arrow.getDestX();
							int py = painter.getLocation().y - arrow.getDestY();
		//pathThis moves this reference painter
							LinearPath pathThis = new LinearPath(this, this.getLocation(), 
									new Point(this.getLocation().x + px, this.getLocation().y + py), Direction.UP);
		//pathArrow moves the end of the arrow (in moveMotion())
							LinearPath pathArrow = new LinearPath(this, new Point(arrow.getDestX(), 
									arrow.getDestY()), painter.getLocation(), Move.MOVE_SUBTREE);
							ProViz.getAnimationController().scheduleAnimation(this, 
									new Motion[] {pathThis, pathArrow}, new Painter[] {this});
							break;
						}
					}
				}
				if (aliasingPainter == null) {
					//Removes the arrow
					this.getCanvas().removePainterToBePainted(this);
					this.getCanvas().repaint();					
				}
			//}
		}
	};
	
	

	/* (non-Javadoc)
	 * @see viz.painters.Painter#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		arrow.setSource(x + this.getWidth() / 2, y + this.getHeight() / 2);
	}

	/**
	 * If the path's mode is Move.MOVE_SUBTREE, this moves the destination of the arrow; otherwise
	 * it moves this ReferencePainter
	 * @see viz.painters.Painter#moveMotion(java.awt.Point)
	 */
	@Override
	public void moveMotion(Point next, Path path) {
		if (path.getMode() == Move.MOVE_SUBTREE) {
			arrow.setDestination(next.x, next.y);
			this.getCanvas().repaint();			
		}
		else {
			super.moveMotion(next, path);
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#draw_userImp(java.awt.Graphics)
	 */
	@Override
	public synchronized void draw(Graphics g) {
		if (this.getVariablesToThisPainter() == null) {
			return;
		}
		if (this.aliasingPainter != null && !isArrowMoving) {
			arrow.setDestination(aliasingPainter.getLocation().x, aliasingPainter.getLocation().y);
		}
		/*if (!isArrowMoving && !this.getVariablesToThisPainter().isEmpty()) {
			List<Painter> list = ProViz.getInstance().getVPM().getPaintersWithID(this.getVariable().getUniqueObjectID());
			if (list != null) {
				for (Painter painter : list) {
					if (painter == aliasingPainter) {
						if (painter != this) {
							arrow.setDestination(painter.getLocation().x, painter.getLocation().y);
							break;
						}
					}
				}
			}
		}*/
		if (this.aliasingPainter != null) {
			arrow.draw(g);
		}
	}
}
