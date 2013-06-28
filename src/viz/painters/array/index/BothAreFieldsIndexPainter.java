package viz.painters.array.index;

import viz.painters.Painter;
import viz.painters.java.array.IndexPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * <p>Both this index variable and the target array variables are fields.
 * This index painter assumes that (1) the index variable of this painter 
 * is a field; (2) the target array variable is also a field in the same
 * class.</p>
 * 
 * Example:<br>
 * class Example {
 *   @DViz
 *   int[] array;
 *   @Viz ("BothAreFieldsIndexPainter")
 *   int index;
 * }
 * 
 * @author JW
 *
 */
public abstract class BothAreFieldsIndexPainter extends IndexPainter {

	public BothAreFieldsIndexPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public Painter getArrayPainter() {
		if (getParent() == null) {
			return null;
		}
		return getParent().getFieldPainter(getArrayName());
	}

	public abstract String getArrayName();
}
