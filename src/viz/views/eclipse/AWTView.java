package viz.views.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;

import viz.ProViz;
import viz.VizMonitor;

/** This sample class demonstrates how to plug-in a new workbench view. The view
  * shows data obtained from the model. The sample creates a dummy model on the
  * fly, but a real implementation would connect to the model available either in
  * this or another plug-in (e.g. the workspace). The view is connected to the
  * model using a content provider.
  * <p>
  * The view uses a label provider to define how model objects should be
  * presented in the view. Each view can present the same model objects using
  * different labels and icons, if needed. Alternatively, a single label provider
  * can be shared between views in order to ensure that objects of the same type
  * are presented in the same way everywhere.
  */
public class AWTView extends ViewPart {



	private Composite swingContainer;
	
  /** Constructs the view. */
  public AWTView () {
  	DebugPlugin.getDefault().addDebugEventListener(VizMonitor.getInstance());
  }
  
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(VizMonitor.getInstance());
	}
	

  /** Callback that creates and initialize the viewer. */
  public void createPartControl(org.eclipse.swt.widgets.Composite swtParent) {
    swingContainer = new Composite(swtParent, SWT.EMBEDDED);
    ProViz.getInstance().constructFrame(SWT_AWT.new_Frame(swingContainer));
    
    //JFrame jFrame = new JFrame();
    //jFrame.setSize(500, 500);
    
    /*VizCanvas canvas = VizCanvas.getInstance();
    canvas.setPreferredSize(new Dimension(1000, 1000));
	  JScrollPane workspacePane = new JScrollPane(canvas);
	  workspacePane.getViewport().setDoubleBuffered(true);
	  frame.getFrame().add(workspacePane, BorderLayout.CENTER);
	  */
    //jFrame.getContentPane().add(workspacePane, BorderLayout.CENTER);
    //jFrame.setVisible(true);
	  
		//getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
  	makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
  }
  
  /**
   * Gets only the TOP debug target in session.
   * @return
   */
  public IDebugTarget getDebugTarget () {
    DebugPlugin debugPlugin = DebugPlugin.getDefault();
    ILaunchManager launchManager = debugPlugin.getLaunchManager();
    IDebugTarget targets [] = launchManager.getDebugTargets();
    return targets.length > 0 ? targets [0] : null;
  }
  
  private void hookContextMenu () {
    MenuManager menuManager = new MenuManager ("#PopupMenu");
    menuManager.setRemoveAllWhenShown (true);
    menuManager.addMenuListener (new IMenuListener () {
      public void menuAboutToShow (IMenuManager manager) {
        AWTView.this.fillContextMenu (manager);
      }
    });
  }

  private void contributeToActionBars () {
    IActionBars bars = getViewSite ().getActionBars ();
    fillLocalPullDown (bars.getMenuManager ());
    fillLocalToolBar (bars.getToolBarManager ());
  }
  
  private void fillLocalPullDown (IMenuManager manager) {
  }

  private void fillContextMenu (IMenuManager manager) {
  }

  private void fillLocalToolBar (IToolBarManager manager) {
  	//manager.add(testAction);
  	//manager.add(stepIntoAction);
  	//manager.add(stepReturnAction);
  }

  private void makeActions () {

  }
  
  private void hookDoubleClickAction () {

  }

  /** Passes the focus request to the viewer's control. */
  public void setFocus () {
  }

	protected void setViewerInput(IStructuredSelection ssel) {

	}
}