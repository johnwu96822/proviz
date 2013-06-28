package viz.animation.motion;

public interface Motion {
	
	/**
	 * When animation checkbox is checked off, this method is used to finish the motion,
	 * so animation should not be defined in here.
	 * @ param painter The painter that is scheduled with this motion
	 */
	public void noAnimationMotion();//IPainter painter);

	/**
	 * @param speedScale
	 * @return true when the motion finishes; false when the motion is still on going
	 */
	public boolean stepMotion(int speedScale);
}
