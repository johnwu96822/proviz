package viz.painters.java.list;

import java.awt.Point;

import viz.ProViz;
import viz.animation.motion.move.LinearPath;
import viz.animation.motion.Motion;
import viz.painters.Painter;
import viz.painters.method.MethodPainter;
import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;
import viz.views.VizCanvas;

public class PushMethodPainter extends MethodPainter {
	public PushMethodPainter(VizStackFrame stackFrame, VizCanvas canvas) {
		super(stackFrame, canvas);
	}

	@Override
	public void methodInvoked() {
		IVizVariable arrayVar = this.getInstanceVariable("elementData");
		Painter arrayPainter = ProViz.getVPM().getPainter(arrayVar);
		IVizVariable elementCount = this.getInstanceVariable("elementCount");
		Painter param = ProViz.getVPM().getPainter(this.getStackFrame().getVariable("arg0"));
		if (arrayPainter != null && elementCount != null && param != null) {
			int count = Integer.parseInt(elementCount.getValueAsString());
			Point orig = arrayPainter.getParent().getLocation();
			Point dest = new Point(orig);
			if (count > 0) {
				Painter topPainter = arrayPainter.getFieldPainter("[" + (count - 1) + "]");
				dest.y = topPainter.getLocation().y;
			}
			dest.y -= param.getHeight();
			param.setLocation(orig.x, orig.y - arrayVar.getFields().size() * 25 - 30);
			param.setSize(arrayPainter.getParent().getWidth(), param.getHeight());
			LinearPath path = new LinearPath(param, param.getLocation(), dest, null);
			ProViz.getAnimationController().animateNow(false, path);
			//ProViz.getAnimationController().scheduleAnimation(this, new Motion[] {path}, null);
		}
	}

	@Override
	public void methodReturned() {
	}
}
