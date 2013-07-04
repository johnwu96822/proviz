package viz.views.eclipse;

import java.awt.Point;
import java.util.LinkedList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.SaveAsDialog;

import viz.ProViz;
import viz.convert.eclipse.ast.ASTAnnotationToViz;
import viz.io.VizSaveUtil;
import viz.io.xml.XMLToViz;
import viz.io.xml.XmlException;
import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;
import viz.model.VizMapModel;
import viz.util.EclipseResourceUtil;

/**
 * TODO When modification occur to system type TypeViz's and their trees, change them to non-system types
 * <p>An explorer that shows program elements and their viz annotations
 * in a tree view. It allows re-ordering of viz classes within
 * the same node. Modification reflecting in the source code is yet to be done.</p>
 * @author John
 * Created on April 10, 2006
 */
public class VizSetEditor extends AbstractVizExplorer {
	private static final String VIZLIBXML = "vizlib.xml";
//	protected Action saveVizToXMLAction;
	private Action saveAllAction;
	private Action loadVizLib;
	
	private Action expandAllPainters;
	
	/* This will load the "vizlib.xml" library file when created.
	 * @see viz.views.eclipse.AbstractVizExplorer#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		this.loadLib();
		ProViz.getInstance().setVizSetEditor(this);
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#loadViz()
	 */
	@Override
	public void loadViz() {
		setStatusLine("");
  //Try loading from Java source code
		IPath javaPath = ASTAnnotationToViz.eclipseActiveEditorFillVizes();
		if (javaPath != null) {
			setStatusLine(javaPath.lastSegment() + " loaded!");
			updateAll();
		}	else {
	//If not, the active editor may be XML. Try load the XML
			try {
				IPath path = EclipseResourceUtil.getActiveEditorFilePath();
				TypeViz[] tvs = XMLToViz.loadVizFromXmlFile(path);
				if (tvs != null) {
					for (TypeViz tv : tvs) {
						VizMapModel.getInstance().addOrReplace(tv, false);
						//Vizes.getInstance().addTypeToFile(path.toOSString(), tv);
					}
					updateAll();
					setStatusLine(path.lastSegment() + " loaded!");
				} else {
					showMessage("File contains no Viz annotation");
					setStatusLine("File contains no Viz annotation");
				}
			} catch (XmlException e) {
				ProViz.errprintln(e);
				showMessage("Error parsing the file: " + e.getMessage());
				setStatusLine(e.getMessage());
			}
		}
	} //end loadViz

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#initializeTreeViewer()
	 */
	@Override
	protected void initializeTreeViewer(TreeViewer treeViewer) {
		treeViewer.setContentProvider(new VizSetEditorContentProvider(this));
		treeViewer.setLabelProvider(new VizLabelProvider());
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#updateAll()
	 */
	@Override
	public void updateAll() {
		getTreeViewer().setInput(VizMapModel.getInstance());
		treeViewer.getTree().setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();
/*		saveVizToXMLAction = new Action() {
			public void run() {
				saveViz(false);
			}
		};
		saveVizToXMLAction.setText("Save the selected top-level classes to XML");
		saveVizToXMLAction.setToolTipText("Save the selected top-level class to XML");
		saveVizToXMLAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		*/
		saveAllAction = new Action() {
			public void run() {
				saveViz(true);
			}
		};
		saveAllAction.setText("Save to XML");
		saveAllAction.setToolTipText("Save to XML");
		saveAllAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
		
		loadVizLib = new Action() {
			public void run() {
				loadLib();
			}
		};
		loadVizLib.setText("Load Viz Library");
		loadVizLib.setToolTipText("Load Viz Library");
		loadVizLib.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		expandAllPainters = new Action() {
			public void run() {
				System.out.println("Expand all painters");
				ProViz.println("Expand all painters 2");
				try {
					expandPainters();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		expandAllPainters.setText("Expand All Painters");
		expandAllPainters.setToolTipText("Expand All Painters");
		expandAllPainters.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
	}
	
	private void expandPainters() {
		LinkedList<Visualization> list = new LinkedList<Visualization>();
		for (TypeViz tv : VizMapModel.getInstance().getTypeList()) {
			addTypeVizes(list, tv);
		}
		System.out.println(list.size());
		treeViewer.setExpandedElements(list.toArray());
	}
	
	private void addTypeVizes(LinkedList<Visualization> list, TypeViz tv) {
		addVisualization(list, tv);
		for (TypeViz inner : tv.getInnerTypeVizes()) {
			addTypeVizes(list, inner);
		}
		for (MethodViz mv : tv.getMethodVizes()) {
			addMethodVizes(list, mv);
		}
		for (FieldViz fv : tv.getFieldVizes()) {
			addFieldVizes(list, fv);
		}
	}
	
	private void addVisualization(LinkedList<Visualization> list, Association asso) {
		Visualization[] vizes = asso.getVisualizations();
		if (vizes.length > 0) {
			for (Visualization viz : vizes) {
				list.add(viz);
			}
		}
	}

	private void addFieldVizes(LinkedList<Visualization> list, FieldViz fv) {
		addVisualization(list, fv);
		if (fv.getFieldVizes() != null) {
			for (FieldViz customField : fv.getFieldVizes()) {
				addFieldVizes(list, customField);
			}
		}
	}

	private void addMethodVizes(LinkedList<Visualization> list, MethodViz mv) {
		addVisualization(list, mv);
		for (TypeViz inner : mv.getInnerTypeVizes()) {
			addTypeVizes(list, inner);
		}
		for (VariableViz vv : mv.getVariableVizes()) {
			addVisualization(list, vv);
			if (vv.getFieldVizes() != null) {
				for (FieldViz customField : vv.getFieldVizes()) {
					addFieldVizes(list, customField);
				}
				
			}
		}
	}

	/**
	 * Loads the Viz annotations from vizlib.xml
	 */
	public void loadLib() {
		TypeViz[] tvs = null;
		try {
			tvs = XMLToViz.loadVizFromXmlFile(VIZLIBXML);
		} catch (Exception e) {
			//ProViz.errprintln(e.getMessage());
			showMessage(e.getMessage());
			setStatusLine(e.getMessage());
		}
		if (tvs != null) {
			for (TypeViz tv : tvs) {
				tv.setSystemType(true);
				VizMapModel.getInstance().addOrReplace(tv, true);
				//Vizes.getInstance().addTypeToFile(path.toOSString(), tv);
			}
			updateAll();
			setStatusLine("Viz Library (vizlib.xml) loaded.");
		} else {
			setStatusLine("Viz Library not loaded!");
		}
	}
	
	/**
	 * Saves selected top-level TypeVizes or all in Vizes to file. If parameter 'all'
	 * is true, it saves everything in Vizes; otherwise it saves any selected top-level
	 * TypeVizes to file. The pre-set folder for Viz xml files is the 'viz_xml' folder.
	 * @param all TODO True to save everything in Vizes; false to save just the selected ones.
	 */
	private void saveViz(boolean all) {
		if (VizMapModel.getInstance().getVizSize() == 0) {
			this.showMessage("Viz Mapping Model is empty!");
			return;
		}
		Shell shell = getSite().getShell();
		SaveAsDialog saveDialog = new SaveAsDialog(shell);
	//TODO Set to the project's viz_xml folder
		IPath path = EclipseResourceUtil.getActiveEditorFilePath();
		IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath relativePath = path.removeFirstSegments(rootPath.segmentCount()).removeFileExtension();
		saveDialog.setOriginalFile(ResourcesPlugin.getWorkspace().getRoot().getFile(relativePath.addFileExtension("xml")));
		//saveDialog.setOriginalName(relativePath.lastSegment() + ".xml");
		//saveDialog.setOriginalName(path.toString());
		if (saveDialog.open() == Window.CANCEL) {
			return;
		}
		IPath filePath= saveDialog.getResult();
		if (filePath != null) {
				VizSaveUtil.save(filePath, VizMapModel.getInstance().getTypeList());
				//VizSaveUtil.save(workspacePath.append(filePath), Vizes.getInstance().getTypeList());
		}
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#clear()
	 */
	@Override
	public void clear() {
		VizMapModel.getInstance().clearAll();
		this.getTreeViewer().getTree().removeAll();
		treeViewer.getTree().setEnabled(false);
	}
	/*
	class InputThread implements Runnable {
		String input = null;
		String fieldName;
		VizSetEditor waiting;
		public InputThread(String fieldName, VizSetEditor waiting) {
			this.fieldName = fieldName;
			this.waiting = waiting;
		}
		@Override
		public void run() {
			this.input = JOptionPane.showInputDialog(null, "Enter the painter name for " + this.fieldName);
			synchronized (waiting) {
				waiting.notifyAll();
			}
		}
		public String getInput() {
			return input;
		}
	}*/
	
	private void enterNewPainterName(FieldViz fv, VariableVizBase parent) {
		/*InputThread inputThread = new InputThread(fv.getFullName(), this);
		Thread thread = new Thread(inputThread);
		thread.start();
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				ProViz.errprintln(e);
			}
		}*/
		//String painterName = JOptionPane.showInputDialog(null, "Enter the painter name for " + fv.getFullName()); 
			//inputThread.getInput();
		//if (painterName != null) {
			FieldViz current = parent.getFieldViz(fv.getFullName());
			Visualization viz;
			if (current == null) {
				current = new FieldViz(fv.getFullName(), parent, fv.getType());
				viz = new Visualization("", current);
				current.addVisualization(viz);
				parent.addFieldViz(current);
			} else {
				viz = new Visualization("", current);
				current.addVisualization(viz);
			}
			treeViewer.refresh(parent);
			//treeViewer.reveal(viz);
			treeViewer.editElement(viz, 0);
		//}
	}
	
	private void fillFieldVizMenu(MenuManager menu, final FieldViz fv, final VariableVizBase parent) {
			MenuManager submenu = new MenuManager(fv.getFullName());
			for (Visualization viz : fv.getVisualizations()) {
				final String painterName = viz.getPainterName();
				submenu.add(new Action(viz.getPainterName()) {
					public void run() {
						FieldViz current = parent.getFieldViz(fv.getFullName());
						if (current == null) {
							current = new FieldViz(fv.getFullName(), parent, fv.getType());
							current.addVisualization(new Visualization(painterName, current));
							parent.addFieldViz(current);
						} else {
							current.addVisualization(new Visualization(painterName, current));
						}
						treeViewer.refresh(parent);
					}
				});
			}
			submenu.add(new Separator());
			submenu.add(new Action("Enter New Painter") {
				public void run() {
					enterNewPainterName(fv, parent);
				}
			});
			menu.add(submenu);
		//}
	}

	protected void addPainter(Association parent) {
		Visualization viz = new Visualization("Enter New Painter", parent);
		parent.addVisualization(viz);
		treeViewer.refresh(parent);
		//treeViewer.reveal(viz);
		treeViewer.editElement(viz, 0);
	}

	protected void addArrayField(VariableVizBase varViz) {
		FieldViz fv = varViz.getFieldViz("[]");
		if (fv == null) {
			fv = new FieldViz("[]", varViz, null);
			varViz.addFieldViz(fv);
			treeViewer.refresh(varViz);
		}
		addPainter(fv);
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(editAction);
		manager.add(new Separator());
		TreeSelection selection = (TreeSelection) treeViewer.getSelection();
	//Allows adding a custom visualization for a field
		if (selection.size() == 1) {
			final Object selectedObj = selection.getFirstElement();
			if (selectedObj instanceof Association) {
	//Adds a new painter option
				manager.add(new Action("Add New Painter") {
					public void run() {
						addPainter((Association) selectedObj);
					}
				});
				if (selectedObj instanceof VariableVizBase) {//VariableViz) {
	//Adds the option of adding FieldViz TODO allow FieldViz under FieldViz
	//11.01.18 Allows only one extra level of FieldViz under a FieldViz or VariableViz
					final VariableVizBase varViz = (VariableVizBase) selectedObj;
					if (!(varViz.getParent() instanceof VariableVizBase)) {
						String type = varViz.getType();
						if (type.indexOf('[') != -1) {
	//Handling array type
							manager.add(new Action("Specify Painter for Array Elements") {
								public void run() {
									addArrayField(varViz);
								}
							});
						} else {
							MenuManager submenu = new MenuManager("Add Field Painter");
							TypeViz tv = VizMapModel.getInstance().findTypeStartsWith(type);
							if (tv != null) {
								for (FieldViz fv : tv.getFieldVizes()) {
									this.fillFieldVizMenu(submenu, fv, varViz);
								}
								manager.add(submenu);
							}
						} //end else
					} //end if
				}
			} else if (selectedObj instanceof Visualization) {
				manager.add(new Action("Edit Starting Location") {
					public void run() {
						editLocation((Visualization) selectedObj);
					}
				});
			}
		}
		manager.add(new Separator());
		manager.add(moveUpAction);
		manager.add(moveDownAction);
		manager.add(removeAction);
		manager.add(new Separator());
		manager.add(showVizAction);
		manager.add(clearAction);
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));	
	}
	
	/**
	 * Gets an integer with Eclipse's pop up window
	 * @param loc
	 * @param xOrY
	 * @return
	 */
	private String getInteger(Point loc, String xOrY) {
		InputDialog dlg;
		if (loc != null) {
			String value = loc.x + "";
			if (xOrY.equalsIgnoreCase("y")) {
				value = loc.y + "";
			}
			dlg = new InputDialog(this.getSite().getShell(),
	        "Enter Location", "Enter " + xOrY, value, new IntegerValidator());
		}
		else {
			dlg = new InputDialog(this.getSite().getShell(),
	        "Enter Location", "Enter " + xOrY, null, new IntegerValidator());
		}
    if (dlg.open() == Window.OK) {
  // User clicked OK
      return dlg.getValue();
    }
    else {
    	return null;
    }
	}

	private void editLocation(Visualization viz) {
		Point loc = viz.getStartingLocation();
		String inputX = getInteger(loc, "x");
		if (inputX != null) {
			String inputY = getInteger(loc, "y");
			if (inputY != null) {
				viz.setStartingLocation(Integer.parseInt(inputX), Integer.parseInt(inputY));
				treeViewer.update(viz, null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#fillLocalToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(showVizAction);
		manager.add(loadVizLib);
		manager.add(refreshAction);
		manager.add(new Separator());
		manager.add(saveAllAction);
		manager.add(removeAction);
//		manager.add(saveVizToXMLAction);
		manager.add(new Separator());
		manager.add(editAction);
		manager.add(moveUpAction);
		manager.add(moveDownAction);
		manager.add(new Separator());
		manager.add(clearAction);
		//TODO expand all painters don't work yet
		//manager.add(expandAllPainters);
		manager.add(expandAllAction);
		manager.add(collapseAllAction);
		drillDownAdapter.addNavigationActions(manager);
	}
	
	/**
   * Handles selection-changed event.
   * @param event The SelectionChagnedEvent.
   */
  protected void handleSelectionChanged(SelectionChangedEvent event) {
  	IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		removeAction.setEnabled(true);
		editAction.setEnabled(false);
		moveUpAction.setEnabled(false);
		moveDownAction.setEnabled(false);
		//TODO this.clearCheckBoxViewer();
//		saveVizToXMLAction.setEnabled(false);
  	if (sel.size() == 0) {
//  		saveVizToXMLAction.setEnabled(true);
  		removeAction.setEnabled(false);
  	}
  	else if (sel.size() == 1) {
    	Object singleSelection = sel.getFirstElement();
    	if (singleSelection instanceof Visualization) {
  //A viz class is selected.
    		editAction.setEnabled(true);
    		moveUpAction.setEnabled(true);
    		moveDownAction.setEnabled(true);
  			//TODO this.loadCheckboxViewer((Visualization) singleSelection);
    	} //end if
    	else {
    		if (singleSelection instanceof VariableVizBase) {
    		}
    		else if (singleSelection instanceof TypeViz) {
  //A TypeViz is selected. Check if the TypeViz is a top level type.
    			if (VizMapModel.getInstance().contains((TypeViz) singleSelection)) {
//    				removeAction.setEnabled(true);
//    				saveVizToXMLAction.setEnabled(true);
    			} //end if
    		} //end if
    	} //end else
  	} //end if
  	else {
  		for (Object obj : sel.toArray()) {
  			if (obj instanceof TypeViz && VizMapModel.getInstance().contains((TypeViz) obj)) {
//  				saveVizToXMLAction.setEnabled(true);
  				break;
  			} //end if
  		} //end for
  	} //end else
  } //end handleSelectionChanged

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#removeTopLevelTypeViz(viz.model.TypeViz)
	 */
	@Override
	public boolean removeTopLevelTypeViz(TypeViz tv) {
		return VizMapModel.getInstance().removeTypeViz(tv) != null;
	}
}

class IntegerValidator implements IInputValidator {
	@Override
	public String isValid(String newText) {
		try {
			Integer.parseInt(newText);
		}	catch (NumberFormatException e) {
			return "Enter integer only";
		}
		return null;
	}
}