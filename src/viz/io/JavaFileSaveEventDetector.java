package viz.io;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaCore;

import viz.ProViz;
import viz.convert.eclipse.ast.ASTAnnotationToViz;
import viz.io.xml.XMLToViz;
import viz.io.xml.XmlException;
import viz.model.TypeViz;
import viz.views.eclipse.VizAnnotationExplorer;
/**
 * Detects events occur when Java files are saved in Eclipse and ensures the
 * integrity of Viz structures across the source file, VizAnnotationExplorer, and
 * Viz XML files. It detects file-saved events in Eclipse by listening to 
 * resource-changed events. Modified Java files are then parsed again for
 * the Viz structures, and these newly parsed Viz structure are sent back to
 * update VizAnnotationExplorer's cache. Finally, they are saved to the Viz XML
 * files in the 'viz_xml' directory under the project.   
 * @author John
 * Created on Oct 30, 2006
 */
public class JavaFileSaveEventDetector implements IResourceChangeListener {
	private VizAnnotationExplorer explorer = null;
	
	public JavaFileSaveEventDetector(VizAnnotationExplorer explorer) {
		this.explorer = explorer;
	}
	
//	private static final IPath DOC_PATH = new Path("Visualization/src");
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (explorer == null || event.getType() != IResourceChangeEvent.POST_CHANGE) {
			return;
		}
		ArrayList<IResource> changed = getChangedResources(event.getDelta());

    //nothing more to do if there were no changed text files
    if (changed.size() == 0) {
       return;
    }
    else {
    	ProViz.println("<CHANGED: ");
    	System.out.println("<CHANGED: ");
    	for (IResource resource : changed) {
  			IPath path = resource.getRawLocation();
    		if (resource.getFileExtension().equalsIgnoreCase("xml")) {
					try {
						TypeViz[] tempTypes = XMLToViz.loadVizFromXmlFile(path);
						if (tempTypes != null) {
							explorer.updateCache(path, tempTypes);
		    			ProViz.println(resource.getName());
		    			System.out.println(resource.getName());
						}
						else {
							explorer.setStatusLine("Viz not found in this file: " + resource.getName());
							explorer.clear();
						}
					}
					catch (XmlException e) {
						explorer.setStatusLine(e.getMessage());
						explorer.clear();
					}
    		}
    		else if (resource instanceof IFile) {
    			IJavaElement javaElement = JavaCore.create(resource);
    			if (javaElement instanceof IOpenable) {
    				Collection<TypeViz> roots = ASTAnnotationToViz.createVizesFromAST((IOpenable) javaElement);
    				if (roots != null && !roots.isEmpty()) {
  						TypeViz[] types = roots.toArray(new TypeViz[0]);
  						explorer.updateCache(path, types);
  	    			VizSaveUtil.doSaveVizFromJavaFile((IFile) resource, types);
    				}
    			}
	    		System.out.println(resource.getName());
	    		ProViz.println(resource.getName());
    		}
    	}
    	System.out.println("/CHANGED>");
  		ProViz.println("/CHANGED>");
    	
    	/*
    	if (changed.size() == 1) {
    		IResource resource = changed.get(0);
    		System.out.println("Size 1: " + resource.getName());
    		if (resource.getRawLocation().equals(explorer.getFilePath())) {
      		String path = VizSaver.getFilePathInXmlExt(explorer.getFilePath().toOSString());
      		if (path == null) {
      			explorer.showMessage("Bad filename!");
      			return;
      		}
      		//TODO save TypeViz's in VizAnnotationExplorer
    		}
    		else {
    			
    		}
    	}
    	else {
	    	System.out.println("<CHANGED: ");
	    	for (IResource resource : changed) {
	    		if (resource instanceof IFile) {
		    		System.out.println(resource.getName());
	    			//VizSaver.doSaveVizFromJavaFile((IFile) resource);
	    		}
	    	}
	    	System.out.println("/CHANGED>");
    	}*/
    }
	}
	
	public ArrayList<IResource> getChangedResources(IResourceDelta rootDelta) {
    //get the delta, if any, for the documentation directory
//    IResourceDelta docDelta = rootDelta.findMember(DOC_PATH);
//    if (docDelta == null) {
//    	System.out.println("NOT FOUND");
//    	return;
//    }
    final ArrayList<IResource> changed = new ArrayList<IResource>();
    IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
      public boolean visit(IResourceDelta delta) {
        //only interested in changed resources (not added or removed)
        if (delta.getKind() != IResourceDelta.CHANGED)
          return true;
        //only interested in content changes
        if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
          return true;
        IResource resource = delta.getResource();
        //only interested in files with the "java" extension
        if (resource.getType() == IResource.FILE &&
  //XML files will be saved by the platform, so only parse and save for Java files.
        	  (resource.getFileExtension().equalsIgnoreCase("xml") || 
          	resource.getFileExtension().equalsIgnoreCase("java"))) {
          changed.add(resource);
        }
        return true;
      }
    };
    try {
       //docDelta.accept(visitor);
    	rootDelta.accept(visitor);
    } catch (CoreException e) {
       e.printStackTrace();
    }
		return changed;
	}
}