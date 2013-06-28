package test.painters.robot;

import java.awt.Graphics;

import viz.painters.DrawPainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class RobotPainter extends DrawPainter {
	private int bodyHeight = 80;
	public RobotPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}
	
	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			this.setLocation(200, 100);
			System.out.println("ADD TO CANVAS");
			super.addToCanvas_userImp();
			this.getFieldPainter("leftArm").addToCanvas();
			this.getFieldPainter("rightArm").addToCanvas();
			this.getFieldPainter("leftLeg").addToCanvas();
			this.getFieldPainter("rightLeg").addToCanvas();
			this.getFieldPainter("head").addToCanvas();
			paint();
		}
	}

	/* Draws an I-shape body 
	 * @see viz.painters.DrawPainter#draw_userImp(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics g) {
		int x = this.getLocation().x;
		int y = this.getLocation().y;
		g.drawLine(x, y, x + 100, y);
		g.drawLine(x + 50, y, x + 50, y + bodyHeight);
		g.drawLine(x + 20, y + bodyHeight, x + 80, y + bodyHeight);
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.DrawPainter#getHeight()
	 */
	@Override
	public int getHeight() {
		return bodyHeight;
	}

	/* (non-Javadoc)
	 * @see viz.painters.DrawPainter#getWidth()
	 */
	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public void paint_userImp() {
		if (!this.getVariable().isNull()) {
			this.getFieldPainter("head").paint();
			int x = this.getLocation().x;
			int y = this.getLocation().y;
			this.getFieldPainter("leftArm").setLocation(x + 15, y);
			this.getFieldPainter("rightArm").setLocation(x + 85, y);
			this.getFieldPainter("leftLeg").setLocation(x + 30, y + bodyHeight);
			this.getFieldPainter("rightLeg").setLocation(x + 70, y + bodyHeight);
		}
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		this.addToCanvas();
	}

	@Override
	protected void destroy_userImp() {
		// TODO Auto-generated method stub
		
	}
}
