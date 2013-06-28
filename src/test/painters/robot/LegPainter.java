package test.painters.robot;

import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class LegPainter extends ArmPainter {

	public LegPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.getUpper().setLength(40);
		this.getUpper().setAngle(270);
		this.getLower().setLength(40);
		this.getLower().setAngle(270);
	}
}
