package viz.painters.array.index;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.java.array.IndexPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public abstract class ArrayIsLocalIndexPainter extends IndexPainter {

	public ArrayIsLocalIndexPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public Painter getArrayPainter() {
		return ProViz.getVPM().getPainter(this.getLocalVariable(getArrayName()));
	}

	public abstract String getArrayName();
}
