package viz.views.eclipse;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;
import viz.model.VizMapModel;

/**
 * @author John
 * Created on Nov 2, 2006
 */
public class VizSetEditorContentProvider implements ITreeContentProvider {
	protected AbstractVizExplorer explorer;

	/**
	 * @param explorer The VizAnnotationExplorer that this provider is for.
	 */
	VizSetEditorContentProvider(AbstractVizExplorer explorer) {
		this.explorer = explorer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent.equals(VizMapModel.getInstance())) {
			Object[] types = VizMapModel.getInstance().getTypeList();
			return types;
		}
		return getChildren(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		Object rv = null;
		if (child instanceof Association) {
			rv = ((Association) child).getParent();
		}
		if (child instanceof Visualization) {
			rv = ((Visualization) child).getParent();
		}
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof TypeViz) {
			return this.generateTypeVizChildren((TypeViz) parent);
		}
		else if (parent instanceof FieldViz || parent instanceof VariableVizBase) {
			return this.generateActualTypeChildren((VariableVizBase) parent);
		}
		else if (parent instanceof MethodViz) {
			return this.generateMethodVizChildren((MethodViz) parent);
		}
		return new Object[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object parent) {
		if (parent instanceof TypeViz) {
			return true;
		}
		else if (parent instanceof VariableVizBase) {
			VariableVizBase base = (VariableVizBase) parent;
			return base.getDefaultViz() != null || base.getFieldVizes() != null;
		}
		else if (parent instanceof MethodViz) {
			return true;
		}
		return false;
	}
	
	/**
	 * Takes a TypeViz object, takes its direct children, and returns them in an array.
	 * @param tv A TypeViz.
	 * @return An array of the TypeViz's direct children.
	 */
	private Object[] generateTypeVizChildren(TypeViz tv) {
		ArrayList<Object> children = new ArrayList<Object>();
		this.fillVizClasses(tv, children);
		if (tv.getFieldVizes() != null) {
			for (FieldViz fv : tv.getFieldVizes()) {
				children.add(fv);
			}
		}
		if (tv.getMethodVizes() != null) {
			for (MethodViz mv : tv.getMethodVizes()) {
				children.add(mv);
			}
		}
		if (tv.getInnerTypeVizes() != null) {
			for (TypeViz innerType : tv.getInnerTypeVizes()) {
				children.add(innerType);
			}
		}
		return children.toArray();
	}
	
	/**
	 * Generates children for a viz object with actual type (VariableViz and FieldViz).
	 * It first fills the normal VCs of viz object, and then fills the actual type VCs.
	 * Because actual types are stored as TypeViz, they will be recognized and displayed
	 * without any further work.
	 * @param variable Viz with actual types, which can be VariableViz or FieldViz.
	 * @return An array of the VariableViz' direct children.
	 */
	private Object[] generateActualTypeChildren(VariableVizBase variable) {
		ArrayList<Object> children = new ArrayList<Object>();
  //First fills in normal visualizations
		this.fillVizClasses(variable, children);
		Collection<FieldViz> fvs = variable.getFieldVizes();
		if (fvs != null) {
			children.addAll(fvs);
		}
  //Then fills in actual type visualizations
		if (variable.getActualTypes() != null) {
			children.addAll(variable.getActualTypes());
		}
		return children.toArray();
	}
	
	/**
	 * Takes a MethodViz object, takes its direct children, and returns them in an array.
	 * @param mv A MethodViz
	 * @return An array of the MethodViz's direct chidlren.
	 */
	private Object[] generateMethodVizChildren(MethodViz mv) {
		ArrayList<Object> children = new ArrayList<Object>();
		this.fillVizClasses(mv, children);
		if (mv.getVariableVizes() != null) {
			for (VariableViz vv : mv.getVariableVizes()) {
				children.add(vv);
			}
		}
		if (mv.getInnerTypeVizes() != null) {
			for (TypeViz innerType : mv.getInnerTypeVizes()) {
				children.add(innerType);
			}
		}
		return children.toArray();
	}
	
	/**
	 * Adds the VCs of any Association object into an ArrayList. New VizTreeNode
	 * objects are created to wrap around each VC everytime this method is called.
	 * @param viz The viz type with VCs to be extracted and wrapped by VizTreeNode.
	 * @param children The ArrayList that generated VizTreeNode's will be added to.
	 */
	private void fillVizClasses(Association viz, ArrayList<Object> children) {
//			boolean isFirst = true;
		//int count = 0;
		if (viz.getVizCount() != 0) {
			for (Visualization vClass : viz.getVisualizations()) {
				//children.add(count + ": " + vClass.getName());
				//VizTreeNode vac = new VizTreeNode(vClass, viz, count);
				children.add(vClass);
				//count++;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
