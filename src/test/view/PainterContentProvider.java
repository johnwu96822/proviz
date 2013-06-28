package test.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import viz.painters.*;
import viz.runtime.IVizVariable;

public class PainterContentProvider implements ITreeContentProvider {

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Painter) {
			ArrayList aList = new ArrayList();
			Painter painter = (Painter) parentElement;
			if (painter.hasFieldPainter()) {
				for (Painter field : painter.getFieldPainters().values()) {
					aList.add(field);
				}
			}
			StringBuffer sb = new StringBuffer("Listening to: ");
			for (IVizVariable var : painter.getEventGenerators()) {
				sb.append(var.getName() + ":" + var.getUniqueObjectID() + ", ");
			}
			aList.add(sb.toString());
			return aList.toArray();
		}
		/*if (parentElement instanceof VizPainterManager) {
			ArrayList aList = new ArrayList();
			VizPainterManager vpm = (VizPainterManager) parentElement;
			for (Painter painter : vpm.getPrimitivePainters()) {
				aList.add(painter);
			}
			for (Entry<String, List<Painter>> entry : vpm.getUniqueObjectTable().entrySet()) {
				aList.add("Painter(s) for object: " + entry.getKey() + " --");
				for (Painter painter : entry.getValue()) {
					aList.add(painter);
				}
			}
			return aList.toArray();
		}*/
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		/*if (element instanceof Painter) {
			return ((Painter) element).hasFieldPainter();
		}*/
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof VizPainterManager) {
			ArrayList aList = new ArrayList();
			VizPainterManager vpm = (VizPainterManager) inputElement;
			for (Painter painter : vpm.getPrimitivePainters()) {
				if (painter.getParent() == null) {
					aList.add(painter);
				}
			}
			for (Entry<String, List<Painter>> entry : vpm.getUniqueObjectTable().entrySet()) {
				//aList.add("Painter(s) for object: " + entry.getKey() + " --");
				for (Painter painter : entry.getValue()) {
					if (painter.getParent() == null) {
						aList.add(painter);
					}
				}
			}
			return aList.toArray();
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
