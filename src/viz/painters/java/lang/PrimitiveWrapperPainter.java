package viz.painters.java.lang;

import viz.painters.lib.StringPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Paints wrapper classes for primitive data types, such as Integer, Long, Double,
 * Float, Short, Boolean, and so on. The value is stored under the "value" field.
 * @author JW
 * @deprecated
 * TODO combine this with PrimitivePainter
 */
public class PrimitiveWrapperPainter extends StringPainter {

	public PrimitiveWrapperPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull() && !isAdded) {
			getCanvas().add(label);
			isAdded = !isAdded;
		}
		paint();
	}

	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#resize()
	 */
	@Override
	public void resize() {
		if (!this.getVariable().isNull()) {
			label.setSize(fm.stringWidth(getVariable().getField("value").getValueAsString()) + marginX, fm.getHeight() + marginY);
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			String value = getVariable().getField("value").getValueAsString();
			if (!label.getText().equals(value)) {
				label.setText(value);
			}
		}
		else {
			label.setText("null");
		}
	}

	/**
	 * Does nothing.
	 * @see viz.painters.lib.StringPainter#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
	}
}
