package viz.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import viz.ProViz;

/**
 * @author John
 * Created on Mar 6, 2006, 2006
 */
public class MethodViz extends Association {
	private Map<String, TypeViz> innerTypes;
	private Map<String, VariableViz> vars;
	
	public MethodViz(String fullPathName, Association parent) {
		super(fullPathName, parent);
		initialize();
	}
	
	public void initialize() {
		this.innerTypes = new HashMap<String, TypeViz>();
		this.vars = new HashMap<String, VariableViz>();
	}
	
	public void addInnerTypeViz(TypeViz tv) {
		if (tv != null) {
			this.innerTypes.put(tv.getFullName(), tv);
		}
	}
	
	public void addVariableViz(VariableViz vv) {
		if (vv != null) {
			this.vars.put(vv.getFullName(), vv);
		}
	}
	
	public boolean removeInnerTypeViz(TypeViz tv) {
		boolean rv = false;
		if (tv != null) {
			if (innerTypes.remove(tv.getFullName()) != null) {
				tv.setParent(null);
				rv = true;
			}
		}
		return rv;
	}

	
	public boolean removeVariableViz(VariableViz vv) {
		boolean rv = false;
		if (vv != null) {
			if (vars.remove(vv.getFullName()) != null) {
				vv.setParent(null);
				rv = true;
			}
		}
		return rv;
	}

	public void print(String indent) {
		ProViz.println(indent + this.getSimpleName() + " - " + getVisualizationString());
		indent += "  ";
		for (VariableViz vv : this.getVariableVizes()) {
			vv.print(indent);
		}
		for (TypeViz tv : this.getInnerTypeVizes()) {
			tv.print(indent);
		}
	}
	
	/**
	 * @return Returns the types.
	 */
	public Collection<TypeViz> getInnerTypeVizes() {
		return innerTypes.values();
	}

	/**
	 * @return Returns the vars.
	 */
	public Collection<VariableViz> getVariableVizes() {
		return vars.values();
	}

	/* (non-Javadoc)
	 * @see viz.model2.Association#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return getFullName().substring(getFullName().lastIndexOf(':') + 1);
	}
	
	public boolean hasChildren() {
		return !(vars.isEmpty() && innerTypes.isEmpty());
	}
	
	public VariableViz getVariableViz(String vID) {
		return this.vars.get(vID);
	}
}
