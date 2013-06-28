package viz.painters.graphics;

/**
 * An IMovable supports the move motion for animation
 * @author JW
 *
 */
public interface IMovable {
	//public Point getLocation();
	
	/**
	 * Sets the location of the graphical component in this painter. If the location moves outside
	 * the range of ProViz's canvas, the canvas's size will be expanded.
	 * @param x
	 * @param y
	 */
	public void setLocation(int x, int y);
	
	//public int getWidth();
	
	//public int getHeight();
}
