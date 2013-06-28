package viz.painters;

import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * A DummyPainter is to be associated with a variable that is not to be 
 * visualized, but the variable have fields that need to be visualized.
 * A DummyPainter is essentially a bridge between its parent painter
 * and its field painters. The reason and the effect of using DummyPainter
 * is encapsulation, as some fields tend to be hidden in the implementation.
 * 
 * Take java.util.ArrayList as an example. ArrayList has a "elementData" field
 * that is an array of elements. In encapsulation, the "elementData" variable
 * should not be revealed, but its elements should be visualized. Thus "elementData"
 * should be visualized by a DummyPainter, and the ArrayList painter would access
 * elementData's field painters to arrange the visualization for the ArrayList.
 * 
 *  ArrayList       <-      ArrayListPainter
 *      |                          |
 * elementData      <-        DummyPainter
 *      |                          |
 * {[0] [1] ...}   <-      {element painters}
 * 
 * What one must know is that the <b>handleChange() in DummyPainter redirects
 * all change events to its parent painter</b>. So any painter whose field painter
 * is a DummyPainter must handle changes from the field.
 * 
 * Location management is available in DummyPainter, since it inherits that
 * from PainterWithNoComponent.
 * 
 * The implementations of addToCanvas_userImp(), paint_userImp(), and
 * destroy_userImp() all do nothing, so it is never necessary to call
 * corresponding public methods for these.
 * @author JW
 *
 */
public final class DummyPainter extends PainterWithNoComponent {

	public DummyPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	/**
	 * Returns 0
	 * @see viz.painters.PainterWithNoComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		return 0;//this.getParent().getHeight();
	}

	/**
	 * Returns 0
	 * @see viz.painters.PainterWithNoComponent#getWidth()
	 */
	@Override
	public int getWidth() {
		return 0;//this.getParent().getHeight();
	}

	/**
	 * Does nothing.
	 * @see viz.painters.Painter#addToCanvas_userImp()
	 */
	@Override
	protected void addToCanvas_userImp() {
		//System.err.println("Should not call methods in DummyPainter");
	}

	/**
	 * Does nothing.
	 * @see viz.painters.Painter#destroy_userImp()
	 */
	@Override
	protected void destroy_userImp() {
		//System.err.println("Should not call methods in DummyPainter");
	}

	/**
	 * Relays the handle change call to the parent painter. So <b>the parent painter
	 * of any DummyPainter must handle changes from the DummyPainter field painter</b>.
	 * @see viz.painters.Painter#handleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	@Override
	public void handleChange(Change change, IVizVariable source) {
		this.getParent().handleChange(change, source);
	}

	/**
	 * Does nothing.
	 * @see viz.painters.Painter#paint_userImp()
	 */
	@Override
	protected void paint_userImp() {
		//System.err.println("Should not call methods in DummyPainter");
	}
}
