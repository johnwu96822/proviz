package viz.painters.java.hashtable;

import java.awt.Color;

import javax.swing.JComponent;

import viz.model.FieldViz;
import viz.model.TypeViz;
import viz.model.VizMapModel;
import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class TTableEntryPainter extends Painter {
	//index is set by the parent painter
	private int index = -1;
	private VizCanvas canvas;
	private boolean myIsAdded = false;
	
	public TTableEntryPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.canvas = new VizCanvas(this.getCanvas().getComponentListener());
		this.setDefaultBorderColor(Color.BLACK);
	}
	
	/**
	 * Initializes the indices of all painters in a prefix traversal.
	 */
	private void init() {
		init(0);
	}
	
	private int init(int count) {
		this.setIndex(count);
		if (!this.getVariable().isNull()) {
			TTableEntryPainter left = (TTableEntryPainter) this.getFieldPainter("left");
			TTableEntryPainter right = (TTableEntryPainter) this.getFieldPainter("right");
			if (!left.getVariable().isNull()) {
				count = left.init(count + 1);
			}
			if (!right.getVariable().isNull()) {
				count = right.init(count + 1);
			}
		}
		return count;
	}

	/* 
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
	//Should not have an NullPointerException, since a TTableEntryPainter must have a TreeMapPainter as a root
			//((TTableEntryPainter) tmPainter.getFieldPainter("root")).init();
			this.canvas.setBounds(0, 0, 2 * this.getTreeMapPainter().getCellWidth() + 2, this.getTreeMapPainter().getCellHeight() + 2);
			Painter key = this.getFieldPainter("key");
			Painter value = this.getFieldPainter("value");
			TTableEntryPainter left = (TTableEntryPainter) this.getFieldPainter("left");
			TTableEntryPainter right = (TTableEntryPainter) this.getFieldPainter("right");
			key.setCanvas(this.canvas);
			key.setRedirectMouseSelection(this);
			key.addToCanvas();
			value.setCanvas(this.canvas);
			value.setRedirectMouseSelection(this);
			value.addToCanvas();
			left.addToCanvas();
			right.addToCanvas();
			paint();
		}
	}

	@Override
	protected void destroy_userImp() {
		
	}

	@Override
	public JComponent getComponent() {
		return this.canvas;
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
		this.getTreeMapPainter().paint();
	}

	public TreeMapPainter getTreeMapPainter() {
		Painter painter = this;
		while (painter.getParent() != null && !(painter instanceof TreeMapPainter)) {
			painter = painter.getParent();
		}
		return (painter instanceof TreeMapPainter) ? (TreeMapPainter) painter : null;
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
	//If the parent is a TreeMapPainter, then this painter is the first, root node.
			TreeMapPainter tmPainter = this.getTreeMapPainter();
			((TTableEntryPainter) tmPainter.getFieldPainter("root")).init();
			Painter key = this.getFieldPainter("key");
			Painter value = this.getFieldPainter("value");
			TTableEntryPainter left = (TTableEntryPainter) this.getFieldPainter("left");
			TTableEntryPainter right = (TTableEntryPainter) this.getFieldPainter("right");
			int width = tmPainter.getCellWidth();
			int height = tmPainter.getCellHeight();
			this.setLocation(tmPainter.getLocation().x, tmPainter.getLocation().y 
					+ this.index * height);
			this.canvas.setBounds(this.getLocation().x, this.getLocation().y, 
					2 * width + 2, height + 2);
			if (!myIsAdded) {
				this.getCanvas().add(canvas);
				myIsAdded = !myIsAdded;
			}
			key.setLocation(1, 1);
			key.setSize(width, height);
			value.setLocation(width + 1, 1);
			value.setSize(width, height);
			left.paint();
			right.paint();
		}
		else {
			if (myIsAdded) {
				this.getCanvas().remove(this.canvas);
				myIsAdded =! myIsAdded;
			}
		}
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#switched()
	 */
	@Override
	public void switchedToThisPainter() {
		this.getCanvas().getConnectorManager().removeAll(this);
		String ttp = this.getClass().getName();
		FieldViz fv = VizMapModel.getInstance().findFieldViz_runtime(this.getVariable().getActualType(), "left");
		System.out.println("Changing field viz's current viz - left: " + fv.setCurrentViz(ttp));
		fv = VizMapModel.getInstance().findFieldViz_runtime(this.getVariable().getActualType(), "right");
		System.out.println("Changing field viz's current viz - right: " + fv.setCurrentViz(ttp));
		fv = VizMapModel.getInstance().findFieldViz_runtime(this.getTreeMapPainter().getVariable().getActualType(), "root");
		System.out.println("Changing field viz's current viz - root: " + fv.setCurrentViz(ttp));
		TypeViz tv = VizMapModel.getInstance().findTypeViz_runtime(this.getVariable().getActualType());
		System.out.println("Changing type viz's current viz: " + tv.setCurrentViz(ttp));
		/*
		if (this.getFieldPainter("key") != null) {
			this.getFieldPainter("key").destroy();
		}
		if (this.getFieldPainter("value") != null) {
			this.getFieldPainter("value").destroy();
		}*/
		this.getTreeMapPainter().switchAll(this.getTreeMapPainter().getFieldPainter("root"), this);
		this.getTreeMapPainter().addToCanvas();
	}

	/*private void switchAll(Painter painter, Painter origin) {
		Painter newPainter = ProViz.getInstance().getVPM().switchOver(painter, origin.getClass().getName(), true, false);//, null);
		Painter key = newPainter.getFieldPainter("key");
		if (key != null) {
			key.destroy();
		}
		Painter value = newPainter.getFieldPainter("value");
		if (value != null) {
			value.destroy();
		}
		Painter left = newPainter.getFieldPainter("left");
		if (left != null) {
			if (left == origin) {
	//Call on the original's left and right
				Painter lLeft = left.getFieldPainter("left");
				if (lLeft != null) {
					//System.out.println("Switch origin's left" + lLeft.getVariable().getValueAsString());
					switchAll(lLeft, origin);
				}
				Painter lRight = left.getFieldPainter("right");
				if (lRight != null) {
					//System.out.println("Switch origin's right" + lRight.getVariable().getValueAsString());
					switchAll(lRight, origin);
				}
			}
			else {
				//System.out.println("Switch left" + left.getVariable().getValueAsString());
				switchAll(left, origin);
			}
		}
		Painter right = newPainter.getFieldPainter("right");
		if (right != null) {
			if (right == origin) {
				Painter rLeft = right.getFieldPainter("left");
				if (rLeft != null) {
					//System.out.println("Switch origin's left" + rLeft.getVariable().getValueAsString());
					switchAll(rLeft, origin);
				}
				Painter rRight = right.getFieldPainter("right");
				if (rRight != null) {
					//System.out.println("Switch origin's right" + rRight.getVariable().getValueAsString());
					switchAll(rRight, origin);
				}
			}
			else {
				switchAll(right, origin);
				//System.out.println("Switch right" + right.getVariable().getValueAsString());
			}
		}
	}*/
}
