package viz.util;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import plugin.VizAnnotationPlugin;
import viz.ProViz;

/**
 * @author John
 * Created on Nov 2, 2006
 */
public class EclipseResourceUtil {
	
	/**
	 * Doesn't work...
	 * @param message
	 */
	private static void showMessageDialog(String message) {
    ChooserThread thread = new ChooserThread(message);
	//Avoid the thread problem in Eclipse
	    PlatformUI.getWorkbench().getDisplay().asyncExec(thread);
	    synchronized (thread) {
		    try {
					thread.wait();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
	    }
	}
	private static class ChooserThread implements Runnable {
		//private Shell activeShell;
		private String message;
		public ChooserThread(String message) {
			this.message = message;
		}
		@Override
		public void run() {
      //this.activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			/*Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();//new Shell (display);
			//shell.setSize(0, 0);
			//shell.open ();
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
      fd.setText("Save Recording");
      fd.setFileName(mainFile.substring(mainFile.lastIndexOf('/') + 1, mainFile.lastIndexOf('.')) + ".viz");
      fd.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + 
      		mainFile.substring(0, mainFile.lastIndexOf("/")));
      fd.setFilterExtensions(new String[] {"*.viz", "*.java"});
      mainFile = fd.open();*/
			JOptionPane.showMessageDialog(ProViz.getInstance().getFrame(), message);
      synchronized (this) {
      	this.notify();
      }
		}
	}

	/**
	 * Gets the path of the file that is in the active editor.
	 * @return An IPath specifying the path of the file.
	 *
	public static IPath getActiveEditorProject() {
		IEditorPart part = getActiveEditor();
		//VizAnnotationPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		if (part != null) {
			IEditorInput input = part.getEditorInput();
			
			if (input != null && input instanceof IPathEditorInput) {
				return ((IPathEditorInput) input).getPath();
			}
		}
		return null;
	}*/
	
	/**
	 * Gets the path of the file that is opened the active editor.
	 * @return An IPath specifying the path of the file. null if There is no active editor.
	 */
	public static IPath getActiveEditorFilePath() {
		return getPathFromPart(getActiveEditor());
	}
	
	public static IPath getPathFromPart(IEditorPart part) {
		if (part != null) {
			IEditorInput input = part.getEditorInput();
			if (input != null && input instanceof IPathEditorInput) {
				return ((IPathEditorInput) input).getPath();
			}
		}
		return null;
	}
	
	/**
	 * <p>Code taken from org.eclipse.jdt.astview.EditorUtility.java from ASTView project: 
	 * http://www.eclipse.org/jdt/ui/astview/index.html</p>
	 */
	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow window = VizAnnotationPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page= window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}
		return null;
	}
	
	/**
	 * <p>Code taken from org.eclipse.jdt.astview.EditorUtility.java from ASTView project: 
	 * http://www.eclipse.org/jdt/ui/astview/index.html</p>
	 */	
	public static IOpenable getJavaInput(IEditorPart part) {
		IEditorInput editorInput = part.getEditorInput();
		if (editorInput != null) {
			IJavaElement input = getJavaElementFromEditorInput(editorInput);
			if (input instanceof IOpenable) {
				return (IOpenable) input;
			}
		}
		return null;	
	}
	
	/**
	 * <p>Code taken from org.eclipse.jdt.astview.EditorUtility.java from ASTView project: 
	 * http://www.eclipse.org/jdt/ui/astview/index.html</p>
	 */
	public static IJavaElement getJavaElementFromEditorInput(IEditorInput editorInput) {
		if (editorInput == null) {
			return null;
		}
		IJavaElement je = JavaUI.getWorkingCopyManager().getWorkingCopy(editorInput); 
		if (je != null)
			return je;
		/*
		 * This needs works, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=120340
		 */
		return (IJavaElement) editorInput.getAdapter(IJavaElement.class);
	}
	
	/**
	 * <p>Constrcts a string that is used as the key for finding the MethodViz in
	 * this.methods.</p>
	 * It will return a String of format:<br>
	 * <b>ownerQualifiedName:methodName(params1,params2,...)</b>
	 * <p>08/07/02 Added isConstructor to rename constructors to <init></p>
	 * @param methodName Name of the method.
	 * @param params Parameter types of the method.
	 * @return A method ID.
	 */
	public static String constructMethodID(String owner, String methodName, String[] params, boolean isConstructor) {
		if (methodName == null || methodName.length() < 1) {
			return null;
		}
		String name = null;
		if (isConstructor) {
			methodName = "<init>";
		}
		if (owner == null || owner.length() < 1) {
			name = methodName + "(";
		}
		else {
			name = owner + ":" + methodName + "(";
		}
		if (params != null && params.length > 0) {
			for (String type : params) {
				name += type + ",";
			}
			name = name.substring(0, name.length() - 1);
		}
		name += ")";
		return name;
	}
}
