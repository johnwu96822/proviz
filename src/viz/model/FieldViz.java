package viz.model;

/**
 * @author John
 * Created on Mar 6, 2006, 2006
 */
public class FieldViz extends VariableVizBase {
	private boolean isStatic = false;
	
	/**
	 * @param fullPathName
	 * @param parent
	 */
	public FieldViz(String fullPathName, Association parent, String type) {
		super(fullPathName, parent, type);
	}

	public void print(String indent) {
		System.out.println(indent + this.getSimpleName() + " - " + getVisualizationString());
	}

	/**
	 * @return the isStatic
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * @param isStatic the isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	
}
