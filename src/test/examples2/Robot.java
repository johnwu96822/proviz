package test.examples2;

import viz.annotation.Viz;

public class Robot {
	@Viz("test.painters.robot.HeadPainter")
	private Head head = new Head();
	@Viz("test.painters.robot.ArmPainter")
	private Arm leftArm = new Arm(30, 0, 15);
	@Viz("test.painters.robot.ArmPainter")
	private Arm rightArm = new Arm(30, 0, 15);
	@Viz("test.painters.robot.LegPainter")
	private Leg leftLeg = new Leg(40, 0, 20);
	@Viz("test.painters.robot.LegPainter")
	private Leg rightLeg = new Leg(40, 0, 20);

	public void run(int distance, int speed) {

	}

	public void walk(int distance) {

	}


	public void jump(int height) {

	}

	public static void main(String[] args) {
		@Viz("test.painters.robot.RobotPainter_Main")
		Robot robot = new Robot();
		robot.head.turnLeft();
		robot.head.turnLeft();
		robot.head.turnLeft(); // three lefts make a right.
		robot.head.turnLeft();
		//robot.leftArm.bend(145); // can't easily be seen.  oops.
		//robot.rightArm.bend(90);
		//robot.rightArm.straighten(180); // this should actually bend backwards.  let's see if it does.
		//robot.rightArm.bend(45);
		//robot.rightLeg.bend(45);
		//robot.jump(10);		
		
		@Viz("test.painters.Painter_JIcon")
		Robot testRobo = new Robot();
		testRobo.head.turnRight();
		// testRobo.head.turnLeft();
	}
}

class Head {
	private int degree = 0;
	public void turnRight() {
		degree += 90;
		degree %= 360;
	}
	public void turnLeft() {
		degree -= 90;
		degree %= 360;
	}
}

class Arm {
	//length is the total length of arm/leg
	private int length;
	//upper is the length of the upper arm/thigh
	private int upper;
	//angle is the angle between the upper and the lower arm/thigh and leg.
	private int angle = 0;
	public Arm(int length, int angle, int upper) {
		this.length = length;
		this.angle = angle;
		this.upper = upper;
	}
	public void bend(int degree) {
		this.angle += degree;
		this.angle %= 360;
	}

	public void straighten(int degree) {
		this.angle -= degree;
		this.angle %= 360;
	}
}

class Leg extends Arm {
	public Leg(int length, int angle, int upper) {
		super(length, angle, upper);
	}
}