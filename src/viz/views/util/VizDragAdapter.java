package viz.views.util;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import viz.model.Visualization;

/**
 * Handles the dragging event in VizAnnotationExplorer. Only single VizTreeNode
 * selection would allow dragging to occur, other program elements cannot be 
 * dragged or dropped.
 * @author John Wu
 * Created on Apr 25, 2006
 */
public class VizDragAdapter implements DragSourceListener {
	private TreeViewer treeViewer;
	
	/**
	 * Constructor that sets the TreeViewer where selection and dragging occur.
	 * @param selectionProvider
	 */
	public VizDragAdapter(TreeViewer treeViewer) {
		super();
		this.treeViewer = treeViewer;
	}
	
	/* Only single VizTreeNode selection would allow dragging to occur, other 
	 * program elements cannot be dragged or dropped.
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		//DragSource dragSource = (DragSource) event.getSource();
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
	//Single selection of annotation only
		if (selection.size() > 1 || !(selection.getFirstElement() instanceof Visualization)) {
			event.doit = false;
			return;
		}
		event.doit = true;
		LocalSelectionTransfer.getInstance().setSelection(selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {
		if (LocalSelectionTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = LocalSelectionTransfer.getInstance().getSelection();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {
		if (event.detail == DND.DROP_MOVE) {
			//System.out.println("drag move finish");
		}
		else {
			//System.out.println("drag not finished");
		}
	}
}
