package viz.painters.java.array;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.eclipse.VizCanvas;

/**
 * @author JW
 * @deprecated
 */
public class ArrayPainter_Comp extends Painter {
	private JLabel[] labels;
	private JPanel panel;
	private int arraySize = 0;
	private int cellWidth = 30;
	private int cellHeight = 30;
	private int borderSize = 1;
	private boolean showIndex = true;
	//private int changedIndex = -1;
	private boolean horizontal = true;
	public ArrayPainter_Comp(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.arraySize = getVariable().getFields().size();
		labels = new JLabel[arraySize];
		if (arraySize < 1) {
			return;
		}
		for (int i = 0; i < arraySize; i++) {
			labels[i] = new JLabel(this.getVariable().getFields().get(i).getValueAsString());
			labels[i].setBounds(i * cellWidth + borderSize, borderSize, cellWidth + 1, cellHeight);//(i + 1) * cellWidth + borderSize, borderSize + cellHeight);
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
			labels[i].setBorder(BorderFactory.createLineBorder(Color.black));
		}
		panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				for (int i = 0; i < arraySize; i++) {
					if (horizontal) {
						if (showIndex) {
	//Draw the index of the cell
							g.drawString(Integer.toString(i), cellWidth * i + cellWidth / 3 
									- (i / 10) * 2 + borderSize, cellHeight + 2 * borderSize + 10);
						}
					}
					else {
						if (showIndex) {
	//Draw the index of the cell
							g.drawString(Integer.toString(i), borderSize, (cellHeight * i) / arraySize 
									+ cellHeight * 2 / 3 + borderSize);
						}
					}
				}
			}
		};
		panel.setLayout(null);
		int width = 1 + cellWidth * arraySize + 2 * borderSize;
		if (showIndex) {
			panel.setBounds(getCanvas().getVisibleRect().width / 2 - width / 2, 
			getCanvas().getVisibleRect().height / 2 - cellHeight / 2, 
			width, cellHeight + 2 * borderSize + 10);
		}
		else {
			panel.setBounds(getCanvas().getVisibleRect().width / 2 - width / 2, 
			getCanvas().getVisibleRect().height / 2 - cellHeight / 2, 
			width, cellHeight + 2 * borderSize);
		}
		//panel.setBorder(BorderFactory.createLineBorder(Color.black));
		for (JLabel label : labels) {
			panel.add(label);
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#addToCanvas()
	 */
	@Override
	protected void addToCanvas_userImp() {
		getCanvas().add(this.panel);
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#destroy()
	 */
	@Override
	public void destroy_userImp() {
		//this.getCanvas().remove(panel);
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		// TODO Auto-generated method stub
		return panel;
	}

	@Override
	protected void paint_userImp() {
		for (int i = 0; i < arraySize; i++) {
			labels[i].setText(this.getVariable().getFields().get(i).getValueAsString());
		}	
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#highlightSelectErase()
	 */
	@Override
	public void highlightSelectErase() {
		this.panel.setBorder(null);
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		
	}
}