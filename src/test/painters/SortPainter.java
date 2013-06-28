package test.painters;

import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class SortPainter extends PainterWithNoComponent {

	public SortPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	protected void addToCanvas_userImp() {
		Painter arrayPainter = this.getFieldPainter("array");
	//TODO INHERITENCE!
		if (arrayPainter != null) {
			arrayPainter.addToCanvas();
		}
		else {
			/*arrayPainter = ProViz.getInstance().getVPM().createFieldPainter("viz.painters.java.array.ArrayPainter", 
					getVariable().getField("array"), this);
			if (arrayPainter != null) {
				arrayPainter.addToCanvas();
			}*/
		}
	}

	@Override
	protected void destroy_userImp() {
		this.getFieldPainter("array").destroy();
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		Painter arrayPainter = this.getFieldPainter("array");
		if (arrayPainter != null) {
			arrayPainter.paint();
		}
	}

	@Override
	protected void paint_userImp() {
		this.getFieldPainter("array").paint();
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}
}
