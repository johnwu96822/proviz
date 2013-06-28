package viz.painters.java.hashtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Visualizes java.util.Hashtable and java.util.HashMap in a table representation, with key
 * occupying one column and value the other. This painter calculates and arranges positions
 * for all entry painters, so that entry painters do not need to position themselves.
 * @author JW
 */
public class HashtablePainter extends PainterWithNoComponent implements ActionListener {
	private static final String SHOW_CHAINS = "Show Chains";
	private static final String HIDE_CHAINS = "Hide Chains"; 
	private boolean showChains = false;
	private int cellWidth = 30;
	private int cellHeight = 20;


	public HashtablePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.setLocation(30, 30);
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		int rv = 0;
		if (!showChains) {
			
		}
		return rv;
	}

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			Painter dummyTable = this.getFieldPainter("table");
			if (!(dummyTable instanceof viz.painters.DummyPainter)) {
				dummyTable = ProViz.getVPM().switchOver(dummyTable, "viz.painters.DummyPainter", true, false, false);
			}
			for (Painter element : dummyTable.getFieldPainters().values()) {
				element.addToCanvas();
			}
			paint();
		}
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			Painter dummyTable = this.getFieldPainter("table");
			int arraySize = dummyTable.getFieldPainters().size();
			int count;
	//java.util.Hashtable uses 'count'
			IVizVariable size = this.getVariable().getField("count");
			if (size == null) {
	//java.util.HashMap uses 'size
				size = this.getVariable().getField("size");
			}
			count = Integer.parseInt(size.getValueAsString());
			int vPos = this.getLocation().y;
			int x = this.getLocation().x;
			for (int i = 0; i < arraySize; i++) {
				Painter entryPainter = dummyTable.getFieldPainter("[" + i + "]");
				if (!entryPainter.getVariable().isNull()) {
	//Position this painter
					entryPainter.setLocation(x, vPos);
					entryPainter.paint();
					Painter nextPainter = entryPainter.getFieldPainter("next");
					//int xPos = x;
					while (!nextPainter.getVariable().isNull()) {
						//System.out.println("Painting next: " + nextPainter.getVariable().getField("key"));
						if (showChains) {
							//xPos += nextPainter.getWidth() + 20;
							//nextPainter.setLocation(xPos, vPos);
						}
						else {
							vPos += nextPainter.getHeight();
							nextPainter.setLocation(x, vPos);
						}
						nextPainter.paint();
						count--;
						nextPainter = nextPainter.getFieldPainter("next");
					}
					vPos += entryPainter.getHeight();
					count--;
				}
				if (count < 1) {
					break;
				}
			} //end for
		} //end if
	}
	
	/**
	 * @return the showChains
	 */
	public boolean isShowChains() {
		return showChains;
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (source == this.getVariable()) {
			this.addToCanvas();
		}
		else if (source.getName().equals("table")) {
			this.addToCanvas();
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActions()
	 */
	@Override
	public String[] getActionCommands() {
		String[] rv = new String[1];
		if (!showChains) {
			rv[0] = SHOW_CHAINS;
		}
		else {
			rv[0] = HIDE_CHAINS;
		}
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActionListener()
	 */
	@Override
	public ActionListener getRightClickActionListener() {
		return this;//new RightClickMenuListener();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SHOW_CHAINS) || e.getActionCommand().equals(HIDE_CHAINS)) {
			showChains = !showChains;
			paint();
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
}
