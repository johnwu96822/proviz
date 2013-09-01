package viz.painters.method;

import viz.runtime.Change;
import viz.runtime.IVizVariable;

public class MethodAction {
	private Change change;
	public MethodAction(Change change) {
		this.change = change;
	}
	
	/**
	 * Actions for variable events should override this method with a IVizVariable 
	 * parameter
	 * @param variable
	 */
	public void run(IVizVariable variable) {}
	
	/**
	 * Actions for method events should override this method with a string parameter
	 * @param methodID
	 */
	public void run(String methodID) {}
	
	/**
	 * @return the change
	 */
	public Change getChange() {
		return change;
	}
	/**
	 * @param change the change to set
	 */
	public void setChange(Change change) {
		this.change = change;
	}
}
