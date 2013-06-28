package viz.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import viz.ProViz;

/**
 * @author John
 * Created on Mar 6, 2006, 2006
 */
public class TypeViz extends Association {
	private Map<String, FieldViz> fields;
	private Map<String, MethodViz> methods;
	private Map<String, TypeViz> innerTypes;
  
	//Indicates whether this TypeViz is used as actual types.
  private boolean isForActualTypes = false;
  
  private boolean isSystemType = false;
  
	public TypeViz(String fullPathName, VariableVizBase viz, boolean isForActualTypes) {
		super(fullPathName, viz);
		this.isForActualTypes = isForActualTypes;
		initialize();
	}
	
	public TypeViz(String fullPathName, Association parent) {
		super(fullPathName, parent);
		initialize();
	}
	
	public void initialize() {
		this.innerTypes = new TreeMap<String, TypeViz>();
		this.fields = new TreeMap<String, FieldViz>();
		this.methods = new TreeMap<String, MethodViz>();
	}

	public void addFieldViz(FieldViz fv) {
		if (fv != null) {
			this.fields.put(fv.getFullName(), fv);
		}
	}
	
	/**
	 * @return the isSystemType
	 */
	public boolean isSystemType() {
		return isSystemType;
	}

	/**
	 * @param isSystemType the isSystemType to set
	 */
	public void setSystemType(boolean isSystemType) {
		this.isSystemType = isSystemType;
	}

	/**
	 * Adds a MethodViz
	 * @param mv
	 */
	public void addMethodViz(MethodViz mv) {
		if (mv != null) {
			this.methods.put(mv.getFullName(), mv);
		}
	}
	
	/**
	 * Adds an inner TypeViz
	 * @param tv
	 */
	public void addInnerTypeViz(TypeViz tv) {
		if (tv != null) {
			this.innerTypes.put(tv.getFullName(), tv);
		}
	}
	
	
	/**
	 * Removes an inner TypeViz while setting its parent to null.
	 * @param tv
	 * @return
	 */
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
	
	/**
	 * Removes a FieldViz while setting its parent to null.
	 * @param fv
	 * @return
	 */
	public boolean removeFieldViz(FieldViz fv) {
		boolean rv = false;
		if (fv != null) {
			if (fields.remove(fv.getFullName()) != null) {
				fv.setParent(null);
				rv = true;
			}
		}
		return rv;
	}

	/**
	 * Removes a MethodViz while setting its parent to null.
	 * @param mv
	 * @return
	 */
	public boolean removeMethodViz(MethodViz mv) {
		boolean rv = false;
		if (mv != null) {
			if (methods.remove(mv.getFullName()) != null) {
				mv.setParent(null);
				rv = true;
			}
		}
		return rv;
	}
	
	public void print(String indent) {
		ProViz.println(indent + this.getSimpleName() + " - " + getVisualizationString());
		if (indent.equals("")) {
			indent = new String("  ");
		}
		else {
			indent += "  ";
		}
		for (FieldViz fv : this.getFieldVizes()) {
			fv.print(indent);
		}
		for (MethodViz mv : this.getMethodVizes()) {
			mv.print(indent);
		}
		for (TypeViz tv : this.getInnerTypeVizes()) {
			tv.print(indent);
		}
	}
	
	/**
	 * @return Returns the fieldVizes.
	 */
	public Collection<FieldViz> getFieldVizes() {
		return fields.values();
	}

	/**
	 * @return Returns the innerTypeVizes.
	 */
	public Collection<TypeViz> getInnerTypeVizes() {
		return innerTypes.values();
	}

	/**
	 * @return Returns the methodVizes.
	 */
	public Collection<MethodViz> getMethodVizes() {
		return methods.values();
	}

	/**
	 * @return Returns the isForActualTypes.
	 */
	public boolean isForActualTypes() {
		return isForActualTypes;
	}
	
	/**
	 * @param fullName
	 * @return The FieldViz with specified name if this TypeViz contains it; null otherwise. 
	 */
	public FieldViz getFieldViz(String fullName) {
		return this.fields.get(fullName);
	}
	/*
	public boolean hasChildren() {
		return !(fields.isEmpty() && methods.isEmpty() && innerTypes.isEmpty());
	}*/
}
