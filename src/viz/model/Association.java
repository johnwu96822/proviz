package viz.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author John
 * Created on Mar 6, 2006, 2006
 */
/**
 * @author JW
 *
 */
public abstract class Association implements Serializable {
	private String fullName = null;
	
	//Never null
	private List<Visualization> vizes = new LinkedList<Visualization>();
	private Association parent = null;
	private int currentViz = DEFAULT;
	protected static final int DEFAULT = 0;

  private List<String> dependingVars = new LinkedList<String>();
  
	public Association(String fullPathName, Association parent) {
		this.setFullName(fullPathName);
		this.setParent(parent);
	}
	
	/**
	 * @return Returns the parent.
	 */
	public Association getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(Association parent) {
		this.parent = parent;
	}
	
	public void addVisualization(Visualization vc) {
		if (!vizes.contains(vc)) {
			this.vizes.add(vc);
			vc.setParent(this);
		}
	}

	/**
	 * @return Returns the visualizationClasses.
	 */
	public Visualization[] getVisualizations() {
		return vizes.toArray(new Visualization[0]);
	}

	/**
	 * @return Returns the simpleName.
	 */
	public String getSimpleName() {
		int i = fullName.lastIndexOf('.');
		if (i == -1) {
			return fullName;
		}
		else {
			return fullName.substring(fullName.lastIndexOf('.') + 1);
		}
	}

	/**
	 * @return Returns the fullPathName.
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullPathName The fullPathName to set.
	 */
	public void setFullName(String fullPathName) {
		this.fullName = fullPathName;

	}
	
	public String toString() {
		return this.getFullName();
	}
	
	public Visualization getDefaultViz() {
		if (vizes.size() > 0) {
			return vizes.get(0);
		}
		return null;
	}
	
	public Visualization getViz(int i) {
		if (vizes.size() >= i) {
			return vizes.get(i);
		}
		return null;
	}
	
  /** Returns the description of the association as an XML tag.
   * <p>Note that the description may span several lines.
   * @return XML tag corresponding to the association
   *
	public String toString() {return toString ("");}

	/** Returns the description of the association as an indented XML tag.
	 * <p>Note that the description may span several lines.
	 * @param indent spaces to indent all tag's lines with
	 * @return XML tag corresponding to the association
	 *
	public void print(String indent) {
	  String tagLabel = tagLabel ();
	  String nameAttribute = " " + XML.nameAttribute + "=\"" + name () + "\"";
	  String vizAttribute = "";
	  String nested = "";
	  if (vizes != null) {
	    if (vizes.size () == 1) {
	      vizAttribute = " " + XML.vizAttribute + "=\"" + viz ().getName () + "\"";
	    } else {
	      for (Class<? extends VizBase> viz : vizes) {
	        String name = viz.getName ();
	        nested += indent + "  <" + XML.vizTag + " " + XML.classAttribute 
	            + "=\"" + name + "\"/>\n";
	      }
	    }
	  }
	  nested += nested (indent + "  ");
	  nested = nested.equals ("") ? "/" : ">\n" + nested + indent + "</" + tagLabel;
	  return indent + "<" + tagLabel + nameAttribute + vizAttribute + nested + ">\n";
	}*/
	/**
	 * Gets the index of a particular viz class.
	 * @param viz The viz class to be found.
	 * @return The index of the viz class, or -1 if it is not 
	 * found in this Association.
	 */
	public int indexOf(Visualization vc) {
		return vizes.indexOf(vc);
	}
	
	public String getVisualizationString() {
		String rv = new String();
		for (Visualization viz : this.vizes) {
			rv += viz.getPainterName() + ", ";
		}
		if (rv.length() > 2) {
			rv = rv.substring(0, rv.length() - 2);
		}
		return rv;
	}
	
	public boolean remove(Visualization vc) {
		boolean rv = false;
		if (vc != null) {
			if (this.vizes.remove(vc)) {
				vc.setParent(null);
				rv = true;
			}
		}
		return rv;
	}
	
	/*---------------------------------------------------------------------
	 Provides ordering operations for viz classes.
	---------------------------------------------------------------------*/
		
	/**
	 * Adds a viz class and sets it as the default viz.
	 * @param vc The viz class to be set as the default.
	 */
	public void setDefaultVisualization(Visualization vc) {
		if (vc != null) {
			int index = this.indexOf(vc);
			if (index != -1) {
				moveToDefault(index);
			}
			else {
				vizes.add(0, vc);
			}
		}
	}
	
	/**
	 * Switches the positions of two viz classes.
	 * @param p1 Index of one viz class this Association has.
	 * @param p2 Index of one viz class this Association has.
	 */
	public void swapPosition(int p1, int p2) {
		if (p1 < 0 || p2 < 0 || p1 >= vizes.size() || p2 >= vizes.size()) {
			throw new IndexOutOfBoundsException();
		}
		if (p1 == p2) {
			return;
		}
		Visualization temp = vizes.get(p1);
		vizes.set(p1, vizes.get(p2));
		vizes.set(p2, temp);
	}
	
	/**
	 * Moves an existing viz class to the default position by its index.
	 * @param index
	 */
	public void moveToDefault(int index) {
		if (index > 0) {
			if (index == 1) {
				swapPosition(0, 1);
			}
			else {
				vizes.add(0, vizes.remove(index));
			}
		}
	}

	/**
	 * Returns the number of VCs in this Association.
	 * @return The number of viz classes.
	 */
	public int getVizCount() {
		return vizes.size();
	}

	/**
	 * Gets the current visualization set previously by setCurrentViz(String).
	 * @return the currentViz
	 */
	public Visualization getCurrentViz() {
		if (!this.vizes.isEmpty()) {
			return this.vizes.get(currentViz);
		}
		return null;
	}

	/**
	 * @param currentViz the currentViz to set
	 *
	public void setIndexOfCurrentViz(int currentViz) {
		if (currentViz >= 0 && currentViz < vizes.size()) {
			this.currentViz = currentViz;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}*/
	
	public boolean setCurrentViz(String painterName) {
		for (int i = 0; i < this.vizes.size(); i++) {
			if (this.vizes.get(i).getPainterName().equals(painterName)) {
				this.currentViz = i;
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a depending variable.
	 * @param var
	 */
	public void addDependingVariable(String var) {
		if (!this.dependingVars.contains(var)) {
			this.dependingVars.add(var);
		}
	}
	
	/**
	 * Removes a depending variable.
	 * @param var
	 * @return true if the variable is removed; false if it does not exist in the list
	 */
	public boolean removeDependingVariable(String var) {
		return this.dependingVars.remove(var);
	}
	
	/**
	 * Clears the list of depending variables.
	 */
	public void clearDependingVars() {
		this.dependingVars.clear();
	}
	
	/**
	 * Gets the list of depending variables.
	 * @return
	 */
	public List<String> getDependingVars() {
		return this.dependingVars;
	}
}
