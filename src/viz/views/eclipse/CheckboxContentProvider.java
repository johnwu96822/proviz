package viz.views.eclipse;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import viz.ProViz;
import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.Visualization;
import viz.model.VizMapModel;

public class CheckboxContentProvider implements IStructuredContentProvider {

	private Object[] currentElements = null;
	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		//TODO local variables inside static methods should not be able to listen to instance variables
		if (inputElement instanceof Visualization) {
			Association parent = ((Visualization) inputElement).getParent();
			TypeViz type = null;
			if (parent instanceof VariableViz) {
				MethodViz method = (MethodViz) parent.getParent();
				type = (TypeViz) method.getParent();
				Collection<FieldViz> fields = type.getFieldVizes();
				Collection<VariableViz> vars = method.getVariableVizes();
				ArrayList list = new ArrayList();
				for (FieldViz fv : fields) {
					list.add(fv);
				}
				for (VariableViz vv : vars) {
					if (vv != parent) {
						list.add(vv);
					}
				}
				this.fillSuperClassFields(list, type);
				this.currentElements = list.toArray();
				return this.currentElements;
			}
			else if (parent instanceof FieldViz) {
				type = (TypeViz) parent.getParent();
				Collection<FieldViz> fields = type.getFieldVizes();
				ArrayList list = new ArrayList();
				for (FieldViz fv : fields) {
					if (fv != parent) {
						list.add(fv);
					}
				}
				this.fillSuperClassFields(list, type);
				this.currentElements = list.toArray();
				return this.currentElements;
			}
		}
		/*if (inputElement instanceof VariableViz[]) {
			return (VariableViz[]) inputElement;
		}*/
		this.currentElements = null;
		return new Object[0];
	}
	
	@SuppressWarnings("unchecked")
	private void fillSuperClassFields(ArrayList list, TypeViz typeViz) {
		System.out.println("fillSuperClassFields");
		try {
			Class current = Class.forName(typeViz.getFullName());
			Class superClass = current.getSuperclass();
			System.out.println("Checking super class: " + superClass.getName());
			if (superClass.getName().equals("java.lang.Object")) {
				return;
			}
			TypeViz type = VizMapModel.getInstance().findTypeViz_runtime(superClass.getName());
			if (type != null) {
				for (FieldViz fv : type.getFieldVizes()) {
					list.add(fv);
				}
				fillSuperClassFields(list, type);
			}
		} catch (ClassNotFoundException e) {
			ProViz.errprintln(e);
		}
	}

	/**
	 * @return the currentElements
	 */
	public Object[] getCurrentElements() {
		return currentElements;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}

}
