package viz.animation.motion.move;

import java.awt.Point;

import viz.animation.motion.Direction;
import viz.animation.motion.MoveMode;
import viz.painters.graphics.IMovable;

/**
 * Defines a 180-degree, half-a-circle path with moving mode being Direction.UP or DOWN.
 * @author JW
 */
public class CurvePath extends Path {
	private int dx;
	private int dy;
	private int radius;
	private double originX;
	private double originY;
	private Point current = new Point();
	private double theta;
	private int iterations = 60;
	private int counter = iterations;
	
	/**
	 * @param movingComp
	 * @param source
	 * @param destination
	 * @param mode Should be either Direction.UP or DOWN
	 */
	public CurvePath(IMovable movingComp, Point source, Point destination, MoveMode mode) {
		super(movingComp, source, destination, mode);
		current.x = source.x;
		current.y = source.y;
		dx = destination.x - source.x;
		dy = destination.y - source.y;
		originX = (source.x + destination.x) / 2;
		originY = (source.y + destination.y) / 2;
		radius = (int) Math.sqrt(dx * dx + dy * dy) / 2;
		if (dx != 0) {
			theta = Math.atan2(dy, dx);
		}
		else {
			if (dy > 0) {
				theta = Math.PI * 3 / 2;
			}
			else {
				theta = Math.PI / 2;
			}
		}
		//ProViz.println("(dx, dy) - " + dx + " ," + dy);
		//ProViz.println(Math.cos(theta) + ", " + Math.sin(theta) + " - " + (theta / Math.PI * 180));
	}

	/*
	 * Sets the number of iteratioins, which can an int between 30 to 180.
	 * @param iter
	 * @return
	 *
	public boolean setIterations(int iter) {
		boolean rv = false;
		if (iter > 29 && iter <= 180) {
			this.iterations = iter;
			counter = iterations;
			rv = true;
		}
		return rv;
	}*/

	@Override
	public Point getNextPoint(int scale) {
	//scale is from 1 to 6
		scale = scale / 5 + 1;
		if (counter < 1) {
			return null;
		}
		if (this.getMode() == Direction.DOWN) {
			if (dx >= 0) {
				theta -= Math.PI / (iterations / scale);
				current.x = (int) (originX - radius * Math.cos(theta));
				current.y = (int) (originY - radius * Math.sin(theta));
				
			}
			else {
				theta += Math.PI / (iterations / scale);
				current.x = (int) (originX - radius * Math.cos(theta));
				current.y = (int) (originY - radius * Math.sin(theta));
			}
		}
		else {//if (this.getMode() == Direction.UP) {
			if (dx >= 0) {
				theta += Math.PI / (iterations / scale);
				current.x = (int) (originX - radius * Math.cos(theta));
				current.y = (int) (originY - radius * Math.sin(theta));
			}
			else {
				theta -= Math.PI / (iterations / scale);
				current.x = (int) (originX - radius * Math.cos(theta));
				current.y = (int) (originY - radius * Math.sin(theta));
			}
		}
		//theta %= 2 * Math.PI;
		//System.out.println(theta / Math.PI * 180);
		//current.x = (int) (radius * Math.cos(theta) + origin.x);
		//current.y = (int) (radius * Math.sin(theta) + origin.y);
		//System.out.println(current);
		counter -= scale;
		return current;
	}
}
