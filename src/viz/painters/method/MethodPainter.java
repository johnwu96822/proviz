package viz.painters.method;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.painters.AbstractPainter;
import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;
import viz.views.VizCanvas;

/**
 * 
 * For a method call with a designated method painter, the sequence of beforeActions are:
 * New Method Call/Stack Frame: SF with method painter: M
 * (1) VPM updates painters in SF -> (2) M.methodInvoked()
 * ... in between are same method steps or others ...
 * SF Returned (popped)
 * (1) M.methodReturned -> (2) VPM updates painters in SF
 * So methodInvoked() is called AFTER parameter painters are updated in the new method call;
 * methodReturned() is called BEFORE variable painters in the popped method are updated
 * @author JW
 */
public abstract class MethodPainter extends AbstractPainter {
	private VizStackFrame stackFrame;
//	private VizCanvas canvas;
	private Map<String, List<MethodAction>> beforeActions = null;
	private Map<String, List<MethodAction>> afterActions = null;
	
	/**
	 * @param stackFrame
	 * @param canvas
	 */
	public MethodPainter(VizStackFrame stackFrame, VizCanvas canvas) {
		this.stackFrame = stackFrame;
		this.canvas = canvas;
	}
	
	@Override
	public final void addToCanvas() {
		AnimationController.screenModified = true;
		methodInvoked();
		super.addToCanvas();
	}
	
	@Override
	public final void destroy() {
		super.destroy();
	}
	
	/**
	 * MethodPainter returns null by default. Subclasses can override this method to provide
	 * a graphical component.
	 * @see viz.painters.IPainter#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return null;
	}
	
	/**
	 * Displays the visualization for a method when it is invoked. It is called after the stack frame
	 * and the parameter painters are created, so this method can work on parameter painters.
	 */
	public abstract void methodInvoked();
	
	/**
	 * This method is called after the method associated with this method painter returns. It is called
	 * <strong>BEFORE</strong> the de-allocated painters are updated, so this method can work with variable 
	 * painters in the returned method.
	 */
	public void methodReturned() {}
	
	/**
	 * Does a static paint of what the method does. This is used when animation check box is unchecked.
	 * Default implementation is doing nothing.
	 */
	public void paintMethod() {}
	
	public Painter getParameterPainter(String name) {
		Painter rv = null;
		IVizVariable var = this.getStackFrame().getVariable(name);
		if (var != null) {
			rv = ProViz.getVPM().getPainter(var);
			if (rv == null && var.isObject()) {
				rv = ProViz.getVPM().getPainter(var);
			}
		}
		return rv;
	}
	
	/**
	 * Gets a parameter variable by name
	 * @param name
	 * @return
	 */
	public IVizVariable getParameter(String name) {
		return this.getStackFrame().getVariable(name);
	}
	
	/**
	 * Subclass of method painters can use this method to schedule specific beforeActions for visualizing
	 * variable changes in the method.
	 * @param variableName If the variable is an instance variable, it must be prefixed with "this."
	 * @param action
	 */
	public void addBeforeAction(String variableName, MethodAction action) {
		List<MethodAction> mActions = null;
		if (this.beforeActions == null) {
			this.beforeActions = new TreeMap<String, List<MethodAction>>();
		}	else {
			mActions = this.beforeActions.get(variableName);
		}
		if (mActions == null) {
			mActions = new LinkedList<MethodAction>();
			mActions.add(action);
			this.beforeActions.put(variableName, mActions);
		}	else {
			mActions.add(action);
		}
	}
	
	
	/**
	 * Subclass of method painters can use this method to schedule specific beforeActions for visualizing
	 * variable changes in the method.
	 * @param variableName If the variable is an instance variable, it must be prefixed with "this."
	 * @param action
	 */
	public void addAfterAction(String variableName, MethodAction action) {
		List<MethodAction> mActions = null;
		if (this.afterActions == null) {
			this.afterActions = new TreeMap<String, List<MethodAction>>();
		}
		else {
			mActions = this.afterActions.get(variableName);
		}
		if (mActions == null) {
			mActions = new LinkedList<MethodAction>();
			mActions.add(action);
			this.afterActions.put(variableName, mActions);
		}
		else {
			mActions.add(action);
		}
	}
	
	/**
	 * Variable names of instance variables are prefixed with "this."
	 * 8/27/10 Does not remove for now
	 * 7/13/13 Why remove??
	 * @param variableName
	 * @param changeEvent
	 * @return
	 */
	public MethodAction getBeforeAction(String variableName, Change changeEvent) {
		if (this.beforeActions != null) {
			List<MethodAction> mActions = this.beforeActions.get(variableName);
			if (mActions != null) {
				MethodAction rv = null;
				for (MethodAction action : mActions) {
					if (action.getChange() == changeEvent) {
						rv = action;
						break;
					}
				}
				//if (rv != null) {
					//mActions.remove(rv);
				//}
				return rv;
			}
		}
		return null;
	}
	

	/**
	 * TODO 8/27/10 Does not remove for now
	 * @param variableName
	 * @param changeEvent
	 * @return
	 */
	public MethodAction getAfterAction(String variableName, Change changeEvent) {
		if (this.afterActions != null) {
			List<MethodAction> mActions = this.afterActions.get(variableName);
			if (mActions != null) {
				MethodAction rv = null;
				for (MethodAction action : mActions) {
					if (action.getChange() == changeEvent) {
						rv = action;
						break;
					}
				}
				//if (rv != null) {
					//mActions.remove(rv);
				//}
				return rv;
			}
		}
		return null;
	}
	
	/**
	 * Gets the stack frame that this MP belongs to
	 * @return the stackFrame
	 */
	public VizStackFrame getStackFrame() {
		return stackFrame;
	}

	/*
	 * @return the canvas
	 *
	public VizCanvas getCanvas() {
		return canvas;
	}*/
	
	/**
	 * Determines whether the method should continue to execute or just return after this method
	 * painter's visualization. The default implementation returns false.
	 * @return true if the method should continue to execute; if false, the method monitoring
	 * will step out of the method right after it enters.
	 */
	public boolean shouldContinueMethod() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.IPainter#getInstanceVariable(java.lang.String)
	 */
	public IVizVariable getInstanceVariable(String name) {
		IVizVariable thisVar = this.getStackFrame().getVariable("this");
		if (thisVar != null) {
			return thisVar.getField(name);
		}
		return null;
	}

  /**
   * @deprecated Replaced by using MethodAction just like handling variable changes
   * 
   * Any method invoked within this method would trigger this method, notifying
   * this method painter that a method is invoked. This method painter can handle
   * different method calls based on their method IDs.
   * 
   * This method is called before the new method is ever processed, i.e., variable
   * or method painters for the new methods are not created yet.
   * @param methodID
   *
  public void handleMethodCall(String methodID) {}

  public void handleMethodReturned(String previousMethodID) {}
  */
}
