package viz.painters.java.arraylist;

import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.swing.VLabel;
import viz.views.VizCanvas;

/**
 * Paints java.util.ArrayList, currently in the horizontal fashion.
 * @author JW
 */
public class ArrayListPainter extends PainterWithNoComponent {
	private VLabel label0 = new VLabel();
	private VLabel labelEnd = new VLabel();

	public ArrayListPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setLocation(10, 10);
		label0.setText("0");
		getCanvas().add(labelEnd);
		label0.setOpaque(true);
		getCanvas().add(label0);
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			Painter dummy = this.getFieldPainter("elementData");
			IVizVariable sizeVar = this.getVariable().getField("size");
			if (sizeVar == null) {
	//For java.util.Stack
				sizeVar = this.getVariable().getField("elementCount");
			}
			sizeVar.addListener(this);
			this.addEventGenerator(sizeVar);
			int size = Integer.parseInt(sizeVar.getValueAsString());
			for (int i = 0; i < size; i++) {
				dummy.getFieldPainter("[" + i + "]").addToCanvas();
			}
			paint();
			getCanvas().addPainterToBePainted(this);
		}
	}

	@Override
	protected void destroy_userImp() {
		getCanvas().remove(label0);
		getCanvas().remove(labelEnd);
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (change == Change.TO_NULL && source == this.getVariable()) {
			label0.setSize(0, 0);
			labelEnd.setSize(0, 0);
			return;
		}
		if (source.getName().equals("size")) {// || source.getName().equals("elementCount")) {
			Painter dummy = this.getFieldPainter("elementData");
			for (int i = 0; i < dummy.getFieldPainters().size(); i++) {
				dummy.getFieldPainter("[" + i + "]").destroy();
			}
		}
		getCanvas().repaint();
		this.addToCanvas();
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			Painter dummy = this.getFieldPainter("elementData");
			IVizVariable sizeVar = this.getVariable().getField("size");
			int size = Integer.parseInt(sizeVar.getValueAsString());
			int x = this.getLocation().x;
			for (int i = 0; i < size; i++) {
				Painter element = dummy.getFieldPainter("[" + i + "]");
				element.setLocation(x, this.getLocation().y);
				x += element.getWidth();
			}
			if (size > 0) {
				label0.setBounds(this.getLocation().x + 10, this.getLocation().y 
						+ dummy.getFieldPainter("[0]").getHeight() + 2, 15, 15);
				Painter last = dummy.getFieldPainter("[" + (size - 1) + "]");
				labelEnd.setText("" + (size - 1));
				labelEnd.setBounds(last.getLocation().x + 10, last.getLocation().y 
						+ last.getHeight() + 2, labelEnd.getPreferredSize().width, 15);
			}
			else {
				label0.setSize(0, 0);
				labelEnd.setSize(0, 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.PainterWithNoComponent#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		if (!this.getVariable().isNull()) {
			Painter dummy = this.getFieldPainter("elementData");
			IVizVariable sizeVar = this.getVariable().getField("size");
			int size = Integer.parseInt(sizeVar.getValueAsString());
			if (size > 0) {
				label0.setBounds(this.getLocation().x + 10, this.getLocation().y 
						+ dummy.getFieldPainter("[0]").getHeight() + 2, 15, 15);
				Painter last = dummy.getFieldPainter("[" + (size - 1) + "]");
				//labelEnd.setText("" + (size - 1));
				labelEnd.setBounds(last.getLocation().x + 10, last.getLocation().y 
						+ last.getHeight() + 2, labelEnd.getPreferredSize().width, 15);
			}
		}
	}
}
