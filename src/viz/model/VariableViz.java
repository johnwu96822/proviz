package viz.model;

/** Association between a variable that references an object that
  * can have different actual type, i.e. a instance variable, a parameter
  * or a local variable than the type it was declared with. Maintains 
  * a set of associations that correspond to different actual types.
  * @author Copyright ©2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii @Manoa
  * @version 1.0.0 */
public class VariableViz extends VariableVizBase {

  /**
	 * @param fullPathName
	 * @param parent
	 */
	public VariableViz(String fullPathName, Association parent, String type) {
		super(fullPathName, parent, type);
	}

}