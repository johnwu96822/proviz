package viz.animation.rotate;

import test.painters.robot.Stick;
import viz.ProViz;

/**
 * This rotation is reusable, which means when one rotation is finished (getNext() returns
 * null), values will be reset so that this rotation can rotate the same Stick in the same
 * degree rotation. One can change the degree by calling setRotation().
 * @author JW
 *
 */
public class StickRotation implements Rotation {
	private int iterations = 60;
	private int counter = 0;
	private double offset;
	private double destination;
	private Stick stick;
	private double amount;

	public StickRotation(Stick stick, double degree) {
		this.amount = degree;
		this.offset = degree;
		this.destination = stick.getAngle() + degree;
		this.stick = stick;
	}
	
	@Override
	public Double getNext(int speedScale) {
		if (counter >= iterations || this.offset == 0) {
			this.setRotation(this.amount);
			return null;
		}
		double current = stick.getAngle();
		offset = destination - current;
	//scale ranges from 1 to 6
		int scale = 1 + speedScale / 7;
		stick.setAngle(current + offset * scale / (iterations - counter));
		counter += scale;
		return current;
	}

	@Override
	public void noAnimationMotion() {//IPainter painter) {
		this.stick.setAngle(destination);
		ProViz.getVPM().getCanvas().repaint();
		//painter.getCanvas().repaint();
	}
	
	public void setRotation(double degree) {
		this.amount = degree;
		this.offset = degree;
		this.destination = stick.getAngle() + degree;
		counter = 0;
	}

	@Override
	public boolean stepMotion(int speedScale) {
		if (getNext(speedScale) != null) {
			return false;
		}
		return true;
	}
}
