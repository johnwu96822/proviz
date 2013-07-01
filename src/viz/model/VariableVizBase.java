package viz.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import viz.ProViz;

/**
 * The base class for variables (VariableViz and ParamViz) and fields (FieldViz). 
 * @author JW
 *
 */
public abstract class VariableVizBase extends Association {
	private String type = null;
	/** The associations for different actual types. */
  private Map<String, TypeViz> actualTypeVizes = null;
  
  private Map<String, FieldViz> fieldVizes = null;
  
  /**
	 * @param fullPathName
	 * @param parent
	 */
	public VariableVizBase(String fullPathName, Association parent, String type) {
		super(fullPathName, parent);
		setType(type);
	}

	public void addFieldViz(FieldViz fv) {
		if (fv != null) {
			if (this.fieldVizes == null) {
				this.fieldVizes = new TreeMap<String, FieldViz>();
			}
			this.fieldVizes.put(fv.getFullName(), fv);
		}
	}
	/**
	 * @param fullName
	 * @return The FieldViz with the specified name if this viz has it; null otherwise. 
	 */
	public FieldViz getFieldViz(String fullName) {
		if (this.fieldVizes == null) {
			return null;
		}
		return this.fieldVizes.get(fullName);
	}

	/**
	 * Removes a FieldViz while setting its parent to null.
	 * @param fv
	 * @return
	 */
	public boolean removeFieldViz(FieldViz fv) {
		boolean rv = false;
		if (this.fieldVizes != null && fv != null) {
			if (fieldVizes.remove(fv.getFullName()) != null) {
				fv.setParent(null);
				rv = true;
			}
		}
		return rv;
	}

	/**
	 * @return Returns the fieldVizes, which can be null.
	 */
	public Collection<FieldViz> getFieldVizes() {
		if (fieldVizes == null) {
			return null;
		}
		return fieldVizes.values();
	}

  /** Adds a viz to the set.
    * @param typeViz for an actual type */
  public void addActualTypeViz (TypeViz typeViz) {
  	if (actualTypeVizes == null) {
  		this.actualTypeVizes = new TreeMap<String, TypeViz>();
  	}
    actualTypeVizes.put(typeViz.getFullName(), typeViz);
  }
  
  /** Returns the viz association for the given type name.
    * Returns null if there is no entry with this name
    * @param name of the actual type
    * @return the viz association for the type. */
  public TypeViz actualTypeViz (String name) {
  	if (this.actualTypeVizes == null) {
  		return null;
  	}
    return actualTypeVizes.get(name);
  }

  /** Returns the XML tags that corresponds to the nested definitions of 
    * the visualizations for the actual types of a variable. 
    * Returns empty string if there are no visualizations of nested elements.
    * @param indent spaces to indent all nested tags
    * @return XML tags corresponding to the nested associations *
  protected String nested (String indent) {
    if (actualTypeVizes == null) {return "";}
    StringBuilder nested = new StringBuilder ();
    for (TypeViz typeViz : actualTypeVizes.values ()) {
      nested.append (typeViz.toString (indent));
    }
    return nested.toString ();
  }
  */
  /**
   * Gets the actual types in a Collection of TypeViz's.
   * @return A Collection of TypeViz's.
   */
  public Collection<TypeViz> getActualTypes() {
  	if (this.actualTypeVizes != null && !actualTypeVizes.isEmpty()) {
  		return this.actualTypeVizes.values();
  	}
  	return null;
  }
  
	public boolean removeActualType(TypeViz tv) {
		boolean rv = false;
		if (this.actualTypeVizes != null && tv != null) {
			if (actualTypeVizes.remove(tv.getFullName()) != null) {
				tv.setParent(null);
				rv = true;
			}
		}
		return rv;
	}

	public void print(String indent) {
		ProViz.println(indent + this.getSimpleName() + " - " + getVisualizationString());
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

}
