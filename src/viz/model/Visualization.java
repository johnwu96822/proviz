package viz.model;

import java.awt.Point;
import java.io.Serializable;

/**
 * Models a visualization for any Association. The visualization is essentially the name
 * of a painter's class path. A starting location can be specified. A Visualization also
 * has link to its parent Association (for use with the Viz views).
 * @author John
 * Created on Feb 8, 2007
 */
public class Visualization implements Serializable {
	private String painterName = null;
	private Association parent = null;
	public static final int DEFAULT = 0;

	
  /**
   * If null, no starting location is specified.
   */
  private Point startingLocation = null;
  
	/**
	 * @param painterName
	 */
	public Visualization(String vc) {
		this.painterName = vc;
	}

	/**
	 * @param painterName
	 */
	public Visualization(String vc, Association parent) {
		this.painterName = vc;
		this.parent = parent;
	}

	/**
	 * @return the startingLocation null if no location is specified
	 */
	public Point getStartingLocation() {
		return startingLocation;
	}

	/**
	 * Sets the starting location.
	 * @param x
	 * @param y
	 */
	public void setStartingLocation(int x, int y) {
		this.startingLocation = new Point(x, y);
	}

	/**
	 * @return Returns the parent.
	 */
	public Association getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(Association parent) {
		this.parent = parent;
	}

	/**
	 * @return Returns the painterName.
	 */
	public String getPainterName() {
		return painterName;
	}

	/**
	 * Sets the painter's path in this visualization.
	 * @param painterName The painterName to set.
	 */
	public void setVisualization(String painterName) {
		this.painterName = painterName;
	}
	
	/**
	 * Gets the order of this visualization in its parent Association.
	 * @return The order of this visualization in its parent Association; -1 if the parent is null
	 */
	public int getOrder() {
		if (this.parent != null) {
			return parent.indexOf(this);
		}
		return -1;
	}

	@Override
	public String toString() {
		return getPainterName();
	}
	
	/**
	 * @return
	 */
	public boolean isDefault() {
		return getOrder() == DEFAULT;
	}
	
	public boolean isCompatible(Visualization viz) {
		return this.painterName.equals(viz.getPainterName());
	}
	
	public boolean isCompatible(String vizClassName) {
		return this.painterName.equals(vizClassName);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Visualization) {
			return ((Visualization) obj).getPainterName().equals(this.getPainterName());
		}
		return false;
	}
}
