package viz.painters;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;

import viz.painters.graphics.IMovable;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;
import viz.views.util.IConnectable;

/**
 * An IPainter can be used as an anchor to draw on a VizCanvas
 * @author JW
 */
public interface IPainter extends IConnectable, IMovable {
	public static final Point INIT_POINT = new Point(0, 0);
	
	/**
	 * Draws on the VizCanvas using the Java Graphics. This method is called in paint(Graphics)
	 * in VizCanvas, as VizCanvas is a Swing component. Painters which really paint by overriding
	 * this method must call the VizCanvas.addPainterToBePainted(..) method in addToCanvas_userImp().
	 * @param g
	 */
	public void draw(Graphics g);

	/**
	 * Initializes the painter and adds the graphical component onto the canvas.
	 */
	public void addToCanvas();
	
	public void destroy();
	
	public JComponent getComponent();
	
	public VizCanvas getCanvas();
	
	/**
	 * Gets an instance variable of this stack frame. The variable is a field stored under the 
	 * "this" local variable. Do not use this method if the method is static and that the target is
	 * a static variable). 
	 * @param varName
	 * @return
	 */
	public IVizVariable getInstanceVariable(String varName);
}
