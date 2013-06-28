package viz.model;

/**
 * This singleton model is the Viz Map Model used for runtime look up.
 * @author John
 * Created on Jan 30, 2007
 */
public class VizMapModel extends VizMapStorage {
  /** The singleton. */
  private static VizMapModel theInstance = null;

  /** Creates a new singleton Vizes. Declared private to prevent instantiation. */
  private VizMapModel () {}
  
  /** 
   * Returns the singleton Vizes.
   * @return the singleton Vizes
   */
  public static VizMapModel getInstance () {
  	if (theInstance == null) {
  		theInstance = new VizMapModel();
  	}
  	return theInstance;
  }
}
