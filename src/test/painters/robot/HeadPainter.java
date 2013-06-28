package test.painters.robot;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Graphics;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class HeadPainter extends Painter {
	private JPanel panel;
	private int width = 50;
	public HeadPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				int x = (50 - width) / 2;
				g.drawLine(x, 0, 50 - x, 0);
				g.drawLine(x, 0, 50 / 2, 30);
				g.drawLine(50 / 2, 30, 50 - x, 0);
			}
		};
		panel.setOpaque(false);
		this.setSize(50, 31);
	}

	@Override
	protected void addToCanvas_userImp() {
		this.getCanvas().add(this.panel);
		paint();
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public JComponent getComponent() {
		return this.panel;
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
		
	}

	@Override
	protected void paint_userImp() {
		Point parentP = this.getParent().getLocation();
		this.setLocation(parentP.getLocation().x + 50 - width / 2, parentP.getLocation().y - 35);
		panel.repaint();
	}
}
