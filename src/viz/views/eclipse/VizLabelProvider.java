package viz.views.eclipse;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.ParamViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.Visualization;

/**
 * Provides labels and icons for different types of tree node objects.
 * @author John
 * Created on April 15, 2006
 */
class VizLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object obj) {
		if (obj instanceof TypeViz) {
			return ((TypeViz) obj).getFullName();
		}
		else if (obj instanceof VariableViz) {// VariableVizBase) {
			/*VariableVizBase variable = (VariableVizBase) obj;
			String type = variable.getType();
			int index = type.lastIndexOf('.');
			if (index != -1) {
				type = type.substring(index + 1, type.length());
			}
			return type + " " + variable.getFullName();*/
			return ((VariableViz) obj).getType() + " " + ((VariableViz) obj).getFullName();
		}
		else if (obj instanceof Association) {
			return ((Association) obj).getSimpleName();
		}
		else if (obj instanceof Visualization) {
			Visualization viz = (Visualization) obj;
			if (viz.getStartingLocation() != null) {
				return viz.toString() + " (" + viz.getStartingLocation().x + ", " + viz.getStartingLocation().y + ")";
			}
			else {
				return viz.toString();
			}
		}
		return obj.toString();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_FILE;
		
		if (obj instanceof VariableViz) {
			if (obj instanceof ParamViz) {
				imageKey = ISharedImages.IMG_OBJS_INFO_TSK;
			}
			else {
				imageKey = ISharedImages.IMG_TOOL_CUT;
			}
		}
		else if (obj instanceof FieldViz) {
			imageKey = ISharedImages.IMG_TOOL_NEW_WIZARD;
		}
		else if (obj instanceof MethodViz) {
			imageKey = ISharedImages.IMG_TOOL_FORWARD;
		}
		else if (obj instanceof Visualization) {
			Visualization vac = (Visualization) obj;
			if (vac.getOrder() == Visualization.DEFAULT) {
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			}
		}
		else if (obj instanceof TypeViz) {
			TypeViz tv = (TypeViz) obj;
			if (tv.isSystemType()) {
				imageKey = ISharedImages.IMG_TOOL_UP;
			}
			else {
			  imageKey = ISharedImages.IMG_OBJ_FOLDER;
			}
			/*if (tv.isForActualTypes()) {
				imageKey = ISharedImages.IMG_TOOL_COPY;
			}*/
		}
		else if (obj instanceof Association) {
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}