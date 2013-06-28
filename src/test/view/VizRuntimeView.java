package test.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import viz.ProViz;

public class VizRuntimeView extends ViewPart {
	private Action showVizAction;
	protected TreeViewer treeViewer;
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new VizRuntimeViewContentProvider());
		treeViewer.setLabelProvider(new VizRuntimeLabelProvider());
		
		showVizAction = new Action() {
			public void run() {
				treeViewer.setInput(ProViz.getInstance().getVizRuntime());
				treeViewer.expandToLevel(2);
			}
		};
		showVizAction.setText("Load Viz");
		showVizAction.setToolTipText("Load Viz annotations in the active editor");
		showVizAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(showVizAction);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
