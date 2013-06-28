package viz.convert;

import java.lang.annotation.*;

import javax.swing.JOptionPane;

import viz.ProViz;
import viz.model.*;
import viz.util.EclipseResourceUtil;

/** Parses the string within an annotation for a variable (field, parameter 
  * or local variable) and fills an association accordingly. The annotation
  * string may contain the specification of separate visualizations for 
  * different actual types as defined in the documentation for the @Viz
  * annotation.
  * <p> Does not check (yet) whether the actual types can be assigned to the 
  * declared type of the variable.
  * @see viz.annotation.Viz
  * @author Copyright Jan Stelovsky, AMI Lab, ICS, University of Hawaii @Manoa
  * @version 1.0.0 */
public class AnnotationParser {
  
  /** The character that separates the main pair of variable and viz names 
    * from the pairs of actual types and their viz names and among these pairs 
    * within the main list of the variable annotation. */
  public static final String varVizPartsSeparator = ";";
  /** The character that separates variable names from the viz names in the 
    * variable annotation. */
  public static final String varsFromVizesSeparator = ":";
  /** The character that separates actual types names from the viz names 
    * in the variable annotation. */
  public static final String typesFromVizesSeparator = ":";
  /** The character that separates names in a list. */
  public static final String listSeparator = ",";
  
  /** Parses an annotation and fills a viz association with 
    * viz classes and with viz for actual types.
    * @param viz the viz association
    * @param annotation for a variable or field possibly including different
    *         visualizations for actual types
    * @throws AnnotationFormatError if annotation is malformed
    */
  public static void annotationToViz (Association viz, String annotation)
      throws AnnotationFormatError {
      //, ClassFormatError, NoSuchMethodException {
  	try {
    	annotation = annotation.trim();
    	if (annotation.length() == 0) {
  //@Viz ("")
    		annotation = "1D";
    	}
    	if (annotation.matches("[(]\\s*\\d+\\s*,\\s*\\d+\\s*[)]")) {
    		annotation = VizMapStorage.DEFAULT_TYPE + annotation;
    	}
  //@Viz ("part1 : part2") <== part2 is for actual types, which are not used
	    //String [] parts = annotation.split (varVizPartsSeparator, 2); //up to 2 strings
	    //fillVizes (viz, parts [0]);
	    fillVizes1(viz, annotation);//parts [0]);
	    //if (parts.length > 1) {fillActualTypes (viz, parts [1]);}
  	}
  	catch (Exception e) {
  		//JOptionPane.showMessageDialog(null, "Bad Viz annotation: " + annotation);
  		//EclipseResourceUtil.showMessageDialog("Bad Viz annotation: " + annotation);
  		ProViz.errprintln(e);
  		throw new AnnotationFormatError("@Viz format error");
  	}
  }

  /** Fills a viz association with viz classes.
    * @param viz the viz association
    * @param vizNamesList list of names of viz classes
    * @throws ClassNotFoundException if a viz class is missing 
    * @throws ClassFormatError if the viz class isn't subclass 
    *         of viz.VizBase
    * @throws NoSuchMethodException if the viz class doesn't
    *         have the correct form of required methods */
  public static void fillVizes (Association viz, String vizNamesList) {
    String [] vizNames = vizNamesList.split (listSeparator);
    for (String vizName : vizNames) {
    	Visualization vc = new Visualization(vizName.trim());
    	vc.setParent(viz);
      viz.addVisualization(vc);
    	//viz.addViz(EncounteredVizes.viz (vizName));
    }
  }
  
  private static void fillVizes1 (Association viz, String vizNamesList) {
  	vizNamesList = vizNamesList.trim();
  	int comma = vizNamesList.indexOf(',');
  	if (comma == -1) {
  //@Viz ("some.painter.ClassName"), nothing fancy
  		Visualization vc = new Visualization(vizNamesList);
			vc.setParent(viz);
		  viz.addVisualization(vc);
		  return;
  	}
  	int openLoc = vizNamesList.indexOf('(');
  	int closeLoc = vizNamesList.indexOf(')');
  	if (comma < openLoc || openLoc == -1) {
  //@Viz ("vizName1,vizName2(...")
  //vizNamesList.substring(0, comma) is vizName1
  		Visualization vc = new Visualization(vizNamesList.substring(0, comma).trim());
			vc.setParent(viz);
		  viz.addVisualization(vc);
		  if (vizNamesList.length() > comma + 1) {
	//Continues parsing the "vizName2(..." part
		  	fillVizes1(viz, vizNamesList.substring(comma + 1));
		  }
  	}
  	else {
  		if (openLoc != -1) {
  //Sets starting location: "vizName1(x,y), ..."
	  		Visualization vc = new Visualization(vizNamesList.substring(0, openLoc).trim());
				vc.setParent(viz);
			  viz.addVisualization(vc);
			  parseLocation(vc, vizNamesList.substring(openLoc + 1, closeLoc));
			  int otherComma = vizNamesList.indexOf(',', closeLoc);
			  if (otherComma != -1) {
			  	fillVizes1(viz, vizNamesList.substring(otherComma + 1));
			  }
  		}
  	}
  }
  
  private static void parseLocation(Visualization vc, String xCommaY) throws AnnotationFormatError{
  	String[] xy = xCommaY.split(",");
  	if (xy.length == 2) {
  		vc.setStartingLocation(Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim()));
  	}
  	else {
  		throw new AnnotationFormatError("Location should be (x, y)");
  	}
  }

  /** Fills a viz association with visualizations for actual types.
    * @param viz the viz association
    * @param actualTypesSpecs specs for actual types and their visualizations
    * @throws AnnotationFormatError if annotation is malformed
    * @throws ClassNotFoundException if a viz class is missing 
    * @throws ClassFormatError if the viz class isn't subclass 
    *         of viz.VizBase
    * @throws NoSuchMethodException if the viz class doesn't
    *         have the correct form of required methods *
  private static void fillActualTypes (VariableVizBase viz, String actualTypesSpecs) throws AnnotationFormatError {//, NoSuchMethodException {
    String [] actualTypesParts = actualTypesSpecs.split (varVizPartsSeparator);
    for (String part : actualTypesParts) {
      String [] typesAndVizes = part.split (typesFromVizesSeparator);
      String typeNamesList = typesAndVizes [0];
      if (typesAndVizes.length == 1) {
        throw new AnnotationFormatError ("\"" + typesFromVizesSeparator
            + "\" missing");
      }
      String [] typeNames = typeNamesList.split (listSeparator);
      String [] vizNamesForTypes = typesAndVizes [1].split (listSeparator);
      for (String typeName : typeNames) {
        TypeViz typeViz = new TypeViz(typeName, viz, true);
      	//TypeViz typeViz = new TypeViz (Reflection.type (typeName), true);
        for (String vizNameForTypes : vizNamesForTypes) {
        	Visualization vc = new Visualization(vizNameForTypes.trim());
        	vc.setParent(typeViz);
        	typeViz.addVisualization(vc);
          //typeViz.addViz (EncounteredVizes.viz (vizNameForTypes));
        }
        viz.addActualTypeViz(typeViz);
      }
    }
  }*/
}