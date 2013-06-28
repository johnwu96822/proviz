package viz.painters.array.index;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.java.array.IndexPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * This index variable is a local variable, and the target array is a field.
 * @author JW
 *
 */
public abstract class ArrayIsFieldIndexPainter extends IndexPainter {

	public ArrayIsFieldIndexPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public Painter getArrayPainter() {
		return ProViz.getVPM().getPainter(this.getInstanceVariable(getArrayName()));
	}

	public abstract String getArrayName();
}