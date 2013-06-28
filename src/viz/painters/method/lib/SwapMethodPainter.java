package viz.painters.method.lib;

import java.awt.Point;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.animation.motion.Direction;
import viz.animation.motion.Motion;
import viz.animation.motion.move.CurvePath;
import viz.animation.motion.move.Path;
import viz.painters.Painter;
import viz.painters.method.MethodPainter;
import viz.runtime.VizStackFrame;
import viz.views.VizCanvas;

public abstract class SwapMethodPainter extends MethodPainter {

	public SwapMethodPainter(VizStackFrame stackFrame, VizCanvas canvas) {
		super(stackFrame, canvas);
	}
	
	public abstract Painter getPainter1();
	
	public abstract Painter getPainter2();
	
	/**
	 * If true, each painter will be swapped with 180 degrees of animation.
	 * Otherwise, the swap will be 540 degrees, one-and-a-half-circle path.
	 * @return true by default, can be changed in subclasses
	 */
	public boolean simpleSwap() {
		return true;
	}

	@Override
	public void methodInvoked() {
		AnimationController ac = ProViz.getAnimationController();
		Painter iPainter = getPainter1();
		Painter jPainter = getPainter2();
		
		Point destI = new Point(jPainter.getLocation().x, iPainter.getLocation().y);
		Point destJ = new Point(iPainter.getLocation().x, jPainter.getLocation().y);
		
		Path iPath = new CurvePath(iPainter, iPainter.getLocation(), destI, Direction.UP);
		Path jPath = new CurvePath(jPainter, jPainter.getLocation(), destJ, Direction.DOWN);
		ac.scheduleAnimation(iPainter, new Motion[] {iPath}, null);
		ac.scheduleAnimation(jPainter, new Motion[] {jPath}, null);
		if (simpleSwap()) {
			Path iPath2 = new CurvePath(iPainter, destI, iPainter.getLocation(), Direction.DOWN);
			Path jPath2 = new CurvePath(jPainter, destJ, jPainter.getLocation(), Direction.UP);
			ac.scheduleAnimation(iPainter, new Motion[] {iPath2}, null);
			ac.scheduleAnimation(jPainter, new Motion[] {jPath2}, null);
			
			Path iPath3 = new CurvePath(iPainter, iPainter.getLocation(), destI, Direction.UP);
			Path jPath3 = new CurvePath(jPainter, jPainter.getLocation(), destJ, Direction.DOWN);
			ac.scheduleAnimation(iPainter, new Motion[] {iPath3}, null);
			ac.scheduleAnimation(jPainter, new Motion[] {jPath3}, null);//new Painter[] {arrayPainter});
		}
	}

}
