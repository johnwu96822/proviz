package viz.painters.java.lang;

import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * @author JW
 * @deprecated
 */
public class PrimitiveWrapperClassPainter extends PainterWithNoComponent {

	public PrimitiveWrapperClassPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public int getHeight() {
		if (!this.getVariable().isNull()) {
			return this.getFieldPainter("value").getHeight();
		}
		return 0;
	}

	@Override
	public int getWidth() {
		if (!this.getVariable().isNull()) {
			return this.getFieldPainter("value").getWidth();
		}
		return 0;
	}

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			this.getFieldPainter("value").setLocation(this.getLocation().x, this.getLocation().y);
			this.getFieldPainter("value").addToCanvas();
		}
	}

	@Override
	protected void destroy_userImp() {
		if (!this.getVariable().isNull()) {
			this.getFieldPainter("value").destroy();
		}
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		this.addToCanvas();
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			this.getFieldPainter("value").setLocation(this.getLocation().x, this.getLocation().y);
			this.getFieldPainter("value").paint();
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.PainterWithNoComponent#setSize(int, int)
	 */
	@Override
	public void setSize(int x, int y) {
		if (!this.getVariable().isNull()) {
			this.getFieldPainter("value").setSize(x, y);
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#setTooltips(java.lang.String)
	 */
	@Override
	public void setTooltipsVariablePortion(String tooltips) {
		//super.setTooltips(tooltips);
		if (!this.getVariable().isNull()) {
			this.getFieldPainter("value").setTooltipsVariablePortion(tooltips);
		}
	}
}
