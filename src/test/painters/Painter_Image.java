/**
 * 
 */
package test.painters;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * A basic painter that allows the use of supported image types as the object
 * (instead of drawing it, use an image).
 * 
 * @author dkamigaki
 *
 */
public class Painter_Image extends Painter {
	
	private JLabel label;
	private Image originalImage;  // holds the original unscaled image.
	private String imagePath = "c://Robot/default.jpg";

	/**
	 * @param vvar
	 * @param canvas
	 */
	public Painter_Image(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		
		// first things first.  This needs to created here.
		label = new JLabel();
		// needed for display.
		label.setBounds(0, 0, 100, 100);

		// Create the ImageIcon and perform operation on the image.
		ImageIcon tempIcon = new ImageIcon(imagePath);	
		
		// Now extract the Image from it so it can be scaled to fit this painter.
		originalImage = tempIcon.getImage();
		Image scaledImage = originalImage.getScaledInstance(this.getWidth(), this.getHeight(), java.awt.Image.SCALE_SMOOTH);
		// create the newImageIcon with the appropriately scaled image/
		ImageIcon scaledIcon = new ImageIcon(scaledImage);		
		label.setIcon(scaledIcon);
		label.repaint();
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		// Just add the label to the canvas.
		this.getCanvas().add(label);
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#destroy_userImp()
	 */
	@Override
	protected void destroy_userImp() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return label;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.VizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		// nothing happens here since image is a static thing ?  
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		label.repaint();
	}
	
	/*
	 * (non-Javadoc)
	 * @see viz.painters.Painter#setSize(int, int)
	 */
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		// Extract the Image from it so it can be scaled to fit this painter.
		Image scaledImage = originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
		// create the newImageIcon with the appropriately scaled image
		ImageIcon scaledIcon = new ImageIcon(scaledImage);		
		label.setIcon(scaledIcon);
		label.repaint();
	}
	
	/**
	 * Set the image path.
	 * @param path
	 */
	public void setImagePath(String path) {		
		this.imagePath = path;
		
		ImageIcon tempIcon = new ImageIcon(this.imagePath);
		this.originalImage = tempIcon.getImage();
		
		Image scaledImage = this.originalImage.getScaledInstance(this.getWidth(), this.getHeight(), java.awt.Image.SCALE_SMOOTH);
		// create the newImageIcon with the appropriately scaled image
		ImageIcon scaledIcon = new ImageIcon(scaledImage);		
		label.setIcon(scaledIcon);
		label.repaint();
	}
	
	/**
	 * Gets the image path for the painter.
	 * @return
	 */
	public String getImagePath() {
		return(imagePath);
	}
}
