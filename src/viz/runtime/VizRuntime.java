package viz.runtime;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import viz.ProViz;
import viz.model.VizMapModel;
import viz.painters.IValueChangedListener;

/**
 * VizRuntime contains the runtime data of program execution. 
 * @author John
 */
public class VizRuntime implements IMethodStateReactor {
/*	//Variables are identified by methods in which they are contained.
	private Map<String, IVariable> changedLocalVariables = new Hashtable<String, IVariable>();
	
	//Fields are identified by classes in which they are contained.
	private Map<String, IVariable> changedFields = new Hashtable<String, IVariable>();
*/	
	//private static VizRuntime theInstance;
	
	public static final String STRING = "java.lang.String";

	//Top of the stack is the last element in the vector
	private Stack<VizStackFrame> stackFrames = new Stack<VizStackFrame>();
	private int programState = OTHER;
	
	//public final static String NULL_ID = "-1";
	
	//These three lists are only used in same method step.
	private List<ChangedVariable> changedVars = new ArrayList<ChangedVariable>();
	private List<IVizVariable> newVariables = new ArrayList<IVizVariable>();
	private List<IVizVariable> removedVars = new ArrayList<IVizVariable>();
	
	private Map<String, List<IVizVariable>> uniqueObjectTable = new Hashtable<String, List<IVizVariable>>();
	
	
	public VizRuntime() {}
	
	/**
	 * @return
	 *
	public static VizRuntime getInstance() {
		if (theInstance == null) {
			theInstance = new VizRuntime();
		}
		return theInstance;
	}
	*/
	/**
	 * Refreshes all stack frames in Viz Runtime to the given stack frames. This method is called
	 * when irregular cases in the execution occur, where Viz Monitor would not be able to keep track
	 * off the execution, so it calls this method to refresh Viz Runtime to the latest state.
	 * The top stack frame for the parameter is at index 0.
	 * @param The current state of the program as stack frames. The top stack frame in ths parameter 
	 * should be a index 0 (vFrames[0]).
	 */
	public void refreshAllStackFrames(VizStackFrame[] newFrames) {
	//Refresh changed stack frames only
		/*int oldSize = this.stackFrames.size();
		int dif = 0;
		while (dif < oldSize && dif < newFrames.length) {
	//TODO Weak checking by merely the method's ID. Can improve?
			if (!stackFrames.get(dif).getMethodID().equals(newFrames[newFrames.length - 1 - dif].getMethodID())) {
				break;
			}
			dif++;
		}
	//Deallocate stack frames from top to diff
		for (int i = oldSize - 1; i >= dif; i--) {
			try {
				this.returnFromMethod(stackFrames.peek().getMethodID(), null);
			} catch (MethodDetectionFailException e) {
				ProViz.errprintln(e);
			}
		}
		for (int i = dif; i < newFrames.length; i++) {
			this.newMethod(newFrames[i], null, true);
		}
		ProViz.println("VizRuntime: Refreshing all");*/
		// Commented out 12/28/2010 for changing the refresh to retain unchanged stack frames
		this.clearAll();
		ProViz.println("VizRuntime: Refreshing all");
		for (int i = newFrames.length - 1; i >= 0; i--) {
			newMethod(newFrames[i], null, true);
		}
		//*/
	}
	
	/* The given newFrame will be added to the stack of stack frames in VizRuntime.
	 * @see viz.runtime.IMethodStateReactor#newMethod(viz.runtime.VizStackFrame)
	 */
	public void newMethod(VizStackFrame newFrame, IVizVariable[] varsInPrevStackFrame, boolean isRefresh) {
		programState = NEW_METHOD;
		//for (VizVariable var : vFrame.getVariables()) {
		//	this.copyListeners(var);
		//}
	//Update the previous stack frame in the case of method call and something else are in the same line.
	//EX: int i = 0; methodCall();
		if (varsInPrevStackFrame != null && !isRefresh) {		
	//TODO Validate: Process any possible one-line statement affecting aliasing vars in previous stack frames
			this.sameMethod(varsInPrevStackFrame);
			ProViz.getVPM().sameMethod(null);
		}
		for (IVizVariable var : newFrame.getVariables()) {
			this.addToUniqueObjectTable(var);
		}
		if (newFrame != null) {
			stackFrames.push(newFrame);
		}
	}
	
	/* The parameter, 'previous', is not used in VizRuntime.
	 * @see viz.runtime.IMethodStateReactor#returnFromMethod(java.lang.String)
	 */
	@Override
	public VizStackFrame returnFromMethod(String previousMethodID, VizStackFrame previous)//, final VizVariable[] topVars) 
			throws MethodDetectionFailException {
		//int previousProgramState = programState;
		programState = RETURN_FROM_METHOD;
		this.changedVars.clear();
		VizStackFrame rv = null;
		if (VizMapModel.getInstance().findMethodViz_runtime(previousMethodID) == null) {
			//System.out.println("Returning from a library method. Do nothing");
	//Previous method is not in the Viz model, so no stack was created for it.
	//Do nothing
		}
		else {
			try {
				if (stackFrames.peek().getMethodID().equals(previousMethodID)) {
	//TODO process the previous top stack frame? Cannot, because cannot get the previous top stack frame
					rv = stackFrames.pop();
					for (IVizVariable var : rv.getVariables()) {
						if (this.removeFromUniqueObjectTable(var)) {
							//System.out.println("VizRuntime: removed");
						}
					}
				} //end if
				else {
					ProViz.errprintln(previousMethodID + " <VizRuntime> " + stackFrames.peek().getMethodID());
					throw new MethodDetectionFailException("VizRuntime: Method ID not matching the previous one!");
				}
			}
			catch (EmptyStackException e) {
				throw new MethodDetectionFailException("VizRuntime: Stack is already empty!");
			}
		}
		return rv;
	}
	
	/**
	 * TODO this method needs more work
	 * Synchronizes variable trees in other stack frames to the given variable tree
	 * @param field A field variable that is changed and needs to synchronize other alias fields
	 * @param event
	 */
	private void synchronizeFields(VizVariable field, Change event) {
		IVizVariable parent = field.getParent();
	//var must be a field
		if (parent != null) {
			List<IVizVariable> list = this.uniqueObjectTable.get(parent.getUniqueObjectID());
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
//				for (VizVariable aliasParent : list) {
					IVizVariable aliasParent = list.get(i);
	//The purpose of synchronizeFields is to synchronize fields in previous stack frames.
	//The current stack frame is refreshed, so no need to synchronize variables in current stack frame.
	/*              parent            aliasParent
	 *                |                    |
	 *              field      ->     updateField
	 *                |                    |
	 *               ...       ->         ...
	 */
					if (aliasParent.getStackFrame() != parent.getStackFrame()) {
						VizVariable updateField = (VizVariable) aliasParent.getField(field.getName());
						if (updateField == null) {
							continue;
						}
						ChangedVariable chVar = new ChangedVariable(updateField, event, updateField.getUniqueObjectID(), 
								updateField.getActualType(), updateField.getFields(),	updateField.getListeners());
						updateField.setValueAsString(field.getValueAsString());
						if (field.isObject()) {
							this.removeFromUniqueObjectTable(updateField);
							updateField.setActualType(field.getActualType());
							updateField.setUniqueObjectID(field.getUniqueObjectID());
	//As always, a changed reference IVizVariable will have brand new fields IVizVariables
	//The old fields should be deallocated by VPM handling the change
							//TODO 11.1.2010 If updateVar has no field, it should be the cutoff for circular references
							boolean isCircular = false;
							//if (!updateField.hasField()) {
								while (aliasParent != null) {
	//If circular pointer, do not se the field
									if (aliasParent.getUniqueObjectID().equals(updateField.getUniqueObjectID())) {
										isCircular = true;
										updateField.setFields(new LinkedList<IVizVariable>());
										break;
									}
									aliasParent = aliasParent.getParent();
								//}
							}
							if (!isCircular) {
								//if (field.hasField()) {
								updateField.setFields(this.duplicate(field.getFields(), updateField));
								//}
							}
	//10.05.23 - Add to the table AFTER fields are set
							this.addToUniqueObjectTable(updateField);
						}			
						//ProViz.println("Synchronize fields add changed variable: " + updateVar.getName() 
						//		+ " " + updateVar + " - " + updateVar.getStackFrame().getMethodID());
	//TODO Adding these to changed variables?!?
						this.changedVars.add(chVar);
						//System.out.println("Changed add: " + aliasParent.getParentStack().getMethodID() 
						//+ " - " + aliasParent.getName());
					}
				}
			}					
		}
	}
	
	/**
	 * Duplicates a list of field variables that can be used to update changed variables
	 * in previous stack frames.
	 * @param fields
	 * @param parent
	 * @return
	 */
	private List<IVizVariable> duplicate(List<IVizVariable> fields, IVizVariable parent) {
		List<IVizVariable> rv = new LinkedList<IVizVariable>();
	//TODO update listeners
		for (IVizVariable var : fields) {
			VizVariable newVar = new VizVariable(var.getName(), var.getType(), 
					var.getValueAsString(),	var.getActualType(), var.getUniqueObjectID(), 
					parent.getStackFrame(), parent, var.isStatic());
			if (var.isObject()) {
	//Checks if the new variable constitutes the circular pointer. If so, set its fields to
	//empty.
				boolean isCircular = false;
				IVizVariable parent2 = parent.getParent();
				while (parent2 != null) {
					if (parent2.getUniqueObjectID().equals(newVar.getUniqueObjectID())) {
						isCircular = true;
						break;
					}
					parent2 = parent2.getParent();
				}
				if (isCircular) {
					newVar.setFields(new LinkedList<IVizVariable>());
				}
				else {
					newVar.setFields(duplicate(var.getFields(), newVar));
				}
			}
			else {
				newVar.setFields(new LinkedList<IVizVariable>());
			}
			rv.add(newVar);
		}
		return rv;
	}
	
	/**
	 * TODO Write a paragraph explaining this
	 * This method checks a variable to see whether there are changes to the values of this variable 
	 * and updates it if there are changes, and it continues recursively with fields of the variable.
	 * 
	 * It works on four cases based on the type of variables. The first case is only for primitive
	 * variables, the 2nd and 3rd cases are for the involvement of null value, and the last case is
	 * for changes for non-null reference variables.
	 * 1. A primitive variable can only have its 'value' changed. So simply updates its 'valueAsString'
	 * 2. A variable is going to be set to null
	 * 3. A null variable is set to an object
	 * 4. A (non-null) variable is assigned to another object (non-null)
	 * 
	 * A primitive type variable could only have its value changed; an object variable could have its
	 * string value and its object ID changed.
	 * Pre-condition: Prior to entering this method, newVar and OldVar should represent the same variable,
	 * i.e. have the same name.
	 * Any change detection stops at the first level node of change. In other words, if a variable is
	 * reassigned to another object, the changed variable is the variable, the detection stops at this
	 * level and does not go into the fields of the variable.
	 * @param newVar
	 * @param currentVar
	 * @param changedVars
	 * @throws Exception 
	 */
	private void checkAndUpdateChangedVariables(VizVariable newVar, VizVariable currentVar) throws Exception {
		if (newVar.isPrimitive() != currentVar.isPrimitive() //|| !newVar.getType().equals(currentVar.getType()) //10.06.21 commented out for problem with uninitialized user-type fields 
				|| !newVar.getName().equals(currentVar.getName())) {
			throw new Exception("Unmatched variables" + newVar.getName() + " | " + currentVar.getName());
		}
		Change event = Change.NONE;
	//VALUE_CHANGED for primitive and String variables
		if (currentVar.isPrimitive()) {// || currentVar.getType().equals(STRING)) {
			if (!newVar.getValueAsString().equals(currentVar.getValueAsString())) {
				event = Change.VALUE_CHANGED;
	//Updates the original variable's value
				currentVar.setValueAsString(newVar.getValueAsString());
	//Update the ID for String
				currentVar.setUniqueObjectID(newVar.getUniqueObjectID());
				ChangedVariable chVar = new ChangedVariable(currentVar, event, null, 
						currentVar.getType(), null, currentVar.getListeners());
				changedVars.add(chVar);
				this.synchronizeFields(currentVar, event);
			}
			return;
		}
		else if (newVar.isNull()) {
	//The variable is going to be set to null
			if (!currentVar.isNull()) {
				event = Change.TO_NULL;
				List<IVizVariable> oldFields = currentVar.getFields();
				String previousActualType = currentVar.getActualType();
				String previousID = currentVar.getUniqueObjectID();
				this.removeFromUniqueObjectTable(currentVar);
				currentVar.setActualType(newVar.getActualType());
				currentVar.setUniqueObjectID(newVar.getUniqueObjectID());
				currentVar.setValueAsString(newVar.getValueAsString());
				currentVar.setFields(newVar.getFields());
	//No need to add 'null' variable to unique object table
				ChangedVariable chVar = new ChangedVariable(currentVar, event, previousID, 
						previousActualType, oldFields, currentVar.getListeners());
				changedVars.add(chVar);
				this.synchronizeFields(currentVar, event);
			}
			return;
		}
	//newVar is not null
		else if (currentVar.isNull()){
	//From null to an object
			event = Change.NULL_TO_OBJ;
			String previousActualType = currentVar.getActualType();
	//10.06.21 setType() added to handle the problem with uninitialized user-type fields 
			currentVar.setType(newVar.getType());
			
			currentVar.setActualType(newVar.getActualType());
			currentVar.setUniqueObjectID(newVar.getUniqueObjectID());
			currentVar.setValueAsString(newVar.getValueAsString());
			currentVar.setFields(newVar.getFields());
			this.addToUniqueObjectTable(currentVar);
			ChangedVariable chVar = new ChangedVariable(currentVar, event, IVizVariable.NULL_VAL, 
					previousActualType, null, currentVar.getListeners());
			changedVars.add(chVar);
			this.synchronizeFields(currentVar, event);
			return;
		}
	//Value strings of objects are their ID, so if objects have string value changed, meaning their IDs are changed
		//boolean changed = false;
		List<IVizVariable> newFields = newVar.getFields();
		List<IVizVariable> oldFields = currentVar.getFields();
		List<IValueChangedListener> listeners = currentVar.getListeners();
		String previousID = currentVar.getUniqueObjectID();
		String actualType = currentVar.getActualType();
	//For an reference variable, if its object ID didn't change, nothing else would change
	//except for its fields. So just recursively check its fields afterwards
		if (!newVar.getUniqueObjectID().equals(previousID)) {
	//Object ID is changed, meaning the variable is assigned a new object
			//changed = true;
			this.removeFromUniqueObjectTable(currentVar);
			currentVar.setValueAsString(newVar.getValueAsString());
			currentVar.setUniqueObjectID(newVar.getUniqueObjectID());
			currentVar.setFields(newFields);
			this.addToUniqueObjectTable(currentVar);
	//When the actual type is not changed, the structure of the fields should be unchanged as well. So proceed with updating the fields 
			if (actualType.equals(newVar.getActualType())) {
				if (newFields.size() != oldFields.size()) {
	//May happen to arrays...
					event = Change.DIFF_OBJECT_SAME_TYPE_DIFF_FIELD_SIZE;
				} //end if
				else {
	//Proceed normally
					event = Change.DIFF_OBJECT_SAME_TYPE;
				} //end else
			} //end if
			else {
		//Actual type is changed, the variable is assigned to a different subclass
				event = Change.DIFF_OBJECT_DIFF_TYPE;
				currentVar.setActualType(newVar.getActualType());
			} //end else
		} //end if
	//TODO 10/20/10 prev/circular link crapping out; Temporary fix for a bug
		else if (newFields.size() != oldFields.size()) {
			currentVar.setValueAsString(newVar.getValueAsString());
			//currentVar.setUniqueObjectID(newVar.getUniqueObjectID());
			currentVar.setFields(newFields);
			currentVar.setActualType(newVar.getActualType());
			event = Change.DIFF_OBJECT_SAME_TYPE;
		}
		if (event != Change.NONE) {//changed) {
			ChangedVariable chVar = new ChangedVariable(currentVar, event, previousID, actualType, 
					oldFields, listeners);
			changedVars.add(chVar);
			this.synchronizeFields(currentVar, event);
			//System.out.println("VizRuntime changed variable: " + newVar.getName() + "; event: " + event);
			return;
		}
	//No change. Process the fields
		if ((oldFields.size() > 0)) {
			//ProViz.println(oldFields.size() + " -- " + newFields.size());
			for (int i = 0; i < oldFields.size(); i++) {
				this.checkAndUpdateChangedVariables((VizVariable) newFields.get(i), (VizVariable) oldFields.get(i));
			}
		}
	}

	/* Is it possible that a variable is allocated and deleted in in one step?
	 * X is new variable:
	 * Normal:
	 * O O O O
	 * O O O O X
	 * 
	 * O O O O
	 * O O O
	 * 
	 * Abnormal:
	 * O O O O
	 * O O O X
	 * 
	 * O O O O
	 * O O O X X
	 * 
	 * O O O O
	 * O O X
	 * 
	 * 
	 * 
	 * @see viz.runtime.IMethodStateReactor#sameMethod(viz.runtime.VizVariable[])
	 */
	public void sameMethod(IVizVariable[] newVars) {
		this.programState = SAME_METHOD;
/*		VizStackFrame frame = stackFrames.peek();
		frame.updateAllVariables(newVars);
*/	
		this.clearChangedLists();
		List<IVizVariable> topVars = stackFrames.peek().getVariables();
		//Once an unmatched variable is found, the rest must be newly created or have been removed
		if (newVars.length > topVars.size()) {
			int i = 0;
			while (i < newVars.length) {
				if (i < topVars.size()) {
					if (newVars[i].getName().equals(topVars.get(i).getName())) {
						try {
							checkAndUpdateChangedVariables((VizVariable) newVars[i], (VizVariable) topVars.get(i));
						} catch (Exception e) {
							ProViz.errprintln("VizRuntime: error in checking updating vars 1");
							ProViz.errprintln(e);
							break;
						}
					}
					else {
						//The first unmatch variables found at index i
						break;
					}
				}
				else {
					//New variables starting from index i
					break;
				}
				i++;
			} //end while
			int j = i;
			if (j < topVars.size()) {
				//Rarely some variables are de-allocated and more new varaibles are created in one step
				//System.err.println("VizRuntime: Should never come here 1!!!");
				while (j < topVars.size()) {
					removedVars.add(topVars.get(j));
					j++;
				}
	//Remove all variables at and after i
				for (int k = topVars.size() - 1; k >= i; k--) {
					this.removeFromUniqueObjectTable(topVars.remove(k));
				}
			}
			while (i < newVars.length) {
				//System.out.println("VizRuntime: Adding: " + newVars[i].getName());
				this.addToUniqueObjectTable(newVars[i]);
				topVars.add(newVars[i]);
				newVariables.add(newVars[i]);
				i++;
			}
		}
		else {//if (newVars.length < oldVars.length) {
			int i = 0;
			while (i < topVars.size()) {
				if (i < newVars.length) {
					if (newVars[i].getName().equals(topVars.get(i).getName())) {
						try {
							checkAndUpdateChangedVariables((VizVariable) newVars[i], (VizVariable) topVars.get(i));
						} catch (Exception e) {
							ProViz.errprintln("VizRuntime: error in checking updating vars 2");
							ProViz.errprintln(e);
							break;
						}
					}
					else {
						//The first unmatch variables found at index i
						break;
					}
				}
				else {
					//Old variables that are removed starting from index i
					break;
				}
				i++;
			}
			/*if (topVars.size() == newVars.length) {
				System.out.println("VizRuntime: Stack size: " + newVars.length + " & the first unmatch var: " + i);
			}*/
			int j = i;
			while (j < topVars.size()) {
				//System.out.println("VizRuntime: Removing: " + topVars.get(j).getName());
				removedVars.add(topVars.get(j));
				j++;
			}
			for (int k = topVars.size() - 1; k >= i; k--) {
				this.removeFromUniqueObjectTable(topVars.remove(k));
			}			
			while (i < newVars.length) {
				//Should not come here
				//Rarely some variables are popped while there are new variables
				//System.err.println("VizRuntime: Should never come here 2!!!");
				this.addToUniqueObjectTable(newVars[i]);
				topVars.add(newVars[i]);
				newVariables.add(newVars[i]);
				i++;
			}
		}
	}
	
	/**
	 * Recursively adds a variable tree to the unique object table.
	 * @param var
	 */
	private void addToUniqueObjectTable(IVizVariable var) {
		if (var == null || var.isPrimitive() || var.isNull()) {
			return;
		}
		List<IVizVariable> list = this.uniqueObjectTable.get(var.getUniqueObjectID());
		if (list == null) {
			list = new LinkedList<IVizVariable>();
			list.add(var);
			this.uniqueObjectTable.put(var.getUniqueObjectID(), list);
		}
		else {
	//The variable's ID is already an entry in uniqueObjectTable.
	//Check if the variable is already in the table
			if (list.contains(var)) {
				ProViz.errprintln("Trying to the same variable twice: " + var.getName());
			}
			else {
				list.add(var);
			}
		}
		for (IVizVariable field : var.getFields()) {
			addToUniqueObjectTable(field);
		}
	}
	
	/**
	 * Recursively remove an entire variable tree from the unique object table.
	 * @param var
	 * @return
	 */
	private boolean removeFromUniqueObjectTable(IVizVariable var) {
		if (var == null || var.isPrimitive() || var.isNull()) {
			return false;
		}
		List<IVizVariable> list = this.uniqueObjectTable.get(var.getUniqueObjectID());
		boolean rv = false;
		if (list != null) {
			rv = list.remove(var);
			if (list.isEmpty()) {
				this.uniqueObjectTable.remove(var.getUniqueObjectID());
			}
		}
		for (IVizVariable field : var.getFields()) {
			removeFromUniqueObjectTable(field);
		}
		return rv;
	}

	/**
	 * Gets the state of the program defined in IMethodStateReactor
	 * @return the programState
	 */
	public int getProgramState() {
		return programState;
	}
	
	public void printStackFrames() {
		for (VizStackFrame vFrame : stackFrames) {
			ProViz.println("VizRuntime: " + vFrame.getMethodID());// + " " + vFrame.getVariables().length);
		}
	}

	/**
	 * @return the stackFrames
	 */
	public Stack<VizStackFrame> getStackFrames() {
		return stackFrames;
	}
	
	/**
	 * Clears everything in Viz Runtime
	 */
	public void clearAll() {
		//System.out.println("Clearing VizRuntime");
		stackFrames.clear();
		programState = OTHER;
		clearChangedLists();
		this.uniqueObjectTable.clear();
	}

	/**
	 * 
	 */
	public void clearChangedLists() {
		this.changedVars.clear();
		this.newVariables.clear();
		this.removedVars.clear();
	}
	
	/**
	 * Gets the current top stack frame, i.e. the current executing method.
	 * @return The top stack frame. null if there is no stack frame.
	 */
	public VizStackFrame getTopStackFrame() {
		VizStackFrame rv = null;
		if (!stackFrames.isEmpty()) {
			rv = stackFrames.peek();
		}
		return rv;
	}
	
	public VizStackFrame getPreviousStackFrame() {
		VizStackFrame rv = null;
		if (!stackFrames.isEmpty() && stackFrames.size() > 1) {
			rv = stackFrames.get(stackFrames.size() - 2);
		}
		return rv;
	}
	
	/**
	 * Gets the list of changed variables, which can be an empty list but would not be null.
	 * This list is for processing in VPM in the same method case.
	 * @return A list of changed variables (not null)
	 */
	public List<ChangedVariable> getChangedVars() {
		return changedVars;
	}

	/**
	 * @return the newVariables
	 */
	public List<IVizVariable> getNewVariables() {
		return newVariables;
	}

	/**
	 * @return the removedVars
	 */
	public List<IVizVariable> getRemovedVars() {
		return removedVars;
	}

}
