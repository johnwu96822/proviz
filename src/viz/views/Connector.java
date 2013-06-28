package viz.views;

//import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;

import viz.views.util.IConnectable;

//import javax.swing.JPanel;

/**
 * The abstract class of a cable. It defines the basic cable methods and properties.
 * 
 * The original approach of Connector implementation is to use a transparent JPanel that
 * connects two network components diagonally. So two components could be at the upper-
 * left and lower-right corners of the JPanel, or at the lower-left and upper-right
 * corners. Then a line is drawn diagonally connecting the two corners. This approach has
 * problem in display when two components are dragged to the same horizon or vertical
 * positions. When that happens, one of the dimension of that JPanel becomes 0, and the
 * line cannot be drawn. 
 * 
 * Current fix is to get rid of the JPanel, having Connector class the ability to display
 * NOTHING but to keep track of the points where it should be connecting in the network
 * space. Then the Controller uses a method to draw all cables onto the network space
 * using the points in each Connector.
 * 
 * This might still need to be improved because of the fact that Connector is not an individual
 * GUI component (JPanel) any more, but the packet animation will need to be operate on
 * cables. In the JPanel approach, the scope of packet animation would have coordinates
 * only in the local JPanel. But now the scope becomes the network space, that coordinates
 * need to be calculated by the network space to perform and draw the animation.
 *  
 * @author Jo-Han Wu
 */
public class Connector {
  //point1 and point2 are the two ends of this cable relative to the position
  //in the network space.
  private Point point1 = new Point();
  private Point relative1 = new Point();
  private Point point2 = new Point();
  private Point relative2 = new Point();
  //Optional, to identify this connector
  private String id = null;
  
  //Two ends of the NetworkComponent that this cable connects.
  private IConnectable painter1 = null;
  private IConnectable painter2 = null;
  
  //private Color color;
  //protected boolean isInUse = false;
  
  //protected String cableName = "Connector";


  /**
   * Default constructor.
   */
  public Connector() {
  }
  
  /**
   * Creates a cable connecting c1 and c2 by their central points. 
   */
  public Connector(IConnectable p1, IConnectable p2) {// throws PortsFullException {
  	this.painter1 = p1;
  	this.painter2 = p2;
  	this.relative1.x = p1.getWidth() / 2;
  	this.relative1.y = p1.getHeight() / 2;
  	this.relative2.x = p2.getWidth() / 2;
  	this.relative2.y = p2.getHeight() / 2;
  	this.refresh();
  }
  
  /**
   * Creates a cable connecting c1 and c2 by their central points. 
   */
  public Connector(IConnectable p1, IConnectable p2, String id) {// throws PortsFullException {
  	this(p1, p2);
  	this.id = id;
  }
  
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public Color getCableColor() {
		return Color.BLACK;
	}

	/**
	 * @return Returns the point1.
	 */
	public Point getPoint1() {
		return point1;
	}
	/**
	 * @return Returns the point2.
	 */
	public Point getPoint2() {
		return point2;
	}
	
  /**
   * Refreshes everything for this cable, reflecting all changes. Including the 
   * points based on the connected two components, the boundary of this panel,
   * and the slope of the line. 
   *
   */
  public void refresh() {
  	this.updateLocation(this.painter1);
  	this.updateLocation(this.painter2);
  }
  
  public Point getPoint(IConnectable component) {
  	Point rv = null;
  	if (this.painter1 == component) {
  		rv = this.point1;
  	}
  	else if (this.painter2 == component) {
  		rv = this.point2;
  	}
  	return rv;
  }
  
	/**
	 * Updates the location of the specified component.
	 * @param component
	 */
	public void updateLocation(IConnectable component) {
		if (this.painter1 == component) {
			Point point = component.getLocation();
			this.point1.x = point.x + this.relative1.x;
			this.point1.y = point.y + this.relative1.y;
			//this.point1 = new Point(point.x + component.getWidth() / 2, 
			//		point.y + component.getHeight() / 2);
		}
		else if (this.painter2 == component) {
			Point point = component.getLocation();
			this.point2.x = point.x + this.relative2.x;
			this.point2.y = point.y + this.relative2.y;
			//this.point2 = new Point(point.x + component.getWidth() / 2, 
			//		point.y + component.getHeight() / 2);
		}
	}
	
	public void setRelativeLocation(IConnectable painter, int x, int y) {
		if (this.painter1 == painter) {
			this.relative1.x = x;
			this.relative1.y = y;
			this.updateLocation(painter);
		}
		else if (this.painter2 == painter) {
			this.relative2.x = x;
			this.relative2.y = y;
			this.updateLocation(painter);
		}
	}
	
	/**
	 * Gets the network component on the other end of the cable. So the caller must
	 * pass the component on this end. If no component is found, null is returned.
	 * @param component The known end of the component.
	 * @return The other NetworkComponent.
	 */
	public IConnectable getTheOtherPainter(IConnectable component) {
		IConnectable c1 = this.getComponent1();
		IConnectable c2 = this.getComponent2();
		if (c1 == component) {
  		if (c2 != null && c2 != component) {
	  		return c2;
		  }
		}
		if (c2 == component) {
		  if (c1 != null && c1 != component) {
			  return c1;
		  }
		}
		return null;
	}

	/**
	 * @return Returns the color.
	 *
	public Color getColor() {
		return color;
	}*/

	/**
	 * @return Returns the networkComponent1.
	 */
	public IConnectable getComponent1() {
		return painter1;
	}
	/**
	 * @return Returns the networkComponent2.
	 */
	public IConnectable getComponent2() {
		return painter2;
	}
	
	/*
	 * Return the String representation of this object.
	 * @return String
	 *
	public String toString() {
		String s = this.painter1.getVariable().getName() + "\t";
		//s += this.networkComponent1.getLocation().toString() + "\n";
		//s += this.point1.toString() + "\n";
		s += this.painter2.getVariable().getName() + "\t";
		//s += this.networkComponent2.getLocation().toString() + "\n";
		//s += this.point2.toString() + "\n";
		return s;
	}*/
}
