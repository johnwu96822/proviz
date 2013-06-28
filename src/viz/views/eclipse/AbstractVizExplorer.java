package viz.views.eclipse;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import viz.ProViz;
import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;
import viz.model.VizMapModel;
import viz.runtime.IVizVariable;
import viz.views.util.VizCellModifier;
import viz.views.util.VizDragAdapter;
import viz.views.util.VizDropAdapter;

/**
 * An abstract tree viewer that displays Viz structures and provides some basic
 * actions for manipulating Viz items in the viewer.
 * @author John
 * Created on Nov 30, 2006
 */
public abstract class AbstractVizExplorer extends ViewPart {
	protected TreeViewer treeViewer;
	protected CheckboxTableViewer checkboxViewer;
	protected DrillDownAdapter drillDownAdapter;
	protected Action showVizAction;
	protected Action clearAction;
	protected Action editAction;
	protected Action moveUpAction;
	protected Action moveDownAction;
	protected Action setDefaultAction;
	protected Action removeAction;
	protected Action expandAllAction;
	protected Action collapseAllAction;
	
	protected Action refreshAction;
	
	private Label statusLabel;
	private boolean isEditable = false;
	protected static final String XML_FOLDER = "viz_xml";
	private Association selected = null;
	private ViewForm checkBoxViewForm;
	private Button saveButton;
	//private SashForm sashForm;
	/* ***********************************************************************
                     Abstract methods
 *************************************************************************/
	/**
   * Handles selection-changed event.
   * @param event The SelectionChagnedEvent.
   */
  protected abstract void handleSelectionChanged(SelectionChangedEvent event);
  
	/**
	 * @param treeViewer
	 */
	protected abstract void initializeTreeViewer(TreeViewer treeViewer);

	/**
	 * Fills the right-click context menu.
	 * @param manager
	 */
	protected abstract void fillContextMenu(IMenuManager manager);
	
	/**
	 * Fills the toolbar menu.
	 * @param manager
	 */
	protected abstract void fillLocalToolBar(IToolBarManager manager);
	
	/**
	 * Clears the explorer and its internal data structure.
	 */
	public abstract void clear();

	/**
	 * Updates the tree by refreshing the input.
	 */
	public abstract void updateAll();

	/**
	 * Loads Vizes from the active editor.
	 */
	public abstract void loadViz();
	
	/**
	 * Responds to the event when loading Viz from a file failed.
	 */
//	public abstract void loadFailed();

	/**
	 * Adds an array of TypeViz's as the input to the explorer. The two subclasses
	 * handle it differently. VizAnnotationExplorer simply takes this array of TypeViz's
	 * as input and displays it, while VizSetEditor adds these TypeViz's to its
	 * internal data structure, Vizes, and then displays the content of Vizes.
	 * @param types An array of TypeViz's to be added to the input.
	 * @param filePath The path of the file that these TypeViz's are from.
	 */
//	public abstract void addToInput(TypeViz[] types, IPath filePath);

	public void setStatusLine(String text) {
		if (statusLabel != null) {
			this.statusLabel.setText(text);
		}
	}
	

	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
//		treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		fSash= new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
		
	//Adding a status line to the view.
		ViewForm viewForm = new ViewForm(parent, SWT.NONE);
		statusLabel = new Label(viewForm, SWT.NONE);
		viewForm.setTopLeft(statusLabel);

		//sashForm = new SashForm(viewForm, SWT.HORIZONTAL);
		treeViewer = new TreeViewer(viewForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		//treeViewer = new TreeViewer(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewForm.setContent(treeViewer.getTree());

		/*checkBoxViewForm = new ViewForm(sashForm, SWT.NONE);
		this.saveButton = new Button(checkBoxViewForm, SWT.PUSH);
		this.saveButton.setText("Save");
		checkBoxViewForm.setTopRight(saveButton);
		
		Label checkBoxFormLabel = new Label(checkBoxViewForm, SWT.NONE);
		checkBoxFormLabel.setText("Select variables to listen to:");
		checkBoxViewForm.setTopLeft(checkBoxFormLabel);
		
		this.checkboxViewer = CheckboxTableViewer.newCheckList(checkBoxViewForm, SWT.H_SCROLL | SWT.V_SCROLL);
		this.checkboxViewer.setContentProvider(new CheckboxContentProvider());
		this.checkboxViewer.setLabelProvider(new CheckboxLabelProvider());
		this.checkboxViewer.addCheckStateListener(new CheckBoxListener());
		sashForm.setWeights(new int[] {60, 40});

		checkBoxViewForm.setContent(this.checkboxViewer.getControl());
		checkBoxViewForm.setBorderVisible(true);
		
		
		
		
		viewForm.setContent(sashForm);*/
//		trayForm.setContent(treeViewer.getTree());
		
		drillDownAdapter = new DrillDownAdapter(treeViewer);
		initializeTreeViewer(treeViewer);
		treeViewer.getTree().setEnabled(false);

		final CellEditor[] cellEditors = new CellEditor[1];
	// to enable cell editing, create a dummy cell editor
		cellEditors[0] = new TextCellEditor(treeViewer.getTree());
		treeViewer.setCellEditors(cellEditors);
		treeViewer.setColumnProperties(new String[] {"Name"});
		treeViewer.setCellModifier(new VizCellModifier(this));

		initListeners(treeViewer);
		initDragAndDrop();
		makeActions();
		hookContextMenu();
		makeActionBars();
	}

	/**
	 * Initializes and makes all actions.
	 */
	protected void makeActions() {
		showVizAction = new Action() {
			public void run() {
				loadViz();
			}
		};
		showVizAction.setText("Load Viz");
		showVizAction.setToolTipText("Load Viz annotations in the active editor");
		showVizAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		
		clearAction = new Action() {
			public void run() {
				clear();
			}
		};
		clearAction.setText("Clear All");
		clearAction.setToolTipText("Clear All");
		clearAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		editAction = new Action() {
			public void run() {
				editVisualization();
			}
		};
		editAction.setText("Edit");
		editAction.setToolTipText("Edit Visualization Class");
		editAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		editAction.setEnabled(false);
		
		moveUpAction = new Action() {
			public void run() {
				move(true);
			}
		};
		moveUpAction.setText("Move Up");
		moveUpAction.setToolTipText("Move Selected Visualization Class Up");
		moveUpAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_UP));
		moveUpAction.setEnabled(false);
		
		moveDownAction = new Action() {
			public void run() {
				move(false);
			}
		};
		moveDownAction.setText("Move Down");
		moveDownAction.setToolTipText("Move Selected Visualization Class Down");
		moveDownAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		moveDownAction.setEnabled(false);
		
		setDefaultAction = new Action() {
			public void run() {
				
			}
		};
		setDefaultAction.setToolTipText("Set as Default Visualization");
		
		removeAction = new Action() {
			public void run() {
				remove();
			}
		};
		removeAction.setText("Remove");
		removeAction.setToolTipText("Remove the selected nodes");
		removeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		removeAction.setEnabled(false);
		
		expandAllAction = new Action() {
			public void run() {
				treeViewer.expandAll();
			}
		};
		expandAllAction.setText("Expand All");
		expandAllAction.setToolTipText("Expand all nodes in the tree");
		expandAllAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		expandAllAction.setEnabled(true);
		
		collapseAllAction = new Action() {
			public void run() {
				treeViewer.collapseAll();
			}
		};
		collapseAllAction.setText("Collapse All");
		collapseAllAction.setToolTipText("Collapse all nodes in the tree");
		collapseAllAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		collapseAllAction.setEnabled(true);
		
		refreshAction = new Action() {
			public void run() {
				updateAll();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refreshes the display");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		refreshAction.setEnabled(true);
	}

	/**
	 * Moves a viz class's order up (true) or down (false) under the same parent.
	 * @param up True is up, false is down.
	 */
	private void move(boolean up) {
		try {
			IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
			if (selection.size() != 1) {
				return;
			}
			Object obj = selection.getFirstElement();
			if (!(obj instanceof Visualization)) {
				return;
			}
			Visualization vc = (Visualization) obj;
			Association parentViz = vc.getParent();
			int order = vc.getOrder();
			//VizExplorerContentProvider provider = (VizExplorerContentProvider) treeViewer.getContentProvider();
			if (up) {
	//Move up operation, so the order of the selected VC must be greater than 0.
				if (order > 0) {
					parentViz.swapPosition(order, order - 1);
					treeViewer.refresh(parentViz);
					//treeViewer.expandToLevel(viz, 0);
				}
			}
			else {
	//Move down operation, so the order of the selected VC must be 2 less than the number
	//of VCs in the parent viz.
				if (order < parentViz.getVizCount() - 1) {
					parentViz.swapPosition(order, order + 1);
					treeViewer.refresh(parentViz);
				} //end if
			} //end else

			//TODO Selects the previous node after the operation. Solve the content model first!
			//treeViewer.getRawChildren(parentViz);
			//Object[] children = provider.getChildren(asso);
			//treeViewer.setSelection(new StructuredSelection(children[order - 1]), true);
		} //end try
		catch (Exception e) {
			ProViz.errprintln(e);
		} //end catch
	} //end move

	/**
	 * Removes one selected top-level TypeViz from Vizes.
	 * @param tv
	 * @return
	 */
	public abstract boolean removeTopLevelTypeViz(TypeViz tv);
	
	/**
	 */
	protected void remove() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		Association parent = null;
		for (Object obj : selection.toArray()) {
			if (obj instanceof Visualization) {
				parent = ((Visualization) obj).getParent();
				parent.remove((Visualization) obj);
			}
			else {
				if (obj instanceof Association) {
					parent = ((Association) obj).getParent();
					if (obj instanceof TypeViz) {
						TypeViz tv = (TypeViz) obj;
						if (parent == null) {
			//tv is the root node.
							removeTopLevelTypeViz(tv);
						}
						else {
			//tv is an inner class TypeViz or an actual type
							if (parent instanceof TypeViz) {
								((TypeViz) parent).removeInnerTypeViz(tv);
							}
							else if (parent instanceof MethodViz) {
								((MethodViz) parent).removeInnerTypeViz(tv);
							}
							else if (parent instanceof VariableVizBase) {
								((VariableVizBase) parent).removeActualType(tv);
							}
							else {
								ProViz.errprintln("Invalid parent for TypeViz");
							}
						}
					}
					else if (obj instanceof FieldViz) {
						if (parent instanceof TypeViz) {
							((TypeViz) parent).removeFieldViz((FieldViz) obj);
						}
						else if (parent instanceof VariableVizBase) {
							((VariableVizBase) parent).removeFieldViz((FieldViz) obj);
						}
						else {
							ProViz.errprintln("Invalid parent for FieldViz");
						}
					}
					else if (obj instanceof MethodViz) {
						if (parent instanceof TypeViz) {
							((TypeViz) parent).removeMethodViz((MethodViz) obj);
						}
						else {
							ProViz.errprintln("Invalid parent for MethodViz");
						}				
					}
					else if (obj instanceof VariableViz) {
						if (parent instanceof MethodViz) {
							((MethodViz) parent).removeVariableViz((VariableViz) obj);
						}
						else {
							System.err.println("Invalid parent for VairableViz");
						}	//end else
					} //end else if
				} //end if
			} //end else
			if (parent != null) {
				getTreeViewer().refresh(parent);
			}
			else {
				updateAll();
			}
		} //end for
/*		if (selection.size() != 1) {
			return;
		}
		Object obj = selection.getFirstElement();
		if (!(obj instanceof TypeViz)) {
			return;
		}
		Vizes.getInstance().remove((TypeViz) obj);*/
	}

	/**
	 * TODO INCOMPLETE Edits a selected viz class.
	 */
	protected void editVisualization() {
		this.isEditable = true;
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
//		viewer.getCellEditors()[0].activate();
		Object obj = selection.getFirstElement();
		treeViewer.editElement(obj, 0);
	}
	
	/**
	 * Initializes listeners.
	 * @param viewer
	 */
  protected void initListeners(TreeViewer viewer) {
    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
          handleSelectionChanged(event);
      }
    });
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
          handleDoubleClick(event);
      }
    });
  }
  
  /**
   * Gets the instance of this explorer. This is used by inner classes in methods to
   * access this explorer instance.
   * @return This instance of AbstractVizExplorer.
   *
  protected AbstractVizExplorer getInstance() {
  	return this;
  }*/
  
  /**
   * Checks if this explorer is the active viewer in the view parts. True if it is on 
   * top of the view stack.
   * @return True if this explorer is the active viewer.
   */
  public boolean isActiveView() {
  	IViewSite viewSite = getViewSite();
  	if (viewSite != null) {
  		IWorkbenchPage page = viewSite.getPage();
  		if (page != null) {
  			IViewPart[] views = page.getViewStack(this);
  			if (views != null) {
  				return views[0] == this;
  			}
  		}
  	}
  	return false;
  }

  /**
   * Initializes drag and drop support.
   */
  private void initDragAndDrop() {
    int ops = DND.DROP_MOVE;
    Transfer[] transfers = new Transfer[] {LocalSelectionTransfer.getInstance()};
            //ResourceTransfer.getInstance(), FileTransfer.getInstance(),
            //PluginTransfer.getInstance() };
    treeViewer.addDragSupport(ops, transfers, new VizDragAdapter(treeViewer));
    VizDropAdapter adapter = new VizDropAdapter(treeViewer);
    adapter.setFeedbackEnabled(true);
    treeViewer.addDropSupport(ops, transfers, adapter);
  }
  
	/**
	 * Handles the double click event on the selected item.
	 * @param event DoubleClickEvent.
	 */
	private void handleDoubleClick(DoubleClickEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    Object element = selection.getFirstElement();
    if (treeViewer.isExpandable(element)) {
    	treeViewer.setExpandedState(element, !treeViewer.getExpandedState(element));
    }
    else if (element instanceof Visualization) {
    	editVisualization();
    }
	}

	/**
	 * Registers the right-click context menu to the viewer.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}

	/**
	 * Makes the top-right toolbar menu.
	 */
	private void makeActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Pops up a message dialog with a message.
	 * @param message The String to be displayed in the message dialog.
	 */
	public void showMessage(String message) {
		MessageDialog.openInformation(
			treeViewer.getControl().getShell(),
			"Viz Explorer",
			message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	/**
	 * @return Returns the isEditable.
	 */
	public boolean isEditable() {
		return isEditable;
	}

	/**
	 * @param isEditable The isEditable to set.
	 */
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	/**
	 * @return Returns the treeViewer.
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	/**
	 * @param input
	 */
	protected void loadCheckboxViewer(Visualization input) {
		if (input == null) {
			return;
		}
		Association parent = input.getParent();
		this.selected = parent;
	//TODO 9/9/10 Need to handle when parent.getParent is a VariableViz
		if (parent instanceof VariableViz && parent.getParent() instanceof MethodViz) {
			MethodViz method = (MethodViz) parent.getParent();
			TypeViz type = (TypeViz) method.getParent();
			Collection<FieldViz> fields = type.getFieldVizes();
			Collection<VariableViz> vars = method.getVariableVizes();
			for (FieldViz fv : fields) {
				checkboxViewer.add(IVizVariable.THIS_PREFIX + fv.getSimpleName());
			}
			for (VariableViz vv : vars) {
				if (vv != parent) {
					checkboxViewer.add(vv.getSimpleName());
				}
			}
			this.fillSuperClassFields(type);
		}
		else if (parent instanceof FieldViz) {
			if (parent.getParent() instanceof TypeViz) {
				TypeViz type = (TypeViz) parent.getParent();
				Collection<FieldViz> fields = type.getFieldVizes();
				for (FieldViz fv : fields) {
					if (fv != parent) {
						checkboxViewer.add(IVizVariable.THIS_PREFIX + fv.getSimpleName());
					}
				}
				this.fillSuperClassFields(type);
			}
		}
		else if (parent instanceof TypeViz) {
			Collection<FieldViz> fields = ((TypeViz) parent).getFieldVizes();
			for (FieldViz fv : fields) {
				checkboxViewer.add(fv.getSimpleName());
			}
			this.fillSuperClassFields((TypeViz) parent);
		}
		else {
			this.selected = null;
			return;
		}
		List<String> list = this.selected.getDependingVars();
		for (String depending : list) {
			checkboxViewer.setChecked(depending, true);
		}
	}
	@SuppressWarnings("unchecked")
	private void fillSuperClassFields(TypeViz typeViz) {
		try {
			Class current = Class.forName(typeViz.getFullName());
			Class superClass = current.getSuperclass();
			ProViz.println("Checking super class: " + superClass.getName());
			if (superClass.getName().equals("java.lang.Object")) {
				return;
			}
			TypeViz type = VizMapModel.getInstance().findTypeViz(superClass.getName());
			ProViz.println(type + "");
			if (type != null) {
				for (FieldViz fv : type.getFieldVizes()) {
					ProViz.println(type.getSimpleName() + "." + fv.getSimpleName());
					checkboxViewer.add(type.getSimpleName() + "." + fv.getSimpleName());
				}
				fillSuperClassFields(type);
			}
		} catch (ClassNotFoundException e) {
			//ProViz.errprintln("Super class cannot be found: " + e.getMessage());
		}
	}
	protected void clearCheckBoxViewer() {
		this.checkboxViewer.setInput(null);
	}

	class CheckBoxListener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			if (selected != null) {
				if (event.getChecked()) {
					selected.addDependingVariable(event.getElement().toString());
				}
				else {
					selected.removeDependingVariable(event.getElement().toString());
				}
			}
		}
	}
}
