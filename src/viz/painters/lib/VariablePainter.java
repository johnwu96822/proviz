package viz.painters.lib;

import java.awt.Color;

import javax.swing.JComponent;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.swing.VLabel;
import viz.views.VizCanvas;
import viz.views.util.JUtil;

/**
 * Paints a variable's type and name on two lines.
 * @author JW
 *
 */
public class VariablePainter extends Painter {
	protected VLabel objLabel = new VLabel();
	private boolean isAdded = false;
	
	public VariablePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setDefaultBorderColor(Color.BLACK);
	  //objLabel.setBorder(BorderFactory.createLineBorder(Color.black));
	  objLabel.setVerticalAlignment(VLabel.CENTER);
  	objLabel.setHorizontalAlignment(VLabel.CENTER);
	}

	@Override
	protected void addToCanvas_userImp() {
		VizCanvas canvas = this.getCanvas();
		//String declaredType = this.getVariablesToThisPainter().get(0).getType();
		myPaint();
  	//paint();
		if (!isAdded) {
			canvas.add(objLabel);
			isAdded = true;
		}
  	canvas.validate();
	}

	@Override
	protected void destroy_userImp() {
		isAdded = false;
	}

	@Override
	public JComponent getComponent() {
		return this.objLabel;
	}
	
	protected void myPaint() {
		//Check for null value
		if (!this.getVariable().isPrimitive() && this.getVariable().isNull()) {
			objLabel.setText("null");
		  objLabel.setSize(JUtil.getBorderedStringBounds(objLabel, objLabel.getText(), 6, 3, 2));
		}
		else {
			String className = simplifyClassName(this.getVariable().getActualType());
			String varName = this.getAllVariableNames();
			objLabel.setText("<html><center>" + className 
					+ "<br>" + varName + "</center></html>");
			String textSize = (className.length() > varName.length()) ? className : varName;
		  objLabel.setSize(JUtil.getBorderedStringBounds(objLabel, textSize, 3, 3, 2));
		}
	}


	/**
	 * Sets the text and size of the painter.
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		myPaint();
	}

	/**
	 * Calls paint().
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		myPaint();
	}
}
