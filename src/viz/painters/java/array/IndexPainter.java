package viz.painters.java.array;

import java.awt.Point;

import viz.ProViz;
import viz.animation.motion.Motion;
import viz.animation.motion.Move;
import viz.animation.motion.move.LinearPath;
import viz.animation.motion.move.Path;
import viz.painters.Painter;
import viz.painters.lib.StringPainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Paints an index of an array. The position of this index is calculated based
 * on the value of this painter's variable and the corresponding element in
 * the array. Animations are built-in to move this painter along the array when
 * its variable's value changes.
 * TODO Currently only works with ArrayPainter in horitontal orientation.
 * @author JW
 */
public abstract class IndexPainter extends StringPainter {
	protected int offset = 2;
	public IndexPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setDefaultBorderColor(null);
	}
	
	/**
	 * Gets the array painter that this index painter should attach the index to.
	 * @return
	 */
	public abstract Painter getArrayPainter();

	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			super.addToCanvas_userImp();
			this.setText(this.getVariable().getName());
			paint();
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		if (this.getVariable().isNull()) {
			return;
		}
		Painter array = getArrayPainter();
		if (array != null && array.hasFieldPainter()) {
			int index = -1;
			IVizVariable variable = this.getVariable();
			if (variable.isObject()) {
	//Handle primitive wrapper class
				if (!variable.isNull()) {
					index = Integer.parseInt(variable.getField("value").getValueAsString());
				}
			}
			else {
			  index = Integer.parseInt(variable.getValueAsString());
			}
			boolean isVertical = false;
			if (array instanceof IArrayPainter) {
				isVertical = ((IArrayPainter) array).isVertical();
			}
		//First handle out-of-bounds values
			if (index >= array.getFieldPainters().size()) {
				if (isVertical) {
					this.setLocation(array.getLocation().x - this.getWidth(), array.getLocation().y + array.getHeight() + 1);
				}
				else {
					this.setLocation(array.getLocation().x + array.getWidth(), array.getLocation().y + array.getHeight() + 1);
				}
			}
			else if (index < 0) {
				if (isVertical) {
					this.setLocation(array.getLocation().x - this.getWidth(), array.getLocation().y - this.getHeight());
				}
				else {
					this.setLocation(array.getLocation().x - this.getWidth(), array.getLocation().y + array.getHeight() + 1);
				}
			}
			else {
				Painter elementPainter = array.getFieldPainter("[" + index + "]");
				if (elementPainter != null) {
					Point point = elementPainter.getLocation();
					if (isVertical) {
						this.setLocation(point.x - this.getWidth(), point.y);//array.getLocation().y + array.getHeight() + 1);
					}
					else {
						this.setLocation(point.x + offset, point.y + elementPainter.getHeight() + 1);//array.getLocation().y + array.getHeight() + 1);
					}
				}
			}
		}
	}

	/* 
	 * @see viz.painters.lib.StringPainter#handleChange(viz.runtime.Change, viz.runtime.VizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		//super.paint_userImp();
		if (change == Change.VALUE_CHANGED) {
			Painter array = getArrayPainter();//ProViz.getInstance().getVPM().getPainterByUniqueID(theArray.getUniqueObjectID());
			if (array != null && array.hasFieldPainter()) {
				int index = Integer.parseInt(this.getVariable().getValueAsString());
				if (index >= array.getFieldPainters().size()) {
					this.setLocation(array.getLocation().x + array.getWidth(), array.getLocation().y + array.getHeight() + 1);
				}
				if (index < 0) {
					this.setLocation(array.getLocation().x - this.getWidth(), array.getLocation().y + array.getHeight() + 1);
				}
				else {
					Painter elementPainter = array.getFieldPainter("[" + index + "]");
					if (elementPainter != null) {
						Point point = elementPainter.getLocation();
						Point destination;
						if (array instanceof IArrayPainter && ((IArrayPainter) array).isVertical()) {
							destination = new Point(point.x - this.getWidth(), point.y);
						}
						else {
						  destination = new Point(point.x + offset, point.y + elementPainter.getHeight() + 1);//array.getLocation().y + array.getHeight() + 1);
						}
						Path path = new LinearPath(this, this.getLocation(), destination, Move.MOVE_ONE);
						ProViz.getAnimationController().scheduleAnimation(this, 
								new Motion[] {path}, null);//new Painter[] {array});
					}
				}
			} //end if
		} //end if
		else if (change == Change.NULL_TO_OBJ){
			addToCanvas();
		}
		else if (change == Change.TO_NULL) {
			destroy();
		}
	} //end handleChange
}
