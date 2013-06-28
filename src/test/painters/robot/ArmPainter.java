package test.painters.robot;

import java.awt.Graphics;
import java.awt.Point;

import viz.painters.DrawPainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class ArmPainter extends DrawPainter {
	private Stick upper;
	private Stick lower;
	//private HandComponent hand;
	public ArmPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.upper = new Stick(30, 30, getFieldValue("upper"), 270);
	//Sets the anchor of upper to be the same object of this painter's location
		this.upper.setAnchor(this.getLocation());
		Point temp = this.upper.getEndPoint();
		lower = new Stick(temp.x, temp.y, getFieldValue("length") - getFieldValue("upper"), getFieldValue("angle") + upper.getAngle());
	}
	
	public int getFieldValue(String name) {
		return Integer.parseInt(this.getVariable().getField(name).getValueAsString());
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#draw(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics g) {
		Point temp = upper.getEndPoint();
		lower.setAnchor(temp.x, temp.y);
		Point lo = lower.getEndPoint();
		g.drawLine(upper.getAnchor().x, upper.getAnchor().y, temp.x, temp.y);
		g.drawLine(temp.x, temp.y, lo.x, lo.y);
	}
	
	/**
	 * @return the upper
	 */
	public Stick getUpper() {
		return upper;
	}

	/**
	 * @return the lower
	 */
	public Stick getLower() {
		return lower;
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		paint();
	}

	@Override
	protected void paint_userImp() {
		this.getCanvas().repaint();
	}

	@Override
	protected void destroy_userImp() {
		// TODO Auto-generated method stub
		
	}
}
