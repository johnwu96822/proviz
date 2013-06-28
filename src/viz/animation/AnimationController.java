package viz.animation;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JSlider;

import viz.ProViz;
import viz.IVizController;
import viz.animation.motion.Motion;
import viz.animation.motion.Sleep;
import viz.painters.IPainter;
import viz.painters.Painter;
import viz.views.eclipse.AnimationToolBar;
import java.util.Hashtable;
import java.util.ArrayList;
/**
 * Controls how animations are performed in ProViz. One must set the VizController at the
 * very beginning of the visualization by calling setVizController().
 * 
 * An animation is performed by an AnimationThread, and AnimationController creates one
 * for every painter that requests animation. Each animation thread created is added to
 * the schedule, and when VPM finishes updating its model, it then asks this controller
 * to start animating the step. All animation threads in the schedule will then be started.
 * When an animation thread finishes, it calls the finish(AnimationThread) method to
 * notify this controller that it is done, and then that thread is removed from the
 * schedule. When all animation threads are done, the schedule is empty, then the main
 * thread is notified and continues, finishing this step.
 * 
 * For any painter which wants to pause the visualization until the user presses start
 * buttons, it needs to call customPause() to set the
 * @author Jo-Han Wu
 */
public class AnimationController {
	public static enum State {stop, running, pause, stepping, next, customPause, stepOver, stepReturn};
	/*public static final int STOP = 0;
	public static final int RUNNING = 1;
	public static final int PAUSE = 2;
	public static final int STEPPING = 3;
	public static final int NEXT = 4;
	public static final int CUSTOM_PAUSE = 5;
	
	public static final int STEP_OVER = 6;
	public static final int STEP_RETURN = 7;
	*/
	private static volatile State state = State.stop;
	//private static AnimationController theInstance;
	public static boolean screenModified = false;
	
	//private boolean pauseAtTheEndOfStep = false;

	//private List<AnimationThread> schedule = new LinkedList<AnimationThread>();

	private Map<IPainter, List<AnimationThread>> schedule2 = new Hashtable<IPainter, List<AnimationThread>>();
	
	private IVizController controller = null;
	
  private JSlider speedMeter;
  private JCheckBox checkBox;
  
  private AnimationToolBar aToolBar;
	
  public AnimationController() {
    this.speedMeter = new JSlider(-20, 100, 20);
    this.speedMeter.setPreferredSize(new Dimension(80, 0));
    this.speedMeter.setToolTipText("Simulation Speed");
    this.speedMeter.setMajorTickSpacing(10);
    this.speedMeter.setPaintTicks(true); 
    
    this.checkBox = new JCheckBox("On/Off");
    this.checkBox.setSelected(true);
  	aToolBar = new AnimationToolBar(this);
  }
  
	/*public static AnimationController getInstance() {
  	if (theInstance == null) {
			theInstance = new AnimationController();
		}
		return theInstance;
  }*/
  /**
   * NEW with Varargs!
   * If a painter wants animation, it calls this method to put itself in the schedule
   * for animation, which will then be performed after VPM finished processing the model
   * update. This method does not need to be syncrhonized.
   * @param monitor
   * @param path
   */
  public void scheduleAnimation(IPainter monitor, Motion... motions) {
  	AnimationThread aThread = new AnimationThread(monitor, motions, null, this);
  	if (schedule2.containsKey(monitor)) {
  		schedule2.get(monitor).add(aThread);
  	}
  	else {
  		List<AnimationThread> list = new ArrayList<AnimationThread>();
  		list.add(aThread);
  		schedule2.put(monitor, list);
  	}
  }
  
  
  /**
   * If a painter wants animation, it calls this method to put itself in the schedule
   * for animation, which will then be performed after VPM finished processing the model
   * update. This method does not need to be syncrhonized.
   * @param painter
   * @param path
   *
  public void scheduleAnimation(IPainter painter, Motion[] motions) {
  	AnimationThread aThread = new AnimationThread(painter, motions, null, this);
  	if (schedule2.containsKey(painter)) {
  		schedule2.get(painter).add(aThread);
  	}
  	else {
  		List<AnimationThread> list = new ArrayList<AnimationThread>();
  		list.add(aThread);
  		schedule2.put(painter, list);
  	}
  	
  //Synchornize on the animation threads, just in case (should not be needed)
  	//synchronized (schedule) {
  	//	schedule.add(aThread);
  	//}
  }*/
  
  /**
   * If a painter wants animation, it calls this method to put itself in the schedule
   * for animation, which will then be performed after VPM finished processing the model
   * update. This method does not need to be syncrhonized.
   * @param painter
   * @param path
   */
  public void scheduleAnimation(IPainter painter, Motion[] motions, Painter[] correctionPaints) {
  	AnimationThread aThread = new AnimationThread(painter, motions, correctionPaints, this);
  	if (schedule2.containsKey(painter)) {
  		schedule2.get(painter).add(aThread);
  	}
  	else {
  		List<AnimationThread> list = new ArrayList<AnimationThread>();
  		list.add(aThread);
  		schedule2.put(painter, list);
  	}
  	
  //Synchornize on the animation threads, just in case (should not be needed)
  	//synchronized (schedule) {
  	//	schedule.add(aThread);
  	//}
  }

  /**
   * Starts all scheduled animations for this step and waits for them to finish.
   * If the animation check box is checked off, meaning the user doesn't want to
   * see the animation, then the viz will be performed step-by-step
   * with the pausing time defined in VPM.
   */
  public void stepAnimation() {
  	if (state == State.stop) {	  	
  		aToolBar.setStopState();
  		controller.terminate();
  		return;
  	}
  	/*if (time != -1) {
  		long current = System.currentTimeMillis();
  		if (max < current - time) {
  			max = current - time;
  		}
  		System.out.println(max);
  	}*/
  	//System.out.println(timer++);
  	//animationInProgress = true;
  //Must syncrhonize on the threads because finish() could do concurrent remove
  //before the entire list is started
  	synchronized (schedule2) {
  		for (List<AnimationThread> list : schedule2.values()) {
  			list.get(0).start();
  			screenModified = true;
  		}
  	}
  	if (!this.schedule2.isEmpty()) {
  		if (getAnimationCheckBoxValue()) {
  //The main thread waits for the the threads to finish
		  	synchronized (this) {
		  		try {
						wait();
					} catch (InterruptedException e) {
						ProViz.errprintln(e);
					}
		  	}
  		}
  		else {
  			ProViz.println("AnimationController: Pausing 1");
  			this.delay();
  		}
  	}
  	else {
  		if (screenModified) {
				if (state != State.stepping && state != State.next) {
	//Normal case
					//ProViz.println("AnimationController: Pausing 2");
					this.delay();
				}
  		}
  	}
  	//time = System.currentTimeMillis();
  }
  
  /**
   * Sets the states of animation control buttons according to the animation state.
   */
  public void stepControl() {
		if (state == State.running) {
			controller.step();
		}
		else if (state == State.stepping || state == State.stepOver || state == State.stepReturn) {
			ProViz.getAnimationController().getToolBar().setStepState();
			state = State.stepping;
		}
		else if (state == State.next) {
			if (AnimationController.screenModified == false) {
				controller.step();
			}
			else {
				ProViz.getAnimationController().getToolBar().setStepState();
				state = State.stepping;
			}
		}
		else if (state == State.stop) {
			ProViz.getAnimationController().getToolBar().setStopState();
	  	controller.terminate();
  		return;
  	}
  }

  public void go(AnimationController.State command) {
  	if (state == State.stop) {
  		state = command;
  //Stop state, so start the execution
  		controller.step();
  	}
  	else if (state == State.customPause) {
  		state = command;
  //Some painter has paused the execution by waiting on the ProViz object
  		synchronized (ProViz.getInstance()) {
  			ProViz.getInstance().notifyAll();
  		}
  	}
  	else {
  		state = command;
  		if (state == State.running) {
    		aToolBar.setResumeState();
  		}
  		else {
    		aToolBar.setIntermediateState();
  		}
  		if (!schedule2.isEmpty()) {
	//Notifies paused animation threads to continue.
				synchronized (controller) {
					controller.notifyAll();
				}
			}
			else {
				controller.step();
			}
  	}
  }
  
  /*
   * Proceeds to the next step of the execution, used by resume and step actions.
   * @param autoStepping true: instructs VizMonitor to step automatically (resume action);
   * 				false: to step manually (step action)
   * @return
   *
  public boolean go(boolean autoStepping, boolean pauseWhenVisualizationChange) {
  	if (state == STOP) {
  //Stop state, so start the execution
  		controller.step();
  	}
  	if (state == CUSTOM_PAUSE) {
  //Some painter has paused the execution by waiting on the ProViz object
  		synchronized (ProViz.getInstance()) {
  			ProViz.getInstance().notifyAll();
  		}
    	if (autoStepping) {
    		state = RUNNING;
    	}
    	else {
    		if (pauseWhenVisualizationChange) {
    			state = NEXT;
    		}
    		else {
    			state = STEPPING;
    		}
    	}
  		return true;
  	}
  	if (autoStepping) {
  		aToolBar.setResumeState();
  		state = RUNNING;
  	}
  	else {
  		if (pauseWhenVisualizationChange) {
  			aToolBar.setIntermediateState();
  			state = NEXT;
  		}
  		else {
  			aToolBar.setIntermediateState();
  			state = STEPPING;
  		}
  	}
		if (!schedule2.isEmpty()) {
	//Notifies paused animation threads to continue.
			synchronized (controller) {
				controller.notifyAll();
			}
		}
		else {
  		//synchronized (this) {
  		//	notify();
  		//}
			controller.step();
		}
		return true;
  }
  */
  /**
   * Stops the animation by calling each network component's stop() method.
   */
  public void stop() {
  	//if (state == State.pause || state == State.next || state == State.stepping) {
   	if (state != State.stop) {
  		state = State.stop;
   		boolean noAnimation;
   		synchronized (schedule2) {
   			noAnimation = schedule2.isEmpty();
   		}
  		if (!noAnimation) {
  //Start the animation threads, and because the state is stop, these
  //threads will quickly die
  			synchronized (controller) {
  				controller.notifyAll();
  			}
  			synchronized (this) {
  				this.notifyAll();
  			}
  		}	
	  	aToolBar.setStopState();
	  	controller.terminate();
	  	return;
  	}
  //Let stepAnimation() (i.e. the main thread) exit the system
 		//state = State.stop;
  } //end stop
  
  /**
   * Sets the pause state on the animation.
   */
  public void pause() {
  	state = State.pause;
  	aToolBar.setStepState();
  }
  
  /**
   * Painters can call this method to pause the visualization so that the viewer can see
   * important information. The user will click on an animation control to continue.
   */
  public void customPause() {
  	state = State.customPause;
  	aToolBar.setStepState();
  	synchronized (ProViz.getInstance()) {
			try {
				ProViz.getInstance().wait();
			} catch (InterruptedException e) {
				ProViz.errprintln(e);
			}
		}
  }

  /**
   * An AnimationThread must call this method when it finishes animating, which
   * will remove itself from the schedule in the controller. The last thread to
   * do so will make the schedule empty, meaning that all animations are finished.
   * Then the main thread is notified and continues to the next step.
   * @param aThread
   */
  public void finish(AnimationThread aThread) {
  	synchronized (schedule2) {
  		Painter[] corrections = aThread.getCorrectionPaints();
  		if (corrections != null) {
  			for (Painter cor : corrections) {
  				cor.paint();
  			}
  			this.delay();
  		}
  		IPainter painter = aThread.getPainter();
  //Starts the next animation thread that was scheduled with 'painter'
  		List<AnimationThread> list = schedule2.get(painter);
  		int index = list.indexOf(aThread);
  		if (state != State.stop && index < list.size() - 1) {
  			list.get(index + 1).start();
  		}
  		else {
  //All animation threads for this painter are finished, so remove it from the schedule
  			schedule2.remove(painter);
  		}
  		//schedule.remove(aThread);
  //Paint the scheduled painters to correct any mis-location caused by step animation
  		if (schedule2.isEmpty()) {
    		synchronized (this) {
    			this.notify();
    		}
    	}
  	}
  }
  
	/**
	 * @return Returns the state.
	 */
	public AnimationController.State getState() {
		return state;
	}
	
	/**
	 * @param state The state to set.
	 */
	public void setState(AnimationController.State state) {
		AnimationController.state = state;
	}
	

  /**
   * Gets the speed value from the speed meter, which ranges from -20 to 100.
   * @return The speed value.
   */
  public int getSpeed() {
    return this.speedMeter.getValue();
  }
  
  public int getSpeedScale() {
  	if (getSpeed() < 1) {
			return 2 - getSpeed();
		}
  	return 1;
  }
  
  public int getSleepTime() {
		if (getSpeed() < 1) {
	//This is the minimum sleep time
			return 0;
		}
  	return this.speedMeter.getValue();
  }

  /**
   * Gets whether the animation check box is checked or not.
   * @return true if the check box is checked; false otherwise.
   */
  public boolean getAnimationCheckBoxValue() {
    return this.checkBox.isSelected();
  } //end method
  
  /**
   * Clears everything in AnimationController.
   */
  public void clear() {
  	state = State.stop;
  	this.aToolBar.setStopState();
  	//this.schedule.clear();
  	//this.paintSchedule2.clear();
  	this.schedule2.clear();
  }

	/**
	 * Causes the current thread to sleep for the amount of time selected by the
	 * speed slider. It is used to cause a delay on the viz changes <b>when
	 * animation is not involved</b>.
	 */
	public void delay() {
		if (getSpeed() > -19) {
			try {
				Thread.sleep((getSpeed() + 21) * 10);
			} catch (InterruptedException e1) {
				ProViz.errprintln(e1);
			}
		}
	}

  /**
   * The VizController for this animation controller must be set at the beginning of
   * the visualization.
   * @param controller
   */
  public void setController(IVizController controller) {
  	this.controller = controller;
  }
  
  /**
	 * @return the controller
	 */
	public IVizController getVizController() {
		return controller;
	}

	/**
	 * @return the aToolBar
	 */
	public AnimationToolBar getToolBar() {
		return aToolBar;
	}

	/**
	 * @return the speedMeter
	 */
	public JSlider getSpeedMeter() {
		return speedMeter;
	}
	
	public JCheckBox getCheckBox() {
		return this.checkBox;
	}
	
	/**
	 * Animates a motion immediately without scheduling it to the step animation.
	 * @param motion
	 *
	public void animateNow(Motion motion, boolean repaint) {
		if (this.getAnimationCheckBoxValue()) {
			while (!motion.stepMotion(this.getSpeedScale())) {
				Sleep.sleep(this.getSleepTime());
				if (repaint) {
					ProViz.getVPM().getCanvas().repaint();
				}
			}
		}
		else {
			motion.noAnimationMotion();
		}
	}*/
	

	/**
	 * Animates a motion immediately without scheduling it to the step animation.
	 * @param motion
	 */
	public void animateNow(boolean repaint, Motion... motions) {
		if (this.getAnimationCheckBoxValue()) {
			int count = 0;
			boolean[] finished = new boolean[motions.length];
			while (count < motions.length) {
				if (state == State.stop) {
					return;
				}
				for (int i = 0; i < motions.length; i++) {
					Motion motion = motions[i];
					if (!finished[i]) {
						finished[i] = motion.stepMotion(this.getSpeedScale());
						if (finished[i]) {
							count++;
						}
					}
				}
				Sleep.sleep(this.getSleepTime());
				if (repaint) {
					ProViz.getVPM().getCanvas().repaint();
				}
			}
		}
		else {
			for (Motion motion : motions) {
				motion.noAnimationMotion();
			}
		}
	}
}
