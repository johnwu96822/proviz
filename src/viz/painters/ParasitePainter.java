package viz.painters;

import java.awt.Point;

import javax.swing.JComponent;

import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * The purpose of ParasitePainter is to stick to a target painter location-wise so that
 * this and the target painter will always have the same location. It is similar to
 * PainterWithNoComponent except that there is no location management, isLocationRedirected()
 * returns true, and set/get location is redirected to what's returned by getTargetPainter().
 * @author JW
 *
 */
public abstract class ParasitePainter extends Painter {

	public ParasitePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}
	
	public abstract Painter getTargetPainter();
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#getLocation()
	 */
	@Override
	public Point getLocation() {
		if (getTargetPainter() != null) {
			return getTargetPainter().getLocation();
		}
		return INIT_POINT;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		if (getTargetPainter() != null) {
			getTargetPainter().setLocation(x, y);
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getComponent()
	 */
	@Override
	public final JComponent getComponent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#isLocationRedirected()
	 */
	@Override
	public boolean isLocationRedirected() {
		return true;
	}
}
