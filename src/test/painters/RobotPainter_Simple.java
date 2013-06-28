/**
 * 
 */
package test.painters;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * @author kamigaki
 *
 */
public class RobotPainter_Simple extends Painter {

	private Robot2DDrawPanel robotPanel2D = new Robot2DDrawPanel(this.getVariable());

	public RobotPainter_Simple(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);

	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		this.setSize(this.getCanvas().getVisibleRect().height, this.getCanvas().getVisibleRect().width);
		// Variable initialization
		//robotPanel2D = new RobotLabel();
		
		// Set up field listeners.
		IVizVariable thisVariable = this.getVariable();
		List<IVizVariable> thisFields = thisVariable.getFields();
		
		// Try to keep it generalized, but this isn't strictly needed since
		// implementation of class is known.  This may be a little wasteful, but
		// will probably reduce implementation time (as new fields are added to
		// the robot class).
		for(IVizVariable field : thisFields) {
			List<IVizVariable> fieldFields = field.getFields();
			for(IVizVariable fieldField : fieldFields) {
				fieldField.addListener(this);
				this.addEventGenerator(fieldField);
			}
		}
		
		// setup the default size for the robot painter.
		// the following doesn't seem to work:
		// VizVariable vvRightArm = this.getVariable().getStackFrame().getVariable("this");
		IVizVariable vvRightArm = this.getVariable().getField("rightArm");
		if(vvRightArm == null) {
			System.out.println("Why didn't this work");
			this.robotPanel2D.setBounds(100, 0, 100, 200);
		} else {
			int panelHeight = 6 * Integer.parseInt(vvRightArm.getField("length").getValueAsString());
			this.robotPanel2D.setBounds(100, 0, 100, panelHeight);
		}
		this.getCanvas().add(robotPanel2D);
		
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#destroy_userImp()
	 */
	@Override
	protected void destroy_userImp() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return robotPanel2D;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.VizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		//System.out.println("The following change happened: " + change);
		paint();
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		System.out.println("A robot was painted.");
		
		robotPanel2D.repaint();

	}

}

class Robot2DDrawPanel extends JPanel {
	
	private IVizVariable  vvRobot;

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Graphics2D g2d;

	public Robot2DDrawPanel(IVizVariable robot) {
		this.vvRobot = robot;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D)g;

		// Extract data from the ProViz engine.
		// First get the individual VizVariables.
		IVizVariable vvHead = vvRobot.getField("head");
		IVizVariable vvLeftArm = vvRobot.getField("leftArm");
		IVizVariable vvRightArm = vvRobot.getField("rightArm");
		IVizVariable vvLeftLeg = vvRobot.getField("leftLeg");
		IVizVariable vvRightLeg = vvRobot.getField("rightLeg");
		
		int reSize = 2;
		
		// now extract the values
		int headAngle = Integer.parseInt(vvHead.getField("degree").getValueAsString());
		
		int leftArmLength = reSize * Integer.parseInt(vvLeftArm.getField("length").getValueAsString());
		int leftUpperArmLength = reSize * Integer.parseInt(vvLeftArm.getField("upper").getValueAsString());
		int leftElbowAngle = Integer.parseInt(vvLeftArm.getField("angle").getValueAsString());
		
		int rightArmLength = reSize * Integer.parseInt(vvRightArm.getField("length").getValueAsString());
		int rightUpperArmLength = reSize * Integer.parseInt(vvRightArm.getField("upper").getValueAsString());
		int rightElbowAngle = Integer.parseInt(vvRightArm.getField("angle").getValueAsString());
				
		int leftLegLength = reSize * Integer.parseInt(vvLeftLeg.getField("length").getValueAsString());
		int leftUpperLegLength = reSize * Integer.parseInt(vvLeftLeg.getField("upper").getValueAsString());
		int leftKneeAngle = Integer.parseInt(vvLeftLeg.getField("angle").getValueAsString());
		
		int rightLegLength = reSize * Integer.parseInt(vvRightLeg.getField("length").getValueAsString());
		int rightUpperLegLength = reSize * Integer.parseInt(vvRightLeg.getField("upper").getValueAsString());
		int rightKneeAngle = Integer.parseInt(vvRightLeg.getField("angle").getValueAsString());

		// Torso spec based off of arm and leg length + window size.
		// gives 5 + arm length space from the top of the window to leg length + 5 from the bottom
		// of the window.  Torso width is merely a quarter of arm length.
		int torsoHeight = this.getHeight() - (5 + leftLegLength) - leftArmLength;
		int torsoWidth = leftArmLength / 4;

		// Head size is merely a quarter of arm length.
		int headHeight = leftArmLength / 4;
		int headWidth = leftArmLength / 4;
		
		// TODO make this scalable
		// arms anchor in the middle of the screen, 5 pixels lower than the top of the torso
		Point armAnchorPoint = new Point(this.getWidth() / 2, (leftArmLength + 5));
		// Legs anchor in the middle of the screen at the bottom of the torso.
		Point legAnchorPoint = new Point(this.getWidth() / 2, this.getHeight() - (5 + leftLegLength));
		// Head anchors 1/8 of an arm length to left of center, headwidth high
		Point headAnchorPoint = new Point(this.getWidth()/ 2 - leftArmLength / 8, leftArmLength - headHeight);
		Point torsoAnchorPoint = new Point((this.getWidth() / 2) - leftArmLength / 8, leftArmLength);

		// Going to divide the image into 3 depths so that nearer objects draw
		// over farther away ones (don't have to worry about clipping).
		// TODO determine viewing orientation (e.g. from left side, right side,
		//      front, back
		// TODO scale body to match arm size and label size somehow.

		
		// draw "far" appendages
		drawArm(leftArmLength, leftUpperArmLength, leftElbowAngle, armAnchorPoint, g2d);
		drawLeg(leftLegLength, leftUpperLegLength, leftKneeAngle, legAnchorPoint, g2d);

		// draw "middle" objects (body and head)
		drawHead(headAngle, headHeight, headWidth, headAnchorPoint);
		
		// TODO should turn this into a method.  it will need to become more advanced.		
		drawTorso(torsoAnchorPoint, torsoWidth, torsoHeight);

		// draw "close" appendages.
		drawLeg(rightLegLength, rightUpperLegLength, rightKneeAngle, legAnchorPoint, g2d);
		drawArm(rightArmLength, rightUpperArmLength, rightElbowAngle, armAnchorPoint, g2d);
		
	}

	private void drawHead(int orientation, int height, int width, Point location) {
		RoundRectangle2D head2D = new RoundRectangle2D.Double(location.x, location.y, height, width, 2, 2);
		RoundRectangle2D eyes2D;
		Color original;
		original = g2d.getColor();
		g2d.setColor(Color.DARK_GRAY);
		g2d.fill(head2D);

		if(orientation == 0) {
			// facing forward add eye bump to right side
			eyes2D = new RoundRectangle2D.Double(location.x + width, location.y + height /4 , width / 4, height / 4, 2, 2);
			g2d.fill(eyes2D);
		} else if(orientation == 90 || orientation == -270) {
			// facing to its right
			// add eyes looking at user
			Ellipse2D eye = new Ellipse2D.Double(location.x + width * 2 / 5 - (height / 10), location.y + width / 4, height / 5, height / 5);

			g2d.setColor(Color.LIGHT_GRAY);
			eyes2D = new RoundRectangle2D.Double(location.x + width * 1 / 5, location.y + height / 4, width * 3 / 5, height / 4, 3, 3);
			g2d.fill(eyes2D);
			g2d.setColor(Color.RED);
			g2d.fill(eye);
			eye = new Ellipse2D.Double(location.x + width * 3 / 5 - (height / 10), location.y + height / 4, height / 4, height / 4);
			g2d.fill(eye);
		} else if(orientation == 180 || orientation == -180) {
			// facing rearward
			eyes2D = new RoundRectangle2D.Double(location.x - width/4, location.y + height / 4 , width /4, height /4, 2, 2);
			g2d.fill(eyes2D);
		} else if(orientation == 270 || orientation == -90) {
			// currently do nothing.
		}
		g2d.setColor(original);
	}

	/**
	 * Draws an an "arm" at the provided location on "g".  An arm is two
	 * connected segments.  Initial implementation is simply two lines.
	 * 
	 * Viewing orientation to come later.
	 * 
	 * @param length - the total length of the arm.
	 * @param forearmLength - the length of the forearm.
	 * @param angle - the bend angle at the elbow, 0 being straight down.
	 * @param location - where the arm's shoulder would be (i.e. where the arm
	 *                   starts).
	 */
	private void drawArm(int length, int upperArmLength, int angle, Point location, Graphics g) {
		drawAppendage(length, upperArmLength, angle, location, g2d, Color.CYAN);
	}

	private void drawLeg (int length, int upperArmLength, int angle, Point location, Graphics g) {
		drawAppendage(length, upperArmLength, angle, location, g2d, Color.RED);
	}

	private void drawAppendage(int length, int upperLength, int angle, 
			Point location, Graphics g, Color lineColor){
		int foreLength = length - upperLength;
		int bend = angle % 360;  // Just in case some "out of range" value is 
		// given (e.g. 720).

		Color originalColor = g2d.getColor();
		g2d.setColor(lineColor);
		g2d.drawLine(location.x, location.y, location.x, location.y + upperLength);
		int endX = location.x, endY = location.y;
		switch(bend) {
		case 0:
			endX = location.x;
			endY = location.y + length;
			break;
		case 90:
			endX = location.x + foreLength;
			endY = location.y + upperLength;
			break;
		case 180:
			endX = location.x;
			endY = location.y - foreLength;
			break;
		case 270:
			endX = location.x - foreLength;
			endY = location.y + upperLength;
			break;
		default:
			double deltaX = 0, deltaY = 0;
			if(bend > 0 && bend < 90) {
				// lower right hand quadrant ( with robot facing right)
				// endX will be higher, endY will be lower.
				deltaX = Math.sin(Math.toRadians(bend)) * foreLength;
				deltaY = upperLength + (Math.cos(Math.toRadians(bend)) * foreLength);
			} else if (bend > 90 && bend < 180) {
				// upper right hand quadrant
				bend = 180 - bend;
				deltaX = Math.sin(Math.toRadians(bend)) * foreLength;
				deltaY = upperLength - (Math.cos(Math.toRadians(bend)) * foreLength);
			} else if (bend > 180 && bend < 270) {
				// upper left hand quadrant
				bend = 270 - bend;
				deltaX = -(Math.sin(Math.toRadians(bend)) * foreLength);
				deltaY = upperLength - (Math.cos(Math.toRadians(bend)) * foreLength);
			} else {
				// lower left hand quadrant
				bend = 360 - bend;
				deltaX = -(Math.sin(Math.toRadians(bend)) * foreLength);
				deltaY = upperLength + (Math.cos(Math.toRadians(bend)) * foreLength);
			}
			endX = location.x + (int)deltaX;
			endY = location.y + (int)deltaY;
			break;
		}

		g2d.drawLine(location.x, location.y + upperLength, endX, endY);
		g2d.setColor(originalColor);
	}
	
	private void drawTorso(Point anchor, int width, int height) {
		g2d.setColor(Color.GRAY);
		RoundRectangle2D body2D = new RoundRectangle2D.Double(anchor.x, anchor.y, width, height, 5, 5);
		g2d.fill(body2D);
	}
}
