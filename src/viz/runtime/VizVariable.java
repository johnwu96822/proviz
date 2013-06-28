package viz.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import viz.ProViz;
import viz.painters.IValueChangedListener;

/**
 * Models a runtime variable in ProViz. Variables form tree structures where
 * the root of a variable tree is a local variable.
 * @author JW
 */
public class VizVariable implements IVizVariable, Serializable {//extends AbstractEventGenerator {
	//Never null, and will not be recorded, so it's transient.
	private transient List<IValueChangedListener> listeners = new ArrayList<IValueChangedListener>();
	/////////////////////////////////////////////////////////////////////////
	//Group 1: unchangeable values
	private String name;
	private String type;
	private boolean isStatic = false;
	private transient VizStackFrame stackFrame = null;
	//All local sync2_varMap and parameters have parentVariable as null
	private transient IVizVariable parentVariable = null;
	
	/////////////////////////////////////////////////////////////////////////
	//Group 2: mutable values. Need to be updated correctly in VizRuntime
	//uniqueObjectID is null for primitive data types, "-1" for null value
	private String uniqueObjectID = null;
	private String valueAsString;
	private String actualType;
	//Won't be null after processed in EclipseVizRuntimeAdapter
	private List<IVizVariable> sync1_varList = null;
	//private Map<String, IVizVariable> sync2_varMap = new Hashtable<String, IVizVariable>();

	public VizVariable(IVizVariable parent) {
		this.parentVariable = parent;
	}
	
	public VizVariable(String name, String typeName, String value, String actualType, 
			String objectID, VizStackFrame parentStack, IVizVariable parent, boolean isStatic) {
		this.name = name;
		this.type = typeName;
		this.valueAsString = value;
		this.actualType = actualType;
		this.uniqueObjectID = objectID;
		this.stackFrame = parentStack;
		this.parentVariable = parent;
		this.isStatic = isStatic;
	}
	
	
	/**
	 * Gets the stack frame where this variable belongs to. If this variable is a local
	 * variable, it is contained directly in the stack frame; if this variable is a field,
	 * the stack frame is the one that contains its root (local) variable.
	 * @return The stack frame that contains this variable or the root variable of this variable 
	 */
	@Override
	public VizStackFrame getStackFrame() {
		return stackFrame;
	}

	/**
	 * Sets the stack frame where this variable belongs.
	 * @param stackFrame The stackFrame to set
	 */
	public void setStackFrame(VizStackFrame stackFrame) {
		this.stackFrame = stackFrame;
	}

	/**
	 * Checks if this variable is static.
	 * @return the isStatic
	 */
	@Override
	public boolean isStatic() {
		return isStatic;
	}
	/**
	 * @param isStatic the isStatic to set
	 */
	protected void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * @param name the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}
	/**
	 * @param type the type to set
	 */
	protected void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the value as a string.
	 * @return the valueAsString
	 */
	@Override
	public String getValueAsString() {
		return valueAsString;
	}
	
	/**
	 * Gets the actual type of the data contained in this variable. Whereas getType()
	 * returns the type of the variable (which could be of any parent type).
	 * @return A class or a type that is the actual type of the value.
	 * @see getType()
	 */
	@Override
	public String getActualType() {
		return actualType;
	}
	
	/**
	 * Gets the fields of this variable as a list. The list could be empty but <b>should never
	 * be null</b>.
	 * @return The list of fields, which should NOT be null
	 */
	@Override
	public List<IVizVariable> getFields() {
		return this.sync1_varList;
	}
	
	/**
	 * Checks if this variable has any field.
	 * @return true if this variable has fields; false otherwise (the list of fields is empty)
	 */
	@Override
	public boolean hasField() {
		return !this.sync1_varList.isEmpty();
	}
	
	/**
	 * Gets the unique object ID of the value of this variable, which should be a positive number
	 * for object variables, "-1" for null values, and null for primitive variables.
	 * @return A positive number (as string) for object variables; "-1" for null values, and "null"
	 * for primitive variables.
	 */
	@Override
	public String getUniqueObjectID() {
		return uniqueObjectID;
	}
	
	/**
	 * Tests if this variable is an object variable. In other words, is it NOT a primitive
	 * variable. A null variable is still an object variable.
	 * @return true if this variable is object variable or has null value; false if it is
	 * a primitive painter.
	 */
	@Override
	public boolean isObject() {
		return uniqueObjectID != null;
	}
	
	/**
	 * Checks if this variable is null value (unique object ID is "-1").
	 * Primitive variables can't be null, so they will return false.
	 * @return true if this variable is null value; false otherwise
	 */
	@Override
	public boolean isNull() {
		return uniqueObjectID != null && uniqueObjectID.equals(NULL_VAL);
	}
	
	/**
	 * Checks if this variable is primitive (unique object ID is 'null')
	 * @return true if this variable is primitive; false otherwise
	 */
	@Override
	public boolean isPrimitive() {
		return uniqueObjectID == null;
	}
	
	/**
	 * Gets the name of this variable.
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the declared type of this variable. But the actual value could have a different
	 * type as returned by getActualType().
	 * @return The declared type of this variable.
	 * @see getActualType()
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param indent
	 * @return
	 */
	public String toString(String indent) {
		String s = indent;
		if (this.isStatic) {
			s += "(s) ";
		}
		s += type + " " + name + " = " + this.getValueAsString() + " -> ID: " + this.uniqueObjectID + "\n";
		for (IVizVariable vv : this.sync1_varList) {
			s += ((VizVariable) vv).toString(indent + "  ");
		}
		return s;
	}

	/**
	 * @param valueAsString the valueAsString to set
	 */
	protected void setValueAsString(String valueAsString) {
		this.valueAsString = valueAsString;
	}
	/**
	 * @param actualType the actualType to set
	 */
	protected void setActualType(String actualType) {
		this.actualType = actualType;
	}
	/**
	 * @param uniqueObjectID the uniqueObjectID to set
	 */
	protected void setUniqueObjectID(String objectID) {
		this.uniqueObjectID = objectID;
	}
	/**
	 * @param sync2_varMap the sync2_varMap to set
	 */
	protected void setFields(List<IVizVariable> variables) {
		if (variables == null) {
			ProViz.errprintln("Cannot set the fields of a VizVariable to null");
			return;
		}
		this.sync1_varList = variables;
		//this.sync2_varMap.clear();
		for (IVizVariable field : variables) {
			((VizVariable) field).setParent(this);
			//this.sync2_varMap.put(field.getName(), field);
		}
	}
	
	/**
	 * Gets a field by its name. Returns null if no field has such a name.
	 * @param name The name of the field.
	 * @return A VizVariable. null if no field has such a name
	 */
	@Override
	public IVizVariable getField(String name) {
		for (IVizVariable field : this.sync1_varList) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
		//return this.sync2_varMap.get(name);
	}
	
	/**
	 * Gets the parent of this variable. If this variable is a local variable, its parent is
	 * 'null' because it is located directly on the stack frame; otherwise all field variables
	 * should have a non-null parent.
	 * @return the parent if this variable is a field; 'null' if this variable is a local variable
	 */
	@Override
	public IVizVariable getParent() {
		return parentVariable;
	}
	
	/**
	 * @param parent the parent to set
	 */
	public void setParent(IVizVariable parent) {
		this.parentVariable = parent;
	}
	
	/**
	 * Checks to see if this variable is a local variable.
	 * @return true if it is local; false if it is a field.
	 */
	@Override
	public boolean isLocalVariable() {
		return this.parentVariable == null;
	}
	
	//*********************************************************
	
	/**
	 * Adds the listener to this variable. If the listener is already registered
	 * with this variable, then do nothing.
	 * @param listener The listener that wishes to listen to the changes of this variable.
	 */
	@Override
	public void addListener(IValueChangedListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IValueChangedListener>();
			listeners.add(listener);
		}
		else {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}
	
	/**
	 * Removes the listener from this variable.
	 * @param listener The listener to be removed.
	 * @return true if the listener is removed; false if the listener wasn't registered with
	 * this variable and hence cannot be removed.
	 */
	@Override
	public boolean removeListener(IValueChangedListener listener) {
		if (listeners != null) {
			return listeners.remove(listener);
		}
		return false;
	}
	
	/**
	 * Removes all listeners registered with this variable and also removes this variable
	 * from those listeners.
	 */
	@Override
	public void removeAllListeners() {
		if (this.listeners != null) {
			for (IValueChangedListener listener : this.listeners) {
				listener.removeEventGenerator(this);
			}
			//this.listeners.clear();
			this.listeners = null;
		}
	}
	
	/**
	 * Fires the give change event on all listeners registered with this variable.
	 * @param change As defined in viz.runtime.Change
	 */
	@Override
	public void fireEvent(Change change) {
		if (listeners != null) {
			for (IValueChangedListener listener : listeners.toArray(new IValueChangedListener[0])) {
				listener.systemHandleChange(change, this);
			}
		}
	}
	
	/**
	 * @return the listeners
	 */
	protected List<IValueChangedListener> getListeners() {
		return listeners;
	}


	/**
	 * @param listeners the listeners to set
	 *
	protected void setListeners(List<IValueChangedListener> listeners) {
		if (listeners != null) {
			this.listeners = listeners;
		}
	}*/
	
	/**
	 * 
	 */
	public void resetListeners() {
		//this.listeners = new ArrayList<IValueChangedListener>();
		this.listeners = null;
	}
}
