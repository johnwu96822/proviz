package viz.painters.graphics;

import java.awt.Graphics;
import java.awt.Polygon;

public class StraightArrow {
	private int srcX = 0;
	private int srcY = 0;
	private int destX = 0;
	private int destY = 0;
	
	public StraightArrow() {}
	
	public StraightArrow(int srcX, int srcY, int destX, int destY) {
		this.srcX = srcX;
		this.srcY = srcY;
		this.destX = destX;
		this.destY = destY;
	}
	
	public void draw(Graphics g) {
		draw(g, srcX, srcY, destX, destY);
	}
	
	public void setDestination(int x, int y) {
		destX = x;
		destY = y;
	}
	
	public void setSource(int x, int y) {
		srcX = x;
		srcY = y;
	}

	/**
	 * @return the srcX
	 */
	public int getSrcX() {
		return srcX;
	}

	/**
	 * @return the srcY
	 */
	public int getSrcY() {
		return srcY;
	}

	/**
	 * @return the destX
	 */
	public int getDestX() {
		return destX;
	}

	/**
	 * @return the destY
	 */
	public int getDestY() {
		return destY;
	}

	public static void draw(Graphics g, int srcX, int srcY, int destX, int destY) {
		g.drawLine(srcX, srcY, destX, destY);
		Polygon polygon = new Polygon();
		polygon.addPoint(destX, destY);
		double theta = Math.atan2(srcY - destY, srcX - destX);
		int v = 8;
		int l = 6;
		polygon.addPoint((int) (destX + v * Math.cos(theta) + l * Math.sin(theta)), 
				(int) (destY + v * Math.sin(theta) - l * Math.cos(theta)));
		polygon.addPoint((int) (destX + v * Math.cos(theta) - l * Math.sin(theta)), 
				(int) (destY + v * Math.sin(theta) + l * Math.cos(theta)));
		g.fillPolygon(polygon);
	}
}
