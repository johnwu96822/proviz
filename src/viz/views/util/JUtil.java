package viz.views.util;

import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JComponent;

public class JUtil {
	public static Dimension getStringBounds(JComponent component, String text, int numOfLines) {
		Dimension dim = new Dimension();
		FontMetrics metrics = component.getFontMetrics(component.getFont());
		dim.setSize(metrics.stringWidth(text), metrics.getHeight() * numOfLines);
		return dim;
	}
	
	public static Dimension getBorderedStringBounds(JComponent component, String text, int marginX, int marginY, int numOfLines) {
		Dimension dim = getStringBounds(component, text, numOfLines);
		dim.setSize(dim.width + marginX * 2, dim.height + marginY * 2);
		return dim;
	}
	
}
