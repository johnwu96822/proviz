package viz.painters.lib;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.swing.VComponent;
import viz.views.VizCanvas;

/**
 * <p>Paints a variable's string value in a single-line format. By default it paints the 
 * value of this painter's variable. To display some other value, one should override 
 * the paint_userImp() method.
 * </p>
 * <p>To show a value permanently using setText() (for example, a variable's name), 
 * one should override the paint_userImp() method with a blank body (so no changes would 
 * update this painter) and then use the setText() to display custom text.</p> 
 * @author JW
 */
public class StringPainter extends Painter {
	protected VComponent label = new VComponent();
	protected FontMetrics fm;
	protected boolean isAdded = false;
	protected int marginX = 20;
	protected int marginY = 6;
	
	public StringPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		fm = label.getFontMetrics(label.getFont());
		//label.setBorder(BorderFactory.createLineBorder(Color.black));
		label.setHorizontalAlignment(SwingConstants.CENTER);

		resize();
		
		label.setLocation(10, 10);
		this.setDefaultBorderColor(Color.black);
	}

	/**
	 * @param fm the FontMetrics to set
	 */
	public void setFontMetrics(FontMetrics fm) {
		this.fm = fm;
		paint();
	}

	/**
	 * Sets the value of the label and adds it to canvas. Does not call resize(), 
	 * so other painters or subclasses which want to to call resize() should do it 
	 * explicitly or by overriding this method.
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
	//Prevents multiple adding to the canvas (Swing does not permit)
		if (!isAdded) {
			//String value = getVariable().getValueAsString();
			//label.setText(value);
			paint();
			getCanvas().add(label);
			isAdded = !isAdded;
		}
	}

	@Override
	protected void destroy_userImp() {
		isAdded = false;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return this.label;
	}

	/**
	 * Updates the display value of the painter to the value of this painter's variable.
	 * Does not call resize(), so other painters or subclasses which want to to call
	 * resize() should do it explicitly or by overriding this method.
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		label.setText(getVariable().getValueAsString());
		//String value = getVariable().getValueAsString();
		//if (!label.getText().equals(value)) {
		//	label.setText(value);
		//}
	}
	
	/**
	 * Resizes the dimension of this painter to a preset value.
	 */
	public void resize() {
		label.setSize(fm.stringWidth(getVariable().getValueAsString()) + marginX, fm.getHeight() + marginY);
	}

	/**
	 * Calls paint()
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		paint();
	}
	
	/**
	 * Sets the text of this StringPainter. By default, a paint() or handleChange() call will 
	 * override the text, so subclasses that want to use this method should override either
	 * one of the above two methods.
	 * @param text
	 */
	public void setText(String text) {
		this.label.setText(text);
	}
	
	/**
	 * <b>Under construction</b>. Do no use!
	 * @param clockWise
	 * @param theta
	 */
	private void rotateText(boolean clockWise, double theta) {
		Graphics2D g2d = (Graphics2D) label.getGraphics();
		if (clockWise) {
			g2d.rotate(theta, this.getWidth() / 2, this.getHeight() / 2);
		}
		else {
			g2d.rotate((-1) * theta, this.getWidth() / 2, this.getHeight() / 2);
		}
		label.setText(this.getPreviousValue());
		if (clockWise) {
			g2d.rotate((-1) * theta, this.getWidth() / 2, this.getHeight() / 2);
		}
		else {
			g2d.rotate(theta, this.getWidth() / 2, this.getHeight() / 2);
		}
	}
}
