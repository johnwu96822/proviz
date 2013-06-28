package viz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;

import java.io.FileNotFoundException;

public class VizRecorder {
	private ObjectOutputStream oos;
	
	//THE_END cannot be the same as any constants in IMethodStateReactor
	public final static int THE_END = -1;
	
	/**
	 * The parameter, filePath, is the path relative to the workspace root. So it should
	 * be "projectName/.../filename"
	 * @param filePath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public VizRecorder(String filePath) throws FileNotFoundException, IOException {		
		/*IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFile(new Path(filePath));*/
		oos = new ObjectOutputStream(new FileOutputStream(new File(filePath)));//file.getLocation().toOSString())));
		oos.writeObject("ProViz");
	}
	
	public void reset() {
		try {
			oos.reset();
		} catch (IOException e) {
			ProViz.errprintln(e);
		}
	}
	
	public void writeVariable(IVizVariable var) {
		try {
			//System.out.println("Writing VizVariable");
			oos.writeObject(var);
			oos.flush();
		} catch (IOException e) {
			ProViz.errprintln(e);
		}
	}
	
	public void writeStackFrame(VizStackFrame frame) {
		try {
			//System.out.println("Writing VizStackFrame");
			oos.writeObject(frame);
			oos.flush();
		} catch (IOException e) {
			ProViz.errprintln(e);
		}
	}
	
	public void writeObject(Object obj) {
		try {
			//System.out.println("Writing Object");
			oos.writeObject(obj);
			oos.flush();
		} catch (IOException e) {
			ProViz.errprintln(e);
		}
	}

	public void writeInt(int i) {
		try {
			//System.out.println("Writing int: " + i);
			oos.writeInt(i);
		} catch (IOException e) {
			ProViz.errprintln(e);
		}
	}
	
	public void close() {
		try {
			oos.writeInt(THE_END);
			oos.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	/*
	 * name;type;id;actualtype;
	 * @param iVar
	 *
	public String writeVizVariable(IVizVariable iVar, int level) {
		VizVariable var = (VizVariable) iVar;
		StringBuffer sb = new StringBuffer("v" + level + var.getName() + ';' + var.getType() + ';');
		sb.append(var.getUniqueObjectID() + ';' + var.getActualType() + ';');
		sb.append(convertNewLine(var.getValueAsString()) + '\n');
		level++;
		for (IVizVariable field : var.getFields()) {
			sb.append(writeVizVariable(field, level));
		}
		System.out.println(sb);
		return sb.toString();
	}
	
	public String convertNewLine(String str) {
		return str.replaceAll("\n", "\\n&");
	}*/
}
