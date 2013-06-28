package test.painters;

import javax.swing.JLabel;

import viz.painters.Painter;
import viz.painters.lib.VariablePainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class AnotherPainter extends VariablePainter {
	private Painter fieldK = null;
	public AnotherPainter(IVizVariable vvar, VizCanvas canvas) throws Exception {
		super(vvar, canvas);
	}

	/* (non-Javadoc)
	 * @see viz.painters.ObjectPainter#paint_userImp(java.lang.String)
	 */
	@Override
	protected void paint_userImp() {
		JLabel label = (JLabel) this.getComponent();
		label.setText(this.getAllVariableNames());
		label.repaint();
		//this.getFieldPainter("k");
		
	}

	/* (non-Javadoc)
	 * @see viz.painters.ObjectPainter#addToCanvas()
	 */
	@Override
	public void addToCanvas_userImp() {
		super.addToCanvas_userImp();
		this.fieldK = getFieldPainter("k");
		if (fieldK != null) {
			fieldK.setLocation(this.getLocation().x + 50, this.getLocation().y + 50);
			fieldK.addToCanvas();
		}
		//ConnectorManager.getInstance().hookUsUp(this, fieldK, this.getCanvas());
		getCanvas().getConnectorManager().hookUsUp(this, fieldK);
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
	}

	/* (non-Javadoc)
	 * @see viz.painters.ObjectPainter#destroy()
	 */
	@Override
	public void destroy_userImp() {
		super.destroy_userImp();
	}
}
