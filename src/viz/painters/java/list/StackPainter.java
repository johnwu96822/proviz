package viz.painters.java.list;

import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Visualizes the java.util.Stack<E> class. Stack's relevant fields are
 * an "elementData" array and an "elementCount" int.
 * @author JW
 *
 */
public class StackPainter extends AbstractStackPainter {
	
	public StackPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public String getArrayVariable() {
		return "elementData";
	}

	@Override
	public String getSizeVariable() {
		return "elementCount";
	}

	@Override
	public int counterStartingValue() {
		return 0;
	}
}
