package viz.painters.java.array;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Paints an integer as a vertical bar using the length to indicate its 
 * value. Used by ChartArrayPainter under parent control. 
 * @author JW
 *
 */
public class BarPainter extends Painter {//DrawPainter {
	private JPanel panel;
	private boolean isAdded = false;
	private int baseWidth = 10;
	private int unitLength = 10;
	private boolean isVertical = true;
	protected int cellPadding = 16;
	
	public static final String VALUE_FIELD = "value";
	
	//Models the actual value of the target variable
	private int value = 0;
	
	public BarPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
	//Some thread issues here. Canvas may still try to paint this painter after
	//the painter is removed.
				if (!getVariablesToThisPainter().isEmpty()) {
					super.paint(g);
					doPaint(g);
				}
			}
		};
	}
	
	/**
	 * Paints the vertical bar as a filled rectangle
	 * @param g The graphics object of the panel to draw in
	 */
	private void doPaint(Graphics g) {
		//ChartArrayPainter parentPainter = this.getChartArrayPainter();
		//int value = Integer.parseInt(this.getVariable().getValueAsString());
		//if (parentPainter != null) {
			//isVertical = !parentPainter.isVertical();
		String sValue = value + "";
			if (isVertical) {
				if (value >= 0) {
					g.fillRect(8, cellPadding, baseWidth, this.getHeight());
					g.drawString(sValue, 8, 12);
				}
				else {
					g.fillRect(8, 0, baseWidth, this.getHeight() - cellPadding);
					g.drawString(sValue, 8, this.getHeight() - 3);
				}
			}
			else {
				if (value >= 0) {
					g.fillRect(0, 5, this.getWidth() - cellPadding, baseWidth);
					g.drawString(sValue, this.getWidth() - 12, 12);
				}
				else {
					g.fillRect(15, 5, this.getWidth() - cellPadding, baseWidth);
					g.drawString(sValue, 3, 12);
				}
			}
		//}
	}
	
	/**
	 * Updates the drawing value of the painter
	 */
	private void updateValue() {
		if (this.getVariable().isNull()) {
			value = 0;
		}
		else {
			if (this.getVariable().isPrimitive()) {
				value = Integer.parseInt(this.getVariable().getValueAsString());
			}
			else {
	//The variable is a primitive wrapper class
				IVizVariable elementField = this.getVariable().getField(VALUE_FIELD);
				if (elementField != null) {
					value = Integer.parseInt(elementField.getValueAsString());
				}
				else {
					value = 0;
				}
			}
		}
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		Painter parent = this.getParent();
		updateValue();
		if (parent != null) {
			parent.paint();
		}
	}

	@Override
	protected void paint_userImp() {
		updateValue();
		panel.setBounds(this.getLocation().x, this.getLocation().y, this.getWidth(), this.getHeight());
		//panel.repaint();
	}

	@Override
	protected void addToCanvas_userImp() {
		updateValue();
		BarGraphArrayPainter caPainter = this.getBarGraphPainter();
		if (caPainter != null) {
			baseWidth = caPainter.getBaseWidth();
			isVertical = !caPainter.isVertical();
			unitLength = caPainter.getUnitLength();
		}
		panel.setBounds(this.getLocation().x, this.getLocation().y, this.getWidth(), this.getHeight());
		if (!isAdded) {
			this.getCanvas().add(panel);
			isAdded = !isAdded;
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getHeight()
	 */
	@Override
	public int getHeight() {
		if (!isVertical) {
			return baseWidth + cellPadding;
		}
		return unitLength * Math.abs(value) + cellPadding;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getWidth()
	 */
	@Override
	public int getWidth() {
		if (!isVertical) {
			return unitLength * Math.abs(value) + cellPadding;
		}
		return baseWidth + cellPadding;
	}

	@Override
	protected void destroy_userImp() {
		isAdded = false;
	}

	@Override
	public JComponent getComponent() {
		return this.panel;
	}
	
	/**
	 * @return the isVertical
	 */
	public boolean isVertical() {
		return isVertical;
	}

	/**
	 * @param isVertical the isVertical to set
	 */
	public void setVertical(boolean isVertical) {
		this.isVertical = isVertical;
	}

	public BarGraphArrayPainter getBarGraphPainter() {
		Painter parent = this.getParent();
		while (parent != null && !(parent instanceof BarGraphArrayPainter)) {
			parent = parent.getParent();
		}
		if (parent instanceof BarGraphArrayPainter) {
			return (BarGraphArrayPainter) parent;
		}
		return null;
	}
}
