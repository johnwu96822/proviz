package viz.painters.java.linkedlist;

import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Visualizes java.util.LinkedList class, controls the first node in the list: head.
 * The element field of head is null, so head is nothing and is camouflaged with a
 * DummyPainter.
 * 
 * Because java.util.LinkedList is a 
 * @author JW
 *
 */
public class LinkedListPainter extends PainterWithNoComponent {
	
	public LinkedListPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			paint();
			Painter painter = this.getFieldPainter("header");
			if (painter != null) {
				if (!painter.getVariable().getField("element").isNull()) {
					painter.getFieldPainter("next").addToCanvas();
				}
			}
		}
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
	}

	@Override
	protected void paint_userImp() {
		Painter painter = this.getFieldPainter("header");
		if (painter != null) {
			painter.setLocation(this.getLocation().x, this.getLocation().y);
		}
	}
}
