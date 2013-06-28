package test.painters;

import javax.swing.JComponent;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class ArrayListPainter extends Painter {
	private int size;

	public ArrayListPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		IVizVariable elementData = getVariable().getField("elementData");
		//VizVariable field = elementData.getFields().get(0);
		IVizVariable field = elementData.getField("[0]");
		//field.addListener(this);
		//this.addEventGenerator(field);
	}

	@Override
	protected void addToCanvas_userImp() {
		paint();
	}

	@Override
	protected void destroy_userImp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JComponent getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		paint();
		
	}

	@Override
	protected void paint_userImp() {
		size = Integer.parseInt(this.getVariable().getField("size").getValueAsString());
		IVizVariable elementData = getVariable().getField("elementData");
		for (int i = 0; i < size; i ++) {
			IVizVariable field = elementData.getFields().get(i);
			System.out.println(field.getName() + " : " + field.getValueAsString());
		}
	}

}
