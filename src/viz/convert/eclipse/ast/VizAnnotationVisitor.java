package viz.convert.eclipse.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import viz.ProViz;
import viz.convert.AnnotationParser;
import viz.model.*;
import viz.util.EclipseResourceUtil;

/**
 * Visits six types of declaration classes, extracts Viz annotations, and puts them into 
 * Vizes internal storage. The terms 'type' and 'class' are used interchangeably.
 * @author John Wu
 * @version 0.0.4 Created on Mar 8, 2006
 */
public class VizAnnotationVisitor extends ASTVisitor {
	private ArrayList<TypeViz> roots;
	
/* These two maps are the temporary storage for quick accessing previously created
	 parent TypeViz or MethodViz, so that the child viz can be added to it. */
	//'types' is using TypeDeclaration.resolveBinding().getBinaryName() as its key.
	private Map<String, TypeViz> types;
	//'methods' uses a string key created by the 'constructMethodID' method.
	private Map<String, MethodViz> methods;
	
	//The default (currently the only) annotation type this visitor looks for.
	public static final String VIZ_ANNOTATION = "Viz";
	
	public static final String DVIZ_ANNOTATION = "DViz";
	
	//Constructor
	public VizAnnotationVisitor() {//String path) {
		this.types = new HashMap<String, TypeViz>();
		this.methods = new HashMap<String, MethodViz>();
		roots = new ArrayList<TypeViz>();
	}

	/**
	 * Gets the closest owner's ID (key) of the node. The owner could be a class or a method, 
	 * depending on what the parameter node is. If null is returned, it means this node is 
	 * the root node.
	 * @param node The current ASTNode.
	 * @return The ID (key) for the parent method or class; null if this node is the root node.
	 */
	protected ASTNode getParentNode(ASTNode node) {
		ASTNode parent = node;
		while (parent != node.getRoot()) {
			parent = parent.getParent();
			if (parent instanceof TypeDeclaration) {
				return parent;
			}
			if (parent instanceof MethodDeclaration) {
				return parent;
			}
		}
		return null;
	}
	
	/**
	 * Gets the closest owner's ID (key). The owner could be a class or a method, depending on
	 * what the parameter node is. If null is returned, that means this node is the root node.
	 * @param node The current ASTNode.
	 * @return The ID (key) for the parent method or class; null if this node is the root node.
	 * @throws ClassNotFoundException 
	 */
	protected String getParentID(ASTNode node) throws ClassNotFoundException {
		String ownerID = null;
		ASTNode parent = this.getParentNode(node);
	//If the parent is a type, gets its full path name as its ID
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration) parent;
			ownerID = this.getTypeID(td);//td.resolveBinding().getBinaryName();
		}
	//If the parent is a method, then construct its ID
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration md = (MethodDeclaration) parent;
			ASTNode temp = md.getParent();
			while (temp != null && !(temp instanceof TypeDeclaration)) {
				temp = temp.getParent();
			}
			if (temp != null && temp instanceof TypeDeclaration) {
				ownerID = this.getTypeID((TypeDeclaration) temp);
			}
			else {
				ownerID = md.resolveBinding().getDeclaringClass().getBinaryName();
			}
			/*Class[] params = this.getParameterTypes(md.resolveBinding().getParameterTypes());
			String[] sParams = null;
			if (params != null) {
				sParams = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					sParams[i] = params[i].getName();
				}
			}*/
			ownerID = this.getMethodID(ownerID, md.getName().getFullyQualifiedName(), md.resolveBinding().getParameterTypes(), md.isConstructor());
		}
		return ownerID;
	}
	
  /* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		this.addType(node);
		return true;
	}

	/**
	 * <p>Creates a TypeViz from a TypeDeclaration node. If the type has no annotation
	 * declared, then do nothing, unless the 'addNoMatterWhat' parameter forces to create
	 * and store the TypeViz no-matter-what.</p>
	 * <p>When this method is called in the visit(TypeDeclaration), addNoMatterWhat is
	 * false in order to skip if this class has no annotation. But later on, if anything
	 * inside this class has annotation, then this type viz needs to be created in order
	 * to store whatever has annotation inside of it. So addNoMatterWhat is true in this
	 * case. Same applies to addMethod().</p>
	 * 
	 * The parent of a type can be a class or a method.
	 * @param node The type node to be added as a TypeViz.
	 * @param addNoMatterWhat If true, add the TypeViz regardless if it has annotations; 
	 * 				if false, do not add if the method has no annotations.
	 */
	private void addType(TypeDeclaration node) {
		String vizValue = this.extractVizValue(VIZ_ANNOTATION, node.modifiers());
	//If the node is the root node, ownerID and parent will be null
		try {
			String typeName = getTypeID(node);
			TypeViz tv = new TypeViz(typeName, null);//node.resolveBinding().getBinaryName(), null);
			if (vizValue != null) {
	      //AnnotationParser.fillVizes(tv, vizValue);
	      AnnotationParser.annotationToViz(tv, vizValue);
			}
			this.addTypeViz(node, tv);	
		} //end try
		catch (Exception e) {
			ProViz.errprintln(e);
		}
	}

	/**
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getTypeID(TypeDeclaration node) {
		StringBuffer typeName = new StringBuffer(node.resolveBinding().getBinaryName());
		List params = node.typeParameters();
		if (!params.isEmpty()) {
			typeName.append('<');
			for (int i = 0; i < params.size(); i++) {
				Object param = params.get(i);
				if (param instanceof TypeParameter) {
					typeName.append(((TypeParameter) param).getName().getFullyQualifiedName());
					if (i != params.size() - 1) {
						typeName.append(',');
					}
				}
			}
			typeName.append('>');
		}
		return typeName.toString();
	}

	/**
	 * Finds or creates the owner of a TypeViz and adds it to that owner. Only classes with
	 * Viz annotation should use this method, and this filtering is done by addType(). The 
	 * owner could be a type or a method. If the owner did not yet exist in the storage 
	 * before (meaning the owner does not have annotation declared), then creates the owner.
	 * @param node The current TypeDeclaration node.
	 * @param tv The TypeViz to be added.
	 * @param className The ID (class) name of this type.
	 * @param ownerID ID of the owner, could be a method ID or a type ID.
	 */
	private void addTypeViz(TypeDeclaration node, TypeViz tv) {
		String ownerID = null;
		try {
			ownerID = getParentID(node);
		} catch (ClassNotFoundException e) {
			System.err.println("VizAnnotationVisitor:addTypeViz() - " + e);
			ProViz.errprintln("VizAnnotationVisitor:addTypeViz() - " + e);
			return;
		}
		if (ownerID == null) {
	//This node is the root node, so simple add it to the Viz buffer
			roots.add(tv);
		}
		else {
	//This node has parent(s)
			ASTNode owner = this.getParentNode(node);
	//Distinguish whether the parent is a class or a method
			if (owner instanceof TypeDeclaration) {
				TypeViz parentType = this.types.get(ownerID);
				if (parentType == null) {
					System.err.println("Should not come here!!! - (VizAnnotationVisitor: addTypeViz1)");
					ProViz.errprintln("Should not come here!!! - (VizAnnotationVisitor: addTypeViz1)");
	//Parent TYPE wasn't added to storage before, so create and add the parent
					this.addType((TypeDeclaration) owner);
					parentType = this.types.get(ownerID);
					if (parentType == null) {
						System.err.println("NULL type owner for a type");
						ProViz.errprintln("NULL type owner for a type");
						return;
					} //end if
				} //end if
				tv.setParent(parentType);
				parentType.addInnerTypeViz(tv);
			} //end if
			else if (owner instanceof MethodDeclaration) {
				MethodViz parentMethod = this.methods.get(ownerID);
				if (parentMethod == null) {
					System.err.println("Should not come here!!! - (VizAnnotationVisitor: addTypeViz2)");
					ProViz.errprintln("Should not come here!!! - (VizAnnotationVisitor: addTypeViz2)");
	//Parent METHOD wasn't added to storage before, so create and add the parent
					this.addMethod((MethodDeclaration) owner);
					parentMethod = this.methods.get(ownerID);
					if (parentMethod == null) {
						System.err.println("NULL method owner for a type");
						ProViz.errprintln("NULL method owner for a type");
						return;
					} //end if
				} //end if
				tv.setParent(parentMethod);
				parentMethod.addInnerTypeViz(tv);
			} //end else if
		} //end else
		this.types.put(tv.getFullName(), tv);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		this.addFieldViz(node);
		return true;
	} //end visit

	/**
	 * Creates a FieldViz from a FieldDeclaration node. If this field has no annotation
	 * declared, then it is not added. The parent of a field must be a type.
	 * @param node The field node to be added as a FieldViz.
	 */
	private void addFieldViz(FieldDeclaration node) {
	//Checks if the Viz annotation exists.
		String vizValue = this.extractVizValue(VIZ_ANNOTATION, node.modifiers());
		if (vizValue == null) {
			vizValue = this.extractVizValue(DVIZ_ANNOTATION, node.modifiers());
		}
		String ownerID = null;
		try {
			ownerID = getParentID(node);
		} catch (ClassNotFoundException e) {
			ProViz.errprintln("VizAnnotationVisitor:addFieldViz() - " + e);
			return;
		}
	//Gets the TypViz that this field should belong to. Creates the parent if it does
	//not exist in the storage.
		TypeViz parent = this.types.get(ownerID);
		if (parent == null) {
			ProViz.errprintln("Should not come here - VizAnnotationVisitor - addFieldViz");
			ASTNode parentType = this.getParentNode(node);
			if (parentType instanceof TypeDeclaration) {
				this.addType((TypeDeclaration) parentType);
				parent = this.types.get(ownerID);
			}
		}
	//Traverses thru each variable declaration fragment and creates a FieldViz
	//for each, adds it to the owner TypeViz
		for (Object obj : node.fragments()) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
			try {
	//TODO Set if a field is 'static'
		    FieldViz fieldViz = new FieldViz(vdf.getName().getFullyQualifiedName(), 
		    		parent, node.getType().resolveBinding().getName());
		    if (vizValue != null) {
		    	AnnotationParser.annotationToViz(fieldViz, vizValue);
		    }
		    parent.addFieldViz(fieldViz);
			}
			catch (Exception e) {
				ProViz.errprintln(e);
			} //end catch
		} //end for
	}

	/**
	 * Changed to using Strings, so this method is unnecessary.
	 * Converts an ITypeBinding array that represents a method parameter types
	 * to a Class array.
	 * @param tBindings An ITypeBinding array representing Java object types.
	 * @return A Class array representing Java object types.
	 * @throws ClassNotFoundException 
	 *
	@SuppressWarnings({ "unchecked" })
	private Class[] getParameterTypes(ITypeBinding[] tBindings) throws ClassNotFoundException {
		Class[] params = null;
		if (tBindings.length != 0) {
			params = new Class[tBindings.length];
			int index = 0;
			for (ITypeBinding it : tBindings) {
				String type = it.getName();
	//For primitive data types, we need to convert it to Class
	//individually and manually.
				if (type.equals("int")) {
					params[index] = int.class;
				}
				else if (type.equals("float")) {
					params[index] = float.class;
				}
				else if (type.equals("double")) {
					params[index] = double.class;
				}
				else if (type.equals("long")) {
					params[index] = long.class;
				}
				else if (type.equals("short")) {
					params[index] = short.class;
				}
				else if (type.equals("byte")) {
					params[index] = byte.class;
				}
				else if (type.equals("char")) {
					params[index] = char.class;
				}
				else if (type.equals("boolean")) {
					params[index] = boolean.class;
				}
				else {
	//The type binding is not a primitive data type, so it is an object.
	//TODO Buggy here, so switch to no Class involvement
					String typeName = it.getBinaryName();
					it.getQualifiedName();
					params[index] = Class.forName(typeName);
				} //end else
				index++;
			} //end for
		} //end if
		return params;
	}*/
	
	private String getMethodID(String ownerID, String methodName, ITypeBinding[] typeBindings, boolean isConstructor) {
		String[] sParams = new String[typeBindings.length];
		for (int i = 0; i < typeBindings.length; i++) {
			sParams[i] = typeBindings[i].getQualifiedName();
		}
		return EclipseResourceUtil.constructMethodID(ownerID, methodName, sParams, isConstructor);
	}
	

	/**
	 * <p>Creates a MethodViz from a MethodDeclaration node. If the method has no annotation
	 * declared, then do nothing, unless the 'addNoMatterWhat' parameter forces to create
	 * and store the MethodViz no-matter-what.</p>
	 * <p>When this method is called in the visit(MethodDeclaration), addNoMatterWhat is
	 * false in order to skip if this method has no annotation. But later on, if anything
	 * inside this method has annotation, then this method viz needs to be created in order
	 * to store whatever has annotation inside of it. So addNoMatterWhat is true in this
	 * case. Same applies to addType().</p>
	 * The parent of a method can only be a class.
	 * @param node The method node to be added as a MethodViz.
	 * @param addNoMatterWhat If true, add the MethodViz regardless if it has annotations; 
	 * 				if false, do not add if the method has no annotations.
	 */
	private void addMethod(MethodDeclaration node) {
		String vizValue = this.extractVizValue(VIZ_ANNOTATION, node.modifiers());
/*		Class[] params = null;
		try {
			params = this.getParameterTypes(node.resolveBinding().getParameterTypes());
		} catch (ClassNotFoundException e) {
			System.err.println("VizAnnotationVisitor:addMethod() - " + e);
			return;
		}*/
	//The ownerID is the ID of the class that this method belongs to.
		ASTNode temp = node.getParent();
		while (temp != null && !(temp instanceof TypeDeclaration)) {
			temp = temp.getParent();
		}
		String ownerID;
		if (temp != null && temp instanceof TypeDeclaration) {
			ownerID = this.getTypeID((TypeDeclaration) temp);
		}
		else {
			ownerID = node.resolveBinding().getDeclaringClass().getBinaryName();
		}
	//Constructs a method ID to be used as the key for this.methods storage.
		String methodID = node.getName().getFullyQualifiedName();
/*		if (params != null) {
			sParams = new String[params.length];
			for (int i = 0; i < params.length; i++) {
				sParams[i] = params[i].getName();
			}
		}*/
		methodID = this.getMethodID(ownerID, methodID, node.resolveBinding().getParameterTypes(), node.isConstructor());
		TypeViz parentType = this.types.get(ownerID);

		try {
	//Constructs the reflection Method object
	//If the parent type does not exist in this.types storage, then create the whole
	//parent hierarchy by calling addType()
			if (parentType == null) {
				ProViz.errprintln("Should not come here!!! - (VizAnnotationVisitor: addMethod)");
				this.addType((TypeDeclaration) this.getParentNode(node));
				parentType = this.types.get(ownerID);
				if (parentType == null) {
					ProViz.errprintln("NULL owner for a method");
					return;
				}
			}
	//Creates and adds the MethodViz
		  MethodViz mv = new MethodViz(methodID, parentType);
		  if (vizValue != null) {
		  	AnnotationParser.fillVizes(mv, vizValue);
		  }
			parentType.addMethodViz(mv);
			this.methods.put(methodID, mv);
		} //end try
		catch (Exception e) {
			ProViz.errprintln(e);
		} //end catch
	} //end addMethod
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		this.addMethod(node);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
	 */
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		String annotationValue = this.extractVizValue(VIZ_ANNOTATION, node.modifiers());
		if (annotationValue == null) {
			annotationValue = this.extractVizValue(DVIZ_ANNOTATION, node.modifiers());
		}
		String ownerID = null;
		try {
			ownerID = getParentID(node);
		} catch (ClassNotFoundException e) {
			ProViz.errprintln("VizAnnotationVisitor:addTypeViz() - " + e);
			return true;
		}
		MethodViz parent = methods.get(ownerID);
//08.1.3 In new implementation, parent should not be null entering
		if (parent == null) {
			ProViz.errprintln("Should not come here!!! - (VizAnnotationVisitor: SingleVariableDeclaration1)");
		//Parent method viz does not exist in the storage, so create and add the parent method.
			MethodDeclaration md = (MethodDeclaration) this.getParentNode(node);
			this.addMethod(md);
			parent = this.methods.get(ownerID);
			if (parent == null) {
				ProViz.errprintln("NULL owner for a variable");
				return true;
			}
		}
		VariableViz vv = null;
		String typeName = node.getType().resolveBinding().getName();
	//Checks whether this variable declaration is a parameter
		if (node.getParent() instanceof MethodDeclaration) {
			MethodDeclaration md = (MethodDeclaration) node.getParent();
			if (md.parameters().contains(node)) {
				vv = new ParamViz(node.getName().getFullyQualifiedName(), parent, typeName);
			}
			else {
				vv = new VariableViz(node.getName().getFullyQualifiedName(), parent, typeName);
			}
		}
		else {
			vv = new VariableViz(node.getName().getFullyQualifiedName(), parent, typeName);
			//ProViz.errprintln("Other types of statements need to be handled " + node.getParent());
			//return true;
		}
		if (annotationValue != null) {
			try {
				AnnotationParser.annotationToViz(vv, annotationValue);
			}
			catch (Exception e) {
				ProViz.errprintln(e);
				return true;
			}
		}
		parent.addVariableViz(vv);
		return true;
	}
	
	/**
	 * Creates multiple VariableViz's from multiple variable declarations. This is used by
	 * visit(VariableDeclarationExpression) and visit(VariableDeclarationStatement). This
	 * method finds whether 'Viz' annotation type exists. If it does, then goes through
	 * each variable declaration fragment, creates a VariableViz for each of them, and adds
	 * them to the parent method. 
	 * @param node A VariableDeclarationExpression node or a VariableDeclarationStatment node.
	 * @param modifiers The list of modifiers in the node.
	 * @param fragments The list of VariableDeclarationFragment in the node.
	 */
	@SuppressWarnings("unchecked")
	private void addMultipleVariableDeclarations(ASTNode node, Type type, List modifiers, List fragments) {
		String annotationValue = this.extractVizValue(VIZ_ANNOTATION, modifiers);
		if (annotationValue == null) {
			annotationValue = this.extractVizValue(DVIZ_ANNOTATION, modifiers);
		}
	//Gets the parent method viz
		String ownerID = null;
		try {
			ownerID = getParentID(node);
		} catch (ClassNotFoundException e) {
			System.err.println("VizAnnotationVisitor:addTypeViz() - " + e);
			ProViz.errprintln("VizAnnotationVisitor:addTypeViz() - " + e);
			return;
		}
		MethodViz mv = this.methods.get(ownerID);
//08.1.3 In new implementation, parent should not be null entering
		if (mv == null) {
			System.err.println("Should not come here!!! - (VizAnnotationVisitor: addMultipleVariableDeclarations)");
			ProViz.errprintln("Should not come here!!! - (VizAnnotationVisitor: addMultipleVariableDeclarations)");
	//The method viz does not exist in the storage, so create one.
			MethodDeclaration md = (MethodDeclaration) this.getParentNode(node);
			this.addMethod(md);
			mv = this.methods.get(ownerID);
			if (mv == null) {
				ProViz.errprintln("NULL owner for a variable");
				return;
			}
		}
		for (Object obj : fragments) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
			VariableViz vv = new VariableViz(vdf.getName().getFullyQualifiedName(), mv, type.resolveBinding().getBinaryName());
			if (annotationValue != null) {
				try {
					//AnnotationParser.fillVizes(vv, annotationValue);
					AnnotationParser.annotationToViz(vv, annotationValue);
				}
				catch (Exception e) {
					ProViz.errprintln(e);
					return;
				}
			}
			mv.addVariableViz(vv);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationExpression)
	 */
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		this.addMultipleVariableDeclarations(node, node.getType(), node.modifiers(), node.fragments());
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		this.addMultipleVariableDeclarations(node, node.getType(), node.modifiers(), node.fragments());
		return true;
	}
	
	/**
	 * Extracts annotation value from a list of Annotations.
	 * @param annotationTypeName The annotation type ("Viz" in this case).
	 * @param modifiers A list of Annotation's.
	 * @return The value of the specific annotation type. null if the annotation type is not found.
	 */
	@SuppressWarnings("unchecked")
	protected String extractVizValue(String annotationTypeName, List modifiers) {
		String value = null;
  	for (Object obj : modifiers) {
  		if (obj instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation sma = (SingleMemberAnnotation) obj;
				if (sma.getTypeName().getFullyQualifiedName().equals(annotationTypeName)) {
					if (sma.getValue().getNodeType() == ASTNode.STRING_LITERAL) {
	//Annotation type found, so gets its value and breaks the loop.
						value = ((StringLiteral) sma.getValue()).getLiteralValue();
						break;
					} //end if
				} //end if
  		} //end if
			else if (obj instanceof MarkerAnnotation) {
				MarkerAnnotation ma = (MarkerAnnotation) obj;
				//this.annotations.put(ma.getTypeName().getFullyQualifiedName(), null);
				if (ma.getTypeName().getFullyQualifiedName().equals(DVIZ_ANNOTATION)) {
					value = VizMapStorage.DEFAULT_TYPE;
					break;
				}
			}
			else if (obj instanceof NormalAnnotation) {
				//not used in current Viz
  			//rv = true;
				//NormalAnnotation na = (NormalAnnotation) obj;
				//MemberValuePair[] mvp = (MemberValuePair[]) (na.values().toArray());
			}
  	} //end for
  	return value;
	} //end extractVizValue

	/**
	 * @return the roots
	 */
	public ArrayList<TypeViz> getRoots() {
		return roots;
	}
} //end VizAnnotationVisitor
