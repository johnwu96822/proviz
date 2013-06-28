package viz.painters;

import java.util.List;

import viz.runtime.Change;
import viz.runtime.IVizVariable;

/**
 * Is this interface necessary?
 * @author JW
 *
 */
public interface IValueChangedListener {
	/*public static final int NEW = 0;
	public static final int CHANGED = 1;
	public static final int REMOVED = 2;*/
	//public void handleChange(ChangeEvent event);
	
	/**
	 * Responds to changes occur on the source variable. It should updates the internal
	 * data state so that paint() can then be used to update the viz.
	 * TODO However, this method may also update the viz directly, depending
	 * on how we will design it. Currently I think paint() method should have the capability
	 * of painting the entire value while this handler produces the right data.
	 * @param change
	 * @param source
	 */
	public void systemHandleChange(Change change, IVizVariable source);
	
	public boolean removeEventGenerator(IVizVariable source);
	/**
	 * Adds a variable to the list of event generators of this painter.
	 * @param var
	 */
	public void addEventGenerator(IVizVariable var);

	public List<IVizVariable> getEventGenerators();
}
