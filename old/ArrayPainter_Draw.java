package viz.painters.java.array;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.eclipse.VizCanvas;

public class ArrayPainter_Draw extends Painter {
	private JLabel label;
	private int cellWidth = 30;
	private int cellHeight = 30;
	private boolean horizontal = true;
	private boolean showIndex = true;
	private int borderSize = 1;
	private int changedIndex = -1;
	
	//private RightClickMenuListener listener = new RightClickMenuListener();
	private static final String CHANGE_ORIENTATION = "Change Orientation";
	private static final String CHANGE_SHOW_INDEX = "Switch index on/off";
	private FontMetrics fm;

	public ArrayPainter_Draw(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		label = new JLabel() {
			/* (non-Javadoc)
			 * @see javax.swing.JComponent#paint(java.awt.Graphics)
			 */
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				painterPaint(g);
			} //end paintComponent()
		};
		int arrayLength = getVariable().getFields().size();
		fm = this.label.getFontMetrics(label.getFont());
		int width, height;
		if (horizontal) {
			if (showIndex) {
				width = cellWidth * arrayLength + 2 * borderSize;
				height = cellHeight + 10 + 2 * borderSize;
			}
			else {
				width = cellWidth * arrayLength + 2 * borderSize;
				height = cellHeight + 2 * borderSize;
			}
		}
		else {
			if (showIndex) {
				width = cellWidth + 10 + 2 * borderSize;
				height = cellHeight * arrayLength + 2 * borderSize;
			}
			else {
				width = cellWidth + 2 * borderSize;
				height = cellHeight * arrayLength + 2 * borderSize;
			}
		}
		label.setBounds(getCanvas().getVisibleRect().width / 2 - width / 2, getCanvas().getVisibleRect().height / 2 - height / 2, width, height);
	}

	@Override
	protected void paint_userImp() {
		List<IVizVariable> vars = getVariable().getFields();
		String longest = new String();
		for (IVizVariable var : vars) {
			if (var.getValueAsString().length() > longest.length()) {
				longest = var.getValueAsString();
			}
		}
		int length = fm.stringWidth(longest);
		int arrayLength = getVariable().getFields().size();
		this.cellWidth = (length > 20) ? length + 10 : 30;
		
		if (horizontal) {
			if (showIndex) {
				label.setSize(cellWidth * arrayLength + 2 * borderSize, cellHeight + 10 + 2 * borderSize);
			}
			else {
				label.setSize(cellWidth * arrayLength + 2 * borderSize, cellHeight + 2 * borderSize);
			}
		}
		else {
			if (showIndex) {
				label.setSize(cellWidth + 10 + 2 * borderSize, cellHeight * arrayLength + 2 * borderSize);
			}
			else {
				label.setSize(cellWidth + 2 * borderSize, cellHeight * arrayLength + 2 * borderSize);
			}
		}
		//System.out.println("Painting ArrayPainter_Draw");
		label.repaint();
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.VizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		changedIndex = -1;
		//System.out.println(source.getParentStack().getMethodID() + " - " + source.getName());
//		if (change == Change.TO_NULL) {

//		}
		if (change != Change.REMOVED) {
			List<IVizVariable> fields = this.getVariable().getFields();
			for (int i = 0; i < fields.size(); i++) {
				if (source.getName().equals(fields.get(i).getName())) {
					this.changedIndex = i;
					break;
				}
			}
			paint();
		}
	}
	
	private void painterPaint(Graphics g) {
		int innerWidth = this.getWidth() - 2 * borderSize;
		int innerHeight = this.getHeight() - 2 * borderSize;
		int x, y;
		List<IVizVariable> vars = getVariable().getFields();
		String value;
		int arrayLength = getVariable().getFields().size();
		for (int i = 0; i < arrayLength; i++) {
			if (horizontal) {
//Draw cell separating lines
				x = (innerWidth * i) / arrayLength + borderSize;
				g.drawLine(x, borderSize, x, cellHeight - borderSize);
//Draw the value of the cell
				value = vars.get(i).getValueAsString();
				x = (innerWidth * i) / arrayLength + cellWidth / 3 + borderSize;
				if (changedIndex == i) {
					g.setColor(Color.RED);
					g.drawString(value, x, cellHeight * 2 / 3 + borderSize);
					g.setColor(Color.BLACK);
				}
				else {
					g.drawString(value, x, cellHeight * 2 / 3 + borderSize);
				}
				if (showIndex) {
//Draw the index of the cell
					g.drawString(Integer.toString(i), x - (i / 10) * 2, innerHeight + borderSize + 1);
				}
			}
			else {
//Draw cell separating lines
				y = (innerHeight * i) / arrayLength + borderSize;
				g.drawLine(innerWidth - cellWidth + borderSize, y, innerWidth, y);
//Draw cell value
				value = vars.get(i).getValueAsString();
				y = (innerHeight * i) / arrayLength + cellHeight * 2 / 3 + borderSize;
				if (changedIndex == i) {
					g.setColor(Color.RED);
					g.drawString(value, innerWidth - cellWidth * 2 / 3 + borderSize - value.length() + 1, y);
					g.setColor(Color.BLACK);
				}
				else {
					g.drawString(value, innerWidth - cellWidth * 2 / 3 + borderSize - value.length() + 1, y);
				}
				if (showIndex) {
//Draw the index of the cell
					g.drawString(Integer.toString(i), borderSize, y);
				}
			}
		} //end for
		if (horizontal) {
//Last vertical line of the array
			g.drawLine(innerWidth, borderSize, innerWidth, cellHeight - borderSize);
//Two long horizontal lines
			g.drawLine(borderSize, borderSize, innerWidth, borderSize);
			g.drawLine(borderSize, cellHeight + 1 - borderSize, innerWidth, cellHeight + 1 - borderSize);
		}
		else {
//Last horizontal line of the array
			g.drawLine(innerWidth - cellWidth + borderSize, innerHeight - 1 + borderSize, innerWidth, innerHeight - 1 + borderSize);
//Two long vertical lines
			g.drawLine(innerWidth - cellWidth + borderSize, borderSize, innerWidth - cellWidth + borderSize, innerHeight);
			g.drawLine(innerWidth + 1 - borderSize, borderSize, innerWidth + 1 - borderSize, innerHeight);
		}
	}

	@Override
	protected void addToCanvas_userImp() {
		for (IVizVariable field : this.getVariable().getFields()) {
			field.addListener(this);
			this.addEventGenerator(field);
		}
		this.getCanvas().add(label);
	}

	@Override
	protected void destroy_userImp() {
		//this.getCanvas().remove(label);
	}

	@Override
	public JComponent getComponent() {
		return this.label;
	}

	/**
	 * @return the horizontal
	 */
	protected boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * @param horizontal the horizontal to set
	 */
	protected void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
	}

	/**
	 * @return the showIndex
	 */
	protected boolean isShowIndex() {
		return showIndex;
	}

	/**
	 * @param showIndex the showIndex to set
	 */
	protected void setShowIndex(boolean showIndex) {
		this.showIndex = showIndex;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#highlightSelectErase()
	 */
	@Override
	public void highlightSelectErase() {
		this.label.setBorder(null);
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActionListener()
	 */
	@Override
	public ActionListener getRightClickActionListener() {
		// TODO Auto-generated method stub
		return new RightClickMenuListener();
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActions()
	 */
	@Override
	public String[] getActionCommands() {
		String[] rv = new String[2];
		rv[0] = CHANGE_ORIENTATION;
		rv[1] = CHANGE_SHOW_INDEX;
		return rv;
	}

	
	/**
	 * @author JW
	 *
	 */
	private class RightClickMenuListener implements ActionListener {
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals(CHANGE_ORIENTATION)) {
				horizontal = !horizontal;
				paint();
			}
			else if (e.getActionCommand().equals(CHANGE_SHOW_INDEX)) {
				showIndex = !showIndex;
				paint();
			}
		}
	}
}
