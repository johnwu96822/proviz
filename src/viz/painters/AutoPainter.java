package viz.painters;

import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Automatically relays addToCanvas() and paint() to all its field painters (in contrast
 * to DummyPainter). This painter is to be used for bridging a variable and all its field 
 * painters. Setting the location of an AutoPainter will not have effect unless its field
 * painters use child-control to position themselves.
 * @author JW
 */
public class AutoPainter extends PainterWithNoComponent {

	public AutoPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/**
	 * Returns 0.
	 * @see viz.painters.PainterWithNoComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		return 0;
	}

	/**
	 * Returns 0.
	 * @see viz.painters.PainterWithNoComponent#getWidth()
	 */
	@Override
	public int getWidth() {
		return 0;
	}

	/**
	 * Calls addToCanvas() on all field painters.
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull() && this.hasFieldPainter()) {
			for (Painter field : this.getFieldPainters().values()) {
				field.addToCanvas();
			}
		}
	}

	@Override
	protected void destroy_userImp() {
	}

	/**
	 * Simply calls addToCanvas().
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		this.addToCanvas();
	}

	/**
	 * Calls paint() on all its field painters.
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull() && this.hasFieldPainter()) {
			for (Painter field : this.getFieldPainters().values()) {
				field.paint();
			}
		}
	}
}
