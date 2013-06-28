package viz.painters.lib;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public abstract class ImagePainter extends Painter {
	protected JLabel label;
	protected ImageIcon image;

	private boolean isAdded = false;
	public ImagePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		image = new ImageIcon(getImageFilePath());
		label = new JLabel(image);
		//label.setBounds(0, 0, label.getPreferredSize().width, label.getPreferredSize().height);
		label.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
	}

	/**
	 * Gets the file location of the image file.
	 * @return
	 */
	public abstract String getImageFilePath();

	@Override
	protected void addToCanvas_userImp() {
		if (!isAdded) {
			if (!getVariable().isNull()) {
				getCanvas().add(label);
				isAdded = !isAdded;
			}
		}
	}

	/**
	 * @return the image
	 */
	public ImageIcon getImage() {
		return image;
	}
	
	/**
	 * Scales the image to the given size.
	 * @param width The width to scale the image to
	 * @param height The height to scale the image to
	 */
	public void scale(int width, int height) {
		image = new ImageIcon(image.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		label.setIcon(image);
		//label.setSize(label.getPreferredSize().width, label.getPreferredSize().height);
		label.setSize(image.getIconWidth(), image.getIconHeight());
	}

	@Override
	protected void destroy_userImp() {
		isAdded = false;
	}

	@Override
	public JComponent getComponent() {
		return label;
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		//addToCanvas();
	}

	@Override
	protected void paint_userImp() {
	}
}
