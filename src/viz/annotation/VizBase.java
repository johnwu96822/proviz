package viz.annotation;

import java.lang.reflect.*;

import viz.ProViz;
import viz.util.Reflection;

/** The superclass of all viz classes. Subclasses must provide
  * two public static methods: instance() and forType(). 
  * Method instance() must return an instance of the subclass and must 
  * have the following format:
  * <pre>public static Class<?> forType () {
  *   return some_package.someType.someInnerType.class;
  * }</pre>
  * Method forType() must return the class that can be visualized by this 
  * viz and must have the following format:
  * <pre>public static VizBase instance () {
  *   return new VizBaseSubclass ();
  * }</pre>
  * <p>If a viz doesn't need to store any internal information 
  * then it should be represented by a single instance. (A singleton pattern.) 
  * In this case the static instance() method must return this singleton 
  * instance. Otherwise the viz must create a new instance 
  * for each object to be visualized and return it with instance().
  * <p>Provides for checks whether the viz can indeed be used 
  * to visualize a given type. It is assumed that if a viz  
  * can visualize a particular class, it can also visualize a subclass of 
  * that class. Similarly, if a viz can visualize a particular
  * interface, it can also visualize a subinterface of that interface.
  * Note, however, that if a subclass (or subinterface) adds more state 
  * information to the superclass (or superinterface) then the viz 
  * that is intended to display the supertype will not be able to depict 
  * this additional information. Even though a viz of a subtype
  * may be in special cases able to visualize just the information declared 
  * in the supertype, it will be considered as illegal. (The authors of such 
  * a viz are encouraged to return the supertype from legalType()
  * method.) Furthermore it is that a viz of an interface can
  * be used to visualize any class that implements the interface, directly
  * or indirectly (i.e. if one of the superclass ancestors implements it).
  * Note also that viz of a class cannot be used to depict 
  * an interface. In other words, a viz can visualize a type 
  * exactly then when a variable declared with the viz's forType() 
  * can be assigned a variable declared with the visualized type.
  * <p>Note that a viz for a subclass could be used to display
  * a superclass if subclass doesn't add any fields (this can be checked)
  * or if the viz doesn't rely on such fields (which can't be 
  * checked). In such a case we encourage the author of such 
  * viz to specify that the superclass is being visualized.
  * <p>The viz of variables that are declared using an interface  
  * is tricky because even though Java allows to declare instance variables
  * within an interface and therefore it makes sense to visualize an 
  * interface, this practice is discouraged and seldom used (except for 
  * the declaration of constants). Therefore even though we consider a 
  * viz for an interface to be able to depict any class that 
  * implements the interface the authors of visualizations are encouraged 
  * to develop visual visualizations for all actual types that the variable 
  * may reference.
  * <p>Provides for checking whether there are the two required public static 
  * methods instance() and forType(). Note that this cannot be ensured in Java 
  * other than by using reflection. Provides also a convenience method  
  * classWithName() that returns the class of given 
  * @author Copyright 2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii
  * @version 1.0.0 */
abstract public class VizBase {
  
  /** The public and static modifiers. */
  private static final int publicStaticModifiers 
      = Modifier.PUBLIC + Modifier.STATIC;
  /** The name of the forType() method. */
  private static final String forTypeMethodName = "forType";
  /** The name of the instance() method. */
  private static final String instanceMethodName = "instance";
  
  /** Checks whether a viz class declares the required static methods.
    * @param viz class to check
    * @throws NoSuchMethodException if some of the required methods
    * are missing */
  public static void checkRequiredMethods (Class<? extends VizBase> viz) 
      throws NoSuchMethodException {
    String message = "";
    try {
      method (viz, forTypeMethodName, Class.class);
    } catch (NoSuchMethodException error) {
      message = error.getMessage ();
    }
    try {
      method (viz, instanceMethodName, viz);
    } catch (NoSuchMethodException error) {
      message += (message == "" ? "" : "; ") + error.getMessage ();
    }
    if (message != "") {throw new NoSuchMethodException (message);}
  }

  /** Returns the required parameterless public static method with given name
    * and return type.
    * @param viz class to check
    * @param name of the required static method
    * @param returnType 
    * @return the method
    * @throws NoSuchMethodException if the method doesn't exist */
  private static Method method (Class<? extends VizBase> viz, String name
      , Class<?> returnType) throws NoSuchMethodException {
    return Reflection.method (viz, name, publicStaticModifiers, returnType);
  }
  
  /** Returns whether a viz class can be visualize the given type.
    * Returns true if the given type is null.
    * @param viz viz class
    * @param typeToVisualize type to visualize 
    * @return true iff the viz can visualize the given type or
    * the given type is null*/
  public static boolean isLegalVisualizationFor (Class<? extends VizBase> viz
      , Class<?> typeToVisualize) {
    if (typeToVisualize == null) {return true;}
    try {
      Method forType = method (viz, forTypeMethodName, Class.class);
      Class<?> visualizedType = (Class <?>) forType.invoke (null);
      return visualizedType.isAssignableFrom (typeToVisualize);
    } catch (Exception error) {return false;}
  }

  /** Throws an exception if the given type is not null and the viz 
    * cannot visualize it.
    * @param viz viz class
    * @param typeToVisualize type to visualize
    * @throws IllegalArgumentException if typeToVisualize can't be visualized */
  public static void check (Class<? extends VizBase> viz,
      Class<?> typeToVisualize) throws IllegalArgumentException {
    if (! isLegalVisualizationFor (viz, typeToVisualize)) {
      throw new IllegalArgumentException (typeToVisualize 
          + " can't be visualized by " + viz.getName ());
    }
  }
  
  public static boolean isClass(String name) {
  	boolean rv = false;
  	try {
			Class.forName(name);
			rv = true;
		} catch (ClassNotFoundException e) {
			ProViz.errprintln(e);
		}
		return rv;
  }
}
