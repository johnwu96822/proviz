package viz.painters.java.linkedlist;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JLabel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.swing.VComponent;
import viz.views.ConnectorManager;
import viz.views.VizCanvas;

/**
 * Visualizes java.util.LinkedList$Entry<E> class, which is the node class for java.util.LinkedList.
 * This painter is child-control.
 * 
 * The element field of head is null, so head does not contain value, and the first real 
 * node with data is actually head.next. So head is camouflaged with a DummyPainter.
 * The first node therefore knows that it is the first node when its parent painter's
 * variable name is "head."
 * 
 * Because java.util.LinkedList is a circular linked list, the last node in the list
 * would be a VizVariable that has no fields in VizRuntime. So to know if this painter
 * is the last node in the list is to see whether this painter has fields or not.
 *  
 * @author JW
 *
 */
public class NodePainter extends Painter {
	public static int width = 30;
	public static int height = 25;
	public static int interval = 20;
	private boolean labelAdded = false;
	private VComponent nullLabel = null;
	private Point location = new Point(0, 0);
	public NodePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		canvas.addPainterToBePainted(this);
	}
	
	private boolean isFirst() {
		return this.getParent().getVariable().getName().equals("header");
	}

	@Override
	protected void addToCanvas_userImp() {
		paint();
		if (!this.hasFieldPainter()) {
			if (!labelAdded) {
				if (nullLabel != null) {
					getCanvas().add(nullLabel);
					labelAdded = !labelAdded;
					//getCanvas().repaint();
				}
			}
		}
		else {
			Painter nextP = this.getFieldPainter("next");
			Painter elementP = this.getFieldPainter("element");
			elementP.addToCanvas();
			nextP.addToCanvas();
			/*if (nextP.hasFieldPainter()) {
				getCanvas().getConnectorManager().hookUsUp(elementP, nextP.getFieldPainter("element"));
			}
			else {
				getCanvas().getConnectorManager().hookUsUp(elementP, nextP);
			}*/
		}
	}

	@Override
	protected void destroy_userImp() {
		
	}


	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
		/*if (change == Change.DIFF_OBJECT_SAME_TYPE_DIFF_FIELD_SIZE && !isFirst()) {
			ConnectorManager cManager = getCanvas().getConnectorManager();
			if (this.hasFieldPainter()) {
	//Changed from the null node to a middle node
	//So previously this painter paints null
				cManager.removeConnector(this.getConnector(getParent().getFieldPainter("element")));
				cManager.hookUsUp(getFieldPainter("element"), getParent().getFieldPainter("element"));
			}
			else {
	//Changed from a middle node to the null node
				cManager.hookUsUp(this, getParent().getFieldPainter("element"));
			}
		}*/
	}

	@Override
	protected void paint_userImp() {
		Painter parent;
		int locX, locY;
		parent = this.getParent();
		if (isFirst()) {
			locX = parent.getLocation().x;
		}
		else {
			locX = parent.getLocation().x + parent.getFieldPainter("element").getWidth() + interval;
		}
		locY = parent.getLocation().y;
		this.setLocation(locX, locY);
		ConnectorManager cManager = getCanvas().getConnectorManager();
		cManager.removeAll(this);
		if (!this.hasFieldPainter()) {
			if (nullLabel == null) {
				this.nullLabel = new VComponent("null");
				this.nullLabel.setHorizontalAlignment(JLabel.CENTER);
				this.nullLabel.setBorder(null);
			}
			this.nullLabel.setBounds(locX, locY, width, height);
			if (!isFirst()) {
				//if (this.getConnector(parent.getFieldPainter("element")) == null) {
				cManager.hookUsUp(this, parent.getFieldPainter("element"));
				cManager.setConnectorPoint(parent.getFieldPainter("element"), this, parent.getWidth() + 6, parent.getHeight() / 2);
					//System.out.println("E");
				//}
			}
		}
		else {
			if (nullLabel != null) {
				getCanvas().remove(nullLabel);
				getCanvas().validate();
				nullLabel = null;
				labelAdded = false;
			}
			Painter elementPainter = this.getFieldPainter("element");
			elementPainter.setLocation(locX, locY);
			elementPainter.paint();
			//this.getFieldPainter("next").setLocation(this.getLocation().x + elementPainter.getWidth() + interval, this.getLocation().y);
			Painter nextPainter = this.getFieldPainter("next");
			nextPainter.paint();
			if (nextPainter.getFieldPainter("element") != null) {
	//Connect with the next painter's element painter
				cManager.removeConnector(cManager.getConnector(elementPainter, nextPainter));
				Painter nextElementPainter = nextPainter.getFieldPainter("element");
				if (cManager.getConnector(elementPainter, nextElementPainter) == null) {
					cManager.hookUsUp(elementPainter, nextElementPainter);
					cManager.setConnectorPoint(elementPainter, nextElementPainter, this.getWidth() + 6, this.getHeight() / 2);
				}
			}
			else {
	//Connect with the next painter
				if (cManager.getConnector(elementPainter, nextPainter) == null) {
					cManager.hookUsUp(elementPainter, nextPainter);
					cManager.setConnectorPoint(elementPainter, nextPainter, this.getWidth() + 6, this.getHeight() / 2);
				}
			}
	//Connect with the previous NodePainter's element painter
			if (!isFirst()) {
				Painter parentElement = parent.getFieldPainter("element");
				if (cManager.getConnector(elementPainter, parentElement) == null) {
					cManager.hookUsUp(elementPainter, parentElement);
					cManager.setConnectorPoint(parentElement, elementPainter, parent.getWidth() + 6, parent.getHeight() / 2);
				}
			}
		}
	}

	@Override
	public int getHeight() {
		if (getFieldPainter("element") == null) {
			return height;
		}
		return getFieldPainter("element").getHeight();
	}

	@Override
	public int getWidth() {
		if (getFieldPainter("element") == null) {
			return width;
		}
		return getFieldPainter("element").getWidth();
	}

	@Override
	public JComponent getComponent() {
		return this.nullLabel;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getLocation()
	 */
	@Override
	public Point getLocation() {
		if (nullLabel == null) {
			return this.location;
		}
		return nullLabel.getLocation();
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		this.location.setLocation(x, y);
		if (nullLabel != null) {
			nullLabel.setLocation(x, y);
		}
	}
	/* (non-Javadoc)
	 * @see viz.painters.Painter#draw(java.awt.Graphics)
	 */
	@Override
	public synchronized void draw(Graphics g) {
		Painter elementPainter = this.getFieldPainter("element");
		if (elementPainter != null) {
			int x = elementPainter.getLocation().x + this.getWidth();
			int y = elementPainter.getLocation().y;
			g.drawRect(x, y, 12, this.getHeight() - 1);
			g.fillOval(x + 4, y + this.getHeight() / 2 - 2, 5, 5);
		}
	}
}
