package viz.animation.rotate;

import viz.animation.motion.MoveMode;

public abstract class AbstractRotation implements Rotation {
	private double original;
	private double destination;
	private MoveMode mode;
	
	public AbstractRotation(double orig, double dest, MoveMode mode) {
		this.original = orig;
		this.destination = dest;
		this.mode = mode;
	}
	
	/**
	 * 
	 * @param scale An integer between 1 and 37
	 * @return
	 */
	public abstract Double getNext(int speedScale);

	/**
	 * @return the original
	 */
	public double getOriginal() {
		return original;
	}

	/**
	 * @return the destination
	 */
	public double getDestination() {
		return destination;
	}

	/**
	 * @return the mode
	 */
	public MoveMode getMode() {
		return mode;
	}
	
	
}
