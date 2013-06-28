package viz.painters.java.array;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Can handle both primitive types and primitive wrapper classes.
 * @author JW
 *
 */
public class BarGraphArrayPainter extends PainterWithNoComponent implements ActionListener, IArrayPainter {
	private boolean isVertical = false;
	private int unitLength = 10;
	private int interval = 16;
	private int baseWidth = 10;
	private static final String CHANGE_ORIENTATION = "Change Orientation";
	
	public BarGraphArrayPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setLocation(getCanvas().getVisibleRect().width / 2 - 50, 
				getCanvas().getVisibleRect().height / 2);
	}

	/* (non-Javadoc)
	 * @see viz.painters.DrawPainter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			this.getCanvas().addPainterToBePainted(this);
			for (Painter fieldPainter : this.getFieldPainters().values()) {
				if (!(fieldPainter instanceof BarPainter)) {
					fieldPainter = ProViz.getVPM().switchOver(fieldPainter, "viz.painters.java.array.BarPainter", true, true, false);
					((BarPainter) fieldPainter).setVertical(!isVertical);
				}
				fieldPainter.addToCanvas();
  //Listen to all fields/elements of the array
				this.addEventGenerator(fieldPainter.getVariable());
				fieldPainter.getVariable().addListener(this);
			}
			this.paint();
			//this.getCanvas().repaint();
		}
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (source == this.getVariable()) {
			if (change == Change.TO_NULL) {
				this.getCanvas().removePainterToBePainted(this);
			}
			else {
				this.addToCanvas();
			}
		}
		else {
			paint();
		}
	}
	
	/**
	 * This method can distinguish a primitive variable from a primitive wrapper 
	 * @param elementPainter
	 * @return
	 */
	private int getValue(Painter elementPainter) {
		IVizVariable var = elementPainter.getVariable();
		if (var.isPrimitive()) {
			return Integer.parseInt(var.getValueAsString());
		}
		else if (!var.isNull()) {
			IVizVariable valueField = var.getField(BarPainter.VALUE_FIELD);
			if (valueField != null) {
				return Integer.parseInt(valueField.getValueAsString());
			}
		}
		return 0;
	}

	/**
	 * The x-axis will be the x coordinate of this painter. Positive values
	 * will be above, and negative values will be below the x axis.
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			int x = this.getLocation().x;
			int y = this.getLocation().y;
			int currentX = x;
			int currentY = y;
			for (int i = 0; i < this.getFieldPainters().size(); i++) {
				Painter fPainter = this.getFieldPainter("[" + i + "]");
				if (fPainter != null) {
					int value = getValue(fPainter);
					int width = fPainter.getWidth();
					int height = fPainter.getHeight();
					if (isVertical) {
						if (value >= 0) {
							fPainter.setLocation(x + 1, currentY);
						}
						else {
							fPainter.setLocation(x - width - 1, currentY);
						}
						currentY += height;
					}
					else {
						if (value >= 0) {
							//fPainter.setLocation(x + i * width, y - height);//1);
							fPainter.setLocation(currentX, y - height);
						}
						else {
							fPainter.setLocation(currentX, y + 1);
						}
						currentX += width;
					}
					fPainter.paint();
				}
			}
			this.getCanvas().repaint();
		} //end if (not null)
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#draw_userImp(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics g) {
		if (!this.getVariable().isNull()) {
			int x = this.getLocation().x;
			int y = this.getLocation().y;
			if (isVertical) {
				g.drawLine(x, y, x, y + this.getFieldPainters().size() * (baseWidth + interval));
			}
			else {
				g.drawLine(x, y, x + this.getFieldPainters().size() * (baseWidth + interval), y);
			}
		}
	}

	/**
	 * @return the isVertical
	 */
	@Override
	public boolean isVertical() {
		return isVertical;
	}

	/* (non-Javadoc)
	 * @see viz.painters.PainterWithNoComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		if (!isVertical) {
			/*//return height;
			int maxHeight = 0;
			for (int i = 0; i < this.getFieldPainters().size(); i++) {
				Painter fPainter = this.getFieldPainter("[" + i + "]");
				if (fPainter != null) {
					int value = getValue(fPainter);
					int height = fPainter.getHeight();
					if (value < 0 && maxHeight < height) {
						maxHeight = height;
					}
				}
			}
			return maxHeight;*/
			return 0;
		}
		return this.getFieldPainters().size() * (baseWidth + interval);
	}

	@Override
	public int getWidth() {
		if (isVertical) {
			return 0;
		}
		return this.getFieldPainters().size() * (baseWidth + interval);
	}

	@Override
	protected void destroy_userImp() {
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActionCommands()
	 */
	@Override
	public String[] getActionCommands() {
		return new String[] {CHANGE_ORIENTATION};
	}


	/* (non-Javadoc)
	 * @see viz.painters.Painter#getRightClickActionListener()
	 */
	@Override
	public ActionListener getRightClickActionListener() {
		return this;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CHANGE_ORIENTATION)) {
			isVertical = !isVertical;
			if (this.hasFieldPainter()) {
				for (Painter field : this.getFieldPainters().values()) {
					if (field instanceof BarPainter) {
						((BarPainter) field).setVertical(!isVertical);
					}
				}
			}
			paint();
		}
	}
	
	/**
	 * @return the unitLength
	 */
	public int getUnitLength() {
		return unitLength;
	}

	/**
	 * @param unitLength the unitLength to set
	 */
	public void setLengthUnit(int lengthUnit) {
		this.unitLength = lengthUnit;
	}

	/**
	 * @return the baseWidth
	 */
	public int getBaseWidth() {
		return baseWidth;
	}

	/**
	 * @param baseWidth the baseWidth to set
	 */
	public void setBaseWidth(int baseWidth) {
		this.baseWidth = baseWidth;
	}
}
