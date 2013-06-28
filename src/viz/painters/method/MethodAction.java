package viz.painters.method;

import viz.runtime.Change;
import viz.runtime.IVizVariable;

public class MethodAction {
	private Change change;
	public MethodAction(Change change) {
		this.change = change;
	}
	
	public void run(IVizVariable variable) {}
	
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
