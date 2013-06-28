package viz.views.util;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import viz.ProViz;
import viz.model.Association;
import viz.model.Visualization;

/**
 * Handles the dragging event in VizAnnotationExplorer. Only single VizTreeNode
 * selection would allow dragging to occur, other program elements cannot be
 * @author John Wu
 * Created on Apr 26, 2006
 */
public class VizDropAdapter extends ViewerDropAdapter {
	/**
	 * @param viewer
	 */
	public VizDropAdapter(TreeViewer viewer) {
		super(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
		/*
		 * if (event.detail != DND.DROP_MOVE) { System.out.println("NOT drop
		 * default"); return; } System.out.println("Drag enter");
		 */
		// will accept text but prefer to have files dropped
		/*
		 * for (int i = 0; i < event.dataTypes.length; i++) { if
		 * (LocalSelectionTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
		 * event.currentDataType = event.dataTypes[i]; // files should only be
		 * copied if (event.detail != DND.DROP_COPY) { event.detail = DND.DROP_NONE; }
		 * break; } }
		 */
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOver(DropTargetEvent event) {
/*
 * //event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL; if
 * (LocalSelectionTransfer.getInstance().isSupportedType(event.currentDataType)) { //
 * NOTE: on unsupported platforms this will return null Object o =
 * LocalSelectionTransfer.getInstance().nativeToJava(event.currentDataType);
 * String t = (String) o; if (t != null) System.out.println(t); }
 */
		if (event.item == null) { 
			return;
		}
		try {
//		if (LocalSelectionTransfer.getInstance().isSupportedType(
//				event.currentDataType)) {
			if (event.item.getData() instanceof Visualization) {
				Visualization target = (Visualization) event.item.getData();
				Visualization source = getFirstSelectedNode(LocalSelectionTransfer.getInstance().getSelection());
				if (target != null && source != null) {
					if (source.getParent() == target.getParent()) {
						if (source == target) {
							event.feedback = DND.FEEDBACK_SCROLL;
							event.detail = DND.DROP_NONE;
						}
						else {
							event.feedback = DND.FEEDBACK_SELECT;
							event.detail = DND.DROP_MOVE;
						}
					}
					else {
						event.feedback = DND.FEEDBACK_SCROLL;
						event.detail = DND.DROP_NONE;
					} //end else
				} //end if
			} //end if
			else {
				event.feedback = DND.FEEDBACK_SCROLL;
				event.detail = DND.DROP_NONE;
			} //end else
		}
		catch (Exception e) {
			ProViz.errprintln(e);
		}
//		} //end if
	} //end dragOver

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOperationChanged(DropTargetEvent event) { 
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragLeave(DropTargetEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dropAccept(DropTargetEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void drop(DropTargetEvent event) {
		if (event.item == null) {
			return;
		}
		if (LocalSelectionTransfer.getInstance().isSupportedType(
				event.currentDataType)) {
			if (event.item.getData() instanceof Visualization) {
				Visualization target = (Visualization) event.item.getData();
				Visualization source = getFirstSelectedNode(event.data);
				if (source != null || target != null) {
					if (target.getParent() == source.getParent()) {
						System.out.println(source + ":" + source.getOrder() + " ---> " + target + ":" + target.getOrder());
						ProViz.println(source + ":" + source.getOrder() + " ---> " + target + ":" + target.getOrder());
						Association parent = target.getParent();
						parent.swapPosition(target.getOrder(), source.getOrder());
						TreeViewer viewer = (TreeViewer) this.getViewer();
						viewer.refresh(parent);
					} //end if
				} //end if
			} //end if
		} //end if
	} //end drop

	/**
	 * Gets the first selection in the selected nodes.
	 * @param selectionData
	 * @return
	 */
	private Visualization getFirstSelectedNode(Object selectionData) {
		Visualization rv = null;
		if (selectionData instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) selectionData;
			rv = (Visualization) selection.getFirstElement();
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(Object data) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return false;
	}
}
