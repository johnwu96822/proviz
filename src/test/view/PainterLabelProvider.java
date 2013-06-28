package test.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import viz.painters.Painter;
import viz.runtime.IVizVariable;

public class PainterLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof Painter) {
			Painter painter = (Painter) element;
			StringBuffer sb = new StringBuffer(painter.getClass().getName() + ": ");
			for (IVizVariable var : painter.getVariablesToThisPainter()) {
				sb.append(var.getName());
				sb.append(", ");
			}
			sb.append("(" + painter.getUniqueID() + " | " + painter.getPreviousValue());
			sb.append(")");
			return sb.toString();
		}
		return element.toString();
	}
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		if (obj instanceof String) {
			imageKey = null;//ISharedImages.IMG_OBJ_FILE;
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
