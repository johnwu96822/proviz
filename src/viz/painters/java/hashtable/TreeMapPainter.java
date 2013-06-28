package viz.painters.java.hashtable;

import java.awt.Graphics;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Paints java.util.TreeMap<K,V> in the parent-control scheme.
 * @author JW
 */
public class TreeMapPainter extends PainterWithNoComponent {
	private boolean showNull = false;
	private int cellWidth = 25;
	private int cellHeight = 20;

	public TreeMapPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setLocation(300, 200);
	}

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			getCanvas().addPainterToBePainted(this);
			paint();
			this.getFieldPainter("root").addToCanvas();
		}
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
	}

	@Override
	protected void paint_userImp() {
		Painter root = this.getFieldPainter("root");
		if (root != null) {
			root.setLocation(this.getLocation().x, this.getLocation().y);
			this.getFieldPainter("root").paint();
			//System.out.println("Depth: " + ((TEntryPainter)root).getDepth(root.getVariable()));
		}
	}

	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#draw_userImp(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics g) {
		if (getVariable() != null) {
			g.drawString(getVariable().getName(), getLocation().x - 50, getLocation().y - 5);
		}
	}

	/**
	 * @return the cellWidth
	 */
	public int getCellWidth() {
		return cellWidth;
	}

	/**
	 * @param cellWidth the cellWidth to set
	 */
	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	/**
	 * @return the cellHeight
	 */
	public int getCellHeight() {
		return cellHeight;
	}

	/**
	 * @param cellHeight the cellHeight to set
	 */
	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}
	
	protected void switchAll(Painter painter, Painter origin) {
		Painter newPainter = origin;
		if (painter != origin) {
			newPainter = ProViz.getVPM().switchOver(painter, origin.getClass().getName(), true, false, false);
		}
		Painter key = newPainter.getFieldPainter("key");
		if (key != null) {
			key.destroy();
		}
		Painter value = newPainter.getFieldPainter("value");
		if (value != null) {
			value.destroy();
		}
		Painter left = newPainter.getFieldPainter("left");
		if (left != null) {
			switchAll(left, origin);
		}
		Painter right = newPainter.getFieldPainter("right");
		if (right != null) {
			switchAll(right, origin);
		}
	}
/*	protected void switchAll(Painter painter, Painter origin) {
		Painter newPainter = ProViz.getInstance().getVPM().switchOver(painter, origin.getClass().getName(), true, false);//, null);
		Painter key = newPainter.getFieldPainter("key");
		if (key != null) {
			key.destroy();
		}
		Painter value = newPainter.getFieldPainter("value");
		if (value != null) {
			value.destroy();
		}
		Painter left = newPainter.getFieldPainter("left");
		if (left != null) {
			if (left == origin) {
	//Call on the original's left and right
				Painter lLeft = left.getFieldPainter("left");
				if (lLeft != null) {
					//System.out.println("Switch origin's left" + lLeft.getVariable().getValueAsString());
					switchAll(lLeft, origin);
				}
				Painter lRight = left.getFieldPainter("right");
				if (lRight != null) {
					//System.out.println("Switch origin's right" + lRight.getVariable().getValueAsString());
					switchAll(lRight, origin);
				}
			}
			else {
				//System.out.println("Switch left" + left.getVariable().getValueAsString());
				switchAll(left, origin);
			}
		}
		Painter right = newPainter.getFieldPainter("right");
		if (right != null) {
			if (right == origin) {
				Painter rLeft = right.getFieldPainter("left");
				if (rLeft != null) {
					//System.out.println("Switch origin's left" + rLeft.getVariable().getValueAsString());
					switchAll(rLeft, origin);
				}
				Painter rRight = right.getFieldPainter("right");
				if (rRight != null) {
					//System.out.println("Switch origin's right" + rRight.getVariable().getValueAsString());
					switchAll(rRight, origin);
				}
			}
			else {
				switchAll(right, origin);
				//System.out.println("Switch right" + right.getVariable().getValueAsString());
			}
		}
	}*/
	
	/**
	 * @return the showNull
	 */
	public boolean isShowNull() {
		return showNull;
	}

	/**
	 * @param showNull the showNull to set
	 */
	public void setShowNull(boolean showNull) {
		this.showNull = showNull;
	}
	
	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}
}
