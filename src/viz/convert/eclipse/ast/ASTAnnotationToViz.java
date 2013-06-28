package viz.convert.eclipse.ast;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import viz.ProViz;
import viz.model.TypeViz;
import viz.model.VizMapModel;
import viz.util.EclipseResourceUtil;

/**
 * Uses Eclipse AST to extract Viz annotations and fills it into Viz model.
 * http://www.eclipse.org/jdt/ui/astview/index.html
 * @author John
 * Created on Mar 21, 2006, 2006
 */
public class ASTAnnotationToViz {
//	private static TypeViz[] types = null;
	/**
	 * This method is intend to be used in the Eclipse plug-in environment. In
	 * Eclipse, whichever editor window is opened and active, this will take
	 * the source code in that editor window and parses its Viz annotations
	 * into the Viz model. 
	 */
	public static IPath/*Collection<TypeViz>*/ eclipseActiveEditorFillVizes() {//throws VizNotFoundException {
		//Collection<TypeViz> rv = null;
		IPath rv = null;
		IPath filePath = EclipseResourceUtil.getActiveEditorFilePath();
		if (filePath != null && filePath.getFileExtension().equalsIgnoreCase("java")) {
			IEditorPart part= EclipseResourceUtil.getActiveEditor();
			if (part instanceof ITextEditor) {
				IOpenable openable = EclipseResourceUtil.getJavaInput(part);
				//ProViz.println("ASTAnnotationToViz-eclipseActiveEditorFillVizes(): " + filePath);
				Collection<TypeViz> temp = ASTAnnotationToViz.createVizesFromAST(openable);
				if (temp != null) {
					for (TypeViz tv : temp) {
						VizMapModel.getInstance().addOrReplace(tv, false);
						//Vizes.getInstance().addTypeToFile(filePath.toOSString(), tv);
					}
				}
				rv = filePath;
				/*else {
					throw new VizNotFoundException();
				}*/
			} //end if
		} //end if
		return rv;
	}
	
	/**
	 * Extracts the Viz model from a file within a given editor.
	 * @param part
	 * @return
	 * @throws VizNotFoundException
	 */
	public static TypeViz[] getVizesFromEditor(IEditorPart part) {//throws VizNotFoundException {
		TypeViz[] rv = null;
		if (part != null && part instanceof ITextEditor) {
			IOpenable openable = EclipseResourceUtil.getJavaInput(part);
			//IPath filePath = EclipseResourceUtil.getActiveEditorFilePath();
			//System.out.println("ASTAnnotationToViz-getVizesFromEditor(): " + filePath);
			Collection<TypeViz> roots = ASTAnnotationToViz.createVizesFromAST(openable);
			//if (roots == null || roots.isEmpty()) {
			//	throw new VizNotFoundException();
			//}
			if (roots != null) {
				rv = roots.toArray(new TypeViz[0]);
			}
		}
		return rv;
	}
	
	/**
	 * NOT WORK - This method intends to parse any given Java file, inside or outside
	 * of Eclipse environment. However, it does not work yet because resolve
	 * binding not is working.
	 * @param filename The file path of a Java source code file.
	 * @throws IOException Any IO error.
	 *
	public static void javaFileFillVizes(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			System.err.println(filename + " does not exist!");
			return;
		}
		FileReader reader = new FileReader(file);
		char[] src = new char[(int) file.length()];
		reader.read(src);
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	  parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setProject(null);
		parser.setUnitName(file.getName());
		parser.setSource(src);
		CompilationUnit root = (CompilationUnit) parser.createAST(null);
		System.out.println(root);
		
//		VizAnnotationVisitor visitor = new VizAnnotationVisitor();
		try {
	//Does not work yet because bindings could not be resolved.
//			root.accept(visitor);
//			System.out.println(Vizes.vizes());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	
	/**
	 * Uses an AST visitor to creates the Viz model from an IOpenable.
	 * <p>Code taken from org.eclipse.jdt.astview.views.ASTView.java from ASTView project: 
	 * http://www.eclipse.org/jdt/ui/astview/index.html<br>
	 * Modified by John Wu.</p>
	 * @param input IOpenable.
	 * @return A collection of top-level TypeVizes.
	 */
	public static Collection<TypeViz> createVizesFromAST(IOpenable input) {
		if (input == null) {
			return null;
		}
		ArrayList<TypeViz> rootTypes = null;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	  parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setProject(((IJavaElement) input).getJavaProject());
		if (input instanceof ICompilationUnit) {
			parser.setSource((ICompilationUnit) input);
		} else {
			parser.setSource((IClassFile) input);
		}
		CompilationUnit root = (CompilationUnit) parser.createAST(null);
		VizAnnotationVisitor visitor = new VizAnnotationVisitor();//path.toOSString());
		try {
			root.accept(visitor);
			rootTypes = visitor.getRoots();
		}
		catch (Exception e) {
			ProViz.errprintln(e);
		}
		return rootTypes;
	}
}
