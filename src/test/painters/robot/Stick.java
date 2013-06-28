package test.painters.robot;

import java.awt.Point;

public class Stick {
	private Point anchor = new Point();
	private Point end = new Point();
	private int r = 1;
	private double theta = 0;
	public static final double RADIAN = Math.PI / 180;
	
	public Stick() {}
	
	public Stick(int x, int y, int length, double theta) {
		this.anchor.x = x;
		this.anchor.y = y;
		this.r = length;
		this.setAngle(theta);
	}
	
	/**
	 * @return the anchor
	 */
	public Point getAnchor() {
		return anchor;
	}
	/**
	 * @param anchor the anchor to set
	 */
	public void setAnchor(int x, int y) {
		this.anchor.x = x;
		this.anchor.y = y;
	}
	/**
	 * @return the end
	 */
	public Point getEndPoint() {
		end.x = anchor.x + (int) (r * Math.cos(theta));
		end.y = anchor.y - (int) (r * Math.sin(theta));
		return end;
	}
	
	/**
	 * @return the length
	 */
	public int getLength() {
		return r;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.r = length;
	}
	
	public void setAnchor(Point loc) {
		this.anchor = loc;
	}

	/**
	 * Gets the angle of this stick in 360 degree format
	 * @return 
	 */
	public double getAngle() {
		return theta / RADIAN;
	}
	
	/**
	 * Gets the angle of this stick in radian format
	 * @return
	 */
	public double getTheta() {
		return this.theta;
	}

	/**
	 * @param theta the theta to set
	 */
	public void setAngle(double theta) {
		this.theta = theta * RADIAN;
	}
}
