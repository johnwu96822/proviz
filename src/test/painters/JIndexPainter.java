package test.painters;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.java.array.IndexPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class JIndexPainter extends IndexPainter {

	public JIndexPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/* (non-Javadoc)
	 * @see test.painters.IndexPainterJ#getArrayPainter()
	 */
	@Override
	public Painter getArrayPainter() {
		IVizVariable arrayVar = this.getVariable().getStackFrame().getVariable("array");
		if (arrayVar != null) {
			return ProViz.getVPM().getPainter(arrayVar);
		}
		return null;
	}	
}
