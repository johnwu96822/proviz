package viz.io.xml;

/** An XML parsing error.
  * @author Copyright 2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii
  * @version 1.0.0 */
public class XmlException extends Exception {

  /** Creates a new XmlException.
    * @param message about which parsing error occured */
  public XmlException (String message) {
    super (message);
  }

}
