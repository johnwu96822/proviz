package viz.painters.java.array;

import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class VerticalArrayPainter extends ArrayPainter {

	public VerticalArrayPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
			this.isVertical = true;
	}

}
