package viz.painters.java.array;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Parent control.
 * @author JW
 */
public class ArrayPainter extends PainterWithNoComponent implements ActionListener, IArrayPainter {
	//private int arrayLength = 0;
	protected boolean isVertical = false;
	private boolean indexOn = false;
	private static final String CHANGE_ORIENTATION = "Change Orientation";
	private static final String SHOW_INDICIES = "Show Indices";
	protected int cellWidth = 0;
	protected int cellHeight = 0;

	public ArrayPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);		
		if (!this.getVariable().isNull()) {
			//List<IVizVariable> fields = this.getVariable().getFields();
			//this.arrayLength = fields.size();
			this.setLocation(50, 200);//getCanvas().getVisibleRect().width / 2 - 50, 
					//getCanvas().getVisibleRect().height / 2);
		}
	}


	/* Gets the overall height of the array, which depends on whether the array is displayed
	 * horizontally or vertically.
	 * @see viz.painters.Painter#getHeight()
	 */
	@Override
	public int getHeight() {
		int rv = 0;
		if (this.getFieldPainters().size() > 0) {
			if (isVertical) {
				for (Painter fieldPainter : this.getFieldPainters().values()) {
					rv += fieldPainter.getHeight();
				}
			}
			else {
				for (Painter fieldPainter : this.getFieldPainters().values()) {
					if (rv < fieldPainter.getHeight()) {
						rv = fieldPainter.getHeight();
					}
				}
			}
		}
		return rv;
	}

	
	/* Gets the overall width of the array, which depends on whether the array is displayed
	 * horizontally or vertically.
	 * @see viz.painters.Painter#getWidth()
	 */
	@Override
	public int getWidth() {
		int rv = 0;
		if (this.getFieldPainters().size() > 0) {
			if (isVertical) {
				for (Painter fieldPainter : this.getFieldPainters().values()) {
					if (rv < fieldPainter.getWidth()) {
						rv = fieldPainter.getWidth();
					}
				}
			}
			else {
				for (Painter fieldPainter : this.getFieldPainters().values()) {
					rv += fieldPainter.getWidth();
				}
			}
		}
		return rv;
	}

	@Override
	protected void addToCanvas_userImp() {		
		if (!this.getVariable().isNull()) {// && this.getFieldPainters() != null) {
			for (Painter painter : this.getFieldPainters().values()) {
				painter.addToCanvas();
  //Listen to all fields/elements of the array
  //TODO VOV only for now
				this.addEventGenerator(painter.getVariable());
				painter.getVariable().addListener(this);
			}
			if (cellWidth == 0 || cellHeight == 0) {
				recalculateCellSize();
			}
			this.paint();
		}
	}
	
	/**
	 * @return the isVertical
	 */
	@Override
	public boolean isVertical() {
		return isVertical;
	}

	/**
	 * @param isVertical the isVertical to set
	 */
	public void setVertical(boolean isVertical) {
		this.isVertical = isVertical;
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (source == this.getVariable()) {
			addToCanvas();
		}
	//Handle NULL_TO_OBJ event from array fields/elements
		else if (change == Change.NULL_TO_OBJ) {
			Painter element = this.getFieldPainter(source.getName());
			element.addToCanvas();
			this.paint();
		}
		else {
			this.paint();
		}
	}

	/** 
	 * Only repositions the element painters but does not recalculates the sizes of each
	 * element. Recomuting the sizes must be called explicitly through recalculateCellSize()
	 * or by setting the cell width/height before paint() is called
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		int x = getLocation().x;
		int y = getLocation().y;
		//int width = 0;
		//int height = 0;
		//Determines the greatest width and height of individual cells, which then
		//are used as the width and height for every cell.
		/*for (Painter painter : this.getFieldPainters().values()) {
			if (width < painter.getWidth()) {
				width = painter.getWidth();
			}*/
			/*int temp = painter.getComponent().getPreferredSize().width;
			if (width < temp) {
				width = temp;
			}
			temp = painter.getComponent().getPreferredSize().height;*/
			/*if (height < painter.getHeight()) {
				height = painter.getHeight();
			}*/
			/*if (height < temp) {
				height = temp;
			}*/
		//}
		for (int i = 0; i < this.getFieldPainters().size(); i++) {
			Painter fPainter = this.getFieldPainter("[" + i + "]");
			if (fPainter != null) {
				if (isVertical) {
					fPainter.setLocation(x, i * cellHeight + y);
				}
				else {
					fPainter.setLocation(x + i * cellWidth, y);
				}
				fPainter.paint();
	//Sets the size of field painters after their paint() to override the size set in their paint()
				fPainter.setSize(cellWidth, cellHeight);
			}
		}
	}
	
	/**
	 * @return the cellWidth
	 */
	public int getCellWidth() {
		return cellWidth;
	}


	/**
	 * @param cellWidth the cellWidth to set
	 */
	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}


	/**
	 * @return the cellHeight
	 */
	public int getCellHeight() {
		return cellHeight;
	}


	/**
	 * @param cellHeight the cellHeight to set
	 */
	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}


	public void recalculateCellSize() {
		//Determines the greatest width and height of individual cells, which then
		//are used as the width and height for every cell.
		for (Painter painter : this.getFieldPainters().values()) {
			if (cellWidth < painter.getWidth()) {
				cellWidth = painter.getWidth();
			}
			if (cellHeight < painter.getHeight()) {
				cellHeight = painter.getHeight();
			}
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActionCommands()
	 */
	@Override
	public String[] getActionCommands() {
		return new String[] {CHANGE_ORIENTATION, SHOW_INDICIES};
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
			paint();
			getCanvas().repaint();
		}
		else if (e.getActionCommand().equals(SHOW_INDICIES)) {
			if (indexOn) {
				getCanvas().removePainterToBePainted(this);
			}
			else {
				getCanvas().addPainterToBePainted(this);
			}
			indexOn = !indexOn;
			getCanvas().repaint();
		}
	}
	
	public void showIndex() {
		if (!indexOn) {
			getCanvas().addPainterToBePainted(this);
			indexOn = !indexOn;
			getCanvas().repaint();
		}
	}
	
	public void hideIndex() {
		if (indexOn) {
			getCanvas().removePainterToBePainted(this);
			indexOn = !indexOn;
			getCanvas().repaint();
		}
	}

	@Override
	public void draw(Graphics g) {
		if (this.getFieldPainters() == null) {
			return;
		}
		int length = this.getFieldPainters().size();
		for (int i = 0; i < length; i++) {
			Painter element = this.getFieldPainter("[" + i + "]");
			if (!isVertical) {
				g.drawString(i + "", element.getLocation().x + 10, 
						element.getLocation().y - 2);
			}
			else {
				g.drawString(i + "", element.getLocation().x - 25, 
						element.getLocation().y + element.getHeight() - 3);
			}
		}
	}
}
