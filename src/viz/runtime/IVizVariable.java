package viz.runtime;

import java.util.List;

import viz.painters.IValueChangedListener;

public interface IVizVariable {
	public static final String THIS_PREFIX = "this.";
	
	public static final String NULL_VAL = "-1";
	/**
	 * Gets the stack frame where this variable belongs to. If this variable is a local
	 * variable, it is contained directly in the stack frame; if this variable is a field,
	 * the stack frame is the one that contains its root (local) variable.
	 * @return The stack frame that contains this variable or the root variable of this variable 
	 */
	public VizStackFrame getStackFrame();
	/**
	 * Checks if this variable is static.
	 * @return the isStatic
	 */
	public boolean isStatic();
	/**
	 * Gets the value as a string.
	 * @return the valueAsString
	 */
	public String getValueAsString();
	/**
	 * Gets the actual type of the data contained in this variable. Whereas getType()
	 * returns the type of the variable (which could be of any parent type).
	 * @return A class or a type that is the actual type of the value.
	 * @see getType()
	 */
	public String getActualType();
	/**
	 * Gets the fields of this variable as a list. The list could be empty but <b>should never
	 * be null</b>.
	 * @return The list of fields, which should NOT be null
	 */
	public List<IVizVariable> getFields();
	
	/**
	 * Checks if this variable has any field.
	 * @return true if this variable has fields; false otherwise (the list of fields is empty)
	 */
	public boolean hasField();
	
	/**
	 * Gets the unique object ID of the value of this variable, which should be a positive number
	 * for object variables, "-1" for null values, and null for primitive variables.
	 * @return A positive number (as string) for object variables; "-1" for null values, and "null"
	 * for primitive variables.
	 */
	public String getUniqueObjectID();
	
	/**
	 * Tests if this variable is an object variable. In other words, it is NOT a primitive
	 * variable. A null variable is still an object variable.
	 * @return true if this variable is object variable or has null value; false if it is
	 * a primitive painter.
	 */
	public boolean isObject();
	
	/**
	 * Checks if this variable is null value (unique object ID is "-1")
	 * @return true if this variable is null value; false otherwise
	 */
	public boolean isNull();
	
	/**
	 * Checks if this variable is primitive (unique object ID is 'null')
	 * @return true if this variable is primitive; false otherwise
	 */
	public boolean isPrimitive();
	
	/**
	 * Gets the name of this variable.
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the declared type of this variable. But the actual value could have a different
	 * type as returned by getActualType().
	 * @return The declared type of this variable.
	 * @see getActualType()
	 */
	public String getType();

	/**
	 * Gets a field by its name. Returns null if no field has such a name.
	 * @param name The name of the field.
	 * @return A VizVariable. null if no field has such a name
	 */
	public IVizVariable getField(String name);
	
	/**
	 * Gets the parent of this variable. If this variable is a local variable, its parent is
	 * 'null' because it is located directly on the stack frame; otherwise all field variables
	 * should have a non-null parent.
	 * @return the parent if this variable is a field; 'null' if this variable is a local variable
	 */
	public IVizVariable getParent();
	
	/**
	 * Checks to see if this variable is a local variable.
	 * @return true if it is local; false if it is a field.
	 */
	public boolean isLocalVariable();
	
	//*********************************************************
	
	/**
	 * Adds the listener to this variable. If the listener is already registered
	 * with this variable, then do nothing.
	 * @param listener The listener that wishes to listen to the changes of this variable.
	 */
	public void addListener(IValueChangedListener listener);
	
	/**
	 * Removes the listener from this variable.
	 * @param listener The listener to be removed.
	 * @return true if the listener is removed; false if the listener wasn't registered with
	 * this variable and hence cannot be removed.
	 */
	public boolean removeListener(IValueChangedListener listener);
	
	/**
	 * Removes all listeners registered with this variable and also removes this variable
	 * from those listeners.
	 */
	public void removeAllListeners();
	
	/**
	 * Fires the give change event on all listeners registered with this variable.
	 * @param change As defined in viz.runtime.Change
	 */
	public void fireEvent(Change change);
}
