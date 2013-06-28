package viz.painters.java.list;

import java.awt.Graphics;

import viz.painters.Painter;
import viz.painters.lib.VariablePainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.swing.VLabel;
import viz.views.VizCanvas;

/**
 * Visualizes a stack that uses an array field and an integer size variable as 
 * its underlying data structure. The array field should be visualized by
 * DummyPainter
 * @author JW
 *
 */
public abstract class AbstractStackPainter extends VariablePainter {
	//labelEnd is managed manually by this painter
	private VLabel labelEnd = new VLabel("top");
	private boolean isAdded = false;
	private int arraySize = -1;
	private int width = 50;

	public AbstractStackPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setDefaultBorderColor(null);
		this.getComponent().setBorder(null);
		this.setLocation(50, 300);
	}
	
	/**
	 * java.util.Stack's size starts at 0; some implementation of a stack may
	 * start the top index at -1, 
	 * @return
	 */
	public abstract int counterStartingValue();
	
	public abstract String getArrayVariable();
	
	public abstract String getSizeVariable();

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			super.addToCanvas_userImp();
			if (!isAdded) {
				getCanvas().add(labelEnd);
				isAdded = true;
			}
	//Listens to the arraySize variable
			IVizVariable sizeVar = this.getVariable().getField(getSizeVariable());
			sizeVar.addListener(this);
			this.addEventGenerator(sizeVar);
			
			Painter dummy = this.getFieldPainter(getArrayVariable());
			int size = Integer.parseInt(sizeVar.getValueAsString()) - counterStartingValue();
			for (int i = 0; i < size; i++) {
				dummy.getFieldPainter("[" + i + "]").addToCanvas();
			}
			paint();
			getCanvas().addPainterToBePainted(this);
		}
	}

	@Override
	protected void destroy_userImp() {
		super.destroy_userImp();
		isAdded = false;
		getCanvas().remove(labelEnd);
	}

	/* Calls destroy on all elements and add elements in the stack back to canvas.
	 * This is easier than checking the push/pop.
	 * @see viz.painters.lib.VariablePainter#handleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (source.getName().equals(getSizeVariable())) {
			Painter dummy = this.getFieldPainter(getArrayVariable());
			for (int i = 0; i < dummy.getFieldPainters().size(); i++) {
				dummy.getFieldPainter("[" + i + "]").destroy();
			}
			int size = Integer.parseInt(source.getValueAsString()) - counterStartingValue();
			for (int i = 0; i < size; i++) {
				dummy.getFieldPainter("[" + i + "]").addToCanvas();
			}
			paint();
			getCanvas().repaint();
		}
		else if (change == Change.TO_NULL) {
			getCanvas().removePainterToBePainted(this);
			this.destroy();
		}
		else {
			this.addToCanvas();
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.AbstractPainter#getWidth()
	 */
	@Override
	public int getWidth() {
		return this.width;
	}
	
	@Override
	public int getHeight() {
		IVizVariable sizeVar = this.getVariable().getField(getSizeVariable());
		Painter dummy = this.getFieldPainter(getArrayVariable());
		int size = Integer.parseInt(sizeVar.getValueAsString()) - counterStartingValue();
		int height = 0;
		for (int i = 0; i < size; i++) {
			height += dummy.getFieldPainter("[" + i + "]").getHeight();
		}
		return height;
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			Painter dummy = this.getFieldPainter(getArrayVariable());
			this.arraySize = dummy.getFieldPainters().size();
			IVizVariable sizeVar = this.getVariable().getField(getSizeVariable());
			int size = Integer.parseInt(sizeVar.getValueAsString()) - counterStartingValue();
			int y = this.getLocation().y - 2;
			for (int i = 0; i < size; i++) {
				Painter element = dummy.getFieldPainter("[" + i + "]");
				y -= element.getHeight();
				element.setLocation(this.getLocation().x, y);
				element.setSize(width, element.getHeight());
			}
			if (size > 0) {
				Painter last = dummy.getFieldPainter("[" + (size - 1) + "]");
				labelEnd.setText("top = " + size);
				labelEnd.setBounds(last.getLocation().x, last.getLocation().y - 25, 
						labelEnd.getPreferredSize().width, 15);
			}
			else {
				labelEnd.setSize(0, 0);
			}
		}
	}

	/* Sets the location of labelEnd
	 * @see viz.painters.PainterWithNoComponent#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		if (!this.getVariable().isNull()) {
			Painter dummy = this.getFieldPainter(getArrayVariable());
			IVizVariable sizeVar = this.getVariable().getField(getSizeVariable());
			int size = Integer.parseInt(sizeVar.getValueAsString());
			if (size > 0) {
				Painter last = dummy.getFieldPainter("[" + (size - 1) + "]");
				labelEnd.setBounds(last.getLocation().x, last.getLocation().y - 25, 
						labelEnd.getPreferredSize().width, 15);
			}
		}
	}

	@Override
	public void draw(Graphics g) {
		int x = this.getLocation().x - 2;
		int y = this.getLocation().y - 1;
		g.drawLine(x, y, x, y - arraySize * 25);
		g.drawLine(x, y, x + width + 3, y);
		g.drawLine(x + width + 3, y, x + width + 3, y - arraySize * 25);
	}
	
	
}
