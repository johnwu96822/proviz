package viz.animation.motion.move;

import java.awt.Point;

import viz.animation.motion.MoveMode;
import viz.painters.graphics.IMovable;

/**
 * Defines a linear path, a straight line, that moves an IMovable from source to destination.
 * @author JW
 */
public class LinearPath extends Path {
	private float stepX = 0;
	private float stepY = 0;
	private float currentX = 0;
	private float currentY = 0;
	private Point point = new Point(0, 0);
	private int counter = 0;
	private int iterations = 60;
	private int speed = 1;
	/**
	 * Initializes a path
	 * @param movingComp The component to be moved
	 * @param source The source point of the component
	 * @param destination The destination the component is moving to
	 * @param mode Not used. Can be null.
	 */
	public LinearPath(IMovable movingComp, Point source, Point destination, MoveMode mode) {
		super(movingComp, source, destination, mode);
		stepX = destination.x - source.x;
		stepY = destination.y - source.y;
		currentX = source.x;
		currentY = source.y;
		if (Math.abs(stepX) > Math.abs(stepY)) {
			this.speed = (int) Math.abs(stepX) / iterations;
			if (speed < 1) {
				speed = 1;
			}
			counter = (int) Math.abs(stepX);
			stepY /= Math.abs(stepX);
			stepX /= Math.abs(stepX);
		}
		else {
			this.speed = (int) Math.abs(stepY) / iterations;
			if (speed < 1) {
				speed = 1;
			}
			counter = (int) Math.abs(stepY);
			stepX /= Math.abs(stepY);
			stepY /= Math.abs(stepY);
		}
		if (speed > 1) {
			counter /= speed;
		}
	}

	@Override
	public Point getNextPoint(int scale) {
		if (counter <= 0) {
			return null;
		}
		while (scale > counter) {
			scale /= 2;
		}
		if (scale == 0) {
			scale = 1;
		}
		counter -= scale;
		currentX += stepX * speed * scale;
		currentY += stepY * speed * scale;
		point.setLocation(currentX, currentY);
		return point;
	}
}
