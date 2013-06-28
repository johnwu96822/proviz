package test.view;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import viz.ProViz;
import viz.runtime.VizRuntime;
import viz.runtime.VizStackFrame;
import viz.runtime.IVizVariable;

public class VizRuntimeViewContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IVizVariable) {
			return ((IVizVariable) parentElement).getFields().toArray();
		}
		else if (parentElement instanceof VizStackFrame) {
			return ((VizStackFrame) parentElement).getVariables().toArray();
		}
		else if (parentElement instanceof VizRuntime) {
			return ((VizRuntime) parentElement).getStackFrames().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IVizVariable) {
			IVizVariable var = (IVizVariable) element;
			if (var.getParent() == null) {
				return var.getStackFrame();
			}
			return ((IVizVariable) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IVizVariable) {
			return !((IVizVariable) element).getFields().isEmpty();
		}
		else if (element instanceof VizStackFrame) {
			return !((VizStackFrame) element).getVariables().isEmpty();
		}
		else if (element instanceof VizRuntime) {
			return !((VizRuntime) element).getStackFrames().isEmpty();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement.equals(ProViz.getInstance().getVizRuntime())) {
			return ((VizRuntime) inputElement).getStackFrames().toArray();
		}
		return null;
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
