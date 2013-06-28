package viz.model.runtime;

import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;

public class VizValue {
	private String valueAsString;
	private String actualType;
	
	//<String, VizVariable> "String" is the SIMPLE NAME of a variable, not complete path.
	private List<VizVariable> variables;
	
	//objectID is null for primitive data types
	private String objectID = null;
//	private ArrayList<IValueChangedListener> listeners = new ArrayList<IValueChangedListener>();
	
	public VizValue(String value, String typeName, List<VizVariable> vars, String objectID) throws DebugException {
		valueAsString = value;
		this.actualType = typeName;
		variables = vars;
		this.objectID = objectID;
	}
	/**
	 * @return the valueAsString
	 */
	public String getStringValue() {
		return valueAsString;
	}
	/**
	 * @return the actualType
	 */
	public String getActualType() {
		return actualType;
	}
	public String toString(String indent) {
		String s = indent + "*" + valueAsString + "*\n";// + " <" + actualType + ">\n";
		for (VizVariable var : variables) {
			s += var.toString(indent + "  ") + "\n";
		}
		return s;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 *
	@Override
	public boolean equals(Object obj) {
		boolean rv = true;
		if (obj instanceof VizValue) {
			VizValue vval = (VizValue) obj;
			if (valueAsString.equals(vval.getStringValue()) && actualType.equals(vval.getTypeName())
					&& objectID == vval.getObjectID() && variables.size() == vval.getVariables().size()) {
				for (int i = 0; i < variables.size(); i++) {
					if (!variables.get(i).equals(vval.getVariables().get(i))) {
						rv = false;
						break;
					}
				}
			}
			else {
				rv = false;
			}
		}
		else {
			rv = false;
		}
		return rv;
	}*/
	/**
	 * @return the variables
	 */
	public List<VizVariable> getVariables() {
		return variables;
	}
	
	public boolean hasVariable() {
		return !this.variables.isEmpty();
	}
	
/*	public VizVariable getVariable(String vName) {
		return variables.get(vName);
	}*/
	/**
	 * @return the objectID
	 */
	public String getObjectID() {
		return objectID;
	}
	
	public boolean isObject() {
		return objectID != null;
	}
	/*
	public void addValueChangedListener(IValueChangedListener listener) {
		this.listeners.add(listener);
	}
	
	public boolean removeValueChangedListener(IValueChangedListener listener) {
		return this.listeners.remove(listener);
	}
	
	private void fireValueChangedEvent(VizValue newValue) {
		for (IValueChangedListener listener : listeners) {
			listener.updateToNewValue(newValue);
		}
	}*/
}
