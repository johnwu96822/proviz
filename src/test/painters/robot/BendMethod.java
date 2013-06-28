package test.painters.robot;

import viz.ProViz;
import viz.animation.motion.Motion;
import viz.animation.rotate.StickRotation;
import viz.painters.Painter;
import viz.painters.VizPainterManager;
import viz.painters.method.MethodPainter;
import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;
import viz.views.VizCanvas;

public class BendMethod extends MethodPainter {

	public BendMethod(VizStackFrame stackFrame, VizCanvas canvas) {
		super(stackFrame, canvas);
	}

	@Override
	public void methodInvoked() {
		IVizVariable thisVar = this.getStackFrame().getVariable("this");
		VizPainterManager vpm = ProViz.getVPM();
		Painter painter = vpm.getPainter(thisVar);
		if (painter != null && painter instanceof ArmPainter) {
			ArmPainter armPainter = (ArmPainter) painter;
			StickRotation rotation = new StickRotation(armPainter.getUpper(), 
					Integer.parseInt(this.getStackFrame().getVariable("degree").getValueAsString()));
			StickRotation rotation2 = new StickRotation(armPainter.getLower(), 
					Integer.parseInt(this.getStackFrame().getVariable("degree").getValueAsString()));
			//LinearPath path = new LinearPath(armPainter.getLocation(), new Point(armPainter.getLocation().x + 50, armPainter.getLocation().y - 20), Direction.UP);
			ProViz.getAnimationController().scheduleAnimation(armPainter, new Motion[] {rotation, rotation2}, null);
			ProViz.getAnimationController().scheduleAnimation(armPainter, new Motion[] {rotation2}, null);
		}
	}
	
	@Override
	public void paintMethod() {
		IVizVariable thisVar = this.getStackFrame().getVariable("this");
		VizPainterManager vpm = ProViz.getVPM();
		Painter painter = vpm.getPainter(thisVar);
		if (painter != null && painter instanceof ArmPainter) {
			ArmPainter armPainter = (ArmPainter) painter;
			armPainter.getLower().setAngle(Integer.parseInt(this.getStackFrame().getVariable("degree").getValueAsString()));
			painter.paint();
		}
	}
}
