package viz.painters.lib;

import java.awt.Point;
import java.util.ArrayList;

import viz.animation.motion.move.Path;
import viz.painters.Painter;
import viz.painters.ParasitePainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.ConnectorManager;
import viz.views.VizCanvas;

/**
 * Paints a binary node which has a "value" field, a "left" child, and a "right" child.
 * The root BinaryNodePainter employs both parent and child control. It does not position
 * any node in the tree in addToCanvas() or paint() but uses doLayout() in handleChange()
 * to layout the tree.
 * @author JW
 *
 */
public abstract class CompleteBinaryNodePainter extends ParasitePainter {//PainterWithNoComponent {

	public CompleteBinaryNodePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}
	
	/**
	 * Gets the name of the field that is the value.
	 * @return 
	 */
	public abstract String getValue();
	
	/**
	 * Gets the name of the field that is the left child.
	 * @return
	 */
	public abstract String getLeftChildName();

	/**
	 * Gets the name of the field that is the right child.
	 * @return
	 */
	public abstract String getRightChildName();

	@Override
	public int getHeight() {
		if (!this.getVariable().isNull()) {
			return this.getValuePainter().getHeight();
		}
		return 0;
	}

	@Override
	public int getWidth() {
		if (!this.getVariable().isNull()) {
			return this.getValuePainter().getWidth();
		}
		return 0;
	}

	/* Does not call paint(), more efficient.
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		if (this.getParent() != null && !(this.getParent() instanceof CompleteBinaryNodePainter)) {
	//This is the root node. Set it to parent's starting location.
			this.setLocation(getParent().getLocation().x, getParent().getLocation().y);
		}
		if (!this.getVariable().isNull()) {
			Painter valuePainter = this.getFieldPainter(getValue());
			valuePainter.addToCanvas();
			//int x = this.getLocation().x;
			//int y = this.getLocation().y;
			//valuePainter.setLocation(x, y);
	//Adds left child
			if (!this.getVariable().getField(getLeftChildName()).isNull()) {
				Painter left = this.getFieldPainter(getLeftChildName());
				//left.setLocation(x - 30, y + valuePainter.getHeight() + 30);
				left.addToCanvas();
				this.getCanvas().getConnectorManager().hookUsUp(valuePainter, left.getFieldPainter(getValue()));
			}
	//Adds right child
			if (!this.getVariable().getField(getRightChildName()).isNull()) {
				Painter right = this.getFieldPainter(getRightChildName());
				//right.setLocation(x + valuePainter.getWidth() + 30, y + valuePainter.getHeight() + 30);
				right.addToCanvas();
				this.getCanvas().getConnectorManager().hookUsUp(valuePainter, right.getFieldPainter(getValue()));
			}
			//TODO call this more efficiently
			doLayout();
		}
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
		if (getParent() != null) {
			getParent().paint();
		}
		this.getCanvas().repaint();
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			Painter valuePainter = this.getFieldPainter(getValue());
			//int x = this.getLocation().x;
			//int y = this.getLocation().y;
			//valuePainter.setLocation(x, y);
			ConnectorManager cManager = getCanvas().getConnectorManager();
			if (!this.getVariable().getField(getLeftChildName()).isNull()) {
				Painter left = this.getFieldPainter(getLeftChildName());
				//left.setLocation(x - 30, y + valuePainter.getHeight() + 30);
				left.paint();
				if (cManager.getConnector(valuePainter, left.getFieldPainter(getValue())) == null) {
					cManager.hookUsUp(valuePainter, left.getFieldPainter(getValue()));
				}
			}
			if (!this.getVariable().getField(getRightChildName()).isNull()) {
				Painter right = this.getFieldPainter(getRightChildName());
				//right.setLocation(x + valuePainter.getWidth() + 30, y + valuePainter.getHeight() + 30);
				right.paint();
				if (cManager.getConnector(valuePainter, right.getFieldPainter(getValue())) == null) {
					cManager.hookUsUp(valuePainter, right.getFieldPainter(getValue()));
				}
			}
			doLayout();
		}
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	private int counter = 0;
	protected static int width = 50;
	protected static int height = 40;
	protected static int vGap = 50;
	private int depth = 0;
	private ArrayList<CompleteBinaryNodePainter> leaves;
	private CompleteBinaryNodePainter root = null;
	
	public boolean isLeaf() {
		boolean rv = true;
		if (!this.getVariable().isNull()) {
			if ((getLeft() != null && getLeft().getValuePainter() != null) || (getRight() != null 
					&& getRight().getValuePainter() != null)) {
					//&& !getLeft().getValuePainter().getVariable().isNull() 
					//&& !getRight().getValuePainter().getVariable().isNull()) {
				rv = false;
			}
		}
		return rv;
	}

	private void layoutLeaves(CompleteBinaryNodePainter node, int depth, CompleteBinaryNodePainter root) {
		node.setDepth(depth);
		if (node.isLeaf()) {
	//node is a leaf
			//node.setLocation((width + 5) * counter + rootLoc.x, depth * vGap + rootLoc.y);
			//node.getValuePainter().setLocation(node.getLocation().x, node.getLocation().y);
			//ProViz.println(node.getValuePainter().getVariable().getValueAsString() + " " + depth + "-" + node.getLocation());
			leaves.add(node);
			counter++;
		}
		else {
			if (node.getLeft() != null && node.getLeft().getValuePainter() != null) {
				layoutLeaves(node.getLeft(), depth + 1, root);
			}
			if (node.getRight() != null && node.getRight().getValuePainter() != null) {
				layoutLeaves(node.getRight(), depth + 1, root);
			}
		}
	}
	
	public void doLayout() {
		counter = 0;
		root = this.getRootNodePainter();
		if (!root.getVariable().isNull()) {
			leaves = new ArrayList<CompleteBinaryNodePainter>();
			layoutLeaves(root, 0, root);
			int leftmost = root.getLocation().x - (leaves.size() - 1) * (width) / 2;
			for (int i = 0; i < leaves.size(); i++) {
				CompleteBinaryNodePainter painter = leaves.get(i);
				if (painter != root) {
					painter.setLocation((width) * i + leftmost, painter.getDepth() * vGap + root.getLocation().y);
				}
			}
			doLayout(root);
		}
	}
	private void doLayout(CompleteBinaryNodePainter node) {
		if (!node.isLeaf()) {
			int leftEdge = -1;
			int rightEdge = -1;
			if (node.getLeft() != null && node.getLeft().getValuePainter() != null) {
				leftEdge = getLeftEdge(node.getLeft());
			}
			if (node.getRight() != null && node.getRight().getValuePainter() != null) {
				rightEdge = getRightEdge(node.getRight());
			}
			if (node.getParent() == null || !(node.getParent() instanceof CompleteBinaryNodePainter)) {
				return;
			}
			Painter valuePainter = node.getValuePainter();
			if (leftEdge == -1) {
				node.setLocation(rightEdge - valuePainter.getWidth() - 3, node.getDepth() * vGap + root.getLocation().y);
				//valuePainter.setLocation(node.getLocation().x, node.getLocation().y);
			}
			else if (rightEdge == -1) {
				node.setLocation(leftEdge + 3, node.getDepth() * vGap + root.getLocation().y);
			}
			else {
				node.setLocation((leftEdge + rightEdge) / 2 - valuePainter.getWidth() / 2, node.getDepth() * vGap + root.getLocation().y);
			}
		}
	}
	
	/**
	 * Gets the left most edge of the left subtree
	 * TODO Maybe not just the left subtree?
	 * @param left
	 * @return
	 */
	private int getLeftEdge(CompleteBinaryNodePainter left) {
		if (left.isLeaf()) {
			return left.getLocation().x;
		}
		doLayout(left);
		return left.getLocation().x;
	}
	
	/**
	 * Gets the right most edge of the right subtree
	 * TODO Maybe not just the right subtree?
	 * @param right
	 * @return
	 */
	private int getRightEdge(CompleteBinaryNodePainter right) {
		if (right.isLeaf()) {
			return right.getLocation().x + right.getValuePainter().getWidth();
		}
		doLayout(right);
		return right.getLocation().x + right.getValuePainter().getWidth();
	}

	public CompleteBinaryNodePainter getLeft() {
		if (!this.getVariable().isNull()) {
			return (CompleteBinaryNodePainter) this.getFieldPainter(this.getLeftChildName());
		}
		return null;
	}
	
	public CompleteBinaryNodePainter getRight() {
		if (!this.getVariable().isNull()) {
			return (CompleteBinaryNodePainter) this.getFieldPainter(this.getRightChildName());
		}
		return null;
	}
	
	public Painter getValuePainter() {
		return this.getFieldPainter(this.getValue());		
	}
	
	/**
	 * Gets the root node of this BinaryNodePainter tree
	 * @return
	 */
	public CompleteBinaryNodePainter getRootNodePainter() {
		Painter painter = this;
		while (painter.getParent() != null && painter.getParent() instanceof CompleteBinaryNodePainter) {
			painter = painter.getParent();
		}
		return (CompleteBinaryNodePainter) painter;
	}

	/* (non-Javadoc)
	 * @see viz.painters.ParasitePainter#getTargetPainter()
	 */
	@Override
	public Painter getTargetPainter() {
		return getValuePainter();
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#moveMotion(java.awt.Point, viz.animation.motion.move.Path)
	 */
	@Override
	public void moveMotion(Point next, Path path) {
		this.setLocation(next.x, next.y);
	}
}
