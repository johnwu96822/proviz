package viz.animation.rotate;

import viz.animation.motion.Motion;

public interface Rotation extends Motion {
	/**
	 * 
	 * @param speedScale An integer value between 1 and 37
	 * @return The next angle of the stick in 360 degree format
	 */
	public Double getNext(int speedScale);
}
