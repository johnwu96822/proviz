package viz.painters.method.lib;

import viz.painters.method.MethodPainter;

import java.awt.Point;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.animation.motion.move.Path;
import viz.animation.motion.move.CurvePath;
import viz.animation.motion.Direction;
import viz.animation.motion.Motion;
import viz.painters.Painter;
import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;
import viz.views.VizCanvas;

public abstract class RotatingSwapMethodPainter extends MethodPainter {

	public RotatingSwapMethodPainter(VizStackFrame stackFrame, VizCanvas canvas) {
		super(stackFrame, canvas);
	}

	/**
	 * Gets the name of the local variable that references the array. If the array variable
	 * is not a local variable in the same stack frame as this painter, this method should
	 * return null and getArrayPainter() should be implemented instead.
	 * @return the name of the local variable that references the array
	 */
	public abstract String getArray();
	/**
	 * This method will be used to retrieve the array painter when getArray() returns null.
	 * Therefore if getArray() returns a variable name, this method will not be used.
	 * @return
	 */
	public abstract Painter getArrayPainter();
	/**
	 * Gets the name of the first index variable, which should be a parameter variable
	 * @return
	 */
	public abstract String getIndex1();
	
	/**
	 * Gets the name of the other index variable, which should be a parameter variable
	 * @return
	 */
	public abstract String getIndex2();
	
	@Override
	public void methodInvoked() {
		Painter arrayPainter;
		if (getArray() != null) {
			IVizVariable array = this.getStackFrame().getVariable(getArray());
			arrayPainter = ProViz.getVPM().getPainter(array);
		}
		else {
			arrayPainter = getArrayPainter();
		}
		if (arrayPainter != null) {
			int i = Integer.parseInt(this.getStackFrame().getVariable(getIndex1()).getValueAsString());
			int j = Integer.parseInt(this.getStackFrame().getVariable(getIndex2()).getValueAsString());
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
				ac.scheduleAnimation(jPainter, new Motion[] {jPath3}, null);//new Painter[] {arrayPainter});
			}
		}
	}
}
