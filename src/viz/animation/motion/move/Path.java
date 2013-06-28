package viz.animation.motion.move;

import java.awt.Point;

import javax.swing.JComponent;

import viz.animation.motion.Motion;
import viz.animation.motion.MoveMode;
import viz.painters.Painter;
import viz.painters.graphics.IMovable;

public abstract class Path implements Motion {
	private Point source;
	private Point destination;
	private MoveMode mode;
	private IMovable movingComp;
	
	/**
	 * Initializes a path
	 * @param movingComp The component to be moved
	 * @param source The source point of the component
	 * @param destination The destination the component is moving to
	 * @param mode The moving mode defined by each subclass
	 */
	public Path(IMovable movingComp, Point source, Point destination, MoveMode mode) {
		this.movingComp = movingComp;
		this.source = (Point) source.clone();
		this.destination = (Point) destination.clone();
		this.mode = mode;
	}
	
	/**
	 * Gets the next point in the path, which advances the current state.
	 * @param scale An integer ranges from 1x to 37x
	 * @return The next point in the path.
	 */
	public abstract Point getNextPoint(int scale);
	
	/* (non-Javadoc)
	 * @see viz.animation.motion.Motion#stepMotion(int)
	 */
	public boolean stepMotion(int speedScale) {//doNext(int speedScale) {
		boolean shouldContinue = true;
		Point next = this.getNextPoint(speedScale);
		if (next != null) {
			doMove(next);
			shouldContinue = false;
		}
		return shouldContinue;
	}
	
	/**
	 * Performs the move motion based on the type of the moving object
	 * @param next
	 */
	public void doMove(Point next) {
		if (movingComp instanceof Painter) {
			((Painter) movingComp).moveMotion(next, this);
		}
		else if (movingComp instanceof JComponent) {
			movingComp.setLocation(next.x, next.y);
		}
	}
	
	/* 
	 * @see viz.animation.motion.Motion#finishMotion(viz.painters.Painter)
	 */
	@Override
	public void noAnimationMotion() {//IPainter painter) {
		//Painter.moveTo(movingPainter, this.getDestination().x, this.getDestination().y);
		if (movingComp instanceof Painter) {
			((Painter) movingComp).moveMotion(this.getDestination(), this);
		}
		else {
			movingComp.setLocation(destination.x, destination.y);
		}
	}

	/**
	 * @return the source
	 */
	public Point getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(Point source) {
		this.source = source;
	}
	/**
	 * @return the destination
	 */
	public Point getDestination() {
		return destination;
	}
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(Point destination) {
		this.destination = destination;
	}
	/**
	 * @return the mode
	 */
	public MoveMode getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(MoveMode mode) {
		this.mode = mode;
	}
}
