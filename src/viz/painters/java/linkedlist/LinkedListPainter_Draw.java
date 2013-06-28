package viz.painters.java.linkedlist;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * @author JW
 * @deprecated
 */
public class LinkedListPainter_Draw extends Painter {
	private JLabel panel;
	private int cellWidth = 50;
	private int cellHeight = 30;
	private int borderSize = 1;
	private int distance = 15;
	
	public LinkedListPainter_Draw(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		panel = new JLabel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintNodes(g);
			}
		};
	}
	
	private void paintNodes(Graphics g) {
		if (this.getVariable().isNull()) {
			return;
		}
		int size = Integer.parseInt(this.getVariable().getField("size").getValueAsString());
		this.panel.setSize((cellWidth + distance - 5) * (size + 1), cellHeight + 2 * borderSize);
		IVizVariable node = this.getVariable().getField("header").getField("next");
		if (!this.getEventGenerators().contains(node)) {
			node.addListener(this);
			this.addEventGenerator(node);
		}
		int x = 0;
		for (int i = 0; i < size; i++) {
			x = i * cellWidth + (distance - 5) * i;
			g.drawRect(x + borderSize, borderSize, cellWidth, cellHeight - borderSize);
			g.drawLine(x + cellWidth - 10, 1, x + cellWidth - 10, cellHeight);
			g.drawString(node.getField("element").getValueAsString(), x + 5, cellHeight * 2 / 3);
			g.drawLine(x + cellWidth - 5, cellHeight / 2, x + cellWidth + distance - 5, cellHeight / 2);
			node = node.getField("next");
			if (!this.getEventGenerators().contains(node)) {
				node.addListener(this);
				this.addEventGenerator(node);
			}
		}
		if (x == 0) {
			g.drawString(getVariable().getName() + " = null", 1, cellHeight * 2 / 3);
		}
		else {
			g.drawString("null", x + cellWidth + distance, cellHeight * 2 / 3);
		}
	}

	@Override
	protected void addToCanvas_userImp() {
		//int size = Integer.parseInt(this.getVariable().getField("size").getValueAsString());
		//panel.setBounds(50, 50, (cellWidth + distance - 5) * (size + 1), cellHeight + 2);
		panel.setBounds(50, 50, 1, 1);
		//panel.setBorder(BorderFactory.createLineBorder(Color.black));
		this.getCanvas().add(panel);
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		//System.out.println("Handle change");
		panel.repaint();
	}

	@Override
	protected void paint_userImp() {
		//System.out.println("Paint_userImp");
		panel.repaint();
	}

	@Override
	public JComponent getComponent() {
		return this.panel;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#highlightSelectErase()
	 */
	@Override
	public void highlightSelectErase() {
		getComponent().setBorder(null);
	}
}
