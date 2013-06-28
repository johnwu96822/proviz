package viz.runtime;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;

import viz.ProViz;
import viz.model.FieldViz;
import viz.model.VizMapModel;
import viz.util.EclipseResourceUtil;

public class EclipseVizRuntimeAdapter {
	/**
	 * The top stack frame is iFrames[0]
	 * @param iFrames
	 * @return
	 * @throws VizRuntimeException
	 */
	public static VizStackFrame[] convertAllStackFrames(IStackFrame[] iFrames) throws VizRuntimeException {
		if (iFrames == null) {
			return null;
		}
		VizStackFrame[] vFrames = new VizStackFrame[iFrames.length];
		for (int i = 0; i < vFrames.length; i++) {
			if (iFrames[i] instanceof JDIStackFrame) {
				try {
					vFrames[i] = convertStackFrame(getMethodID((JDIStackFrame) iFrames[i]), iFrames[i]);
				} catch (DebugException e) {
					throw new VizRuntimeException("Problem converting stack frame: " + iFrames[i]);
				}		
			}
			else {
				throw new VizRuntimeException("Only works for JDIStackFrame");
			}
		}
		return vFrames;
	}
	
	public static VizStackFrame convertStackFrame(String methodID, IStackFrame frame) 
			throws VizRuntimeException {
		if (methodID == null || frame == null) {
			throw new NullPointerException("Null method ID or stack frame in convertStackFrame");
		}
		IVariable[] vars = null;
		VizStackFrame rv = new VizStackFrame(methodID);
		try {
			vars = frame.getVariables();
		} catch (DebugException e) {
			throw new VizRuntimeException("Problem converting stack frame: " + methodID);
		}
		//Map<String, VizVariable> list = new Hashtable<String, VizVariable>();
		List<IVizVariable> list = new LinkedList<IVizVariable>();
		for (IVariable var : vars) {
			try {
	//Top level calls to convertVariable has 'null' as the parent parameter, since they are 
	//all local variables and global variables are under 'this'
				IVizVariable vvar = convertVariable(var, null, rv);
				list.add(vvar);
			}
			catch (VizRuntimeException e) {
				ProViz.errprintln(e.getMessage() + "\nProblem converting a variable, but process continues: " + var);
			}
		} //end for
		rv.updateAllVariables(list);
		return rv;
	}

	/**
	 * @Added parentStack on 2/26/09. All VizVariables now contain a pointer to the stack frame it's contained in.
	 * @param var
	 * @param parentVariable
	 * @param parentStack 
	 * @return
	 * @throws VizRuntimeException
	 */
	public static IVizVariable convertVariable(IVariable var, IVizVariable parentVariable, VizStackFrame parentStack) 
			throws VizRuntimeException {
		if (var == null) {
			throw new NullPointerException("Null variable in convertVariable(IVariable)");
		}
		VizVariable newVar = new VizVariable(parentVariable);
		try {
			IValue value = var.getValue();
			newVar.setName(var.getName());
			try {
	//This throws exception when fields declared as user's type are null
				newVar.setType(var.getReferenceTypeName());
			}
			catch (DebugException e) {
				//ProViz.errprintln("EclipseVizRuntimeAdapter: var.getReferenceTypeName() does not work!" + vvar.getName());
				newVar.setType(value.getReferenceTypeName());
				//e.printStackTrace();
			}
			newVar.setValueAsString(value.getValueString());
			newVar.setActualType(value.getReferenceTypeName());
			newVar.setUniqueObjectID(getValueUniqueID(value));
			newVar.setStackFrame(parentStack);
			//Map<String, VizVariable> list = new Hashtable<String, VizVariable>();
	//Constructs the fields
			List<IVizVariable> list = new LinkedList<IVizVariable>();
			boolean isCircular = false;
			IVizVariable parent = parentVariable;
	//Detecting circular pointers
			if (newVar.isObject() && !newVar.isNull()) {
				while (parent != null) {
					if (newVar.getUniqueObjectID().equals(parent.getUniqueObjectID())) {
						//System.out.println("Circular pointers");
						isCircular = true;
						break;
					}
					parent = parent.getParent();
				}
			}
			if (!isCircular) {
		//We don't store String's fields. Treating String like primitive type
				if (!filterTypes(newVar.getType())) {//.equals(VizRuntime.STRING) && !newVar.getType().equals("java.util.Scanner")) {
					for (IVariable iVar : value.getVariables()) {
						if (iVar instanceof JDIVariable) {
		//The Eclipse Variable View has a show constant option. Constants are final and statif fields.
		//After testing, it's discovered that these constants have deep hierarchy structure underneath,
		//which is unnecessary. TODO So filter these constants out unless they are included in the Viz model.
							if (!((JDIVariable) iVar).isFinal() || !((JDIVariable) iVar).isStatic()) {
								try {
									list.add(convertVariable(iVar, newVar, parentStack));
								}
								catch (VizRuntimeException e) {
									//Skip the problematic one
									ProViz.errprintln(e);
								}
							}
							else {
								FieldViz fv = VizMapModel.getInstance().findFieldViz_runtime(var.getReferenceTypeName(), iVar.getName());
								if (fv != null) {
									try {
										list.add(convertVariable(iVar, newVar, parentStack));
									}
									catch (VizRuntimeException e) {
										//Skip the problematic one
										ProViz.errprintln(e);
									}
								}
							}
						}
						else {
							System.err.println("EclipseVizRuntimeAdapater: Variable not JDIVariable: " + iVar);
						}
					}
				}
			}
			newVar.setFields(list);
			
			if (var instanceof IJavaModifiers) {
				newVar.setStatic(((IJavaModifiers) var).isStatic());
				/*if (vvar.isStatic()) {
					System.out.println("Static variable:: " + vvar.getName());
				}*/
			}
		} catch (DebugException e) {
			ProViz.errprintln(e);
			e.printStackTrace();
			throw new VizRuntimeException("Problem converting variable: " + var);
		}
		return newVar;
	}
	
	/**
	 * Returns true for unwanted types; false for wanted types
	 * TODO currently filtered out everything from java.io
	 * @param typeName
	 * @return
	 */
	private static boolean filterTypes(String typeName) {
		return typeName.equals(VizRuntime.STRING) || typeName.equals("java.util.Scanner")
		|| typeName.startsWith("java.io.");
	}

	/**
	 * Gets the name and the parameter list from the stack frame and returns the
	 * method ID, constructed by EclipseResourceUtil.constructMethodID()
	 * @param jdiFrame The stack frame for current method.
	 * @return The method ID.
	 * @throws DebugException 
	 */
	@SuppressWarnings("unchecked")
	public static String getMethodID(JDIStackFrame jdiFrame) throws DebugException {
		if (jdiFrame == null) {
			return null;
		}
		String methodID = null;
	//Gets the list of parameter types of current method (stack frame)
		List argList = jdiFrame.getArgumentTypeNames();
		String[] params = null;
		if (!argList.isEmpty()) {
			params = new String[argList.size()];
			for (int i = 0; i < argList.size(); i++) {
				params[i] = argList.get(i).toString();
			} //end for
		} //end if
	//Constructs method ID to be looked up in the Viz model
	//The 'isConstructor' is unnecessary here, so false
		methodID = EclipseResourceUtil.constructMethodID(jdiFrame.getDeclaringTypeName(), 
				jdiFrame.getName(), params, false);
		return methodID;
	}
	
	/**
	 * 
	 * @param value null if value is primitive type; 
	 * @return
	 */
	private static String getValueUniqueID(IValue value) {
		String rv = null;
		if (value instanceof JDIPrimitiveValue) {
			//Primitive data types have 'null' as the ID
		}
		else if (value instanceof JDIArrayValue) {
			JDIArrayValue arrayValue = (JDIArrayValue) value;
			try {
				rv = Long.toString(arrayValue.getUniqueId());
			} catch (DebugException e) {
				ProViz.errprintln(e);
				e.printStackTrace();
			}
		}
		else if (value instanceof JDIObjectValue) {
			JDIObjectValue objValue = (JDIObjectValue) value;
			try {
				rv = Long.toString(objValue.getUniqueId());
			} catch (DebugException e) {
				ProViz.errprintln(e);
				e.getStackTrace();
				e.printStackTrace();
			}
		}
		else {
			System.out.println("***** Unknown value type: " + value);
		}
		return rv;
	}
}
