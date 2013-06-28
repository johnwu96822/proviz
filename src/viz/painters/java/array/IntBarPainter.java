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
 * @deprecated
 */
public class IntBarPainter extends Painter {//DrawPainter {
	private JPanel panel;
	private boolean isAdded = false;
	private int baseWidth = 10;
	private int unitLength = 10;
	private boolean isVertical = true;
	
	public IntBarPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
	//Some thread issues here. Canvas may still try to paint this painter after
	//the painter should be removed.
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
		int value = Integer.parseInt(this.getVariable().getValueAsString());
		//if (parentPainter != null) {
			//isVertical = !parentPainter.isVertical();
			if (isVertical) {
				if (value >= 0) {
					g.fillRect(8, 15, baseWidth, this.getHeight());
					g.drawString(this.getVariable().getValueAsString(), 8, 12);
				}
				else {
					g.fillRect(8, 0, baseWidth, this.getHeight() - 15);
					g.drawString(this.getVariable().getValueAsString(), 8, this.getHeight() - 3);
				}
			}
			else {
				if (value >= 0) {
					g.fillRect(0, 5, this.getWidth() - 15, baseWidth);
					g.drawString(this.getVariable().getValueAsString(), this.getWidth() - 12, 12);
				}
				else {
					g.fillRect(15, 5, this.getWidth() - 15, baseWidth);
					g.drawString(this.getVariable().getValueAsString(), 3, 12);
				}
			}
		//}
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		Painter parent = this.getParent();
		if (parent instanceof ChartArrayPainter) {
			//parent.paint();
		}
		else if (parent != null) {
			parent.paint();
		}
	}

	@Override
	protected void paint_userImp() {
		panel.setBounds(this.getLocation().x, this.getLocation().y, this.getWidth(), this.getHeight());
		//panel.repaint();
	}

	@Override
	protected void addToCanvas_userImp() {
		ChartArrayPainter caPainter = this.getChartArrayPainter();
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
			return baseWidth + 15;
		}
		return unitLength * Math.abs(Integer.parseInt(this.getVariable().getValueAsString())) + 16;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getWidth()
	 */
	@Override
	public int getWidth() {
		if (!isVertical) {
			return unitLength * Math.abs(Integer.parseInt(this.getVariable().getValueAsString())) + 16;
		}
		return baseWidth + 15;
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

	public ChartArrayPainter getChartArrayPainter() {
		Painter parent = this.getParent();
		while (parent != null && !(parent instanceof ChartArrayPainter)) {
			parent = parent.getParent();
		}
		if (parent instanceof ChartArrayPainter) {
			return (ChartArrayPainter) parent;
		}
		return null;
	}
}
