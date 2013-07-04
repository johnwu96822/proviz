package viz.painters;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.model.Association;
import viz.model.FieldViz;
import viz.model.MethodViz;
import viz.model.VariableViz;
import viz.model.VariableVizBase;
import viz.model.Visualization;
import viz.model.VizMapModel;
import viz.painters.method.MethodAction;
import viz.painters.method.MethodPainter;
import viz.runtime.Change;
import viz.runtime.ChangedVariable;
import viz.runtime.IMethodStateReactor;
import viz.runtime.IVizVariable;
import viz.runtime.MethodDetectionFailException;
import viz.runtime.VizRuntime;
import viz.runtime.VizStackFrame;
import viz.views.Connector;
import viz.views.ConnectorManager;
import viz.views.VizCanvas;
import viz.views.util.IConnectable;

/**
 * Contains all painters.
 * @author John
 *
 */
public class VizPainterManager implements IMethodStateReactor {
	private Map<IVizVariable, Painter> primitivePainters = 
			new Hashtable<IVizVariable, Painter>();
	
	private Map<VizStackFrame, Hashtable<String, ArrayList<Painter>>> dependencyWaitingLists = 
			new Hashtable<VizStackFrame, Hashtable<String, ArrayList<Painter>>>();
	
	private Map<VizStackFrame, MethodPainter> methodPainters = 
			new Hashtable<VizStackFrame, MethodPainter>();
	
	//Map<unique object ID, list of painters that all point to the ID>
	private Map<String, List<Painter>> uniqueObjectTable = 
			new Hashtable<String, List<Painter>>();
	
	private static final String THIS_VARIABLE = "this";
	private static final String NULL_ID = "-1";
	//private boolean notRefreshingAll = true;
	
	private static Association arrayField;
	{
		arrayField = new FieldViz(null, null, null);
		arrayField.addVisualization(new Visualization("1D", null));
	}
	
	//This is the main VizCanvas that painters will be created on by default.
	private VizCanvas canvas;
	
	//private AnimationController aController;

	public VizPainterManager() {
		this.canvas = new VizCanvas();
	}
	
	/* In the event of a new method call:
	 * (1) If the parent method has a method painter, notify it
	 * (2) Create a dependency waiting list for this method
	 * (3) Process this method's variables, i.e., parameters and 
	 *     fire "NEW" events on these variables
	 * (4) If this method is annotated, create the method painter
	 * @see viz.runtime.IMethodStateReactor#newMethod(viz.runtime.VizStackFrame)
	 */
	@Override
	public void newMethod(VizStackFrame topFrame, IVizVariable[] varsInPrevStackFrame, boolean isRefresh) {
		if (topFrame == null) {
			throw new NullPointerException("Null stack frame in VizPainterManager.newMethod");
		}
	//Inform the previous stack's method painter, if any, that the current
	//method is invoked
		VizStackFrame prevStackFrame = ProViz.getInstance().getVizRuntime().getPreviousStackFrame();
		if (prevStackFrame != null) {
		  MethodPainter mp = methodPainters.get(prevStackFrame);
		  if (mp != null) {
		    mp.handleMethodCall(topFrame.getMethodID());
		  }
		}
		ProViz.getInstance().pushMethodDisplay(topFrame.getMethodID());
	//Creates a waiting list for this method
		Hashtable<String, ArrayList<Painter>> waitingList = new Hashtable<String, ArrayList<Painter>>();
		this.dependencyWaitingLists.put(topFrame, waitingList);
	//These must be parameters of the method, since we have not started
	//executing the method
		for (IVizVariable var : topFrame.getVariables()) {
			this.processVariableAndCreatePainter(var, null, waitingList);//, topFrame, waitingList);
		}
		for (IVizVariable var : topFrame.getVariables()) {
			var.fireEvent(Change.NEW);
		}
	//Creates and runs the method painter
		MethodViz mViz = VizMapModel.getInstance().findMethodViz_runtime(topFrame.getMethodID());
		if (mViz != null) {
			Visualization viz = mViz.getCurrentViz();
			if (viz != null) {
				MethodPainter mPainter = PainterFactory.createMethodPainter(viz.getPainterName(), 
						topFrame, this.canvas);
				if (mPainter != null) {
					this.methodPainters.put(topFrame, mPainter);
					if (!isRefresh) {
						if (ProViz.getAnimationController().getAnimationCheckBoxValue()) {
							mPainter.addToCanvas();
						} else {
							mPainter.paintMethod();
						}
					}
				}
			}
		}
		if (!isRefresh) {
			ProViz.getAnimationController().stepAnimation();
		}
	}

	/**
	 * Finds a FieldViz in Viz Map Model. The first priority is to see 
	 * if the field is a custom FieldViz under a local variable. Otherwise, 
	 * when 'inheritance' is enabled and the search is unsuccessful in the 
	 * current class, it would go up the hierarchy and search for the field 
	 * in super classes.
	 * @param parentType
	 * @param fieldName
	 * @param inheritance
	 * @return
	 */
	private Association findFieldViz(IVizVariable field, boolean inheritance) {
		Association rv = null;
		IVizVariable parentVar = field.getParent();
	//TODO extend the search to fields of fields
		
	//Find the customized FieldViz under a local variable
		if (parentVar.isLocalVariable()) {
	//Look if the parent local variable has this field annotated  
			VariableViz parentAsso = VizMapModel.getInstance().findVariableViz_runtime(
					parentVar.getStackFrame().getMethodID(), parentVar.getName());
			if (parentAsso != null) {
	//The parent local variable is annotated, so look whether the specific field
	//is annotated here
				if (parentAsso.getType().indexOf('[') != -1) {
	//Should not happen, since array types are handled in processVariableAndCreatePainter().
	//Handing array type
					rv = parentAsso.getFieldViz(PainterFactory.ARRAY_KEYWORD);
				}
				else {
					rv = parentAsso.getFieldViz(field.getName());
				}
				if (rv != null) {
					ProViz.println("Customized FieldViz under a local variable: " + rv.getCurrentViz());
					return rv;
				}
			}
		} else {
	//parentVar is a field variable
	//Find the FieldViz of parentVar and see if there is a customized FieldViz under it for the
	//the current field
			FieldViz parentField = VizMapModel.getInstance().findFieldViz_runtime(
					parentVar.getParent().getActualType(), parentVar.getName());
			if (parentField != null) {
				rv = parentField.getFieldViz(field.getName());
				if (rv != null) {
					ProViz.println("Customized FieldViz under a field: " + rv.getCurrentViz());
					return rv;
				}
			}
		}
	//Normal field finding with inheritance, if enabled
		rv = VizMapModel.getInstance().findFieldViz_runtime(parentVar.getActualType(), field.getName());
		if (rv == null && inheritance == true) {
			try {
	//TODO Problem: Sort not loaded as super class for BubbleSort. Need to load class
				Class<?> superClass = Class.forName(parentVar.getActualType()).getSuperclass();
				while (superClass != null && rv == null && !superClass.getName().equals("java.lang.Object")) {
					ProViz.println("************ Finding field in super class: " + superClass.getName());
					rv = VizMapModel.getInstance().findFieldViz_runtime(superClass.getName(), field.getName());
					superClass = superClass.getSuperclass();
					if (rv != null) {
						ProViz.println("************ Inheritance - found annotated field in superclass: " + rv.getFullName());
					}
				}
			} catch (ClassNotFoundException e) {
				ProViz.errprintln("No class for: " + parentVar.getActualType());
			}
		}
		return rv;
	}
	/**
	 * Variables in a stack frame can be:
	 *   static variable
	 *   instance variable (this.x)
	 *   reference variable (ID = x)
	 *   primitive variable (ID = null)
	 *   
	 * The new stack frame contains painters (assuming they are annotated with painters):
	 * this
	 *   this.field1
	 *   this.field2
	 *   ...
	 * local var1
	 * local var2
	 * ...
	 * 
	 * Reference variables could have field variables as descendants
	 * 
	 * One line explanation for VOO and VOV
	 * VOO: One object is painted by one painter, resulting in many aliasing
	 *      variables using one painter
	 * VOV: One variable is painted by one painter, resulting in many painters
	 *      painting one object
	 * 
	 * @param newVar
	 * @param topFrame
	 * @param newPainters
	 * @param waitingList
	 * @return
	 */	
	private Painter processVariableAndCreatePainter(IVizVariable var, Painter parentPainter, //) {
		/*,	VizStackFrame currentStackFrame,*/ Hashtable<String, ArrayList<Painter>> waitingList) {
		this.registerListeners(var, waitingList);
		//boolean isArrayField = false;
		if (!var.isLocalVariable()) {
  //var is a field, and if the parent variable has no painter, var and its
	//descendants should not have painters, either
			if (parentPainter == null) {
				if (var.isObject()) {
					this.registerListenersForFieldsWithNoPainter(var, waitingList);
				}
				return null;
			}
			/*String parentActualType = var.getParent().getActualType();
			if (parentActualType.indexOf(PainterFactory.ARRAY_KEYWORD) != -1) {
				isArrayField = true;
			}*/
		}
		Association vizBase = getAssociation(var/*, parentPainter*/);
//TODO 9/7/2010 bug here for array fields. vizBase goes through, but the viz count is 0
		if (/*!isArrayField && */(vizBase == null || vizBase.getVizCount() == 0)) {
	//The variable is not annotated with a painter
			if (var.isObject()) {
				this.registerListenersForFieldsWithNoPainter(var, waitingList);
			}
			return null;
		}
		Painter painter = null;
	//------------------------------------------------------------------------------
	//------------------------------ VOO -------------------------------------------
	//------------------------------------------------------------------------------
	//Field painters do not go through this process (VOV for field painters)
	//TODO 11/26/2010 Disabled VOO
		if (var.isObject() && var.isLocalVariable() && !var.isNull()) {
	//Find if a compatible painter is already visualizing this object
			List<Painter> pList = this.uniqueObjectTable.get(var.getUniqueObjectID());
			if (pList != null) {
				for (Painter tempPainter : pList) {
					if (tempPainter.voo() && tempPainter.getParent() == null && 
							(vizBase.getCurrentViz().isCompatible(tempPainter.getClass().getName()) ||
							vizBase.getCurrentViz().getPainterName().equals(VizMapModel.DEFAULT_TYPE))) {
	//VOO for root painters only
						for (IVizVariable tempVar : tempPainter.getVariablesToThisPainter()) {
							if (tempVar.isLocalVariable()) {
								painter = tempPainter;	
	//Priority to return the painer whose variable is in the same stack frame as 'var'
								if (tempVar.getStackFrame().equals(var.getStackFrame())) {
									break;
								} //end if
							}
						} //end for
					} //end if
				} //end for
				if (painter != null) {
					ProViz.println("-> VOO is taking place <-");
	//Use this painter since it points to the same object as newVar, and it is the same
	//type of painter that var means to create
					this.bindVariableAndPainter(var, painter);
					if (parentPainter != null) {
						parentPainter.addOrReplaceFieldPainter(var.getName(), painter);
					}
					//TODO maybe not paint() method
					//09.08.30 should be, since no value changes are involved, there might not need
					//animations
					//painter.paint();
					//pause();
					return painter;
				}
			}
		}
	////////////////////////// VOV /////////////////////////////////
		//Either no painter for the object has been created before, 
		//or the variable is primitive, so create a new painter.
		Point startingLoc = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		painter = PainterFactory.createPainter(vizBase, var, /*null,*/ this.canvas, startingLoc);
		//System.out.println(painter.getClass() + " - " + painter.shouldCreateFieldPainters());
		if (painter != null) {
			//ProViz.println("VPM: Adding painter for: " + var.getName() + " - " + painter.getClass().getName());
			this.registerListenerOrAddToWaitingList(painter, var.getStackFrame());//, waitingList);
			if (parentPainter != null) {
				parentPainter.addOrReplaceFieldPainter(var.getName(), painter);
			}
			if (var.isObject()) {
	//Add the new painter to the unique ID table.
				addToUniqueIDTable(painter);
				if (painter.shouldCreateFieldPainters()) {
					for (IVizVariable field : var.getFields()) {
						this.processVariableAndCreatePainter(field, painter, waitingList);//, currentStackFrame, waitingList);
					}
				}	else {
					this.registerListenersForFieldsWithNoPainter(var, waitingList);
				}
				this.registerFieldListeners(painter);
			}	else {
				this.primitivePainters.put(var, painter);
			}
	//VPM only calls addToCanvas() on painters for root/local variables
			if (var.getParent() == null) {
				if (startingLoc.x != Integer.MIN_VALUE || startingLoc.y != Integer.MIN_VALUE) {
					painter.setLocation(startingLoc.x, startingLoc.y);
				}
				painter.addToCanvas();
			}
		}	else {
	//Painter creation is unsuccessful; so create dependency for the variable
			if (var.isObject()) {
				this.registerListenersForFieldsWithNoPainter(var, waitingList);
			}
			ProViz.errprintln("VPM: Cannot create painter: " + var.getName());
		}
		return painter;
	}
	
	private boolean isArray(String type) {
		return type.indexOf(PainterFactory.ARRAY_KEYWORD) != -1;
	}

	/**
	 * @param var
	 * @param parentPainter
	 * @return
	 */
	private Association getAssociation(IVizVariable var/*, Painter parentPainter*/) {
		Association vizBase = null;
		if (var.isLocalVariable()) {
	//var is a local variable 
	//(because fields in current stack frame is stored under 'this' variable,
	//'this' variable can't possibly be changed
			if (!var.getName().equals(THIS_VARIABLE)) {
				vizBase = VizMapModel.getInstance().findVariableViz_runtime(
						var.getStackFrame().getMethodID(), var.getName());
			}
		}	else {
  //var is a field
			IVizVariable parentVar = var.getParent();
			String parentActualType = parentVar.getActualType();
	//-------------------------- Handling array fields -------------------------
			if (/*parentPainter != null && */isArray(parentActualType)) {
				//ProViz.print(var.getName() + " is field of an array, visualized by: ");
	//Priority to finding one-level field annotations
				if (parentVar.isLocalVariable()) {
					VariableViz localVarViz = VizMapModel.getInstance().findVariableViz_runtime(
							var.getStackFrame().getMethodID(), parentVar.getName());
					if (localVarViz != null) {
						vizBase = localVarViz.getFieldViz(PainterFactory.ARRAY_KEYWORD);
					}
				}	else {
	//Parent variable is a field
					FieldViz parentField = VizMapModel.getInstance().findFieldViz_runtime(
							parentVar.getParent().getActualType(), parentVar.getName());
					if (parentField != null) {
						vizBase = parentField.getFieldViz(PainterFactory.ARRAY_KEYWORD);
					}
				}
				if (vizBase == null) {
	//First priorityCheck if there is [] under the parent's type declaration, e.g., int[] -> []
					vizBase = VizMapModel.getInstance().findFieldViz_runtime(
							parentActualType, PainterFactory.ARRAY_KEYWORD);
				}
				/*if (vizBase == null) {
					vizBase = Vizes.getInstance().findTypeViz_runtime(var.getActualType());
				}*/
				if (vizBase == null) {
	//Use array's (parent variable's) declared type
					vizBase = VizMapModel.getInstance().findTypeViz_runtime(
							parentActualType.substring(0, parentActualType.indexOf('[')));
				}
				if (vizBase == null) {
					vizBase = arrayField;
				}
			}	else {
	//-------------------------- Non-array fields ------------------------------
	//4.19.2010 Can handle inheritance
				vizBase = this.findFieldViz(var, true);
				//= Vizes.getInstance().findFieldViz(parentVar.getActualType(), var.getName());
			}
		}
		return vizBase;
	}

	/**
	 * Register ONLY this painter as listener on its fields. No recursive call to
	 * descendants of the fields.
	 * @param painter
	 */
	private void registerFieldListeners(Painter painter) {		
		if (painter == null || painter.getDependentVars() == null || 
				!painter.getVariable().hasField()) { return; }
		for (String dependingVar : painter.getDependentVars()) {
			if (dependingVar.charAt(0) == '*') {
				String fieldName = dependingVar.substring(1);
				IVizVariable field = painter.getVariable().getField(fieldName);
				field.addListener(painter);
				painter.addEventGenerator(field);
			}
		}
	}

	/**
	 * Only object painters will be added to the unique ID table.
	 * @param painter
	 */
	private void addToUniqueIDTable(Painter painter) {
		if (painter.getVariable().isPrimitive() || painter.getVariable().isNull()) {
			return;
		}
		if (this.uniqueObjectTable.containsKey(painter.getUniqueID())) {
	//The key already exists, so append this painter to the list for this key
			this.uniqueObjectTable.get(painter.getUniqueID()).add(painter);
		}	else {
	//The key does not exist, so create a new (key, painter list) pair and add it
	//to the table
			List<Painter> list = new ArrayList<Painter>();
			list.add(painter);
			this.uniqueObjectTable.put(painter.getUniqueID(), list);
		}
	}
	
	/**
	 * VOO: Binds the variable and the painter which points to the same object without 
	 * processing and creating new painters. As a result, multiple variables will point
	 * to the same painter. Only for object painters.
	 * @param var
	 * @param painter
	 */
	private void bindVariableAndPainter(IVizVariable var, Painter painter) {
		if (var.isPrimitive() || var.isNull()) { return; }
		painter.addVariableToThisPainter(var);
		for (IVizVariable field : var.getFields()) {
			Painter fieldPainter = painter.getFieldPainter(field.getName());
			if (fieldPainter != null) {
				this.bindVariableAndPainter(field, fieldPainter);
			}
		}
	}
	
	/**
	 * For the given VizVariable, recursively register listeners for all its descendants.
	 * The variable itself, however, is not registered here.
	 * @param vvar
	 */
	private void registerListenersForFieldsWithNoPainter(IVizVariable vvar, 
			Hashtable<String, ArrayList<Painter>> waitingList) {
		for (IVizVariable field : vvar.getFields()) {
			this.registerListeners(field, waitingList);
			this.registerListenersForFieldsWithNoPainter(field, waitingList);
		}
	}
	
	/* (non-Javadoc)
	 * @see viz.runtime.IMethodStateReactor#returnFromMethod(java.lang.String, viz.runtime.VizStackFrame)
	 */
	@Override
	public VizStackFrame returnFromMethod(String previousMethodID, VizStackFrame previousStackFrame)
			throws MethodDetectionFailException {
		MethodPainter mPainter = null;
		if (VizMapModel.getInstance().findMethodViz_runtime(previousMethodID) == null) {
			//Previous method is not in the Viz model, so no stack was created for it. Do nothing
			//System.out.println("VPM: Previous method not in the Viz model. Do nothing.");
		} else {
			if (!previousStackFrame.getMethodID().equals(previousMethodID)) {
				throw new MethodDetectionFailException("VPM: Method ID not matching the previous one! " +
						"Previous could be a library method call.");
			}
	//1. Call methodReturned() on the previous method painter
			mPainter = this.methodPainters.remove(previousStackFrame);
			if (mPainter != null) {
				mPainter.methodReturned();
			}
			ProViz.getInstance().popMethodDisplay();
			//2.8.10 CANNOT call same method on previous stack frame
			//Map<String, Painter> painters = this.painterStackFrames.get(previousStackFrame);
			//Hashtable<String, ArrayList<Painter>> waitTable = this.dependencyWaitingListStack.peek();
	//2. Fire REMOVED event on variables in the previous stack frame
			for (IVizVariable var : previousStackFrame.getVariables()) {
				var.fireEvent(Change.REMOVED);
			}
	//3. Deallocate all variables in the previous stack frame
			for (IVizVariable var : previousStackFrame.getVariables()) {
				this.deallocateVariable(var, null);//, null);
			}
	//Remove the previous stack frame from the waiting list
			if (this.dependencyWaitingLists.remove(previousStackFrame) == null) {
				ProViz.errprintln("VPM: waiting list failed to remove previous stack frame");
			}
	//09.08.15 - process changed aliasing variables in the previous (now current) stack
	//09.08.31 - commented out because no need updating the top stack after the return
/*			VizRuntime vRuntime = VizRuntime.getInstance();
			this.processChangedVariables(vRuntime.getChangedVars(), vRuntime.getTopStackFrame(), 
					this.dependencyWaitingLists.get(vRuntime.getTopStackFrame()));*/
		}
	//4. Step the animation
		ProViz.getAnimationController().stepAnimation();
	//5. Destroy the previous method painter
		if (mPainter != null) {
			mPainter.destroy();
		}
	//6. Inform the current method painter, if any, that the previous method has returned
		VizStackFrame currentTop = ProViz.getInstance().getVizRuntime().getTopStackFrame();
		if (currentTop != null) {
		  MethodPainter currentMP = this.methodPainters.get(currentTop.getMethodID());
		  if (currentMP != null) {
		    currentMP.handleMethodReturned(previousMethodID);
		  }
		}
		return previousStackFrame;
	}

	/**
	 * Removes a painter from the waiting list. A painter on the waiting list is waiting for a 
	 * certain variable being created. The painter is removed from the waiting list when it
	 * is being destroyed.
	 * @param removedPainter The painter to be removed.
	 * @param waitingList
	 */
	private void removePainterFromWaitingList(Painter removedPainter,
			Hashtable<String, ArrayList<Painter>> waitingList) {
		if (waitingList != null && removedPainter.getDependentVars() != null) {
			for (String varName : removedPainter.getDependentVars()) {
				ArrayList<Painter> tempList = waitingList.get(varName);
				if (tempList != null) {
					tempList.remove(removedPainter);
					if (tempList.isEmpty()) {
						waitingList.remove(varName);
					}
				}
			} //end for
		} //end if
	} //end removePainterFromWaitingList

	/* Refreshes all stack frames by reconstructing with newMethod().
	 * @see viz.runtime.IMethodStateReactor#refreshAllStackFrames(viz.runtime.VizStackFrame[])
	 */
	@Override
	public void refreshAllStackFrames(VizStackFrame[] vFrames) {
		//this.notRefreshingAll = false;
		ProViz.getInstance().clearStackDisplay();
		ProViz.println("VPM: Refreshing all stack frames: " + vFrames.length);
		//this.painterStackFrames.clear();
		this.clearAll();
		for (int i = vFrames.length - 1; i >= 0; i--) {
			this.newMethod(vFrames[i], null, true);
		}
		getCanvas().repaint();
		//this.notRefreshingAll = true;
	}
	
	/**
	 * Notifies a method painter that there is a variable change, and if the method painter has
	 * a corresponding action for this variable and its type of change, run the action.
	 * @param var The changed variable
	 * @param change The type of change occurred to var
	 * @param mPainter The method painter of the current stack
	 */
	private void notifyMethodPainter(IVizVariable var, Change change, MethodPainter mPainter, boolean isBefore) {
		String name = var.getName();
		if (!var.isLocalVariable() && var.getParent().getName().equals(THIS_VARIABLE)) {
			name = "this." + name;
		}
		MethodAction action = null; 
		if (isBefore) {
			action = mPainter.getAndRemoveBeforeAction(name, change);
		}	else {
			action = mPainter.getAndRemoveAfterAction(name, change);
		}
		if (action != null) {
			AnimationController.screenModified = true;
			action.run(var);
		}
	}
	/**
	 * This method does not need the variables in the parameter, so it can be null; 
	 * @param 'variables' is not used, thus can be null
	 * @see viz.runtime.IMethodStateReactor#sameMethod(viz.runtime.VizVariable[])
	 */
	@Override
	public void sameMethod(IVizVariable[] variables) {
	//TODO Do a check on the integrity of same method call? Or does VizMonitor gaurantees it?
		//System.out.println("VPM: Same method visualizing: " + variables.length);
		VizRuntime vRuntime = ProViz.getInstance().getVizRuntime();
		VizStackFrame currentStack = vRuntime.getTopStackFrame();
		//Map<String, Painter> topPainters = painterStackFrames.get(currentStack);
		Hashtable<String, ArrayList<Painter>> waitingList = this.dependencyWaitingLists.get(currentStack);
			//this.dependencyWaitingListStack.peek();
		List<ChangedVariable> changedVars = vRuntime.getChangedVars();
		List<IVizVariable> newVars = vRuntime.getNewVariables();
		List<IVizVariable> removedVars = vRuntime.getRemovedVars();

		MethodPainter mPainter = this.getMethodPainter(currentStack);
		if (mPainter != null) {
			for (IVizVariable var : removedVars) {
				if (var.getStackFrame() == currentStack) {
					this.notifyMethodPainter(var, Change.REMOVED, mPainter, true);
				}
			}
			for (IVizVariable var : newVars) {
				if (var.getStackFrame() == currentStack) {
					this.notifyMethodPainter(var, Change.NEW, mPainter, true);
				}
			}
			for (ChangedVariable chVar : changedVars) {
				if (chVar.getVariable().getStackFrame() == currentStack) {
					this.notifyMethodPainter(chVar.getVariable(), chVar.getEventType(), mPainter, true);
				}
			}
		}
		
	//*** There is no presence of instance variables in 'newVars' and 'removedVars'
		int count = 0;
	//Remove de-allocated variables first
		for (IVizVariable var : removedVars) {
			var.fireEvent(Change.REMOVED);
			count++;
		}
		for (IVizVariable var : removedVars) {
			deallocateVariable(var, /*null, topPainters,*/ waitingList);
		} //end for
	//Process new variables
		for (IVizVariable var : newVars) {
			this.processVariableAndCreatePainter(var, null, waitingList);//, currentStack, waitingList);
			count++;
		}
		for (IVizVariable var : newVars) {
			var.fireEvent(Change.NEW);
		}
		
	//Paints the changed variables first, then fire change events on all other listening painters
		processChangedVariables(changedVars, /*currentStack,*/ waitingList);
		count += changedVars.size();

		//ProViz.println("Number of changed vars: " + count);
		//This is done in the for loop
		/*for (ChangedVariable var : changedVars) {
			var.getVariable().fireEvent(IValueChangedListener.CHANGED);
		}*/
		ProViz.getAnimationController().stepAnimation();
		boolean modified = AnimationController.screenModified;
		AnimationController.screenModified = false;
		if (mPainter != null) {
			for (IVizVariable var : removedVars) {
				if (var.getStackFrame() == currentStack) {
					this.notifyMethodPainter(var, Change.REMOVED, mPainter, false);
				}
			}
			for (IVizVariable var : newVars) {
				if (var.getStackFrame() == currentStack) {
					this.notifyMethodPainter(var, Change.NEW, mPainter, false);
				}
			}
			for (ChangedVariable chVar : changedVars) {
				if (chVar.getVariable().getStackFrame() == currentStack) {
					this.notifyMethodPainter(chVar.getVariable(), chVar.getEventType(), mPainter, false);
				}
			}
		}
		ProViz.getAnimationController().stepAnimation();
		if (!AnimationController.screenModified) {
			AnimationController.screenModified = modified;
		}
	}

	/**
	 * TODO Bug with VOO
	 * Processes the list of changed variables based on their change state.
	 * @param changedVars
	 * @param currentStack
	 * @param waitingList
	 */
	private void processChangedVariables(List<ChangedVariable> changedVars,
			Hashtable<String, ArrayList<Painter>> waitingList) {
		//boolean gChanged = false;
		for (ChangedVariable cVar : changedVars) {
			IVizVariable var = cVar.getVariable();
	//ONLY primitive variables and String will be in this case
			if (cVar.getEventType() == Change.VALUE_CHANGED) {// || cVar.getEventType() == Change.FIELD_CHANGE) {
				//if (var.isPrimitive()) {
				continue;
				//}
			}	
	//All old fields of a changed object variable are deallocated.
			if (cVar.getOldFields() != null) {
				for (IVizVariable oldField : cVar.getOldFields()) {
	  //Deallocate previous fields
					this.deallocateVariable(oldField, /*previousPainter,*/ waitingList);
				}
			}
			Painter previousPainter = this.getPainterFromUniqueIDTable(cVar.getPreviousID(), var);
			if (previousPainter == null) {
  //Variable was never visualized with a painter, so no painter creation for any field - no dangling painters
	//So process the current variable and continue
				this.registerListenersForFieldsWithNoPainter(var, waitingList);
				continue;
			}
	//------------------------------------------------------------------
	//--------- All variables below are object variables.---------------
	//----- The changed variable has a painter: previousPainter --------
	//------------------------------------------------------------------
	
	//Handle manually created painters, which must be fields and do not exist in the Viz model
			if (var.getParent() != null) {
				if (!isArray(var.getParent().getActualType())) {
					VariableVizBase vizBase = VizMapModel.getInstance().findFieldViz_runtime(var.getParent().getActualType(), var.getName());
					if (vizBase == null || vizBase.getVizCount() == 0) {
		//previousPainter is manually created. So do not destroy it!
		//VBV for manually created painters
						this.removePainterFromUniqueIDTable(previousPainter);
						previousPainter.setUniqueID(var.getUniqueObjectID());
						this.addToUniqueIDTable(previousPainter);
						continue;
					}
				}
			}
			if (cVar.getEventType() == Change.TO_NULL) {
				if (previousPainter.getVariablesToThisPainter().size() > 1) {
	//previousPainter is still painting other aliasing variables. So remove var from
	//previousPainter and create another painter to paint it
					previousPainter.removeVariableToThisPainter(var);
					this.processVariableAndCreatePainter(var, previousPainter.getParent(), waitingList);
					continue;
				}	else {
	//The previous painter still paints the variable, just update its place in unique ID table
					this.removePainterFromUniqueIDTable(previousPainter);
				}
			}
			/*else if (cVar.getEventType() ==  Change.NULL_TO_OBJ) {
				Painter replacementPainter = this.findCompatiblePainter(var, previousPainter);
				if (replacementPainter == null) {
	//If a variable declared with @DViz and is initialized to null, it'll be visualized with a StringPainter
	//Then when it is assigned to an object,
					Painter parent = previousPainter.getParent();
					this.removeVariableFromPainter(previousPainter, var, waitingList);
					this.processVariableAndCreatePainter(var, parent);
					continue;
				}
				else {
	//VOO, another painter is already painting this object. So switch to the other painter.
					Painter originalParent = previousPainter.getParent();
					removeVariableFromPainter(previousPainter, var, waitingList);
					this.bindVariableAndPainter(var, replacementPainter);
					if (originalParent != null) {
						originalParent.addOrReplaceFieldPainter(var.getName(), replacementPainter);
					}
					continue;
				}
			}*/
			else if (cVar.getEventType() ==  Change.NULL_TO_OBJ || cVar.getEventType() == Change.DIFF_OBJECT_SAME_TYPE 
					|| cVar.getEventType() == Change.DIFF_OBJECT_SAME_TYPE_DIFF_FIELD_SIZE) {
				Painter replacementPainter = this.findCompatiblePainter(var, previousPainter);
				if (replacementPainter != null) {
	//Already a painter of the same type is painting the new object. Switch over to the replacement painter
					Painter originalParent = previousPainter.getParent();
					this.removeVariableFromPainter(previousPainter, var, waitingList);
					this.bindVariableAndPainter(var, replacementPainter);
					if (originalParent != null) {
						originalParent.addOrReplaceFieldPainter(var.getName(), replacementPainter);
					}
					continue;
				}	else {
					if (previousPainter.getVariablesToThisPainter().size() == 1) {
						//TODO
						if (cVar.getEventType() == Change.NULL_TO_OBJ && !this.shouldUseStringPainter(var.getActualType())) {
	//Handles the dynamic loading of generic types
							Association asso = this.getAssociation(var);
							if (asso == null || asso.getVizCount() == 0) {
								ProViz.errprintln("VPM changed variable, NULL_TO_OBJ, annotation not found in Vizes");
							}	else if (asso.getCurrentViz().getPainterName().equalsIgnoreCase(VizMapModel.DEFAULT_TYPE)) {
								String painterName = PainterFactory.getVisualizationName(VizMapModel.DEFAULT_TYPE, var);
								if (!previousPainter.getClass().getName().equals(painterName)) {
									boolean shouldSwitch = true;
									if (var.getName().startsWith("[")) {
										//Association parentAsso = this.getAssociation(var.getParent());
										//TODO 7/6/2011 Did not work for InfixCalculator's stack						
										//if (!parentAsso.getCurrentViz().getPainterName().equalsIgnoreCase(Vizes.DEFAULT_TYPE)) {
	//var is a field of an array, and this array is not annotated with @DViz, so do not switch
											//shouldSwitch = false; 
										//}
									}
									if (shouldSwitch) {
										//this.switchOver(previousPainter, painterName, true, false);
										Painter parentPainter = previousPainter.getParent();
										this.removeVariableFromPainter(previousPainter, var, waitingList);
										this.processVariableAndCreatePainter(var, parentPainter, waitingList);
										ProViz.println("VPM: NULL_TO_OBJ switching painter type");
										continue;
									}
								}
							}
						}
	//previousPainter is painting var only, and will continue to do so
						if (previousPainter.shouldCreateFieldPainters()) {
							for (IVizVariable fieldVar : var.getFields()) {
		//Process new fields of var
								this.processVariableAndCreatePainter(fieldVar, previousPainter, waitingList);//, var.getStackFrame(), waitingList);
							}
						}	else {
							this.registerListenersForFieldsWithNoPainter(var, waitingList);
						}
						this.registerFieldListeners(previousPainter);
						this.removePainterFromUniqueIDTable(previousPainter);
					}	else if (previousPainter.getVariablesToThisPainter().size() > 1) {
	//previousPainter is painting other variables as well. So dis-associate it with var and re-create a new
	//painter for it
						Painter parentPainter = previousPainter.getParent();
						this.removeVariableFromPainter(previousPainter, var, waitingList);
						//Re-create the painter
						this.processVariableAndCreatePainter(var, parentPainter, waitingList);//, var.getStackFrame(), waitingList);
						continue;
					}	else {
						ProViz.errprintln("VPM: painter has less than one owner variable");
					}
				}
			}	else if (cVar.getEventType() == Change.DIFF_OBJECT_DIFF_TYPE) {
	//TODO the assumption is previous painter may not be able to visualize the new type of object
				Painter parentPainter = previousPainter.getParent();
	//Previous painter is painting other variables as well. So dis-associate it with var and re-create a new
	//painter for it
				this.removeVariableFromPainter(previousPainter, var, waitingList);
				//Re-create the painter
				this.processVariableAndCreatePainter(var, parentPainter, waitingList);//, var.getStackFrame(), waitingList);
				continue;
			} //end else if
	//Updates the unique ID in previousPainter and adds it to the unique ID table
			previousPainter.setUniqueID(var.getUniqueObjectID());
			this.addToUniqueIDTable(previousPainter);
			//previousPainter.setPreviousValue(var.getValueAsString());
		} //end for
		for (ChangedVariable cVar : changedVars) {
			cVar.getVariable().fireEvent(cVar.getEventType());

		}
	}
	
	/**
	 * @param className
	 * @return
	 */
	public boolean shouldUseStringPainter(String className) {
		return className.equals("java.lang.String"); //||
			/*className.equals("java.lang.Integer") ||
			className.equals("java.lang.Double") ||
			className.equals("java.lang.Long") ||
			className.equals("java.lang.Float") ||
			className.equals("java.lang.Short") ||
			className.equals("java.lang.Boolean");*/
	}

	/**
	 * Removes a variable's association with painters, including its existence in VPM and
	 * the event listener model. If the variable has a painter, it will be separated from
	 * the painter; afterwards if the painter has no more variable pointing using it, it
	 * will be removed also from VPM and the event-listener model.
	 * @param var
	 * @param topPainters
	 * @param waitingList
	 */
	private void deallocateVariable(IVizVariable var, //Painter parentPainter,
			Hashtable<String, ArrayList<Painter>> waitingList) {
	//Remove associations of the variable first, then remove its painter if any
		//var.fireEvent(Change.REMOVED);
		var.removeAllListeners();
		if (var.isPrimitive()) {
			Painter removedPainter = this.primitivePainters.remove(var);
			if (removedPainter != null) {
				if (removedPainter.getVariablesToThisPainter().size() == 1 
						&& removedPainter.containsVariable(var)) {
					removedPainter.destroy();
				}
				if (removedPainter.removeVariableToThisPainter(var)
						&& removedPainter.getVariablesToThisPainter().isEmpty()) {
					//ProViz.println("1. Removing painter for: " + var.getName() + " " + var.getUniqueObjectID());
					this.removePainterFromWaitingList(removedPainter, waitingList);
	//Remove itself from variables it listens to. The association with its own variable is
	//cut off when the variable removes all listeners
					for (IVizVariable generator : removedPainter.getEventGenerators()) {
						generator.removeListener(removedPainter);
					}
					Painter parentPainter = removedPainter.getParent();
					if (parentPainter != null) {
						if (parentPainter.removeFieldPainter(var.getName()) == null) {
							ProViz.errprintln("VPM: field not found in parent painter: " + var.getName());
						}
					}
				}
			}
			return;
		}
	//Variable is an object
		Painter removedPainter = this.getPainterFromUniqueIDTable(var.getUniqueObjectID(), var);
		if (removedPainter != null) {
			removeVariableFromPainter(removedPainter, var, waitingList);
			for (IVizVariable field : var.getFields()) {
				this.deallocateVariable(field, /*removedPainter,*/ waitingList);
			}
		}	else {
	//The variable did not have a painter. So continue with deallocating the variable's fields
			for (IVizVariable field : var.getFields()) {
				this.deallocateVariable(field, /*null,*/ waitingList);
			}
		}
	}

	/**
	 * Removes an a variable from its painter ONLY and not its fields.
	 * @param removedPainter
	 * @param var
	 * @param waitingList
	 */
	private void removeVariableFromPainter(Painter removedPainter, IVizVariable var,
			Hashtable<String, ArrayList<Painter>> waitingList) {
		if (removedPainter.getVariablesToThisPainter().size() == 1 
				&& removedPainter.containsVariable(var)) {
			removedPainter.destroy();
		}
		if (removedPainter.removeVariableToThisPainter(var) 
				&& removedPainter.getVariablesToThisPainter().isEmpty()) {
			//System.out.println("2. Removing painter for: " + var.getName() + ":" + var.getUniqueObjectID());
			removePainterFromWaitingList(removedPainter, waitingList);
 //Remove itself from variables it listens to
			for (IVizVariable generator : removedPainter.getEventGenerators()) {
				generator.removeListener(removedPainter);
			}
//A painter IS removed from the VPM. If no variables are pointing to this painter, destroy it
//Remove the de-allocated painter from the unique ID table
			this.removePainterFromUniqueIDTable(removedPainter);
			Painter parentPainter = removedPainter.getParent();
			if (parentPainter != null) {
				if (parentPainter.removeFieldPainter(var.getName()) == null) {
					ProViz.errprintln("VPM: field not found in parent painter: " + var.getName());
				}
			}
		} //end if
	}
	
	/*public Painter getParentPainter(VizVariable vvar) {
		Painter parentPainter = null;
		VizVariable parentVar = vvar.getParent();
		if (parentVar != null) {
			parentPainter = this.getPainterFromUniqueIDTable(parentVar.getUniqueObjectID(), parentVar);
		}
		return parentPainter;
	}*/
	
	/**
	 * Removes the given painter from the unique ID table.
	 * @param toBeRemoved
	 * @return true if a painter does get removed; false if nothing happened
	 */
	private boolean removePainterFromUniqueIDTable(Painter toBeRemoved) {
		boolean rv = false;
		String id = toBeRemoved.getUniqueID();
		List<Painter> painters = this.uniqueObjectTable.get(toBeRemoved.getUniqueID());
		if (painters != null) {
			rv = painters.remove(toBeRemoved);
			if (painters.isEmpty()) {
				this.uniqueObjectTable.remove(id);
			}
		}
		return rv;
	}

	/**
	 * Finds listeners (painters) waiting for the VizVariable on the waiting list and register
	 * var with these listeners. Instance variables under 'this' variable need to be renamed
	 * by prefixing with 'this.'
	 * @param var
	 */
	private void registerListeners(IVizVariable var, Hashtable<String, ArrayList<Painter>> waitingList) {
		//Hashtable<String, ArrayList<Painter>> waitingList = this.dependencyWaitingLists.get(var.getStackFrame()); 
	//TODO 10/20/10 prev link crapping out; Temporary fix
		//if (waitingList == null) {
		//	return;
		//}
			//this.dependencyWaitingListStack.peek();
		String name = var.getName();
		if (var.getParent() != null && var.getParent().getName().equals(THIS_VARIABLE)) {
			name = THIS_VARIABLE + "." + name;
		}
		if (waitingList == null) {
			ProViz.errprintln("How can waitingList be null? " + var.getName() + " " + var.getStackFrame().getMethodID());
		}
		ArrayList<Painter> list = waitingList.get(name);
		if (list != null) {
			for (Painter painter : list) {
				var.addListener(painter);
				painter.addEventGenerator(var);
			}
			waitingList.remove(name);
		}
	}

	/**
	 * Process a painter's depending variables. If the depending variables already existed in the top
	 * stack frame, then simply add this painter as the variable's listener; otherwise register this
	 * painter in the waiting list for the depending variable.
	 * @param painter
	 * @param stackFrame
	 * @param waitingList
	 */
	private void registerListenerOrAddToWaitingList(Painter painter, VizStackFrame stackFrame) {
		//,	Hashtable<String, ArrayList<Painter>> waitingList) {
		if (painter.getDependentVars() == null) {
			return;
		}
		for (String dependingVar : painter.getDependentVars()) {
			//if (dependingVar.startsWith("this.")) {
			if (dependingVar.indexOf('.') != -1) {
	//Handling dependency on instance variable of 'this'
				IVizVariable target = null;
				IVizVariable thisVar = stackFrame.getVariable(THIS_VARIABLE);
				if (thisVar == null) {
					ProViz.errprintln("'this' variable does not exist; maybe a static method");
					continue;
				}
	//Find the instance variable in 'this' VizVariable
				String varName = dependingVar.substring(dependingVar.indexOf('.') + 1);
				for (IVizVariable field : thisVar.getFields()) {
					if (field.getName().equals(varName)) {
						target = field;
						break;
					}
				}
				if (target == null) {
					ProViz.errprintln("Painter has a bad dependency for instance variable: " + dependingVar);
				} else {
					target.addListener(painter);
					painter.addEventGenerator(target);
				}
				continue;
			} //end if
	//TODO right now ignore the default type listening to fields
	//10.05.24 Do this after all fields are created
			/*if (dependingVar.startsWith("*")) {
				String fieldName = dependingVar.substring(1);
				continue;
			}*/
			IVizVariable variable = stackFrame.getVariable(dependingVar);
			if (variable == null) {
				Hashtable<String, ArrayList<Painter>> waitingList = this.dependencyWaitingLists.get(stackFrame);
//The dependent variable is not yet created on the stack, so add this painter to the waiting list
				ArrayList<Painter> wait = waitingList.get(dependingVar);
				if (wait != null) {
	//A waiting list for this variable already exists
					wait.add(painter);
				} else { //wait == null
					wait = new ArrayList<Painter>();
					wait.add(painter);
					waitingList.put(dependingVar, wait);
				}
			} else { //variable != null
//The dependent variable is already there, so add this painter as its listener
				variable.addListener(painter);
				painter.addEventGenerator(variable);
			} //end else
		} //end for
	} //end addToWaitingList

	/**
	 * Finds a compatible painter in a list of aliasing painters for a local variable. 
	 * A compatible painter is defined as: 1. a painter painting the same object; 
	 * 2. the painters' types, i.e. classes, are the same. 
	 * @param var
	 * @param painter
	 * @return
	 */
	private Painter findCompatiblePainter(IVizVariable var, Painter painter) {
	//VOO enforced ONLY on local variables
		if (var.isNull() || !var.isLocalVariable()) {
			return null;
		}
		List<Painter> tempList = this.uniqueObjectTable.get(var.getUniqueObjectID());
		Painter rv = null;
		if (tempList != null) {
			for (Painter tempPainter : tempList) {
	//VOO only for root painters
	//Exactly same type of painter
				if (tempPainter.getParent() == null && tempPainter != painter 
						&& tempPainter.getClass().getName().equals(painter.getClass().getName())) {
					rv = tempPainter;
					for (IVizVariable tempVar : tempPainter.getVariablesToThisPainter()) {
	//Priority to return the painer whose variable is in the same stack frame as 'var'
						if (tempVar.getStackFrame().equals(var.getStackFrame())) {
							return tempPainter;
						} //end if
					} //end for
				} //end if
			} //end for
		} //end if
		return rv;
	}

	/**
	 * @return the primitivePainters
	 */
	public Collection<Painter> getPrimitivePainters() {
		return primitivePainters.values();
	}

	/**
	 * @return the uniqueObjectTable
	 */
	public Map<String, List<Painter>> getUniqueObjectTable() {
		return uniqueObjectTable;
	}
	
	/**
	 * Returns the existing painter for this VizVariable. If the painter does not exist
	 * in VPM, returns null.
	 * @param var
	 * @return
	 */
	public Painter getPainter(IVizVariable var) {
		if (var == null) {
			return null;
		}
		Painter rv = null;
		if (var.isObject()) {
			rv = this.getPainterFromUniqueIDTable(var.getUniqueObjectID(), var);
			if (rv == null) {
	//var could be an aliasing painter the is not annotated with a painter
				if (var.getUniqueObjectID().equals(NULL_ID)) {
	//var has null value, so get var's parent painter, if any, and then get var
					if (var.getParent() != null) {
						Painter parent = getPainter(var.getParent());
						if (parent != null) {
							return parent.getFieldPainter(var.getName());
						}
					}
				} else {
					return this.getPainterByUniqueID(var.getUniqueObjectID());
				}
			}
		} else {
	//TODO If the primitive VizVariable is a new variable of a field, this won't work
			rv = this.primitivePainters.get(var);
		}
		return rv;
	}
	
	/**
	 * Gets the arbitrary first painter that paints the object with the give unique ID. <b>This method
	 * should be used ONLY when it is known that only one painter is painting the object</b>. If more
	 * than one painter is painting the object, use getPaintersWithID(String id) to get the list
	 * of painters first and then find out which painter you need.
	 * @param id
	 * @return The first painter in the list of aliasing painters by this ID.
	 */
	private Painter getPainterByUniqueID(String id) {
		//if (id.equals(NULL_ID)) {
		//	
		//}
		List<Painter> list = this.uniqueObjectTable.get(id);
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	
	public List<Painter> getAliasingPainters(String id) {
		return this.uniqueObjectTable.get(id);
	}

	/**
	 * The parameters are an ID and a VizVariable (not just a  because for changed variables, 
	 * when a variable is assigned to another object, the unique ID of the previous 
	 * object is different from the current VizVariable even though it is the same
	 * VizVariable 
	 * @param uniqueID
	 * @param var
	 * @return
	 */
	private Painter getPainterFromUniqueIDTable(String uniqueID, IVizVariable var) {
		List<Painter> painters = this.uniqueObjectTable.get(uniqueID);
		if (painters != null) {
			for (Painter painter : painters) {
				if (painter.containsVariable(var)) {
					return painter;
				}
			}
		}
		return null;
	}
	
	/**
	 * Clears all painters stored in this painter manager but DOES NOT clear the drawing canvas.
	 */
	public void clearAll() {
		this.primitivePainters.clear();
		this.dependencyWaitingLists.clear();
		this.uniqueObjectTable.clear();
		this.methodPainters.clear();
		//ProViz.getInstance().getAnimationController().clear();
		this.canvas.clearAll();
		PainterFactory.clear();
	}
	
	public MethodPainter getMethodPainter(VizStackFrame stackFrame) {
		return this.methodPainters.get(stackFrame);
	}
	
	/**
	 * @return the canvas
	 */
	public VizCanvas getCanvas() {
		return canvas;
	}

	/*public Painter switchOver(Painter current, Visualization viz, boolean noRecall, boolean retainConnectors) {
		viz.getParent().setCurrentViz(viz.getVisualization());
		return this.switchOver(current, viz.getVisualization(), false, true);
	}*/

	/**
	 * TODO Goal: make this switch over so that addToCanvas() is sufficient for most painters
	 * to call in switchedToThisPainter()
	 * @param current The current painter that will be switched to another painter.
	 * @param newPainterName
	 * @param noRecall If true, the new painter's switch() method won't be called; false then switch() will be called.
	 * @param retainConnectors true to keep the same connectors for the new painter; false there will
	 * no connector for the new painter.
	 * @return
	 */
	public Painter switchOver(Painter current, String newPainterName, boolean noRecall, 
			boolean retainConnectors, boolean retainSize) {//, IVizVariable var) {
		if (current == null || current.getClass().getName().equals(newPainterName)) {
			return current;
		}
	//TODO var is used temporarily replace the parameter until future implementation of VOO
		IVizVariable var = null;
		Painter newPainter = null;
	//TODO VOO for the new painter
		
	//Canvas is set in the creation of the new painter
		if (var != null) {
			//TODO newPainter = PainterFactory.createCustomPainter(newPainterName, var, current.getCanvas());
		} else {
			newPainter = PainterFactory.createCustomPainter(newPainterName, current.getVariable(), current.getCanvas());
		}
		if (newPainter == null) {
			ProViz.errprintln("Unable to switch over to painter: " + newPainterName);
			return current;
		}
		boolean voo = false;
		if (current.getVariablesToThisPainter().size() > 1 && var != null) {
			voo = true;
		}
		//newPainter.setDefaultBorderColor(current.getDefaultBorderColor());
		
		//Problem
		//newPainter.setRedirectMouseSelection(current.redirectMouseSelection());
		
		newPainter.setPreviousValue(current.getPreviousValue());
		newPainter.setUniqueID(current.getUniqueID());

	//Establish the new painter and the parent. Parent is set in here.
		if (current.getParent() != null) {
			current.getParent().addOrReplaceFieldPainter(newPainter.getVariable().getName(), newPainter);
		}
		

		//if (!voo) {
	//Switch over the field painters
			if (current.hasFieldPainter()) {
				for (Map.Entry<String, Painter> entry : current.getFieldPainters().entrySet()) {
					newPainter.addOrReplaceFieldPainter(entry.getKey(), entry.getValue());
				}
				current.getFieldPainters().clear();
			}

	//Transfer variables to the new painter
	//The first variable is already transferred when the new painter was created
			for (int i = 1; i < current.getVariablesToThisPainter().size(); i++) {
				newPainter.addVariableToThisPainter(current.getVariablesToThisPainter().get(i));
			}
	//Transfer event-listener model
			for (IVizVariable vvar : current.getEventGenerators()) {
				newPainter.addEventGenerator(vvar);
				vvar.addListener(newPainter);
			}
			for (IVizVariable vvar : newPainter.getEventGenerators()) {
				current.removeEventGenerator(vvar);
				vvar.removeListener(current);
			}
	//Update the VPM storage
			if (newPainter.getVariable().isPrimitive()) {
	//Previous existence of 'current' will be replaced
				for (IVizVariable vvar : newPainter.getVariablesToThisPainter()) {
					this.primitivePainters.put(vvar, newPainter);
				}
			} else {
				this.removePainterFromUniqueIDTable(current);
				this.addToUniqueIDTable(newPainter);
			}
	//Switch over the connectors if desired
			if (retainConnectors) {
				IConnectable other;
				ConnectorManager cManager = current.getCanvas().getConnectorManager();
				for (Connector connector : cManager.getConnectors(current)) {//current.getConnectors()) {
					other = connector.getTheOtherPainter(current);
					cManager.hookUsUp(newPainter, other);
				}
				cManager.removeAll(current);
			}		
		//}
		current.destroy();
		this.callDestroyOnFields(newPainter);
		//newPainter.addToCanvas();
		if (!noRecall) {
			newPainter.switchedToThisPainter();
		}
		newPainter.setLocation(current.getLocation().x, current.getLocation().y);
		if (retainSize) {
			newPainter.setSize(current.getWidth(), current.getHeight());
		}
		newPainter.paint();
		//ProViz.println("Switching " + current + " to " + newPainter);
		this.getCanvas().repaint();
		return newPainter;
	}
	
	/**
	 * Calls destroy() on all painters under the switched painter so that later on in
	 * switchedToThisPainter(), addToCanvas() will then add these painters back under
	 * the new painter. This scheme works because destroy() retains the painter's
	 * integrity so that addToCanvas() can add it back on.
	 * @param painter
	 */
	private void callDestroyOnFields(Painter painter) {
		if (painter.hasFieldPainter()) {
			for (Painter field : painter.getFieldPainters().values()) {
				callDestroyOnFields(field);
				field.destroy();
			}
		}
	}
}
