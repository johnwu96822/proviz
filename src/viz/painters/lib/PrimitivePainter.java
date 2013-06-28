package viz.painters.lib;

import viz.painters.Painter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Visualizes all primitive data types in Java. PrimitivePainter is actually a StringPainter,
 * and it cannot have any field painter.
 * TODO Should aliasing variables be allowed?
 * @author JW
 *
 */
public class PrimitivePainter extends StringPainter {

	public PrimitivePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (this.getVariable().isPrimitive()) {
			super.addToCanvas_userImp();
		}
		else {
	//Handles primitive wrappers
			if (/*!this.getVariable().isNull() &&*/ !isAdded) {
				getCanvas().add(label);
				isAdded = !isAdded;
			}
			paint();
		}
	}
	

	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		if (this.getVariable().isPrimitive()) {
			super.paint_userImp();
		}
		else {
	//Handles primitive wrappers
			if (!this.getVariable().isNull()) {
				label.setText(getVariable().getField("value").getValueAsString());
			}
			else {
				label.setText("null");
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see viz.painters.lib.StringPainter#resize()
	 */
	@Override
	public void resize() {
		if (this.getVariable().isPrimitive()) {
			super.resize();
		}
		else {
	//Handles primitive wrappers
			if (!this.getVariable().isNull()) {
				label.setSize(fm.stringWidth(getVariable().getField("value").getValueAsString()) 
						+ marginX, fm.getHeight() + marginY);
			}
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#addFieldPainter(viz.painters.Painter)
	 */
	@Override
	public final Painter addOrReplaceFieldPainter(String varName, Painter painter) {
		return null;	//Do nothing
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getUniqueID()
	 *
	@Override
	public final String getUniqueID() {
		return null;
	}*/

	/* (non-Javadoc)
	 * @see viz.painters.Painter#removeFieldPainter(viz.painters.Painter)
	 */
	@Override
	public final Painter removeFieldPainter(String varName) {
		// Do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#setUniqueID(java.lang.String)
	 *
	@Override
	public final void setUniqueID(String uniqueID) {
		// Do nothing
	}*/

	/* TODO May not be neceesary
	 * Adds a variable to this painter ONLY IF this is a field painter. Because local primitive
	 * variables are VOV only.
	 * @see viz.painters.Painter#addVariableToThisPainter(viz.runtime.VizVariable)
	 *
	@Override
	protected final void addVariableToThisPainter(IVizVariable var) {
	//If this primitive painter is a field, it can have more than one variables pointing to it
		if (this.getParent() != null) {
			super.addVariableToThisPainter(var);
		}
	//Otherwise if the primitive painter is a local variable, it can only have one variable pointing to it
		else if (this.getVariablesToThisPainter().isEmpty()) {
			super.addVariableToThisPainter(var);
		}
	}*/

	/* TODO May not be neceesary
	 * (non-Javadoc)
	 * @see viz.painters.Painter#removeVariableToThisPainter(viz.runtime.VizVariable)
	 *
	@Override
	protected final boolean removeVariableToThisPainter(IVizVariable var) {
		if (this.getVariablesToThisPainter().contains(var)) {
			return super.removeVariableToThisPainter(var);
		}
		return false;
	}*/

	/* (non-Javadoc)
	 * @see viz.painters.Painter#shouldCreateFieldPainters()
	 */
	@Override
	public final boolean shouldCreateFieldPainters() {
		return false;
	}
}
