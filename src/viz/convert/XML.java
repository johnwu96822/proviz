package viz.convert;

/**
 * Defines the labels of tags and attributes that occur in an XML document for  the specifications of 
 * visualizations in SoftViz.
 * @author  Copyright ©2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii @Manoa
 * @version  1.0.0
 */
public enum XML {

  mainTag          ("ProViz"),
  vizDefinitionTag ("viz-def"),
  typeTag          ("type"),
  fieldTag         ("field"),
  methodTag        ("method"),
  paramTag         ("param"),
  variableTag      ("var"),
  vizTag           ("viz"),
  idAttribute      ("id"),
  vizRefAttribute  ("viz-ref"),
  refAttribute	   ("ref"),
  vcAttribute      ("vc"),
  nameAttribute    ("name"),
  vizAttribute     ("viz"),
  typeAttribute    ("type"),
  returnTag		   	 ("return"),
  classTag		   	 ("class"),
  listeningTag     ("listenTo"),
  
  xLoc						 ("x"),
  yLoc						 ("y");
  
  /** The label of the tag or attribute. */
  private final String label;
  
  /** Creates a new label of the tag or attribute.
    * @param label of the tag or attribute */
  XML (String label)  {this.label = label;}
  
  /** Returns the label of the tag or attribute.
    * @return the label of the tag or attribute */
  public String label () {return label;}
  
  /** Returns the label of the tag or attribute.
    * @return the label of the tag or attribute */
  public String toString () {return label;}

}