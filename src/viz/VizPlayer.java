package viz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import viz.animation.AnimationController;
import viz.model.TypeViz;
import viz.model.VizMapModel;
import viz.painters.VizPainterManager;
import viz.runtime.IMethodStateReactor;
import viz.runtime.IVizVariable;
import viz.runtime.MethodDetectionFailException;
import viz.runtime.VizRuntime;
import viz.runtime.VizStackFrame;
import viz.runtime.VizVariable;

/**
 * Plays a Viz recording file produced by VizMonitor. VizPlayer uses the thread approach to
 * step each visualization step. That is, each step of the execution is played by a thread,
 * which finishes after the step ends. Next step will then be visualized by the next thread.
 * @author JW
 *
 */
public class VizPlayer implements IVizController, Runnable {
	private ObjectInputStream ois = null;
	//private boolean isStopped = false;

	private UserSpaceClassLoader loader = new UserSpaceClassLoader();
	

	public VizPlayer(String filename) throws FileNotFoundException, IOException {
		ois = new ObjectInputStream(new FileInputStream(new File(filename)));
	}
	
	public void begin() {
		ProViz.getVPM().getCanvas().repaint();
		ProViz.getAnimationController().getToolBar().enablePlayerButton(false);
		VizMapModel.getInstance().compileRuntimeMaps();
		ProViz.getAnimationController().setController(this);
		try {
			Object obj = readObject();
			if (obj instanceof String && ((String) obj).equals("ProViz")) {
	//Read Vizes's list and populate the vizes 
				TypeViz[] typeViz = (TypeViz[]) readObject();
				for (TypeViz tv : typeViz) {
					VizMapModel.getInstance().addOrReplace(tv, true);
				}
				/*final VizSetEditor editor = ProViz.getInstance().getVizSetEditor();
				if (editor != null) {
					new Thread() {
						public void run() {
							editor.updateAll();
						}
					}.start();
				}*/
				VizMapModel.getInstance().compileRuntimeMaps();
	//The state of the file is a refreshAll().
	//Gets the OTHER
				readInt();
				ProViz.getAnimationController().getToolBar().setStepState();
				ProViz.getAnimationController().setState(AnimationController.State.running);
				VizStackFrame[] frames = this.readStackFrames();//(VizStackFrame[]) readObject();
				//for (VizStackFrame frame : frames) {
				//	this.restoreTransientFields(frame);
				//}
				ProViz.getInstance().getVizRuntime().refreshAllStackFrames(frames);
				ProViz.getVPM().refreshAllStackFrames(frames);
				//step();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(ProViz.getInstance().getFrame(), "Incompatible file format", 
					"ProViz", JOptionPane.ERROR_MESSAGE);
			ProViz.errprintln(e);
			terminate();
		}
	}
	/*
	private void restoreTransientFields(VizStackFrame frame) {
		for (IVizVariable var : frame.getVariables()) {
			this.restoreTransientFields((VizVariable) var, null, frame);
		}
	}
	
	private void restoreTransientFields(IVizVariable[] vars, VizStackFrame frame) {
		for (IVizVariable var : vars) {
			this.restoreTransientFields((VizVariable) var, null, frame); 
		}
	}*/

	private void restoreTransientFields(VizVariable var, IVizVariable parent, VizStackFrame frame) {
		var.setParent(parent);
		var.setStackFrame(frame);
		var.resetListeners();
		for (IVizVariable field : var.getFields()) {
			restoreTransientFields((VizVariable) field, var, frame);
		}
	}

	private IVizVariable[] readVizVariableArray(VizStackFrame frame) throws IOException, ClassNotFoundException {
		IVizVariable[] vars = new IVizVariable[readInt()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = (IVizVariable) readObject();
			this.restoreTransientFields((VizVariable) vars[i], null, frame);
		}
		return vars;
	}
	
	private VizStackFrame readVizStackFrame() throws IOException, ClassNotFoundException {
		VizStackFrame stackFrame = new VizStackFrame((String) readObject());
		int size = readInt();
		ArrayList<IVizVariable> list = new ArrayList<IVizVariable>();
		for (int i = 0; i < size; i++) {
			VizVariable var = (VizVariable) readObject();
			list.add(var);
			this.restoreTransientFields(var, null, stackFrame);
		}
		stackFrame.updateAllVariables(list);
		return stackFrame;
	}

	private VizStackFrame[] readStackFrames() throws IOException, ClassNotFoundException {
		VizStackFrame[] frames = new VizStackFrame[readInt()];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = this.readVizStackFrame();
		}
		return frames;
	}
	
	/**
	 * Reads an integer from the file that is the state, and then depending on the method case,
	 * it reads corresponding number of objects and processes the step.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private boolean processStep() throws IOException, ClassNotFoundException {
		boolean rv = true;
		int state = readInt();
		if (state != VizRecorder.THE_END) {
			VizRuntime vRuntime = ProViz.getInstance().getVizRuntime();
			VizPainterManager vpm = ProViz.getVPM();
			switch (state) {
			case IMethodStateReactor.SAME_METHOD:
				IVizVariable[] vars = this.readVizVariableArray(vRuntime.getTopStackFrame());//(IVizVariable[]) readObject();
				//this.restoreTransientFields(vars, vRuntime.getTopStackFrame());
				vRuntime.sameMethod(vars);
				//System.out.println("Visualizing same method step");
				vpm.sameMethod(null);
				vpm.getCanvas().repaint();
				break;
			case IMethodStateReactor.NEW_METHOD:
				IVizVariable[] varsInPrevStackFrame = this.readVizVariableArray(vRuntime.getTopStackFrame());//(IVizVariable[]) readObject();
				//this.restoreTransientFields(varsInPrevStackFrame, vRuntime.getTopStackFrame());
				VizStackFrame vFrame = this.readVizStackFrame();//(VizStackFrame) readObject();
				//this.restoreTransientFields(vFrame);
				vRuntime.newMethod(vFrame, varsInPrevStackFrame, false);
				vpm.newMethod(vFrame, null, false);
					//MethodPainter mPainter = vpm.getMethodPainter(vFrame);
					//if (mPainter != null) {
						//this.shouldStepInto = mPainter.shouldContinueMethod();
					//}
				vpm.getCanvas().repaint();
				break;
			case IMethodStateReactor.RETURN_FROM_METHOD:
				try {
					String previous = (String) readObject();
					VizStackFrame previousStack = vRuntime.returnFromMethod(previous, null);//, vars);
					vpm.returnFromMethod(previous, previousStack);//, vars);
					vpm.getCanvas().repaint();
				} catch (MethodDetectionFailException e) {
					ProViz.errprintln(e);
	//Gets the IMethodStateReactor.OTHER integer
					readInt();
					VizStackFrame[] frames = this.readStackFrames();//(VizStackFrame[]) readObject();
					//for (VizStackFrame frame : frames) {
					//	this.restoreTransientFields(frame);
					//}
					vRuntime.refreshAllStackFrames(frames);
					vpm.refreshAllStackFrames(frames);
					vpm.getCanvas().repaint();
				}
				break;
			case IMethodStateReactor.OTHER:
				VizStackFrame[] frames = this.readStackFrames();//(VizStackFrame[]) readObject();
				//for (VizStackFrame frame : frames) {
				//	this.restoreTransientFields(frame);
				//}
				vRuntime.refreshAllStackFrames(frames);
				vpm.refreshAllStackFrames(frames);
				vpm.getCanvas().repaint();
				break;
			case	VizMonitor.USER_SPACE_CLASS:
				//try {
					ProViz.println("VizPlayer loading class from file 2");
					//System.out.println(readInt());
					byte[] raw = (byte[]) readObject();
					loader.setRawClass(raw);
				/*} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}*/
				//readObject();
				break;
			default:
				ProViz.errprintln("VizPlayer: Unknown method state");
				break;
			} //end switch
		} //end if
		else {
			rv = false;
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see viz.VizController#terminate()
	 */
	@Override
	public void terminate() {
		ProViz.println("VizPlayer terminate()");
		ProViz.getInstance().clearAll();
		if (ois != null) {
			try {
				ois.close();
				ois = null;
			} catch (IOException e) {
				ProViz.errprintln(e);
			}
		}
		ProViz.getAnimationController().getToolBar().enablePlayerButton(true);
	}
	
	private Object readObject() throws IOException, ClassNotFoundException {
		Object obj = ois.readObject();
		//System.out.println(obj);
		return obj;
	}
	
	
	private int readInt() throws IOException {
		int obj = ois.readInt();
		//System.out.println(obj);
		return obj;
	}

	@Override
	public void run() {
		if (ois != null) {
			step2();
		}
	}

	/**
	 * Starts a new thread running the run() method of this class only when the animation is not
	 * in the STOP state.
	 * @see viz.IVizController#step()
	 */
	@Override
	public void step() {
		if (ProViz.getAnimationController().getState() != AnimationController.State.stop) {
			new Thread(this).start();
		}
	}
	
	public void step2() {
		//if (this.isStopped) {
			//return;
		//}
		AnimationController.screenModified = false;
		try {
			if (processStep()) {
				ProViz.getAnimationController().stepControl();
			} //end if
			else {
				//System.out.println("terminate 1");
				terminate();
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(ProViz.getInstance().getFrame(), "Incorrect file format");
			ProViz.errprintln(e);
			terminate();
		}
	} //end play
	
	@Override
	public Class<?> loadClass(String classPath) {
		Class<?> clas = null;
		try {
			clas = loader.loadClass(classPath);
			ProViz.println("VizPlayer class loaded: " + clas);
			//loadedClasses.put(classPath, clas);
		} catch (ClassNotFoundException e) {
			ProViz.errprintln(e);
			this.terminate();
		}
		return clas;
	}
	
	private class UserSpaceClassLoader extends ClassLoader {
		private byte[] raw = null;
		public UserSpaceClassLoader() {
			super();
		}
		
		/**
		 * This method must be called before every loadClass() call to set the data to read from.
		 * @param raw
		 */
		public void setRawClass(byte[] raw) {
			this.raw = raw;
		}
		
		@SuppressWarnings("unchecked")
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#findClass(java.lang.String)
		 */
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			Class clas = null;
			try {
				clas = this.getClass().getClassLoader().loadClass(name);
				ProViz.println("Class already loaded 2: " + clas);
				return clas;
			}
			catch (ClassNotFoundException e) {
			}
			if (raw == null) {
				try {
			//Read from the file
					processStep();
				} catch (Exception e) {
					ProViz.errprintln(e);
					return null;
				}
			}
			clas = this.defineClass(name, raw, 0, (int) raw.length);
			ProViz.println("VizPlayer loading class 1: " + clas);
			this.raw = null;
			return clas;
		}
	} //end class
} //end class
