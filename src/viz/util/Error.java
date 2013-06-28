package viz.util;

import viz.ProViz;

/** Error messages printed to the standard error stream.
  * @author Copyright 2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii
  * @version 1.0.0 */
public class Error {
  
  /** Prints the message and the error with its stack trace to the standard 
    * error stream.
    * @param message to print first
    * @param error to print afterwards */
  public static void complain (String message, Exception error) {
  	ProViz.errprintln(message);
    //System.err.println (message);
    error.printStackTrace ();
  }

  /** Prints the message to the standard error stream.
    * @param message to print */
  public static void complain (String message) {
    //System.err.println (message);
  	ProViz.errprintln(message);
  }

}