package viz.painters;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;

import viz.ProViz;
import viz.views.VizCanvas;

public abstract class AbstractPainter implements IPainter {
	protected VizCanvas canvas;

	/* (non-Javadoc)
	 * @see viz.painters.IPainter#addToCanvas()
	 */
	@Override
	public void addToCanvas() {
		if (getComponent() != null) {
			//if (!isAdded) {
				//isAdded = true;
	//Remove the listeners before adding to avoid mutliple registering the listeners
	//because Swing doesn't prevent multiple registration
			if (canvas.getComponentListener() != null) {
				this.getComponent().removeMouseListener(canvas.getComponentListener());
				this.getComponent().addMouseListener(canvas.getComponentListener());
				this.getComponent().removeMouseMotionListener(canvas.getComponentListener());
				this.getComponent().addMouseMotionListener(canvas.getComponentListener());
			}
			this.getComponent().setOpaque(true);
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.IPainter#destroy()
	 */
	@Override
	public void destroy() {
		this.canvas.getConnectorManager().removeAll(this);
		if (this.getComponent() != null) {
			this.getComponent().removeMouseListener(this.canvas.getComponentListener());
			this.getComponent().removeMouseMotionListener(this.canvas.getComponentListener());
			this.canvas.remove(this.getComponent());
		}
		if (this.canvas.removePainterToBePainted(this)) {
			this.canvas.repaint();
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.IPainter#getCanvas()
	 */
	@Override
	public VizCanvas getCanvas() {
		return canvas;
	}

	/* (non-Javadoc)
	 * @see viz.views.util.IConnectable#getHeight()
	 */
	@Override
	public int getHeight() {
		if (getComponent() != null) {
			return getComponent().getHeight();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see viz.views.util.IConnectable#getWidth()
	 */
	@Override
	public int getWidth() {
		if (getComponent() != null) {
			return getComponent().getWidth();
		}
		return 0;
	}
	
	/**
	 * Draws on the VizCanvas using the Java Graphics. This method is called in paint(Graphics)
	 * in VizCanvas, as VizCanvas is a Swing component. Painters which really paint by overriding
	 * this method must call the VizCanvas.addPainterToBePainted(..) method and must call
	 * VizCanvas.removePainterToBePainted(..) in destroy_userImp() to remove themselves.
	 * @param g
	 */
	@Override
	public void draw(Graphics g) {}
	
	/**
	 * Gets the location of the graphical component in this painter
	 * DO NOT return null! Can return the static INIT_POINT, which is (0, 0).
	 * @return the location of this painter
	 * @see viz.views.util.IConnectable#getLocation()
	 */
	@Override
	public Point getLocation() {
		if (getComponent() != null) {
			return getComponent().getLocation();
		}
		return INIT_POINT;
	}

	/* (non-Javadoc)
	 * @see viz.painters.graphics.IMovable#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		JComponent comp = getComponent();
		if (comp != null) {
			comp.setLocation(x, y);
			VizCanvas canvas = this.getCanvas();
			if (canvas == ProViz.getVPM().getCanvas()) {
				Dimension dim = canvas.getPreferredSize();
				boolean modified = false;
				if (x > canvas.getWidth()) {
					dim.width = x + comp.getWidth() + 20;
					modified = true;
				}
				if (y > canvas.getHeight()) {
					dim.height = y + comp.getHeight() + 20;
					modified = true;
				}
				if (modified) {
					ProViz.getInstance().setPreferredSize(dim);
				}
			}
		}
	}
}
