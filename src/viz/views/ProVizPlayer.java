package viz.views;

import javax.swing.JFrame;

import viz.ProViz;

public class ProVizPlayer {
	public static void main(String[] args) {
		JFrame frame = new JFrame("ProViz Player");
		frame.setSize(1024, 768);
		ProViz.getInstance().constructFrame(frame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
