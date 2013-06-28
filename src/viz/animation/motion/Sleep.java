package viz.animation.motion;

import viz.ProViz;

public class Sleep implements Motion {

	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		}
		catch (Exception e) {
			ProViz.errprintln(e);
		}
	}
	
	@Override
	public void noAnimationMotion() {//IPainter painter) {
		ProViz.getAnimationController().delay();
	}

	@Override
	public boolean stepMotion(int speedScale) {
		ProViz.getAnimationController().delay();
		return true;
	}
}
