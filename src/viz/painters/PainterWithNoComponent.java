package viz.painters;

import java.awt.Point;

import javax.swing.JComponent;

import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * PainterWithNoComponents are not selectable in VizCanvas.
 * @author JW
 *
 */
public abstract class PainterWithNoComponent extends Painter {
	private Point location = new Point();
	
	/**
	 * On the contrary to Painter, it is strongly recommended to set the location
	 * and size in the constructor for subclasses of PainterWithNoComponent
	 * constructor.
	 * @param vvar
	 * @param canvas
	 */
	public PainterWithNoComponent(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getComponent()
	 */
	@Override
	public final JComponent getComponent() {
		return null;
	}

	/**
	 * Gets the height of this painter. For a painter that does not have a graphical 
	 * component, its height should be calculated (most likely from the arrangement of
	 * its field painters).
	 * @see viz.painters.Painter#getHeight()
	 */
	@Override
	public abstract int getHeight();

	/**
	 * Gets the width of this painter. For a painter that does not have a graphical 
	 * component, its width should be calculated (most likely from its field painters).
	 * @see viz.painters.Painter#getWidth()
	 */
	@Override
	public abstract int getWidth();

	/**
	 * Subclasses overriding this method must call super.setLocation(x, y).
	 * @see viz.painters.Painter#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		location.setLocation(x, y);
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#getLocation()
	 */
	@Override
	public final Point getLocation() {
		return this.location;
	}

	/** 
	 * Subclasses of PainterWithNoComponent may want to redirect this setSize() method to its field painters.
	 * @see viz.painters.Painter#setSize(int, int)
	 */
	@Override
	public void setSize(int x, int y) {
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#highlightSelect()
	 */
	//@Override
	//public void highlightSelect() {}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#highlightSelectErase()
	 */
	//@Override
	//public void highlightSelectErase() {}
	
}
