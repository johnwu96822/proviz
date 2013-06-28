package test.painters;

import viz.ProViz;
import viz.painters.Painter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class IIndexPainter extends IndexPainterI {

	public IIndexPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public Painter getArrayPainter() {
		IVizVariable arrayVar = this.getVariable().getStackFrame().getVariable("array");
		if (arrayVar != null) {
			return ProViz.getVPM().getPainter(arrayVar);
		}
		return null;
	}

}
