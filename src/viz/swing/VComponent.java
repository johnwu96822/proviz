package viz.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.JLabel;


/**
 * Still under experiment with the transformations
 * @author JW
 *
 */
public class VComponent extends VLabel {
	private double theta, anchorX, anchorY;
	boolean isRotate = false;
	
	public VComponent() {}
	
	public VComponent(String text) {
		this.setText(text);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//AffineTransform saveAT = g2.getTransform();
		/*if (isRotate) {
			System.out.println("Rotating");
			int x = this.getLocation().x + this.getWidth() / 2;
			int y = this.getLocation().y + this.getHeight() / 2;
			g2.rotate(0.3, 0, 0);//, y);
			isRotate = false;
		}*/
		super.paint(g);
		//g2.setTransform(saveAT);
	}

	public void rotate(double theta, double anchorX, double anchorY) {
		this.theta = theta;
		this.anchorX = anchorX;
		this.anchorY = anchorY;
		this.isRotate = true;
		repaint();
	}
	
}
