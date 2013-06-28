package viz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import viz.animation.AnimationController;
import viz.model.MethodViz;
import viz.model.Visualization;
import viz.model.VizMapModel;
import viz.painters.PainterFactory;
import viz.painters.VizPainterManager;
import viz.painters.method.MethodPainter;
import viz.runtime.EclipseVizRuntimeAdapter;
import viz.runtime.IMethodStateReactor;
import viz.runtime.IVizVariable;
import viz.runtime.MethodDetectionFailException;
import viz.runtime.VizRuntime;
import viz.runtime.VizRuntimeException;
import viz.runtime.VizStackFrame;

/**
 * Monitors and controls a program's execution. At each step, it analyzes the 
 * stack frames to determine the state of method calls and provides Viz painters
 * with IVizRuntime for painting visualizations.
 * 
 * Currently this monitors single thread only. 
 * 
 * Three tasks: 1. See if current step is a method call. 2. Find out what variables on the stack frame
 * are created, changed, or removed. 3. Find out what fields are changed, created, removed. 
 * 
 * Everything starts with handleDebugEvents(), since ProViz passively waits for
 * debug events. Call sequences:
 * 1. handleDebugEvents() -> init() [Processes all stack frames]
 * 2. handleDebugEvents()
 *      -> processStep() finds out whether to step into/over, whether to visualize
 *      -> proceedWithVisualization() calls corresponding methods in VizRuntime and VPM
 * 
 * TODO Currently requires a breakpoint to be put at the starting 
 * point of the program (main method for example).
 * @author John
 */
public class VizMonitor implements IDebugEventSetListener, IVizController {
	private static final String JAVA_LANG_THREAD_EXIT = "java.lang.Thread:exit()";
	private static VizMonitor theInstance = null;
	
	private static VizRecorder recorder = null;

	private IStackFrame previous = null;
	private String previousMethodID = null;
	private int previousNumOfStacks = 0;
	private String projectBinPath = null;
	
	//This value cannot be the same as any value in IMethodStateReactor
	public static final int USER_SPACE_CLASS = 5;

	public static final String MAIN_METHOD = ":main(java.lang.String[])";

	//These are local variables for the step(...) call..
	private IThread currentThread = null;
	private boolean shouldStepInto = true;
	private String methodID = null;
	private boolean hasTopStackFrame = false;
	
	//private boolean autoStepping = false;
	private boolean recordOnly = false;

	private UserSpaceClassLoader loader = null;
	
	private VizMonitor() {}
	
	/**
	 * Returns the singleton VizMonitor.
	 * @return the singleton object of VizMonitor
	 */
	public static VizMonitor getInstance() {
		if (theInstance == null) {
			theInstance = new VizMonitor();
		}
		return theInstance;
	}

	/**
	 * This method analyzes each debug event and dispatches ProViz-related events to ProViz. Events
	 * including: 1. Breakpoint events (indicating the start of monitoring); 2. Step events;
	 * 3. Create events (DebugTarget or TODO threads); 4. Terminate events (end program).
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		if (events == null) {
			ProViz.errprintln("Null debug event set!");
			return;
		}
	//Do not proceed when VizPlayer is in action, which means that the VizController in AnimationController
	//is not this VizMonitor, and that the animation state is not STOP.
		if ((ProViz.getAnimationController().getVizController() != this 
				&& ProViz.getAnimationController().getState() != AnimationController.State.stop) 
				|| VizMapModel.getInstance().getVizSize() < 1) {
			//System.out.println("VizRuntime will not start");
			return;
		}
		for (DebugEvent de : events) {
			if (de.getKind() == DebugEvent.SUSPEND && de.getDetail() == DebugEvent.BREAKPOINT 
					&& previous == null && de.getSource() instanceof IThread) {
				try {
					init((IThread) de.getSource());
				} catch (Exception e) {
					ProViz.errprintln(e);
				} //end catch
			} //end if
			if (de.getKind() == DebugEvent.SUSPEND && de.getDetail() == DebugEvent.STEP_END 
					&& de.getSource() instanceof IThread) {
				//long start = System.currentTimeMillis();
				//System.out.println("1 " + start);
				try {
					processStep((IThread) de.getSource());
				} catch (DebugException e) {
					ProViz.errprintln(e);
					e.printStackTrace();
				}
				//long end = System.currentTimeMillis();
				//System.out.println("2  " + end + " " + (end - start));
			} else if (de.getKind() == DebugEvent.CREATE) {
				if (de.getSource() instanceof IDebugTarget) {
					VizMapModel.getInstance().compileRuntimeMaps();
				} //end if
			} else if (de.getKind() == DebugEvent.TERMINATE && de.getSource() instanceof IThread) {
				//System.out.println("DebugEvent.TERMINATE");
				clearAll();
			} //end if
		} //end for
	} //end handleDebugEvents
	
	/**
	 * A thread to prompt the user for file choosing. Need to do multi-threading
	 * because the main debug process cannot be hanged by the file chooser.
	 * @author John
	 */
	class ChooserThread implements Runnable {
		//private Shell activeShell;
		private String mainFile;
		public ChooserThread(String mainFile) {
			this.mainFile = mainFile;
		}
		@Override
		public void run() {
	      //this.activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();//new Shell (display);
				//shell.setSize(0, 0);
				//shell.open ();
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
	      fd.setText("Save Recording");
	      fd.setFileName(mainFile.substring(mainFile.lastIndexOf('/') + 1, mainFile.lastIndexOf('.')) + ".viz");
	      fd.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + 
	      		mainFile.substring(0, mainFile.lastIndexOf("/")));
	      fd.setFilterExtensions(new String[] {"*.viz", "*.java"});
	      mainFile = fd.open();
	      synchronized (this) {
	      	this.notify();
	      }
		}
		
		/**
		 * Gets the absolute path of the file chosen by the user.
		 * @return
		 */
		public String getFile() {
			return this.mainFile;
		}
	}

	/**
	 * Starts ProViz visualization by setting up all the necessary data.
	 * It also asks if the user wants to save the recording to file.
	 * @param currentThread
	 * @throws DebugException
	 * @throws VizRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	private void init(IThread currentThread) throws DebugException, VizRuntimeException {
		k = 0;
		ProViz.getVPM().getCanvas().repaint();
		ProViz.getAnimationController().setController(this);
		IStackFrame top = currentThread.getTopStackFrame();
		if (top instanceof JDIStackFrame) {
	//Gets two things: (1) the file with the main method; (2) the project's
	//"bin" folder, in order to load the classes later on
			//try {
			String projectName = "";
			String mainFile = "";
			try {
				Map attributes = currentThread.getLaunch().getLaunchConfiguration().getAttributes();
				//System.out.println(attributes);
				projectName = (String) attributes.get("org.eclipse.jdt.launching.PROJECT_ATTR");
	//Example: /Visualization/src/test/Test.java
				mainFile = (String) ((ArrayList) attributes.get("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS")).get(0);
				//mainFile = mainFile.substring(mainFile.lastIndexOf('/') + 1, mainFile.lastIndexOf('.'));
				IFolder binFolder = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFolder("bin");
				if (binFolder.exists()) {
					this.projectBinPath = binFolder.getLocation().toOSString();
				} else {
					this.projectBinPath = null;
				}
			} catch (CoreException e1) {
				ProViz.errprintln(e1);
				e1.printStackTrace();
			}
	    ChooserThread thread = new ChooserThread(mainFile);
	//Avoid the thread problem in Eclipse by using another thread to do file choosing
	    PlatformUI.getWorkbench().getDisplay().asyncExec(thread);
	    synchronized (thread) {
		    try {
					thread.wait();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
	    }
      String selected = thread.getFile();//fd.open();
			VizStackFrame[] frames = EclipseVizRuntimeAdapter.convertAllStackFrames(
					currentThread.getStackFrames());
	//If the user cancels file choosing, there will be no recording
			//if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (selected != null) {
				//filePath = chooser.getSelectedFile().getAbsolutePath();
				try {
					ProViz.println("Open file: " + selected);//filePath);
					recorder = new VizRecorder(selected);//filePath);
					recorder.writeObject(VizMapModel.getInstance().getTypeList());
					recorder.writeInt(IMethodStateReactor.OTHER);
	/////////////////////////////////////////////////////
					recorder.writeInt(frames.length);
					for (VizStackFrame sFrame : frames) {
						this.writeStackFrame(sFrame);
					}
	/////////////////////////////////////////////////////
					//recorder.writeObject(frames);
				} catch (Exception e) {
					recorder = null;
					ProViz.errprintln(e);
				}
	    } else {
				//JOptionPane.showMessageDialog(ProViz.getInstance().getFrame(), 
				//		"Visualization won't be saved!", "ProViz", JOptionPane.INFORMATION_MESSAGE);
			}
			ProViz.getInstance().setStatusLine("Press the Start button to begin");
			if (!recordOnly) {
				ProViz.getInstance().getVizRuntime().refreshAllStackFrames(frames);
				ProViz.getVPM().refreshAllStackFrames(frames);
			}
			this.previous = top;
			this.previousMethodID = EclipseVizRuntimeAdapter.getMethodID((JDIStackFrame) top);
			this.previousNumOfStacks = currentThread.getStackFrames().length;
			this.currentThread = currentThread;
			this.methodID = previousMethodID;
			this.hasTopStackFrame = true;
			ProViz.getAnimationController().setState(AnimationController.State.stepping);
			ProViz.getAnimationController().getToolBar().setStepState();
		}
	}
	
	/**
	 * Processes a step based on the state of the method call and decides 
	 * whether visualization should occur. If visualization is to be proceeded, 
	 * it calls 'proceedWithVisualization' which updates the Viz Runtime model 
	 * and asks VPM to perform the visualization.
	 * <p>1. When a new method is called or when the method call is out of control 
	 * (IMethodStateReactor.OTHER), if current method is in the Viz model, should visualize.
	 * Otherwise should not visualize.</p>
	 * <p>2. When returning from a method, if the current method is not in the Viz model (which is
	 * very unlikely unless the method calls are out of control) then do not visualize. Otherwise,
	 * if the method we returned from is in the Viz model, then visualize; otherwise don't.</p>
	 * <p>3. The rest of the cases, SAME_METHOD and BACK_TO_DRIVER_OR_EXIT, simply visualize.</p>
	 * @param currentThread
	 * @throws DebugException
	 */
	private void processStep(IThread currentThread) throws DebugException {
		//this.currentThread = currentThread;
		//String methodID = new String();
		AnimationController.screenModified = false;
		boolean hasTopStackFrame = false;
		shouldStepInto = true;
		boolean shouldVisualize = false;
		int methodState = IMethodStateReactor.OTHER;
		IStackFrame top = null;
		top = currentThread.getTopStackFrame();
		if (top != null && top instanceof JDIStackFrame) {
			hasTopStackFrame = true;
			JDIStackFrame jdiFrame = (JDIStackFrame) top;
			IStackFrame[] frames = currentThread.getStackFrames();
			methodID = EclipseVizRuntimeAdapter.getMethodID(jdiFrame);
  //After processing method call, must set previous to top and set previous number of stacks
			methodState = this.analyzeMethodCall(top, frames, methodID);

			if (methodState == IMethodStateReactor.NEW_METHOD || 
					methodState == IMethodStateReactor.OTHER) {
  //If the method is not in the Viz Map model, step-return. Otherwise step-into.
				MethodViz mv = VizMapModel.getInstance().findMethodViz_runtime(methodID);
				if (mv != null) {
  //Method is in the Viz model
  //Checks if the method has painter annotation
					if (mv.getVizCount() > 0) {						
  //09.09.30 Method viz is performed in VPM, since the Runtime Model is updated then
	//TODO 5/22/10 Step return a method that has a method painter
						
						//shouldStepInto = false;
					}
					shouldVisualize = true;
				} else {
					//Step return
					shouldStepInto = false;
				}
/*****************************************************/
			} else if (methodState == IMethodStateReactor.RETURN_FROM_METHOD) {
//If the method is not in the Viz model, step-return. Otherwise step-into.
//Technically, this check is unnecessary if stepping progresses normally.
//But if unexpected "OTHER" occurs, this might be necessary.
				if (VizMapModel.getInstance().findMethodViz_runtime(methodID) == null) {
					shouldStepInto = false;
				} else {
					//System.out.println("VizMonitor: Previous methodID: " + previousMethodID);
					if (VizMapModel.getInstance().findMethodViz_runtime(this.previousMethodID) != null) {
						shouldVisualize = true;
					} else {
						//System.out.println("VizMonitor: Returning from a library method, so do not visualize.");
					}
				}
/*****************************************************/
//SAME_METHOD or BACK_TO_DRIVER_OR_EXIT. Simply continue with step-into if not exit
			} else {
				if (methodID.equals(JAVA_LANG_THREAD_EXIT)) {
					terminate();//currentThread);
					return;
				}
				shouldVisualize = true;
			}
	//Entry to the visualization processing
			if (shouldVisualize) {
				try {
					proceedWithVisualization(top, methodID, methodState, 
							top.getVariables(), previousMethodID, frames);
				} catch (Exception e) {
					ProViz.errprintln(e);
					terminate();
				}
			}			
			this.previous = top;
			this.previousMethodID = methodID;
			this.previousNumOfStacks = frames.length;
			//VizRuntime.getInstance().printStackFrames();
			//VizPainterManager.getInstance().printFrames();
		} //end if
		this.hasTopStackFrame = hasTopStackFrame;
		//this.methodID = methodID;
		//step(currentThread, shouldStepInto, methodID, hasTopStackFrame);
		ProViz.getAnimationController().stepControl();
	}
	
	/**
	 * Makes a step in the program execution. Whether the step should be stepInto or stepReturn is
	 * analyzed in processStep(IThread) and stored in 'shouldStepInto' variable.
	 */
	public void step() {
		if (methodID == null || currentThread == null) {
			return;
		}
		if (!methodID.equals(JAVA_LANG_THREAD_EXIT) && currentThread != null && hasTopStackFrame) {
			try {
				if (ProViz.getAnimationController().getState() == AnimationController.State.stepOver) {
					currentThread.stepOver();
				} else if (ProViz.getAnimationController().getState() == AnimationController.State.stepReturn) {
					currentThread.stepReturn();
				} else {
					if (shouldStepInto) {
						//System.out.println("Continue with step-into");
						currentThread.stepInto();
					} else {
						//System.out.println("Continue with step-return");
						currentThread.stepReturn();
					}
				}
			} catch (DebugException e) {
				ProViz.errprintln(e);
			}
		} else {
			terminate();//currentThread);
		} //end else
	}
	/*
	private void step(IThread currentThread, boolean shouldStepInto, String methodID, boolean hasTopStackFrame) {
	//TODO Currently assume there is only one main thread running
	//TODO Ending the program using Thread.exit() might not be a good idea
		if (!methodID.equals("java.lang.Thread:exit()") && currentThread != null && hasTopStackFrame) {
			try {
				if (shouldStepInto) {
					System.out.println("Continue with step-into");
					currentThread.stepInto();
				}
				else {
					System.out.println("Continue with step-return");
					currentThread.stepReturn();
				}
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		else {
			exit(currentThread);
		} //end else
	}*/
	
	/*
	 * @param currentThread
	 *
	private void exit(IThread currentThread) {
		if (currentThread.getDebugTarget().canTerminate()) {
			clearAll();
			try {
				System.out.println("EXIT!!!");
				currentThread.getDebugTarget().terminate();
			} catch (DebugException e) {
				e.printStackTrace();
			} //end catch
		} //end if
	}*/
	
	/* (non-Javadoc)
	 * @see viz.IVizController#terminate()
	 */
	@Override
	public void terminate() {
		IThread thread = currentThread;
		clearAll();
		if (thread != null) {
			if (thread.getLaunch().canTerminate()) {
				try {
					ProViz.println("EXIT!!!");
					//thread.getDebugTarget().terminate();
					//thread.terminate();
					thread.getLaunch().terminate();
				} catch (DebugException e) {
					ProViz.errprintln(e);
				} //end catch
			} else {
				ProViz.println("Launch cannot be terminated");
			}
		}
	}
	
	/**
	 * Resets everything in VizMonitor and initiates clearAll() on the entire ProViz.
	 */
	private void clearAll() {
		previousMethodID = null;
		previous = null;
		previousNumOfStacks = 0;
		//this.autoStepping = false;
		this.currentThread = null;
		this.hasTopStackFrame = false;
		this.shouldStepInto = true;
		this.methodID = null;
		this.loader = null;
		this.projectBinPath = null;
		ProViz.getInstance().clearAll();
		if (recorder != null) {
			recorder.close();
			recorder = null;
		}
	}
	int k = 0;
	/**
	 * @param frame
	 * @param methodID
	 * @param methodState
	 * @param variables Could be empty (size 0) but can't be null
	 * @param previous
	 * @param iFrames
	 * @throws VizRuntimeException
	 */
	protected void proceedWithVisualization(IStackFrame frame, String methodID, int methodState, 
			IVariable[] variables, String previous, IStackFrame[] iFrames) throws VizRuntimeException {
		VizRuntime vRuntime = ProViz.getInstance().getVizRuntime();
		VizPainterManager vpm = ProViz.getVPM();
		//ProViz.println(k + "");
		k++;
		if (recorder != null) {
			if (k % 100 == 99) {
	//Resets recorder's buffer; otherwise it fills up and die
				recorder.reset();
			}
		}
		if (methodState == IMethodStateReactor.SAME_METHOD) {
	//Refresh all variables in the top stack frame
	//vars could be empty but can't be null
	//TODO Future improvement: Do checkAndUpdateChangedVariables here
			IVizVariable[] vars = this.convertVariables(variables, vRuntime.getTopStackFrame());
			if (recorder != null) {
				recorder.writeInt(IMethodStateReactor.SAME_METHOD);
				this.writeVizVariableArray(vars);
			}
			if (!recordOnly) {
				vRuntime.sameMethod(vars);
				//System.out.println("Visualizing same method step");
				vpm.sameMethod(null);
				vpm.getCanvas().repaint();
			}
		} else if (methodState == IMethodStateReactor.RETURN_FROM_METHOD 
				|| methodState == IMethodStateReactor.BACK_TO_DRIVER_OR_EXIT) {
	//Returning from a method does not change any value; the next step-into would 
	//then assign the return value to a variable.
			//final VizVariable[] vars = getVarInTopFrame(variables);
			//System.out.println("Visualizing return from method step");
			try {
				if (recorder != null) {
					recorder.writeInt(IMethodStateReactor.RETURN_FROM_METHOD);
					recorder.writeObject(previous);
				}
				if (!recordOnly) {
					VizStackFrame previousStack = vRuntime.returnFromMethod(previous, null);//, vars);
					vpm.returnFromMethod(previous, previousStack);//, vars);
					IVizVariable[] vars = this.convertVariables(variables, vRuntime.getTopStackFrame());
					vRuntime.sameMethod(vars);
					vpm.sameMethod(null);
					vpm.getCanvas().repaint();
				}
			} catch (MethodDetectionFailException e) {
				ProViz.errprintln(e);
				refreshAll(iFrames);
			//	System.out.println("VizMonitor: Failed! Visualizing refreshing all stack frames");
			}
		} else if (methodState == IMethodStateReactor.NEW_METHOD) {
			IVizVariable[] varsInPrevStackFrame = null;
	//Checks previous stack frame for changes, occurred due to possible one-line code
			if (iFrames.length > 1) {
				try {
					varsInPrevStackFrame = convertVariables(iFrames[1].getVariables(), 
					    vRuntime.getTopStackFrame());
				} catch (DebugException e) {
					ProViz.errprintln(e);
					e.printStackTrace();
				}
			}
			VizStackFrame vFrame = EclipseVizRuntimeAdapter.convertStackFrame(methodID, frame);
			if (recorder != null) {
				recorder.writeInt(IMethodStateReactor.NEW_METHOD);
				this.writeVizVariableArray(varsInPrevStackFrame);
				this.writeStackFrame(vFrame);
			}
			if (!recordOnly) {
				vRuntime.newMethod(vFrame, varsInPrevStackFrame, false);
				vpm.newMethod(vFrame, null, false);
				MethodPainter mPainter = vpm.getMethodPainter(vFrame);
				if (mPainter != null) {
					this.shouldStepInto = mPainter.shouldContinueMethod();
				}
				vpm.getCanvas().validate();
			} else {
				MethodViz mViz = VizMapModel.getInstance().findMethodViz_runtime(vFrame.getMethodID());
				if (mViz != null) {
					Visualization viz = mViz.getCurrentViz();
					if (viz != null) {
						MethodPainter mPainter = PainterFactory.createMethodPainter(viz.getPainterName(), 
								vFrame, vpm.getCanvas());
						if (mPainter != null) {
							this.shouldStepInto = mPainter.shouldContinueMethod();
						}
					}
				}
			}
		} else { 
	//methodState == IMethodStateReactor.OTHER
	//Unknown events happened, so refreshes ALL stack frames
			ProViz.println("VizMonitor - OTHER: refreshing all stack frames");
			refreshAll(iFrames);
		}
	}

	private void writeStackFrame(VizStackFrame vizFrame) {
		recorder.writeObject(vizFrame.getMethodID());
		recorder.writeInt(vizFrame.getVariables().size());
		for (IVizVariable var : vizFrame.getVariables()) {
			recorder.writeObject(var);
		}
	}
	
	private void writeVizVariableArray(IVizVariable[] variables) {
		recorder.writeInt(variables.length);
		for (IVizVariable var : variables) {
			recorder.writeObject(var);
		}
	}
	
	/**
	 * Analyzes the stack frames to find the state of method calls, described by constants 
	 * in IMethodStateReactor
	 * @param top
	 * @param frames
	 * @param methodID
	 * @return
	 */
	private int analyzeMethodCall(IStackFrame top, IStackFrame[] frames, String methodID) {
		int returnState = IMethodStateReactor.OTHER;
		//Figure out if this is a new method call
		//Need to know if this is a new method call. If it is: 
		//1. See if it is in the Viz model. If not, step return this method (shouldStepReturn is set to true)
		//2. Retrieve the MethodViz to see whether it has a VC
		if (previous == top) {
			//Remain within the same method
			if (this.previousNumOfStacks == frames.length) {
				returnState = IMethodStateReactor.SAME_METHOD;
			} else {
				returnState = IMethodStateReactor.OTHER;
			}
		} else if (frames.length > 1) {
			/************************************
			if (previous != null) {
				try {
					System.out.println("Previous: " + previous.getName() + ", stack length: " + this.previousNumOfStacks);
					System.out.println("Current: " + top.getName() + ", stack length: " + frames.length);
				} catch (DebugException e) {
					e.printStackTrace();
				}
			}
			************************************/
			if (previous == frames[1] || previous == null) {
	//New method call since previous stack frame becomes second to the top
				//System.out.println("New method call: " + methodID);
				returnState = IMethodStateReactor.NEW_METHOD;
			} else {
	//Could be returning from a method or an unknown number of method stacks were created
				if (this.previousNumOfStacks == frames.length + 1) {
					//System.out.println("Return from a method: " + methodID);
					returnState = IMethodStateReactor.RETURN_FROM_METHOD;
				} else {
					//System.out.println("Irregular number of method calls!");
					returnState = IMethodStateReactor.OTHER;
				}
			}
		} else {
	//Frame length == 1
			//System.out.println("Back to the first (main) method");
			returnState = IMethodStateReactor.BACK_TO_DRIVER_OR_EXIT;
		} //end else
		return returnState;
	}
	
	/**
	 * @param iFrames
	 * @param enableVPM
	 * @throws VizRuntimeException
	 */
	private void refreshAll(IStackFrame[] iFrames)
			throws VizRuntimeException {
		VizStackFrame[] frames = EclipseVizRuntimeAdapter.convertAllStackFrames(iFrames);
		if (recorder != null) {
			recorder.writeInt(IMethodStateReactor.OTHER);
  /////////////////////////////////////////////////////
			recorder.writeInt(frames.length);
			for (VizStackFrame sFrame : frames) {
				this.writeStackFrame(sFrame);
			}
  /////////////////////////////////////////////////////
			//recorder.writeObject(frames);
		}
		if (!recordOnly) {
			ProViz.getInstance().getVizRuntime().refreshAllStackFrames(frames);
			ProViz.getVPM().refreshAllStackFrames(frames);
			ProViz.getVPM().getCanvas().repaint();
		}
	}

	/**
	 * 
	 * @param variables
	 * @return An array of IVizVariables converted from the parameter
	 */
	private IVizVariable[] convertVariables(IVariable[] variables, VizStackFrame stackFrame) {
	//2010.1.3 Changed to directly using an array
		//List<VizVariable> vizVars = new ArrayList<VizVariable>();
		IVizVariable[] vars = new IVizVariable[variables.length];
		for (int i = 0; i < variables.length; i++) {
			try {
				vars[i] = EclipseVizRuntimeAdapter.convertVariable(variables[i], 
						null, stackFrame);
			} catch (VizRuntimeException e) {
				ProViz.errprintln(e);
			}
		}
		//VizVariable[] vars = vizVars.toArray(new VizVariable[0]);
		return vars;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class loadClass(String classPath) {
		if (this.projectBinPath == null) {
			return null;
		}
		Class clas = null;
		try {
			if (loader == null) {
				loader = new UserSpaceClassLoader(this.projectBinPath);
			}
			clas = loader.loadClass(classPath);
		} catch (FileNotFoundException e) {
			ProViz.errprintln(e);
		} catch (ClassNotFoundException e) {
			ProViz.errprintln(e);
		}
		return clas;
	}
	
	private class UserSpaceClassLoader extends ClassLoader {
		private String binFolder;
		public UserSpaceClassLoader(String bin) throws FileNotFoundException {
			super();
			binFolder = bin;
			File folder = new File(bin);
			if (!folder.isDirectory()) {
				throw new FileNotFoundException(bin + " folder not found");
			}
		}
		
		@SuppressWarnings("rawtypes")
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#findClass(java.lang.String)
		 */
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			Class clas = null;
			try {
				clas = this.getClass().getClassLoader().loadClass(name);
				return clas;
			} catch (ClassNotFoundException e) {
			}
			
			String filename = binFolder + '/' + name.replace('.', '/') + ".class";
			//System.out.println("Loading class: " + filename);
			File file = new File(filename);
			if (file.exists()) {
			// Find out the length of the file
				long len = file.length();
				// Create an array that's just the right size for the file's
				// contents
				byte raw[] = new byte[(int)len];
				// Open the file
				FileInputStream fin;
				try {
					fin = new FileInputStream(file);				
					int r = fin.read(raw);
					fin.close();
					if (r != len) {
						throw new IOException( "Can't read all, " + r + " != " + len );
					}
				} catch (Exception e) {
					ProViz.errprintln(e);
					return super.findClass(name);
				}
				// And finally return the file contents as an array
				clas = this.defineClass(name, raw, 0, (int) len);
				ProViz.println("Class loaded: " + clas);
				if (recorder != null) {
					ProViz.println("VizMonitor writing class to file");
					recorder.writeInt(VizMonitor.USER_SPACE_CLASS);
					recorder.writeObject(raw);
				}
				return clas;
			}
			return super.findClass(name);
		}
	} //end class
}