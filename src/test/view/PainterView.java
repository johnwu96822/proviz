package test.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import viz.ProViz;

public class PainterView extends ViewPart {
	private Action showAction;
	protected TreeViewer treeViewer;
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new PainterContentProvider());
		treeViewer.setLabelProvider(new PainterLabelProvider());
		
		showAction = new Action() {
			public void run() {
				treeViewer.setInput(ProViz.getVPM());
				treeViewer.expandAll();
			}
		};
		showAction.setText("Load Viz");
		showAction.setToolTipText("Load Viz annotations in the active editor");
		showAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(showAction);
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}