package viz.io.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import viz.ProViz;
import viz.convert.XML;
import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.ParamViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;

/**
 * <html>
 * <body>
 * Pass a XML file containing the viz information for methods (overloaded or not),  
 * variables within the method, fields within a class and the class itself. The file could  
 * contain the viz information for more than one class.  The XML file should have all 
 * the information within the <soft-viz> tag 
 * 
 * <p>
 * The viz for a class, method, variable, parameter, field is specified
 * <ul>  
 * <li>	using the "viz" attribute or </li> 
 * <li> using the "viz-ref" attribute 
 *          and/or 
 * <li>	using the "viz" tag with either </li> 
 * 	a)	the "ref" attribute <br>
 * 	b)	or the "class" attribute
 * </ul> 
 * </p>
 * 
 * <p>
 * To specify the viz for a class  "type" tag is used followed  by the name of 
 * the class specified using the "name" attribute followed by one of the ways for specifying 
 * the viz. 
 * </p>
 * 
 * <p>
 * To specify the viz for a field "field" tag followed by the name 
 * of the field which uses the "name" attribute followed by one of the ways for specifying the viz.
 * </p>  
 * 
 * <p>
 * To specify the viz for a method "method" tag followed by the name of the method which uses 
 * the "name" attribute followed by one of the ways for specifying the viz.  
 * Within the method visualizations for the variables and parameters could be specified. 
 * </p>
 * 
 * <p>
 * To specify the viz for a variable within the method "var" tag followed by the name of the 
 * variable which uses the "name" attribute followed by one of the ways for specifying the viz. 
 * </p>
 * 
 * <p>
 * To specify the viz for a parameter within the method signature "param" tag followed by the name 
 * attribute and the type attribute which  could be one of the primitive types or any valid Java class.
 * The viz  could be specified by one of the ways for specifying the viz. 
 * </p.>
 * 
 * <p>
 * The <viz-def> tag specifies a macro for a long viz class names. They could be referred within the <viz> tag 
 * while specifying the viz using the "viz-ref" attribute.  If either the field, method or the class
 * specified in the XML does not exist the error is recorded in an error file created and the parsing continues. 
 * The error file name is derived from the input XML file.  
 * </p>
 * 
 * <p>
 * The error file name is<br>
 * Append the input XML file name with "Error" and change the extension of the input XML file (xml)  with "txt"<br>
 * So for e.g input XML file is abc.xml then the error file is abcError.txt <br> When the first error is recorded
 * a boolean is set to indicate that an error exists. If its not set at all then this boolean is read to remove the
 * empty error file. But if the boolean is set then the error file is read and is displayed on the console as well. <br> 
 * </p>
 * 
 * The parsing terminates if a JDOMException (XML file has formatting errors) is encountered. <br>
 * 
 * </body>
 * </html> 
 * 
 * @author  Subbiah, MurugaMullai
 */

public class XMLToViz {

	/**
	 * root of the XML file
	 */
	private Element rootXML; 

	/**
	 * Name of the XML file input.
	 */
//	private String filename;
	/**
	 * Map a definition to the viz class
	 * 
	 */
	private Map<String, String> defnMap;
	
	/**
	 * File to record the error
	 */
	private File errorRecordFile;
	
	/**
	 * Writer to write on a file
	 */
//	private PrintWriter errorWriter;
	
	/**
	 * Number the lines while writing the error
	 */
	private int errorLineNumber;

	/**
	 * Boolean to indicate an error exists and the error file is written.
	 */
	private boolean errorExists = false;
	
	private static ArrayList<TypeViz> roots = new ArrayList<TypeViz>();

	/**
	 * Loads the Viz-formatted XML file that is opened in the active editor to 
	 * the explorer.
	 * @return True if XML loaded successfully; false otherwise.
	 * @throws VizNotFoundException 
	 * @throws XmlException
	 * @author John Wu
	 */
	/**
	 * Loads the Viz-formatted XML file that is opened in the active editor to 
	 * the explorer.
	 * @param path
	 * @return An array of top-level TypeViz's; null if no Viz definitions are found in the file
	 * @throws XmlException
	 */
	public static TypeViz[] loadVizFromXmlFile(IPath path) throws XmlException {
		//if (path != null && path.getFileExtension().equalsIgnoreCase("xml")) {
		return loadVizFromXmlFile(path.toOSString());
	}
	
	public static TypeViz[] loadVizFromXmlFile(String filename) throws XmlException {
		TypeViz[] rv = null;
		if (filename != null && new File(filename).exists()) {
		  XMLToViz loader = new XMLToViz();
		  loader.readXML(filename.replaceAll(" ", "%20"));
		  loader.analyzeXML(loader.getRootXML());
		  if (!roots.isEmpty()) {
		  	rv = roots.toArray(new TypeViz[0]);
		  }
		} //end if
		return rv;
	}
	
	/**
	 * Return the root of the XML
	 * @return  return the root of the XML file read.
	 */
	public Element getRootXML() {
		return this.rootXML;
	}
	
	/**
	 * Read the XML file given the filename. Also determines the root of the XML file.
	 * 
	 * The following exceptions are thrown and re-directed to XmlException: 
	 * JDOMException	If there is a JDOM parsing error within the given XML file
	 * IOException		If the file cannot be found.
	 * @param filename XML file that has to be analyzed. 
	 */

	public void readXML(String filename) throws XmlException  {
		Document docRead;
		SAXBuilder builder = new SAXBuilder();
		try {
			// Load the file
			docRead = builder.build(filename);
			this.rootXML = docRead.getRootElement();
			if (!rootXML.getName().equalsIgnoreCase(XML.mainTag.label())) {
				throw new XmlException("Not Viz compatible XML file.");
			}
//			this.filename = filename;
//			System.out.println("Root is " + rootXML.toString());
		} catch (Exception e) {
			ProViz.errprintln(e);
			throw new XmlException(e.getMessage());
		}
/*		} catch (JDOMParseException e) {
			throw new XmlException(e.getMessage());
		} catch (JDOMException e) {
			e.printStackTrace(System.err);
			//System.err.println("File " + filename + "cannot be loaded");
			throw new XmlException(e.getMessage());
		} catch (IOException ioe) {
			//System.err.println(filename +" File not found exception");
			throw new XmlException(ioe.getMessage());
		}*/
	}
	
	/**
	 * Given the element get the children and call the appropriate methods for further actions.
	 * Store the viz-def in the defnMap 
	 * Get the class elements specified - type tag in the XML file and find in each
	 * class the viz information using the method analyzeType.
	 * The visualizations found in the XML file are written to the Singleton class
	 * Vizes.
	 *  
	 * @param root	Root of the XML file 
	 */
	public void analyzeXML(Element root) {
		roots.clear();
		defnMap = new HashMap<String, String>();
		
		//Read the XML file for the viz-def tag
		for (Object defnIterate : root.getChildren(XML.vizDefinitionTag.label())) {
			Element defnElement = (Element)defnIterate;
			analyzeDefn(defnElement);
		}
		//Print all the definitions read in the XML
		//printDefn();
		
		//Read the XML file for the type tag
		for (Object typeIterate : root.getChildren(XML.typeTag.label())){
			Element classElement = (Element)typeIterate;
			//Add the vizes found within the class into the Singleton Vizes
			try {
				TypeViz tViz = analyzeType(classElement, null);
				
				//If tViz is null the error has been recorded so continue with the
				//parsing and also if no viz then ignore that as well.
				if (tViz != null) {
  //Modified 12/28/06 by John Wu
					roots.add(tViz);
//					vizes.addOrReplace(tViz);
	//Modified 11/7/06 by John Wu
//					vizes.addTypeToFile(this.filename, tViz);
				}
			} catch (Exception e) {
				ProViz.errprintln(e);
			}
		}
		if (errorExists) {
			//Close the error file
//			errorWriter.flush();
//			errorWriter.close();
			ProViz.println("Writing");
			
			//Print contents of file onto the console
			ProViz.println("Errors in XML file supplied");
			String readChar;
			
			try {
				FileReader input = new FileReader(errorRecordFile);
				BufferedReader reader = new BufferedReader(input);
				
				//Printing onto the console
				for(readChar = reader.readLine(); 
						readChar != null; readChar = reader.readLine()) {
					ProViz.println(readChar);
				}
				input.close();
			} catch (FileNotFoundException e) {
				ProViz.println("File not found" + e.toString());
			} catch (IOException e) {
				ProViz.println("File cannot be read" + e.toString());
			}
		} 
	}
	
	/**
	 * Given the "type" tag within the XML file (which represents a class) this method
	 * reads the visualizations (from the XML file) pertaining to a class and calls appropriate
	 * methods to record the visualizations of its fields and methods. Once parsing is complete
	 * for this class a newly created typeViz is returned which encapsulates the visualizations for
	 * the enitre class.
	 *
	 * @param elmt	The class Element in the XML file 
	 * @return TypeViz return the newly created TypeViz
	 */
	
	public TypeViz analyzeType(Element elmt, Association parent) {
		String classXMLName = elmt.getAttributeValue(XML.nameAttribute.label());
		//System.out.println("class :" + classXMLName);
		TypeViz typeViz = new TypeViz(classXMLName, parent);
		getVisualization(typeViz, elmt);
		this.fillDependingVars(elmt, typeViz);
		
		// Get the field children of the element type which is nothing but the class.
		for (Object fieldList: elmt.getChildren(XML.fieldTag.label())) {
			//System.out.println((Element)fieldIterate);
			Element fieldElement = (Element) fieldList;
			FieldViz fieldViz = field(fieldElement, typeViz);
			//Continue with parsing if the field does not exist  
			//and if no viz is specified. 
			//Otherwise store the fieldViz
			if (fieldViz != null) {
				typeViz.addFieldViz(fieldViz);
			}
		}		
		//Find the method elements within the type - class Element
		for (Object methodList: elmt.getChildren(XML.methodTag.label())) {
			Element methodElement = (Element) methodList;
			MethodViz methodViz = method(methodElement, typeViz);
			//Continue with the parsing if the method does not exist and if a valid method 
			//exists in the XML file but no viz is specified. Otherwise store the
			//methodViz.
			if (methodViz != null) {
				typeViz.addMethodViz(methodViz);
			}
		}
		
		//System.out.println("Method exists");
		
		//Inner class
		for (Object innerList : elmt.getChildren(XML.typeTag.label())) {
			Element innerElement = (Element)innerList;
			TypeViz innerTypeViz = analyzeType(innerElement, typeViz);
			if (innerTypeViz != null) {
				typeViz.addInnerTypeViz(innerTypeViz);
			}
		}
		return typeViz;
	}
	
	/**
	 * Given the field Element and also the class to which it belongs the method returns the 
	 * viz information for the field as a FieldViz.
	 * 
	 * The method attempts to
	 * Check if the field belongs to the class and also if the field can be visualized by the specified viz class 
	 * in the XML file. If so create a fieldviz and return. If the field does not exist within the class or if
	 * the viz class is not found or not appropriate then the error is recorded and the function returns a null.
	 * 
	 * @param fieldElement	The field element that has to be analysed
	 * @param classRef	The class to which the field belongs
	 * @return FieldViz return the newly created FieldViz or null under conditions specified above.
	 */
	
	@SuppressWarnings("unchecked")
	public FieldViz field(Element fieldElement, Association parent) {
		FieldViz fieldViz = null;
		String fieldXMLName = fieldElement.getAttributeValue(XML.nameAttribute.label());
		String fieldType = fieldElement.getAttributeValue(XML.typeAttribute.label());
		if (fieldXMLName != null) {
			fieldViz = new FieldViz(fieldXMLName, parent, fieldType);
			getVisualization(fieldViz, fieldElement);
			//For fields with actual types
			List actualClassList = fieldElement.getChildren(XML.classTag.label());
			if(actualClassList != null) {
				actualTypeVisualization(fieldViz, fieldElement);
			}
			fillDependingVars(fieldElement, fieldViz);
			
	//Gets the custom fields of this field
			for (Object fieldList: fieldElement.getChildren(XML.fieldTag.label())) {
				//System.out.println((Element)fieldIterate);
				Element customField = (Element) fieldList;
				FieldViz fv = field(customField, fieldViz);
				//Continue with parsing if the field does not exist  
				//and if no viz is specified. 
				//Otherwise store the fieldViz
				if (fv != null) {
					fieldViz.addFieldViz(fv);
				}
			}
			
		}
		return fieldViz;
	}
	
	@SuppressWarnings("unchecked")
	public void fillDependingVars(Element varElement, Association var) {
		List list = varElement.getChildren(XML.listeningTag.label());
		for (Object listeningElement : list) {
			if (listeningElement instanceof Element) {
				var.addDependingVariable(((Element) listeningElement).getText());
			}
		}
	}

	/**
	 * Given the method Element and also the class to which it belongs the method returns the 
	 * viz information for the method as a MethodViz.
	 * The method attempts to
	 * Check if the method with parameters, or not and exists within the class. 
	 * Check if the variables exist in the method and also whether they could be visualized 
	 * by the viz class specified. If so, store the visualizations for the method and variables 
	 * in the MethodViz and return.
	 * 
	 * Return value could be null under these circumstances :
	 * If the method does not exist then the error is recorded and null is returned.
	 * If no variables exist and if no viz exists for the method then null is returned. 
	 * 
	 * @param methodElement	The method element to be analyzed
	 * @param classRef	The class to which this method belongs
	 * @return MethodViz return the newly created MethodViz or null under the conditions specified above.
	 *
	 */
	
	@SuppressWarnings("unchecked")
	public MethodViz method(Element methodElement, Association parent) {
		String methodXMLName = methodElement.getAttributeValue(XML.nameAttribute.label());
		MethodViz methodViz = new MethodViz(methodXMLName, parent);
		getVisualization(methodViz, methodElement);
		List paramList = methodElement.getChildren(XML.paramTag.label());

		//Iterate through all the listed parameters to determine the viz classes to visualise the parameter.
		for (Object param : paramList) {
			Element paramElement = (Element) param;
			//Parameter name
			String paramName = paramElement.getAttributeValue(XML.nameAttribute.label());
			String typeName = paramElement.getAttributeValue(XML.typeAttribute.label());
/*			Class type = null;
			try {
				type = Reflection.type(typeName);
				//write either the primitive class or any other type in the paramTypes array.
			} catch (Exception err) {
				recordError("No such primitive or object type exists \"" + typeName + " " + err);
			}*/
			ParamViz paramViz = new ParamViz(paramName, methodViz, typeName);
			getVisualization(paramViz, paramElement);
			//For parameters with actual types
			List actualClassList = paramElement.getChildren(XML.classTag.label());
			if(actualClassList != null) {
				actualTypeVisualization(paramViz, paramElement);
			} 
			
			//Gets the custom fields of this parameter
			for (Object fieldList: paramElement.getChildren(XML.fieldTag.label())) {
				//System.out.println((Element)fieldIterate);
				Element fieldElement = (Element) fieldList;
				FieldViz fieldViz = field(fieldElement, paramViz);
				//Continue with parsing if the field does not exist  
				//and if no viz is specified. 
				//Otherwise store the fieldViz
				if (fieldViz != null) {
					paramViz.addFieldViz(fieldViz);
				}
			}
			
			methodViz.addVariableViz(paramViz);
		}
		//If the method exists in the Class check for the variables
		for (Object varIterate : methodElement.getChildren(XML.variableTag.label())){
			Element varElement = (Element)varIterate;
			//variable name
			String varName = varElement.getAttributeValue(XML.nameAttribute.label());
			String typeName = varElement.getAttributeValue(XML.typeAttribute.label());
			VariableViz varViz = new VariableViz(varName, methodViz, typeName);
			getVisualization(varViz, varElement);
			
			//For variables with actual types
			List actualClassList = varElement.getChildren(XML.classTag.label());
			if(actualClassList != null) {
				actualTypeVisualization(varViz, varElement);
			}
			fillDependingVars(varElement, varViz);
			
	//Gets the custom fields of this local variable
			for (Object fieldList: varElement.getChildren(XML.fieldTag.label())) {
				//System.out.println((Element)fieldIterate);
				Element fieldElement = (Element) fieldList;
				FieldViz fieldViz = field(fieldElement, varViz);
				//Continue with parsing if the field does not exist  
				//and if no viz is specified. 
				//Otherwise store the fieldViz
				if (fieldViz != null) {
					varViz.addFieldViz(fieldViz);
				}
			}
			
			methodViz.addVariableViz(varViz);
		}
		return methodViz;
	}
	
	/**
	 * Given the definition element (given by the viz-def tag in the XML file) extract
	 * the id and the class. With id as the key and class attribute as the value, 
	 * the viz-def is stored in a hashmap.
	 * 
	 * @param defnElement	The element which has to be stored in the hashMap.	
	 */
	
	public void analyzeDefn(Element defnElement) {
		String idAttr = defnElement.getAttributeValue(XML.idAttribute.label());
		String vizClass = defnElement.getAttributeValue(XML.vcAttribute.label());
				
		//Check if the viz class exists. If so create the class Type.
//		if (checkVizClass(vizClass) != null) {
			defnMap.put(idAttr, vizClass);
//		}
		
	}
	
	/**
	 * Print the keys and the values in the definition Map
	 */
	
	@SuppressWarnings("unchecked")
	public void printDefn() {
		//Print the values stored in the map
		Iterator prtIterator = defnMap.keySet().iterator();
		while (prtIterator.hasNext()) {
			Object mapElement = prtIterator.next();
			ProViz.println("Key : " + mapElement.toString() + "   Value:"
					+ defnMap.get(mapElement).toString());
		}
	}

	/**
	 * Get the viz information for a given vizElement
	 * stored in the viz-ref attribute, viz attribute and the viz tag
	 *  
	 *  @param viz			element that needs to be visualized
	 *  @param vizElement	Corresponding element specified in the XML 
	 */
	@SuppressWarnings("unchecked")
	public void getVisualization(Association viz, Element vizElement) {
		//Get the viz children
		List vizIterate = vizElement.getChildren(XML.vizTag.label());
		//Check if the viz class is specified using viz attribute
		String vizClass = vizElement.getAttributeValue(XML.vizAttribute.label());
		//Check if the viz class is specified using viz ref attribute
		String vizRefClass = vizElement.getAttributeValue(XML.vizRefAttribute.label());
		
		//If no viz information then return null
		if (vizIterate.size() == 0 && vizClass == null && vizRefClass == null) {
			viz = null;
			return;
		}
		//Get the viz class name from the class attribute or if viz-def is specified get the key specified for
		//the defnMap and get the class name. If the field cannot be visualized by that viz class record
		//it and go to the next viz class
		for (Object vizClassElement: vizIterate) {
			addVizSpecifiedClass(viz, (Element)vizClassElement);
		}
		//viz class is specified within a viz attribute
		if (vizClass != null) {
			addVizSpecifiedAttribute(viz, vizElement);
		}
		//viz class is specified within a viz-ref attribute
		if (vizRefClass != null) {
			addVizRefSpecifiedAttribute(viz, vizElement );
		}	
	}	
	
	/**
	 * Get the viz when the actual types are specified.
	 * 
	 * @param viz FieldViz, VariableViz where actual types could be specified.
	 * @param vizElement The Corresponding element in the XML file
	 */
	@SuppressWarnings("unchecked")
	public void actualTypeVisualization(VariableVizBase viz, Element vizElement){
		//For fields with actual types
		List actualClassList = vizElement.getChildren(XML.classTag.label());
		
		for(Object actualListElement: actualClassList) {
			String acElemName = ((Element)actualListElement).getAttributeValue(XML.nameAttribute.label());
			TypeViz typeViz = new TypeViz(acElemName, viz, true);
			addVizSpecifiedAttribute(typeViz, vizElement);
			addVizRefSpecifiedAttribute(typeViz, vizElement);
			viz.addActualTypeViz(typeViz);
		}
	}
	
	/**
	 * Determines the viz classes for the given element.
	 * Stores them in the viz association -FieldViz, MethodViz, TypeViz or VariableViz
	 * 
	 * @param viz			element that needs to be visualized
	 * @param vizElement 	The Class which has to be associated with the viz input as a XML element.
	 */
	
	public void addVizSpecifiedClass(Association viz, Element vizElement) {
		String vizClassName = vizElement.getAttributeValue(XML.vcAttribute.label());
		String vizDefClass = vizElement.getAttributeValue(XML.refAttribute.label());
		if (vizClassName != null) {
			Visualization vc = new Visualization(vizClassName);
			String x = vizElement.getAttributeValue(XML.xLoc.label());
			String y = vizElement.getAttributeValue(XML.yLoc.label());
			if (x != null && y != null) {
				vc.setStartingLocation(Integer.parseInt(x), Integer.parseInt(y));
			}
			vc.setParent(viz);
			viz.addVisualization(vc);
		} else if (vizDefClass != null) {
			Visualization vc = new Visualization(defnMap.get(vizDefClass));
			vc.setParent(viz);
			viz.addVisualization(vc);
		}
	}
		
	/**
	 * Associates an association with the specified viz Class. The viz class is
	 * specified with a viz attribute.
	 * 
	 * @param viz 			element that needs to be visualized
	 * @param vizElement 	The viz class to be used for viz.
	 */
	
	public void addVizSpecifiedAttribute(Association viz, Element vizElement) {
		String vizClass = vizElement.getAttributeValue(XML.vizAttribute.label());
		if (vizClass != null) {
			Visualization vc = new Visualization(vizClass);
			vc.setParent(viz);
			viz.addVisualization(vc);
		}
	}
	
	/**
	 * Associates an association with the specified viz Class. The viz class is
	 * specified with a viz-ref attribute.
	 * 
	 * @param viz 			element that needs to be visualized
	 * @param vizElement 	The viz class to be used for viz.
	 */
	
	public void addVizRefSpecifiedAttribute(Association viz, Element vizElement) {
		String vizClass = vizElement.getAttributeValue(XML.vizRefAttribute.label());
		if (vizClass != null) {
			Visualization vc = new Visualization(defnMap.get(vizClass));
			vc.setParent(viz);
			viz.addVisualization(vc);
		}
	}
	
	/**
	 * Records the error message in the error file.
	 * errorLineNumber is local to the error file. Every error is numbered for clarity purpose.
	 * 
	 * @param errorMsg The error message that needs to be recorded.
	 */
	
	public void recordError(String errorMsg) {
//		createErrorFile();
		errorLineNumber++;
//		errorWriter.println(errorLineNumber + "    " + errorMsg + "\n");
	}
}	

