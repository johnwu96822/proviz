package viz.runtime;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class VizStackFrame implements Serializable {
	private String methodID;
	//syncX variables must be synchronized. Changes on one of them must be reflected on the other.
	//This map and list contain the same variables; the list acts as a stack; the map
	//provides faster access in queries.
	//private Map<String, IVizVariable> sync1_varMap = new Hashtable<String, IVizVariable>();
	private List<IVizVariable> sync2_varList = new LinkedList<IVizVariable>();
	
	public VizStackFrame(String methodID) {
		this.methodID = methodID;
	}
	
	/**
	 * Gets the VizVariable in this stack frame based on its simple name.
	 * @param name Simple name of a variable
	 * @return null if the name is not found.
	 */
	public IVizVariable getVariable(String name) {
		for (IVizVariable var : sync2_varList) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
		//return this.sync1_varMap.get(name);
	}

	/**
	 * @return the methodID
	 */
	public String getMethodID() {
		return methodID;
	}
	
	@Override
	public String toString() {
		String s = "****************************\n";
		s += methodID + ":\n";
		for (IVizVariable var : getVariables()) {
			s += ((VizVariable) var).toString("  ");
		}
		return s + "\n****************************\n";
	}

	/**
	 * @return the sync2_varList
	 */
	public List<IVizVariable> getVariables() {
		//return this.sync1_varMap.values();
		return sync2_varList;
	}

	/**
	 * Resets the contents of this stack frame to a new list of VizVariable's.
	 * @param newVars To be the new contents of this stack frame.
	 */
	public void updateAllVariables(List<IVizVariable> newVars) {
		sync2_varList.clear();
		//sync1_varMap.clear();
		for (int i = 0; i < newVars.size(); i++) {
			sync2_varList.add(newVars.get(i));
			//sync1_varMap.put(newVars.get(i).getName(), newVars.get(i));
		}
	}
}
