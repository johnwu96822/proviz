package test.painters;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.java.array.IndexPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class IndexPainterJ extends IndexPainter {

	public IndexPainterJ(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public Painter getArrayPainter() {
		IVizVariable thisVariable = this.getVariable().getStackFrame().getVariable("this");
		if (thisVariable != null) {
			IVizVariable theArray = thisVariable.getField("array");
			if (theArray != null) {
				return ProViz.getVPM().getPainter(theArray);
			}
		}
		return null;
	}

}
