package viz.swing;

import javax.swing.JLabel;

import viz.painters.graphics.IMovable;

public class VLabel extends JLabel implements IMovable {
	public VLabel() {
		super();
	}
	
	public VLabel(String label) {
		super(label);
	}
}
