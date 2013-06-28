package viz.runtime;

/**
 * A reactor should react to four method states (new method, return from a method, 
 * same method, and refresh all) by updating its own data structure and dispatch 
 * events. This interface also defines the five states of method calls.
 * @author John
 */
public interface IMethodStateReactor {
	public static final int NEW_METHOD = 0;
	public static final int RETURN_FROM_METHOD = 1;
	public static final int OTHER = 2;
	
	//3 & 4 do not need to check whether the method has Viz
	public static final int SAME_METHOD = 3;
	public static final int BACK_TO_DRIVER_OR_EXIT = 4;
	
	/**
	 * Reacts to a new method being called, and a new stack frame is created.
	 * @param stackFrame The new stack frame.
	 */
	public void newMethod(VizStackFrame stackFrame, IVizVariable[] varsInPrevStackFrame, boolean isRefresh);
	
	/**
	 * The execution returns from a method. So the current top stack frame was second to the top before,
	 * and the previous stack frame was actually the top stack frame before and was popped from the stack.
	 * @param previousMethodID
	 * @param previous
	 * @return The previous top stack frame (the one that is popped).
	 * @throws MethodDetectionFailException
	 */
	public VizStackFrame returnFromMethod(String previousMethodID, VizStackFrame previous)//, final VizVariable[] currentVars) 
		throws MethodDetectionFailException;
	
	/**
	 * The execution step stays within the same method, so the reactor should react to this 
	 * same method step. The parameter, 'variables', is the list of variables with correct
	 * values, so that the reactor needs to update its own data based on this list of variables.
	 * The top of the stack for 'variables' is at the end of the array.
	 * @param variables The up-to-date variables in the top stack frame. The top of the stack is 
	 * variables[variables.length - 1]
	 */
	public void sameMethod(IVizVariable[] variables);
	
	
	/**
	 * Refreshes all stack frames because of the occurance of unknown events. 
	 * For the parameter, vFrames, the top stack frame is at index 0.
	 * @param vFrames Current stack frames. The top stack frame is vFrames[0].
	 */
	public void refreshAllStackFrames(VizStackFrame[] vFrames);
}
