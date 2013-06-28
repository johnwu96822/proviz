package viz.runtime;

import java.util.List;

import viz.painters.IValueChangedListener;

public class ChangedVariable {
	private IVizVariable variable;
	private Change event = Change.NONE;
	private List<IVizVariable> oldFields = null;
	private String previousID;
	private String previousActualType;
	private List<IValueChangedListener> previousListeners;
	/*
	public static final int VALUE_CHANGED = 0;
	public static final int DIFF_OBJECT_SAME_TYPE = 1;
	public static final int DIFF_OBJECT_SAME_TYPE_DIFF_FIELD_SIZE = 2;
	public static final int DIFF_OBJECT_DIFF_TYPE = 3;
	
	**
	 * Indicating a non-null variable is going to be set to null
	 *
	public static final int TO_NULL = 4;
	
	public static final int NULL_TO_OBJ = 5;*/


	public ChangedVariable(IVizVariable variable, Change event, String previousID,
			String previousActualType, List<IVizVariable> oldFields,
			List<IValueChangedListener> previousListeners) {
		this(variable, previousID, previousActualType, oldFields, previousListeners);
		this.event = event;
	}
	
	public ChangedVariable(IVizVariable variable, String previousID,
			String previousActualType, List<IVizVariable> oldFields,
			List<IValueChangedListener> previousListeners) {
		this.variable = variable;
		this.previousID = previousID;
		this.previousActualType = previousActualType;
		this.oldFields = oldFields;
		this.previousListeners = previousListeners;
	}

	/**
	 * @return the oldFields
	 */
	public List<IVizVariable> getOldFields() {
		return oldFields;
	}

	/**
	 * @return the previousID
	 */
	public String getPreviousID() {
		return previousID;
	}

	/**
	 * @return the previousActualType
	 */
	public String getPreviousActualType() {
		return previousActualType;
	}

	/**
	 * @return the previousListeners
	 */
	public List<IValueChangedListener> getPreviousListeners() {
		return previousListeners;
	}

	/**
	 * @return the variable
	 */
	public IVizVariable getVariable() {
		return variable;
	}

	/**
	 * @return the event
	 */
	public Change getEventType() {
		return event;
	}
}
