package viz.io.xml;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import viz.convert.XML;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.ParamViz;
import viz.model.TypeViz;
import viz.model.VariableViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;

/**
 * @author John
 * Created on Feb 12, 2007
 */
public class VizToXml {
  public static synchronized String vizToXmlString(TypeViz[] types) throws IOException {
  	if (types == null || types.length < 1) {
  		return null;
  	}
    Element root = new Element(XML.mainTag.label());
    Document document = new Document(root);
    for (TypeViz tv : types) {
    	if (!tv.isSystemType()) {
    		Element tvElement = new Element(XML.typeTag.label());
    		fillTypeElement(tvElement, tv);
    		root.addContent(tvElement);
    	}
    }
	  XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
	  return outputter.outputString(document);
/*	  FileWriter writer = new FileWriter(filePath);
	  outputter.output(document, writer);
	  writer.flush();
	  writer.close();*/
  }    
  
  private static void fillTypeElement(Element tvElement, TypeViz tv) {
  	tvElement.setAttribute(XML.nameAttribute.label(), tv.getFullName());
  	fillVisualizations(tvElement, tv.getVisualizations());
  	fillDependingVariables(tvElement, tv.getDependingVars());
  	for (FieldViz fv : tv.getFieldVizes()) {
  		Element fElement = new Element(XML.fieldTag.label());
  		fillVarElement(fElement, fv);
			if (fv.getFieldVizes() != null) {
				for (FieldViz field : fv.getFieldVizes()) {
		  		Element fieldElement = new Element(XML.fieldTag.label());
		  		fillVarElement(fieldElement, field);
		  		fElement.addContent(fieldElement);
				}
			}
  		tvElement.addContent(fElement);
  	}
  	for (MethodViz mv : tv.getMethodVizes()) {
  		Element mElement = new Element(XML.methodTag.label());
  		fillMethodElement(mElement, mv);
  		tvElement.addContent(mElement);
  	}
		for (TypeViz tViz : tv.getInnerTypeVizes()) {
    	Element tElement = new Element(XML.typeTag.label());
    	fillTypeElement(tElement, tViz);
    	tvElement.addContent(tElement);
		}
  }
  
  /**
	 * @param element
	 * @param mv
	 */
	private static void fillMethodElement(Element mElement, MethodViz mv) {
		mElement.setAttribute(XML.nameAttribute.label(), mv.getFullName());
		fillVisualizations(mElement, mv.getVisualizations());
		for (VariableViz vv : mv.getVariableVizes()) {
			Element vElement;
			if (vv instanceof ParamViz) {
				vElement = new Element(XML.paramTag.label());
			}
			else {
				vElement = new Element(XML.variableTag.label());
			}
			fillVarElement(vElement, vv);
			if (vv.getFieldVizes() != null) {
				for (FieldViz fv : vv.getFieldVizes()) {
		  		Element fElement = new Element(XML.fieldTag.label());
		  		fillVarElement(fElement, fv);
		  		vElement.addContent(fElement);
				}
			}
			mElement.addContent(vElement);
		}
		for (TypeViz tv : mv.getInnerTypeVizes()) {
    	Element tvElement = new Element(XML.typeTag.label());
    	fillTypeElement(tvElement, tv);
    	mElement.addContent(tvElement);
		}
	}

	private static void fillVarElement(Element element, VariableVizBase vv) {
		element.setAttribute(XML.nameAttribute.label(), vv.getFullName());
		if (vv.getType() != null) {
			element.setAttribute(XML.typeAttribute.label(), vv.getType());
		}
  	fillVisualizations(element, vv.getVisualizations());
  	fillDependingVariables(element, vv.getDependingVars());
  }

  private static void fillVisualizations(Element element, Visualization[] vcs) {
  	for (Visualization vc : vcs) {
  		Element vElement = new Element(XML.vizTag.label());
  		vElement.setAttribute(XML.vcAttribute.label(), vc.getPainterName());
  		if (vc.getStartingLocation() != null) {
  			vElement.setAttribute(XML.xLoc.label(), vc.getStartingLocation().x + "");
  			vElement.setAttribute(XML.yLoc.label(), vc.getStartingLocation().y + "");
  		}
  		element.addContent(vElement);
  	}
  }
  private static void fillDependingVariables(Element element, List<String> vars) {
  	for (String var : vars) {
  		Element vElement = new Element(XML.listeningTag.label());
  		vElement.addContent(var);
  		//vElement.setAttribute(XML.variableTag.label(), var);
  		element.addContent(vElement);
  	}
  }
}
