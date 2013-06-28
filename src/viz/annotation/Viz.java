package viz.annotation;

import java.lang.annotation.*;

/** The annotation for the viz of program elements, i.e. 
  * classes, interfaces, fields and methods. 
  * Such a annotation must contain the qualified name of the class 
  * to be used when visualizing the annotated program element. Also, several 
  * viz classes can be specified in a comma-separated list of 
  * their qualified names. In this case, the first viz
  * will be the default one and the others will be offered to the user as 
  * alternatives.
  * <p>Note that a variable - whether it is a field, a method's parameter 
  * or a local variable can reference an object whose type is different from 
  * the "declared type" of the variable, i.e. the type it was declared with.
  * Java allows the "actual type" of the referenced object to be different 
  * from the declared type if the object's class is a subclass of the declared 
  * type or if the declared type is an interface and the object's class 
  * implements it. As a consequence, a variable can be visualized differently
  * depending on the actual type of the referenced object. To account for that,
  * annotation of a variable can be a string composed of semicolon-separated 
  * parts where the first part is a list of viz classes for the
  * declared type and the subsequent parts define the visualizations of the
  * actual types. Each at the latter parts consists of a comma-separated list 
  * of actual types followed by a colon and then by a comma-separated list of
  * viz classes for each of these actual types.
  * <p>To give an example, an annotation of a sample field could be formulated 
  * as follows:
  * <pre>    @Viz(V1,V2;T1,T2:VT1,VT2;T3,T4:VT3,VT4) T field = new T1();</pre>
  * which will be interpreted as follows:
  * <br>If the actual type is either T1 or T2 then the variable will be depicted 
  * using viz class VT1 and the user will be given the option of
  * using VT2 instead.If the actual type is either T3 or T4 then the variable 
  * will be depicted using viz class VT3 and the user will be given 
  * the option of using VT4 instead. Otherwise the the variable will be depicted 
  * using viz class V1 and the user will be given the option of using 
  * V2 instead.
  * @author Copyright 2006 Jan Stelovsky, AMI Lab, ICS, University of Hawaii
  * @version 1.0.0 */
@Retention (RetentionPolicy.CLASS)
public @interface Viz {
  
  /** Returns the class for the viz of the associated class, 
    * interface, field or method.*/
  String value (); 
 
}