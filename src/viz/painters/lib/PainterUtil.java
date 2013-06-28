package viz.painters.lib;

import java.awt.Point;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.animation.motion.Direction;
import viz.animation.motion.Motion;
import viz.animation.motion.move.CurvePath;
import viz.animation.motion.move.Path;
import viz.painters.Painter;

public class PainterUtil {

	public static void animateSwapPainters(Painter iPainter, Painter jPainter, Painter[] finishUp) {
		AnimationController ac = ProViz.getAnimationController();
		
		Point destI = new Point(jPainter.getLocation().x, jPainter.getLocation().y);
		Point destJ = new Point(iPainter.getLocation().x, iPainter.getLocation().y);
		
		Path iPath = new CurvePath(iPainter, iPainter.getLocation(), destI, Direction.UP);
		Path jPath = new CurvePath(jPainter, jPainter.getLocation(), destJ, Direction.DOWN);
		ac.scheduleAnimation(iPainter, new Motion[] {iPath}, null);
		ac.scheduleAnimation(jPainter, new Motion[] {jPath}, null);

		Path iPath2 = new CurvePath(iPainter, destI, iPainter.getLocation(), Direction.DOWN);
		Path jPath2 = new CurvePath(jPainter, destJ, jPainter.getLocation(), Direction.UP);
		ac.scheduleAnimation(iPainter, new Motion[] {iPath2}, null);
		ac.scheduleAnimation(jPainter, new Motion[] {jPath2}, null);
		
		Path iPath3 = new CurvePath(iPainter, iPainter.getLocation(), destI, Direction.UP);
		Path jPath3 = new CurvePath(jPainter, jPainter.getLocation(), destJ, Direction.DOWN);
		ac.scheduleAnimation(iPainter, new Motion[] {iPath3}, null);
		ac.scheduleAnimation(jPainter, new Motion[] {jPath3}, finishUp);
	}
}
