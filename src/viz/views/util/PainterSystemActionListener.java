package viz.views.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.VizPainterManager;

public class PainterSystemActionListener implements ActionListener {
	private Painter painter;
	public PainterSystemActionListener(Painter painter) {
		this.painter = painter;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Repaint")) {
			painter.getRootPainter().paint();
			painter.getCanvas().repaint();
		}
		else {
			VizPainterManager vpm = ProViz.getVPM();
			
		}
	}
}
