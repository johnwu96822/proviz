package viz.views.util;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TreeItem;

import viz.model.Association;
import viz.model.Visualization;
import viz.views.eclipse.AbstractVizExplorer;

/**
 * A cell modifier to be used in a VizAnnotationExplorer.
 * @author John
 * Created on Apr 29, 2006
 */
public class VizCellModifier implements ICellModifier {
	private AbstractVizExplorer owner;
	
	/**
	 * Constructor that sets the owner, which should be a VizAnnotationExplorer.
	 * @param owner A VizAnnotationExplorer.
	 */
	public VizCellModifier(AbstractVizExplorer owner) {
		this.owner = owner;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		//if (!"Variety".equals(property))
		//	return false;
		return element instanceof Visualization && owner.isEditable();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {
		if (element instanceof Association) {
			return ((Association) element).getFullName();
		}
		if (element instanceof Visualization) {
			return ((Visualization) element).getPainterName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {
		Object obj = ((TreeItem) element).getData();
		if (obj instanceof Visualization) {
			//Association parent = ((Visualization) obj).getParent();
			String painterName = value.toString();
			Visualization vis = (Visualization) obj;
			Association parent = vis.getParent();
			//parent.remove(vis);
			vis.setVisualization(painterName);
			//parent.addVisualization(vis);
			owner.getTreeViewer().refresh(parent);//.update(vis, null);
			//owner.updateAll();
		}
		owner.setEditable(false);
	}
}
