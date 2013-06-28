package viz.painters;

import java.awt.Graphics;

import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * DrawPainter presets the functionality that it paints directly onto the canvas and cannot
 * have components (since it inherits from PainterWithNoComponent). It adds this painter to
 * the drawing list of its canvas in addToCanvas so whenever this painter is added to canvas,
 * its drawing will start appearing. And by default, destroy() removes it from the canvas. 
 * @author JW
 *
 */
public abstract class DrawPainter extends PainterWithNoComponent {

	public DrawPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/**
	 * The default implementation of this method in DrawPainter adds this painter to VizCanvas' list
	 * of drawing painters. Subclasses of DrawPainter overriding this method must call 
	 * super.addToCanvas_userImp() or add the painter to VizCanvas' list of drawing painters, otherwise
	 * the drawing may not appear on canvas.
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	protected void addToCanvas_userImp() {
		this.getCanvas().addPainterToBePainted(this);
		this.getCanvas().repaint();
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#draw(java.awt.Graphics)
	 */
	@Override
	public abstract void draw(Graphics g);

	/**
	 * The default implementation in DrawPainter returns 0.
	 * @see viz.painters.PainterWithNoComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		return 0;
	}

  /**
	 * The default implementation in DrawPainter returns 0.
	 * @see viz.painters.PainterWithNoComponent#getWidth()
	 */
	@Override
	public int getWidth() {
		return 0;
	}
}
