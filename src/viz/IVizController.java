package viz;

/**
 * Defines the behavior of a program controller that can step through programs. 
 * VizMonitor and VizPlayer implement this interface. AnimationController will 
 * use only one of the two controllers, and the correct controller must be set 
 * in AnimationController at the beginning of the visualization.
 * @author JW
 */
public interface IVizController {
	//public void start(IThread thread);
	/**
	 * Causes the execution to step forward.
	 */
	public void step();
	
	/**
	 * Terminates the target program's execution.
	 */
	public void terminate();
	
	/**
	 * Loads a class based on the given classPath. This is used to create user-space
	 * painters, which ProViz has no access to.
	 * @param classPath
	 * @return The class pointed by classPath; null if class loading is unsuccessful.
	 */
	public Class<?> loadClass(String classPath);
}
