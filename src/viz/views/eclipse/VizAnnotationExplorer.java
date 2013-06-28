package viz.views.eclipse;

import java.util.ArrayList;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

import viz.ProViz;
import viz.convert.eclipse.ast.ASTAnnotationToViz;
import viz.io.JavaFileSaveEventDetector;
import viz.io.VizSaveUtil;
import viz.io.xml.XMLToViz;
import viz.io.xml.XmlException;
import viz.model.FileVizesCache;
import viz.model.TypeViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;
import viz.util.EclipseResourceUtil;

/**
 * <p>A file-based Viz explorer that shows the Vize's in a file. It allows 
 * re-ordering of viz classes within the same node. Modification 
 * is yet to be done.</p>
 * @author John
 * Created on November 1, 2006
 */
public class VizAnnotationExplorer extends AbstractVizExplorer {
	//currentTypes are the TypeViz's that will be displayed
	private ArrayList<TypeViz> currentTypes = new ArrayList<TypeViz>();
	private FileVizesCache cache = new FileVizesCache();
	private IPath currentFilePath = null;
	private JavaFileSaveEventDetector saveListener;
	private IPartListener vPartListener;

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		saveListener = new JavaFileSaveEventDetector(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(saveListener, IResourceChangeEvent.POST_CHANGE);
		updateAll();
	}

	/* Sets the specific content provider in here.
	 * @see viz.views.eclipse.AbstractVizExplorer#initializeTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void initializeTreeViewer(TreeViewer treeViewer) {
		getTreeViewer().setContentProvider(new VizAnnotationExplorerContentProvider(this));
		getTreeViewer().setLabelProvider(new VizLabelProvider());
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#update()
	 */
	@Override
	public void updateAll() {
//		if (this.currentTypes != null) {
			getTreeViewer().setInput(getCurrentTypes());
			getTreeViewer().expandToLevel(2);
			getTreeViewer().getTree().setEnabled(true);
			if (!this.currentTypes.isEmpty()) {
				getTreeViewer().reveal(this.currentTypes.get(0));
			}
//		}
	}
	

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#loadViz()
	 */
	@Override
	public void loadViz() {
		IEditorPart part = EclipseResourceUtil.getActiveEditor();
		IPath path = EclipseResourceUtil.getPathFromPart(part);
		if (path == null) {
			this.setStatusLine("Active editor not found!");
			clear();
			return;
		}
		TypeViz[] tvs = cache.getFile(path.toOSString());
	//The file already parsed and existed in the cache
		if (tvs != null) {
			this.setStatusLine(path.toPortableString());
			this.currentTypes.clear();
			for (TypeViz tv : tvs) {
				this.currentTypes.add(tv);
			}
			this.currentFilePath = path;
			updateAll();
		}
		else {
			String ext = path.getFileExtension();
		//File in the active editor is a Java file.
			if (ext.equalsIgnoreCase("java")) {
					TypeViz[] tempTypes = ASTAnnotationToViz.getVizesFromEditor(part);
					if (tempTypes != null && tempTypes.length > 0) {//eclipseActiveEditorFillVizes()) {
						this.addToInput(tempTypes, path);
						updateAll();
						this.setStatusLine(path.toPortableString());
						IEditorInput eInput = part.getEditorInput();
						if (eInput instanceof IFileEditorInput) {
							System.out.println("Viz Annotation Explorer saving XML");
							ProViz.println("Viz Annotation Explorer saving XML");
							VizSaveUtil.doSaveVizFromJavaFile(((IFileEditorInput) eInput).getFile(), tempTypes);
						}
					}
					else {
						this.setStatusLine("Viz not found in this file!");
						clear();
					}

/*						for (TypeViz tv : this.currentTypes) {
							tv.print("");
						}*/
/*					}
					else {
						this.setStatusLine("No @Viz annotation found in this file!");
						clear();
					}*/
			}
	//File in the active editor is an XML file.
			else if (ext.equalsIgnoreCase("xml")){
				try {
					TypeViz[] tempTypes = XMLToViz.loadVizFromXmlFile(path);
					if (tempTypes != null && tempTypes.length > 0) {
						this.addToInput(tempTypes, path);
						this.setStatusLine(path.toPortableString());
						updateAll();
					}
					else {
						this.setStatusLine("Viz not found in this file!");
						clear();
					}
				}
				catch (XmlException e) {
					ProViz.errprintln(e.getMessage());
					this.setStatusLine(e.getMessage());
					clear();
				} //end catch
			}
			else {
				this.setStatusLine("...");
				clear();
			}
			

		} //end else
	} //end loadViz

	private class VizPartListener implements IPartListener {
  	private IEditorPart activePart = null;
  	
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
//Checks if this explorer is the active part on the view stack.
//			System.out.println("Activated: " + part + " - " + getInstance());
			if (part instanceof IEditorPart) {
				if (isActiveView()) {
					if (part != activePart) {
						loadViz();
					}
					activePart = (IEditorPart) part;
				}
			}
			if (part instanceof VizAnnotationExplorer) {
				VizAnnotationExplorer fv = (VizAnnotationExplorer) part;
				if (fv.getTreeViewer().getContentProvider() != null) {
					loadViz();
				}
			}
		} //end partActivated

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {
//			System.out.println("Opened: " + part + " - " + getInstance());
			if (part instanceof VizAnnotationExplorer) {
				VizAnnotationExplorer fv = (VizAnnotationExplorer) part;
				if (fv.getTreeViewer().getContentProvider() != null) {
					loadViz();
				}
			}
//				System.out.println("HERE: " + part);
			else if (part instanceof IEditorPart) {
				loadViz();
			}
		}
		
		public void partBroughtToTop(IWorkbenchPart part) {}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
//				System.out.println("Part closed: " + part);
				if (part.getSite().getPage().getActiveEditor() == null) {
					clear();
				}
			}
		}

		public void partDeactivated(IWorkbenchPart part) {}
  }
	
	/* Initializes a part listener to listen to EditorPart events and this VizAnnotationExplorer's
	 * events. 
	 * @see viz.views.eclipse.AbstractVizExplorer#initListeners(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void initListeners(TreeViewer viewer) {
		super.initListeners(viewer);
	//Adds a part listener to detect change of active editor in the workbench.
		vPartListener = new VizPartListener();
    getSite().getWorkbenchWindow().getPartService().addPartListener(vPartListener);
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#handleSelectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	protected void handleSelectionChanged(SelectionChangedEvent event) {
  	IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		removeAction.setEnabled(false);
		editAction.setEnabled(false);
		moveUpAction.setEnabled(false);
		moveDownAction.setEnabled(false);
		//TODO this.clearCheckBoxViewer();
  	if (sel.size() == 1) {
  //Single selection
    	Object singleSelection = sel.getFirstElement();
    	if (singleSelection instanceof Visualization) {
  //A viz class is selected.
    		editAction.setEnabled(true);
    		moveUpAction.setEnabled(true);
    		moveDownAction.setEnabled(true);
  			//TODO this.loadCheckboxViewer((Visualization) singleSelection);
    	}
    	else {
    		if (singleSelection instanceof VariableVizBase) {
    		}
    		else if (singleSelection instanceof TypeViz) {
  //A TypeViz is selected. Check if the TypeViz is a top level type. (See VizSetEditor)
    			
    		}
    	}
  	}
  	else if (sel.size() > 1) {
//  		for (Object obj : sel.toArray()) {
 // 			if (obj instanceof TypeViz && Vizes.getInstance().contains((TypeViz) obj)) {
  //				break;
  //			}
  //		}
  	}	
	} //end handleSelectionChanged

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(editAction);
		manager.add(new Separator());
		manager.add(moveUpAction);
		manager.add(moveDownAction);
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));		
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#fillLocalToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(editAction);
		manager.add(new Separator());
		manager.add(moveUpAction);
		manager.add(moveDownAction);
		manager.add(expandAllAction);
		manager.add(collapseAllAction);
		//drillDownAdapter.addNavigationActions(manager);	
	}

	/* Clears current display of Viz structure by setting the input to null.
	 * TODO Not remove the nodes directly in the tree viewer but clear by the content provider's response.
	 * @see viz.views.eclipse.AbstractVizExplorer#clear()
	 */
	@Override
	public void clear() {
		this.currentFilePath = null;
		this.currentTypes.clear();
		this.getTreeViewer().getTree().removeAll();
		treeViewer.getTree().setEnabled(false);
	}

	/**
	 * @param types
	 * @param filePath
	 */
	public void addToInput(TypeViz[] types, IPath filePath) {
		this.currentTypes.clear();
		this.currentFilePath = filePath;
		for (TypeViz tv : types) {
			currentTypes.add(tv);
			cache.addOrReplace(tv, true);
			cache.addTypeToFile(filePath.toOSString(), tv);
		}
	}

	/**
	 * @return Returns the filePath.
	 */
	public IPath getCurrentFilePath() {
		return currentFilePath;
	}

	/**
	 * @return Returns the types.
	 */
	public TypeViz[] getCurrentTypes() {
		return currentTypes.toArray(new TypeViz[0]);
	}
	
	/**
	 * Updates the Viz structure in the cache. The update is file-based, that
	 * the path of the file to be updated and the TypeViz's in that file are
	 * passed in, and the path is used as the identifier to update the cache.
	 * If the updating file's path matches that of the file currently being 
	 * displayed (i.e. file in the active editor is screenModified), then update the 
	 * current view also.
	 * @param path The path of the file to be updated.
	 * @param updatedTypes The TypeViz's in that file.
	 */
	public void updateCache(IPath path, TypeViz[] updatedTypes) {
		if (path != null && updatedTypes != null && updatedTypes.length > 0) {
			cache.removeFile(path.toOSString());
			for (TypeViz tv : updatedTypes) {
				cache.addOrReplace(tv, true);
				cache.addTypeToFile(path.toOSString(), tv);
			}
		}
		if (path.equals(this.currentFilePath)) {
			this.currentTypes.clear();
			for (TypeViz tv : updatedTypes) {
				this.currentTypes.add(tv);
			}
			updateAll();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getPartService().removePartListener(vPartListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(saveListener);
		this.currentTypes.clear();
		this.currentFilePath = null;
		this.cache.clearAll();
	}

	/* (non-Javadoc)
	 * @see viz.views.eclipse.AbstractVizExplorer#removeTopLevelTypeViz(viz.model.TypeViz)
	 */
	@Override
	public boolean removeTopLevelTypeViz(TypeViz tv) {
		boolean rv = this.currentTypes.remove(tv);
		if (rv) {
			updateCache(this.currentFilePath, this.currentTypes.toArray(new TypeViz[0]));
		}
		return rv;
	}
}