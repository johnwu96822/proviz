package viz.painters.lib;

import java.awt.Color;

import javax.swing.SwingConstants;

import viz.ProViz;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class MultiLineStringPainter extends StringPainter {

	public MultiLineStringPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		fm = label.getFontMetrics(label.getFont());
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setLocation(0, 0);
		String value = "<html>" + getVariable().getValueAsString().replaceAll("\n", "<br/>") + "</html>";
		ProViz.println(value);
		//label.setSize(fm.stringWidth(value) + 20, fm.getHeight() + 6);
		label.setText(value);
		this.setDefaultBorderColor(Color.black);
	}

	@Override
	protected void addToCanvas_userImp() {
	//Prevents multiple adding to the canvas (Swing does not permit)
		if (!isAdded) {
			label.setLocation(0, 0);
			label.setText("<html><p align=\"center\">" + getVariable().getValueAsString().replaceAll("\n", "<br/>") + "</p></html>");
			label.setSize(label.getPreferredSize());
			//ProViz.println("StringPainter addToCanvas()");
			getCanvas().add(label);
			isAdded = !isAdded;
		}
	}
	@Override
	protected void paint_userImp() {
		label.setText("<html><p align=\"center\">" + getVariable().getValueAsString().replaceAll("\n", "<br/>") + "</p></html>");
		label.setSize(label.getPreferredSize());
	}
}
