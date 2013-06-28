package viz.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import viz.ProViz;
import viz.io.xml.VizToXml;
import viz.model.TypeViz;

import org.eclipse.ui.dialogs.ContainerGenerator;
/**
 * @author John
 * Created on Nov 13, 2006
 */
public class VizSaveUtil {
	/**
	 * Used by VizAnnotationExplorer to save a Viz XML file. It uses Java IO to do
	 * the saving instead of the Eclipse API because Eclipse resources are locked
	 * during the event notification.
	 * @param javaFile
	 * @param types
	 */
	public static void doSaveVizFromJavaFile(IFile javaFile, TypeViz[] types) {
		if (javaFile == null || types == null || types.length == 0) {
			return;
		}
		if (javaFile.getFileExtension().equalsIgnoreCase("java")) {
			IProject project = javaFile.getProject();
			if (project != null) {
				IFolder xmlFolder = project.getFolder("viz_xml");
	//TODO Eclipse resource API is case-sensitive, so problem checking the existence
	//of files/folders on Windows (case-insensitive)
				File folder = xmlFolder.getRawLocation().toFile();
				if (!folder.exists()) {
					System.out.println("Creating folder: " + xmlFolder.getRawLocation().toOSString());
					ProViz.println("Creating folder: " + xmlFolder.getRawLocation().toOSString());
					folder.mkdir();
				} //end if
				System.out.println(javaFile.getProjectRelativePath().toOSString());
				ProViz.println(javaFile.getProjectRelativePath().toOSString());
				String xmlPath = VizSaveUtil.getFilePathInXmlExt(javaFile.getProjectRelativePath().toOSString());
				if (xmlPath == null) {
					return;
				}
	//TODO In Eclipse projects, source codes are usually stored in the src\ folder.
				int index = xmlPath.indexOf("src\\");
				if (index == 0) {
					xmlPath = xmlPath.substring(index + 4);
				}
				xmlPath = xmlPath.replace('\\', '.');
				
				try {
					String text = VizToXml.vizToXmlString(types);
					PrintStream ps = new PrintStream(new FileOutputStream(new File(xmlFolder.getLocation() + "/" + xmlPath)));
					ps.print(text);
					ps.close();
				} catch (IOException e1) {
					ProViz.errprintln(e1);
				} //end catch
			} //end if
			else {
		//If project == null, project is the workspace.
				
			}
		}
		else {
			ProViz.println("Only Java files for this method.");
			System.out.println("Only Java files for this method.");
		}
/*				String xmlPath = VizSaver.getFilePathInXmlExt(javaFile.getName());
		IFile file = xmlFolder.getFile(xmlPath);
		if (!file.exists()) {
			try {
				file.create(null, true, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		*/
			  /*
				ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());
				try {
					file.setContents(inputStream, true, false, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}*/
	}
	
	public static String getFilePathInXmlExt(String path) {
		String rv = null;
		int i = path.lastIndexOf('.');
		if (i > 0 && i < path.length() - 1) {
			rv = path.substring(0, i) + ".xml";
		}
		return rv;
	}

	/**
	 * @param filePath
	 * @param typeList
	 */
	public static void save(IPath path, TypeViz[] typeList) {
		if (path == null || typeList == null || typeList.length < 1) {
			return;
		}
		System.out.println(path.toOSString());
		ProViz.println(path.toOSString());
	//Create the folder if it does not yet exist
		IPath folderPath = path.removeLastSegments(1);
		ContainerGenerator generator = new ContainerGenerator(folderPath);
		try {
			generator.generateContainer(null);
		} catch (CoreException e) {
			ProViz.errprintln(e);
			e.printStackTrace();
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFile(path);
	//Write Vizes to the file
		try {
		  String text = VizToXml.vizToXmlString(typeList);
			if (file.exists()) {
				file.setContents(new ByteArrayInputStream(text.getBytes()), true, false, null);
			}
			else {
				file.create(new ByteArrayInputStream(text.getBytes()), true, null);
			}
		}
		catch (Exception e) {
			ProViz.errprintln(e);
		}
	}
}
/*			//Loops through each TypeViz and saves it.
for (Object obj : objs) {
	if (!(obj instanceof TypeViz) || !Vizes.vizes().contains((TypeViz) obj)) {
		continue;
	}
	TypeViz temp = (TypeViz) obj;
	String name = temp.name() + ".xml";

//This is to fix the filename if its case is incorrect in case-insensitive systems like Windows.
	File f = new File(xmlFolder.getLocation().toOSString() + "/" + name);
	if (f.exists()) {
		f.renameTo(f);
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		xmlFolder = project.getFolder(XML_FOLDER);
	}
	IFile file = xmlFolder.getFile(name);
	try {
		if (file.exists()) {
			file.setContents(new ByteArrayInputStream(temp.toString().getBytes()), true, false, null);
		}
		else {
			file.create(new ByteArrayInputStream(temp.toString().getBytes()), true, null);
		}
	} catch (CoreException e) {
		e.printStackTrace();
	}

	try {
		PrintStream ps = new PrintStream(new FileOutputStream(new File(xmlFolder.getLocation() + "/" + name)));
		ps.print(temp.toString());
		ps.close();
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}
*/			