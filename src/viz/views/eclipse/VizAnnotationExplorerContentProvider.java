package viz.views.eclipse;

import viz.model.TypeViz;

/**
 * Tree content provider for the viz annotation explorer. It maps the viz model
 * to the tree structure. However, the viz classes are wrapped with
 * dynamically generated VizTreeNode, which is generated everytime getChildren
 * method is called, so it is not really consistent with viz model, and we
 * have difficulty accessing viz children because those wrapper
 * classes are dynamically generated. For example, I am trying to select a VC
 * node after it is dragged and released. I have the previous VizTreeNode 
 * object from the mouse dragging and dropping, but after drag and drop, update
 * is called on parent of that node, which re-generate a new set of VizTreeNode
 * for all its VCs, so I cannot set the selectiong to the previous VizTreeNode
 * that I got a hold of. 
 * @author John Wu
 * Created on Apr 17, 2006
 */
public class VizAnnotationExplorerContentProvider extends VizSetEditorContentProvider {
	/**
	 * @param explorer
	 */
	VizAnnotationExplorerContentProvider(AbstractVizExplorer explorer) {
		super(explorer);
	}

	/* Loads the top-level TypeVizes from the active editor.
	 * @see viz.views.VizExplorerContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object parent) {
		if (parent instanceof TypeViz[]) {
/*			IPath path = EclipseResourceUtil.getActiveEditorFilePath();
			if (path != null) {
				TypeViz[] list = Vizes.vizes().getFile(path.toOSString());
				if (list != null) {
					return list;
				}
			}
		}
		return getChildren(parent);*/
			return (TypeViz[]) parent;
		}
		return getChildren(parent);
	}
}