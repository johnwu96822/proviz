package viz.util;

import java.lang.reflect.*;
import java.util.*;

/** Utilities that provide convenience methods for the use of reflection.
  * 
  * @author Copyright ©2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii @Manoa
  * @version 1.0.0 */
public class Reflection {
  
  /** The flag indicating that modifiers are irrelevant. */
  private static final int anyModifiers = -1;
  /** The representations of primitive types accessible by their names. */
  @SuppressWarnings("unchecked")
	private static Map<String, Class> primitiveTypes = 
      new HashMap<String, Class> ();

  static {
    primitiveTypes.put ("byte",    Byte.TYPE);
    primitiveTypes.put ("short",   Short.TYPE);
    primitiveTypes.put ("int",     Integer.TYPE);
    primitiveTypes.put ("long",    Long.TYPE);
    primitiveTypes.put ("float",   Float.TYPE);
    primitiveTypes.put ("double",  Double.TYPE);
    primitiveTypes.put ("char",    Character.TYPE);
    primitiveTypes.put ("boolean", Boolean.TYPE);
  }
  
  /** Returns the type with given qualified name. The type can be
    * a primitive type, such as "int". This unlike Class.forName()
    * where a primitive type causes a ClassNotFoundException.
    * @param name of the type
    * @return the Class instance that represents the type
    * @throws ClassNotFoundException if no such type exists 
    * @throws ClassCastException if the type can't be cast to Type */
  @SuppressWarnings("unchecked")
  public static Class<Type> type (String name) throws ClassNotFoundException
      , ClassCastException {
    Class type = primitiveTypes.get (name);
    if (type != null) {return type;}
    return (Class<Type>) Class.forName (name);
  }

  /** Returns the method with the given signature. If the method isn't in
    * the declaring class, looks in superclass ancestors, too.
    * @param declaringClass class where the method is defined
    * @param name of the method
    * @param modifiers of the method; anyModifiers if it doesn't matter
    * @param returnType of the method; null if it doesn't matter
    * @param parameterTypes in the method's parameter list 
    * @return the method
    * @throws NoSuchMethodException if the method doesn't exist */
  @SuppressWarnings("unchecked")
	public static Method method (Class<?> declaringClass, String name
      , int modifiers, Class<?> returnType, Class... parameterTypes)
      throws NoSuchMethodException {
    Method method = null;
    try {
      method = declaringClass.getDeclaredMethod (name, parameterTypes);
    } catch (NoSuchMethodException error) {
      if (declaringClass == Object.class) {throwMethodError (name, "missing");}
      method = method (declaringClass.getSuperclass (), name, modifiers
          , returnType, parameterTypes);
    }
    method.setAccessible (true);
    if (modifiers != anyModifiers && modifiers != method.getModifiers ()) {
      throwMethodError (name, "has incorrect modifiers");
    }
    Class <?> actualReturnType = method.getReturnType ();
    if (returnType != null && returnType != actualReturnType) {
      throwMethodError (name, "returns " + returnType + " instead of " 
          + actualReturnType);
    }
    return method; // the method is there and has the correct format
  }

  /** Returns the method with the given signature.
    * @param declaringClass class where the method is defined
    * @param name of the method
    * @param modifiers of the method
    * @param returnTypeName of the method
    * @param parameterTypesNames in the method's parameter list 
    * @return the method
    * @throws NoSuchMethodException if the method doesn't exist 
    * @throws ClassNotFoundException if either one of the parameter types
    *         or the return type doesn't exist */
  @SuppressWarnings("unchecked")
	public static Method method (Class<?> declaringClass, String name
      , int modifiers, String returnTypeName, String... parameterTypesNames)
      throws NoSuchMethodException, ClassNotFoundException {
    Class<?> returnType = type (returnTypeName);
    Class [] parameterTypes = new Class [parameterTypesNames.length];
    for (int i = 0; i < parameterTypesNames.length; i++) {
      parameterTypes [i] = type (parameterTypesNames [i]);
    }
    return method (declaringClass, name, modifiers, returnType, parameterTypes);
  }
  
  /** Throws an exception indicating error in expected method declaration.
    * @param name of the method
    * @param message describing error
    * @throws NoSuchMethodException always */
  static void throwMethodError (String name, String message) 
      throws NoSuchMethodException {
    throw new NoSuchMethodException ("Method " + name + "() " + message);
  }
  
  /** Creates a new Reflection test. */
  public Reflection () {
    try {
      System.out.println (type ("int"));
    } catch (ClassNotFoundException error) {
      error.printStackTrace();
    }
  }

  /** Starts a new Reflection application.
    * @param arguments not used */
  public static void main (String [] arguments) {
    new Reflection ();
  }
}