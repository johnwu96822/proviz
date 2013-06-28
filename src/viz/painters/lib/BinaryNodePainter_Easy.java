package viz.painters.lib;

import java.awt.Point;
import java.util.ArrayList;

import viz.ProViz;
import viz.animation.motion.move.Path;
import viz.painters.Painter;
import viz.painters.ParasitePainter;
import viz.painters.java.hashtable.TEntryPainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.ConnectorManager;
import viz.views.VizCanvas;

/**
 * Paints a binary node which has a "value" field, a "left" child, and a "right" child.
 * The root BinaryNodePainter_Easy employs both parent and child control. It does not position
 * any node in the tree in addToCanvas() or paint() but uses doLayout() in handleChange()
 * to layout the tree.
 * @author JW
 *
 */
public abstract class BinaryNodePainter_Easy extends ParasitePainter {//PainterWithNoComponent {
	public BinaryNodePainter_Easy(IVizVariable vvar, VizCanvas canvas) {
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
		if (this.getParent() != null) {
			if (!(this.getParent() instanceof BinaryNodePainter_Easy)) {
	//This is the root node. Set it to parent's starting location.
				this.setLocation(getParent().getLocation().x, getParent().getLocation().y);
			}
			else {
				this.depth = ((BinaryNodePainter_Easy) getParent()).getDepth() + 1;
			}
		}
		paint();
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
			//if (this.getParent() == null) {
			//	doLayout();
			//}
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
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		addToCanvas();
		//doLayout();
		if (getParent() != null) {
			getParent().paint();
		}
		else {
			paint();
		}
		this.getCanvas().repaint();
	}

	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			Painter valuePainter = this.getFieldPainter(getValue());

			int x = this.getLocation().x;
			int y = this.getLocation().y;
			Painter left = this.getFieldPainter(this.getLeftChildName());
			Painter right = this.getFieldPainter(this.getRightChildName());
			int dist;
			int lDepth = this.getDepth(left.getVariable());
			int rDepth = this.getDepth(right.getVariable());
			dist = this.getWidth();
			int height = this.getHeight();
			int temp = dist * (int) Math.pow(2, lDepth - 1);
			left.setLocation(x - temp, y + 2 * height);
			temp = dist * (int) Math.pow(2, rDepth - 1);
			right.setLocation(x + temp, y + 2 * height);
			
			//int x = this.getLocation().x;
			//int y = this.getLocation().y;
			//valuePainter.setLocation(x, y);
			ConnectorManager cManager = getCanvas().getConnectorManager();
			if (!this.getVariable().getField(getLeftChildName()).isNull()) {
				//Painter left = this.getFieldPainter(getLeftChildName());
				//left.setLocation(x - 30, y + valuePainter.getHeight() + 30);
				left.paint();
				if (cManager.getConnector(valuePainter, left.getFieldPainter(getValue())) == null) {
					cManager.hookUsUp(valuePainter, left.getFieldPainter(getValue()));
				}
			}
			if (!this.getVariable().getField(getRightChildName()).isNull()) {
				//Painter right = this.getFieldPainter(getRightChildName());
				//right.setLocation(x + valuePainter.getWidth() + 30, y + valuePainter.getHeight() + 30);
				right.paint();
				if (cManager.getConnector(valuePainter, right.getFieldPainter(getValue())) == null) {
					cManager.hookUsUp(valuePainter, right.getFieldPainter(getValue()));
				}
			}
			//doLayout();
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
	private ArrayList<BinaryNodePainter_Easy> leaves;
	private BinaryNodePainter_Easy root = null;
	
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

	private void countLeaves(BinaryNodePainter_Easy node, int depth) {
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
				countLeaves(node.getLeft(), depth + 1);
			}
			if (node.getRight() != null && node.getRight().getValuePainter() != null) {
				countLeaves(node.getRight(), depth + 1);
			}
		}
	}
	
	public void doLayout() {
		counter = 0;
		root = this.getRootNodePainter();
		if (!root.getVariable().isNull()) {
			leaves = new ArrayList<BinaryNodePainter_Easy>();
			countLeaves(root, 0);
			int leftmost = root.getLocation().x - (leaves.size() - 1) * (width) / 2;
			if (root.getLeft().getVariable().isNull()) {
				if (!root.getRight().getVariable().isNull()) {
					leftmost += 10;
				}
			}
			else if (root.getRight().getVariable().isNull()) {
				if (!root.getLeft().getVariable().isNull()) {
					leftmost -= 10;					
				}
			}
			for (int i = 0; i < leaves.size(); i++) {
				BinaryNodePainter_Easy painter = leaves.get(i);
				if (painter != root) {
					painter.setLocation((width) * i + leftmost, painter.getDepth() * vGap + root.getLocation().y);
				}
			}
			layoutParents(root.getLeft());
			layoutParents(root.getRight());
			/*if (root.getLeft().getVariable().isNull()) {
				if (!root.getRight().getVariable().isNull()) {
					Point p = root.getRight().getLocation();
					root.getRight().setLocation(p.x + 10, p.y);
				}
			}
			if (root.getRight().getVariable().isNull()) {
				if (!root.getLeft().getVariable().isNull()) {
					Point p = root.getLeft().getLocation();
					root.getLeft().setLocation(p.x - 10, p.y);
				}
			}*/
		}
	}
	private void layoutParents(BinaryNodePainter_Easy node) {
		if (node == null) {
			return;
		}
		if (!node.isLeaf()) {
			//if (node.getParent() == null || !(node.getParent() instanceof BinaryNodePainter_Easy)) {
			//	return;
			//}
			//ProViz.errprintln((node.getParent()) + "");
			int leftEdge = -1;
			int rightEdge = -1;
			if (node.getLeft() != null && node.getLeft().getValuePainter() != null) {
				leftEdge = getLeftEdge(node.getLeft());
			}
			if (node.getRight() != null && node.getRight().getValuePainter() != null) {
				rightEdge = getRightEdge(node.getRight());
			}
			Painter valuePainter = node.getValuePainter();
			if (leftEdge == -1 && rightEdge != -1) {
				ProViz.println(leftEdge + " " + rightEdge);
				node.setLocation(rightEdge - node.getRight().getValuePainter().getWidth() - valuePainter.getWidth() / 2 - 10, node.getDepth() * vGap + root.getLocation().y);
				//node.setLocation(rightEdge - valuePainter.getWidth() - 3, node.getDepth() * vGap + root.getLocation().y);
				//valuePainter.setLocation(node.getLocation().x, node.getLocation().y);
			}
			else if (rightEdge == -1 && leftEdge != -1) {
				ProViz.println(leftEdge + " " + rightEdge);
				node.setLocation(leftEdge + node.getLeft().getValuePainter().getWidth() - valuePainter.getWidth() / 2 + 10, node.getDepth() * vGap + root.getLocation().y);
				//node.setLocation(leftEdge + 3, node.getDepth() * vGap + root.getLocation().y);
			}
			else if (leftEdge != -1 && leftEdge != -1) {
				ProViz.println(leftEdge + " " + rightEdge);
				node.setLocation((leftEdge + rightEdge) / 2 - valuePainter.getWidth() / 2, node.getDepth() * vGap + root.getLocation().y);
			}
			else {
				ProViz.errprintln("What the hell");
			}
		}
	}
	
	/**
	 * Gets the left most edge of the left subtree
	 * TODO Maybe not just the left subtree?
	 * @param left
	 * @return
	 */
	private int getLeftEdge(BinaryNodePainter_Easy left) {
		if (left.isLeaf()) {
			return left.getLocation().x;
		}
		layoutParents(left);
		return left.getLocation().x;
	}
	
	/**
	 * Gets the right most edge of the right subtree
	 * TODO Maybe not just the right subtree?
	 * @param right
	 * @return
	 */
	private int getRightEdge(BinaryNodePainter_Easy right) {
		if (right.isLeaf()) {
			return right.getLocation().x + right.getValuePainter().getWidth();
		}
		layoutParents(right);
		return right.getLocation().x + right.getValuePainter().getWidth();
	}

	public BinaryNodePainter_Easy getLeft() {
		if (!this.getVariable().isNull()) {
			return (BinaryNodePainter_Easy) this.getFieldPainter(this.getLeftChildName());
		}
		return null;
	}
	
	public BinaryNodePainter_Easy getRight() {
		if (!this.getVariable().isNull()) {
			return (BinaryNodePainter_Easy) this.getFieldPainter(this.getRightChildName());
		}
		return null;
	}
	
	public Painter getValuePainter() {
		return this.getFieldPainter(this.getValue());		
	}
	
	/**
	 * Gets the root node of this BinaryNodePainter_Easy tree
	 * @return
	 */
	public BinaryNodePainter_Easy getRootNodePainter() {
		Painter painter = this;
		while (painter.getParent() != null && painter.getParent() instanceof BinaryNodePainter_Easy) {
			painter = painter.getParent();
		}
		return (BinaryNodePainter_Easy) painter;
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
