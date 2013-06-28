package test.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import viz.runtime.VizStackFrame;
import viz.runtime.IVizVariable;

public class VizRuntimeLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		String rv = null;
		if (element instanceof IVizVariable) {
			IVizVariable var = (IVizVariable) element;
			rv = var.getType() + "--" + var.getActualType() + " " + var.getName() + ": " + var.getValueAsString() + " | " + var.getUniqueObjectID();
		}
		else if (element instanceof VizStackFrame) {
			rv = ((VizStackFrame) element).getMethodID();
		}
		return rv;
	}
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		if (obj instanceof IVizVariable) {
			if (((IVizVariable) obj).getFields().isEmpty()) {
				imageKey = ISharedImages.IMG_OBJ_FILE;
			}
		}
		else if (obj instanceof VizStackFrame) {
			imageKey = ISharedImages.IMG_ETOOL_HOME_NAV;
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
