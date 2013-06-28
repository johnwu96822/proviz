package test.painters;

import java.awt.Color;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class IndexPainterI extends IndexPainterJ {

	public IndexPainterI(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/* (non-Javadoc)
	 * @see test.painters.IndexPainter#handleChange(viz.runtime.Change, viz.runtime.VizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		super.handleChange(change, source);
		highlightArray();
	}

	/* (non-Javadoc)
	 * @see test.painters.IndexPainter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		super.paint_userImp();
		highlightArray();
	}
	
	private void highlightArray() {
		Painter arrayPainter = getArrayPainter();
		if (arrayPainter != null) {
			int i = Integer.parseInt(this.getVariable().getValueAsString()) + 1;
			for (int j = 0; j < arrayPainter.getFieldPainters().size(); j++) {
			//for (; i < arrayPainter.getFieldPainters().size(); i++) {
				if (j < i) {
					arrayPainter.getFieldPainter("[" + j + "]").getComponent().setBackground(null);
				}
				else {
					arrayPainter.getFieldPainter("[" + j + "]").getComponent().setBackground(Color.YELLOW);
				}
			}
		}
	}
}
