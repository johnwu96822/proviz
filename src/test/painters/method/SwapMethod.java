package test.painters.method;

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
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class SwapMethod extends MethodPainter {
	private Painter arrayPainter = null;
	public SwapMethod(VizStackFrame stackFrame, VizCanvas canvas) {
		super(stackFrame, canvas);
	}

	@Override
	public void methodInvoked() {
		IVizVariable array = this.getStackFrame().getVariable("this").getField("array");
		arrayPainter = ProViz.getVPM().getPainter(array);
		if (arrayPainter != null) {
			//if (arrayPainter instanceof ArrayPainter) {
				//ArrayPainter aPainter = (ArrayPainter) arrayPainter;
				int i = Integer.parseInt(this.getStackFrame().getVariable("i").getValueAsString());
				int j = Integer.parseInt(this.getStackFrame().getVariable("j").getValueAsString());
				if (i != j) {
					AnimationController ac = ProViz.getAnimationController();
					Painter iPainter = arrayPainter.getFieldPainter("[" + i + ']');
					Painter jPainter = arrayPainter.getFieldPainter("[" + j + ']');
					
					Point destI = new Point(jPainter.getLocation().x, iPainter.getLocation().y);
					Point destJ = new Point(iPainter.getLocation().x, jPainter.getLocation().y);
					
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
					ac.scheduleAnimation(jPainter, new Motion[] {jPath3}, null);
					
					//AnimationController.getInstance().schedulePaint(arrayPainter);
					//AnimationController.getInstance().schedulePaint(jPainter, arrayPainter);
				}
			//}
		}
	}
}
