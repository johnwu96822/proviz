package viz.painters.java.hashtable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import viz.model.FieldViz;
import viz.model.TypeViz;
import viz.model.VizMapModel;
import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.ConnectorManager;
import viz.views.VizCanvas;

/**
 * Parent control; every painter node controls both its field painters (left and right).
 * 
 * Safe to be subclassed.
 * @author JW
 *
 */
public class TEntryPainter extends Painter {
	private VizCanvas myCanvas = null;
	//Flag for whether myCanvas has been added to canvas
	private boolean canvasIsAdded = false;
	private JLabel nullLabel;
	
	//Flag for whether nullLabel is added to myCanvas
	private boolean labelOn = false;
	private static final String ORIENTATION = "Change Orientation";
	private static final String SHOW_NULL = "Show Null";
	private static final String HIDE_NULL = "Hide Null";
	private boolean orientation = true;
	
	public TEntryPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.myCanvas = new VizCanvas(this.getCanvas().getComponentListener());
		this.nullLabel = new JLabel("null");
		this.nullLabel.setHorizontalAlignment(JLabel.CENTER);
		this.setDefaultBorderColor(Color.black);
	}

	@Override
	protected void addToCanvas_userImp() {
		myPaint();
		if (this.hasFieldPainter()) {
			Painter key = this.getFieldPainter("key");
			key.setCanvas(myCanvas);
			key.setRedirectMouseSelection(this);
			key.setTooltipsVariablePortion("key");
			key.addToCanvas();
			
			Painter value = this.getFieldPainter("value");
			value.setCanvas(myCanvas);
			value.setRedirectMouseSelection(this);
			value.addToCanvas();
			
			this.getFieldPainter("left").addToCanvas();
			this.getFieldPainter("right").addToCanvas();
		}
		else {
			if (!canvasIsAdded) {
				this.getCanvas().add(myCanvas);
				canvasIsAdded = true;
			}
		}
	}
	
	private void myPaint() {
		int width = this.getTreeMapPainter().getCellWidth();
		int height = this.getTreeMapPainter().getCellHeight();
		if (!this.getVariable().isNull()) {
			if (labelOn) {
				this.myCanvas.remove(this.nullLabel);
				labelOn = false;
			}
			myCanvas.setBounds(this.getLocation().x, this.getLocation().y, width * 2 + 2, height + 2);
			this.myCanvas.setBorder(BorderFactory.createLineBorder(this.getDefaultBorderColor()));
			
			Painter key = this.getFieldPainter("key");
			key.setLocation(1, 1);
			key.setSize(width, height);
			key.paint();
			Painter value = this.getFieldPainter("value");
			value.setLocation(width + 1, 1);
			value.setSize(width, height);
			value.paint();
			int x = this.getLocation().x;
			int y = this.getLocation().y;
			Painter left = this.getFieldPainter("left");
			Painter right = this.getFieldPainter("right");
			int dist;
			int lDepth = this.getDepth(left.getVariable()) + 1;
			int rDepth = this.getDepth(right.getVariable()) + 1;
			if (!this.getTreeMapPainter().isShowNull()) {
				lDepth = lDepth > 0 ? lDepth - 1 : 0;
				rDepth = rDepth > 0 ? rDepth - 1 : 0;
			}
			((TEntryPainter) left).setOrientation(orientation);
			((TEntryPainter) right).setOrientation(orientation);
			if (orientation) {
				dist = height + 5;
				int temp = dist * (int) Math.pow(2, lDepth - 1);
				left.setLocation(x + 2 * width + 10, y + temp);
				temp = dist * (int) Math.pow(2, rDepth - 1);
				right.setLocation(x + 2 * width + 10, y - temp);
			}
			else {
				dist = width + 10;
				int temp = dist * (int) Math.pow(2, lDepth - 1);
				left.setLocation(x - temp, y + 2 * height);
				temp = dist * (int) Math.pow(2, rDepth - 1);
				right.setLocation(x + temp, y + 2 * height);
			}
			left.paint();
			right.paint();
	//Hooking up the connectors
			ConnectorManager cManager = getCanvas().getConnectorManager();
			if (cManager.getConnector(this, left) == null) {
				if (this.getTreeMapPainter().isShowNull()) {
					cManager.hookUsUp(this, left);
				}
				else {
					if (!left.getVariable().isNull()) {
						cManager.hookUsUp(this, left);
					}
				}
			}
			if (cManager.getConnector(this, right) == null) {
				if (this.getTreeMapPainter().isShowNull()) {
					cManager.hookUsUp(this, right);
				}
				else {
					if (!right.getVariable().isNull()) {
						cManager.hookUsUp(this, right);
					}
				}
			}
			if (!canvasIsAdded) {
				this.getCanvas().add(myCanvas);
				canvasIsAdded = true;
			}
		}
		else {
			paintNull(width, height);
		}
	}

	@Override
	protected void paint_userImp() {
		myPaint();
	}

	/**
	 * Paints null if the owner variable points to null
	 */
	private void paintNull(int width, int height) {
		if (this.getTreeMapPainter().isShowNull()) {
			nullLabel.setBounds(1, 1, width * 2, height);
//This variable is null
			this.myCanvas.setBounds(this.getLocation().x, this.getLocation().y, width * 2, height);
			this.setDefaultBorderColor(null);
			this.myCanvas.setBorder(null);
			if (!labelOn) {
				this.myCanvas.add(this.nullLabel);
				labelOn = true;
			}
			if (!canvasIsAdded) {
				this.getCanvas().add(this.myCanvas);
				canvasIsAdded = true;
			}
		}
		else {
			if (canvasIsAdded) {
				getCanvas().remove(this.myCanvas);
				canvasIsAdded = false;
			}
			ConnectorManager cManager = getCanvas().getConnectorManager();
			if (cManager.getConnector(this, this.getParent()) != null) {
				cManager.removeConnector(cManager.getConnector(this, this.getParent()));
			}
		}
	}
	
	/**
	 * Gets the depth of the given variable in the tree.
	 * @param var
	 * @return
	 */
	public int getDepth(IVizVariable var) {
		int rv = 0;
		if (var != null && var.hasField()) {
			rv = getDepth(var.getField("left")) + 1;
			int temp = getDepth(var.getField("right")) + 1;
			if (rv < temp) {
				rv = temp;
			}
		}
		return rv;
	}

	@Override
	protected void destroy_userImp() {
		this.myCanvas.clearAll();
	}

	@Override
	public JComponent getComponent() {
		return myCanvas;
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (change != Change.TO_NULL) {
			addToCanvas();
		}
		this.getRootPainter().paint();
		this.getCanvas().repaint();
	}
	
	public TreeMapPainter getTreeMapPainter() {
		Painter painter = this;
		while (painter.getParent() != null && !(painter instanceof TreeMapPainter)) {
			painter = painter.getParent();
		}
		return (painter instanceof TreeMapPainter) ? (TreeMapPainter) painter : null;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActionCommands()
	 */
	@Override
	public String[] getActionCommands() {
		String showNull = SHOW_NULL;
		if (this.getTreeMapPainter().isShowNull()) {
			showNull = HIDE_NULL;
		}
		return new String[] {"Change Orientation", showNull};
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getActionListener()
	 */
	@Override
	public ActionListener getRightClickActionListener() {
		return new RightClickMenuListener();
	}	
	
	private class RightClickMenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals(ORIENTATION)) {
				orientation = !orientation;
				paint();
			}
			else if (e.getActionCommand() == SHOW_NULL || e.getActionCommand() == HIDE_NULL) {
				TreeMapPainter tmPainter = getTreeMapPainter();
				tmPainter.setShowNull(!tmPainter.isShowNull());
				tmPainter.paint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#switchedToThisPainter()
	 */
	@Override
	public void switchedToThisPainter() {
		this.getCanvas().getConnectorManager().removeAll(this);
		/*Painter key = this.getFieldPainter("key");
		if (key != null) {
			key.destroy();
		}
		Painter value = this.getFieldPainter("value");
		if (value != null) {
			value.destroy();
		}*/
		String tep = this.getClass().getName();
		FieldViz fv = VizMapModel.getInstance().findFieldViz_runtime(this.getVariable().getActualType(), "left");
		System.out.println("Changing field viz's current viz - left: " + fv.setCurrentViz(tep));
		fv = VizMapModel.getInstance().findFieldViz_runtime(this.getVariable().getActualType(), "right");
		System.out.println("Changing field viz's current viz - right: " + fv.setCurrentViz(tep));
		fv = VizMapModel.getInstance().findFieldViz_runtime(this.getTreeMapPainter().getVariable().getActualType(), "root");
		System.out.println("Changing field viz's current viz - root: " + fv.setCurrentViz(tep));
		TypeViz tv = VizMapModel.getInstance().findTypeViz_runtime(this.getVariable().getActualType());
		System.out.println("Changing type viz's current viz: " + tv.setCurrentViz(tep));

		this.getTreeMapPainter().switchAll(this.getTreeMapPainter().getFieldPainter("root"), this);
		this.getTreeMapPainter().addToCanvas();
	}

	/**
	 * @return the orientation
	 */
	public boolean isOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(boolean orientation) {
		this.orientation = orientation;
	}
}
