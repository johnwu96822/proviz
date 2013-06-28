package viz.animation;

import viz.ProViz;
import viz.animation.motion.Motion;
import viz.animation.rotate.Rotation;
import viz.painters.IPainter;
import viz.painters.Painter;

/**
 * @author Jo-Han Wu
 * @version Jun 28, 2005 10:13:47 PM
 */
public class AnimationThread extends Thread {

	private Painter[] correctionPaints;
	private IPainter painter;
	private Motion[] motions;
	//speedScale goes from 1 to 37 when the speed meter in AnimationController is slid under 16.
	//Otherwise it is 1 when the speed meter is from 17 to 100
	//private int speedScale = 1;
	//private int sleepTime = 16;
	private AnimationController aController;
	
	public AnimationThread(IPainter painter, Motion[] motions, Painter[] correctionPaints, AnimationController ac) {
		this.aController = ac;
		this.painter = painter;
		this.motions = motions;
		this.correctionPaints = correctionPaints; 
	}
	
	/*
	 * Updates the speed of animation, including the sleepTime for this thread to sleep and
	 * the speedScale for motions to scale their animations. Both values come from the speed
	 * slider in AnimationController.
	 *
	private void updateSpeed() {
		int speedValue = aController.getSpeed();
		sleepTime = speedValue;
//Go maximum x frames/second.
		if (speedValue < 1) {
			sleepTime = 0;
			this.speedScale = 2 - speedValue;
		}
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final int total = motions.length;
		int count = total;
		if (this.aController.getAnimationCheckBoxValue()) {
			//this.updateSpeed();
		//Executes each Motion until all of them are finished
			while (count > 0) {
				int i = 0;
				while (i < total) {
					if (motions[i] != null) {
						if (this.executeMotion(motions[i])) {
							motions[i] = null;
							count--;
						}
					}
					i++;
				}
				this.painter.getCanvas().repaint();
				try {
					if (aController.getState() == AnimationController.State.stop) {
						//aController.finish(this);
						return;
					}
	//sleepTime is determined in updateSpeed()
					Thread.sleep(aController.getSleepTime());
					if (aController.getState() == AnimationController.State.pause) {
						synchronized (aController.getVizController()) {
							aController.getVizController().wait();
						}
					}
					if (aController.getState() == AnimationController.State.stop) {
						return;
					}
				} catch (InterruptedException e) {
					ProViz.errprintln(e);
				}
		//Refreshes the speed in case the user slides it
				//this.updateSpeed();
			}
		}
		else {
	//Animation checkbox is unchecked, so step motion
			ProViz.println("Calling noAnimationMotion in AnimationThread ");// + painter.getAllVariableNames());
			for (Motion motion : motions) {
				motion.noAnimationMotion();//this.painter);
			}
			//Painter.moveTo(painter, motions.getDestination().x, motions.getDestination().y);
		}
		//System.out.println("Running thread for painter: " + painter.getVariable().getName());
		aController.finish(this);
	} //end run

	/**
	 * 
	 * @param motion
	 * @param speedScale
	 * @return true when the motion finishes; false if the motion should continue
	 */
	public boolean executeMotion(Motion motion) {
		if (aController.getState() == AnimationController.State.stop) {
			return true;
		}
		/*boolean rv = true;
		if (motion instanceof Path) {
			Path path = (Path) motion;
			rv = path.doNext(aController.getSpeedScale());
		}
		else if (motion instanceof Rotation) {
			if (((Rotation) motion).getNext(aController.getSpeedScale()) != null) {
				this.painter.getCanvas().repaint();
				rv = false;
			}
		}
		else if (motion instanceof Sleep) {
			Sleep.sleep();
		}
		else if (motion instanceof CustomMotion) {
			((CustomMotion) motion).doMotion(aController.getSpeedScale());
		}*/
		//return rv;
		boolean rv = motion.stepMotion(aController.getSpeedScale());
		if (motion instanceof Rotation) {
			this.painter.getCanvas().repaint();
		}
		return rv;
	}
	
	/**
	 * @return the correctionPaints
	 */
	public Painter[] getCorrectionPaints() {
		return correctionPaints;
	}

	/**
	 * @return the painter
	 */
	public IPainter getPainter() {
		return painter;
	}
} //end class
