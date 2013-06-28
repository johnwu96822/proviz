package viz.painters.java.array;

import viz.painters.Painter;
import viz.painters.java.array.ArrayPainter;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public class MultiLineArrayPainter extends ArrayPainter {
	private int lineWidth = 15;
	
	public MultiLineArrayPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		this.showIndex();
	}

	@Override
	protected void paint_userImp() {
		int x = getLocation().x;
		int y = getLocation().y;
		int width = 0;
		int height = 0;
		//Determines the greatest width and height of individual cells, which then
		//are used as the width and height for every cell.
		for (Painter painter : this.getFieldPainters().values()) {
			if (width < painter.getWidth()) {
				width = painter.getWidth();
			}
			if (height < painter.getHeight()) {
				height = painter.getHeight();
			}
		}
		int rowCount = 0;
		for (int i = 0; i < this.getFieldPainters().size(); i++) {
			Painter fPainter = this.getFieldPainter("[" + i + "]");
			rowCount = i / lineWidth;
			if (fPainter != null) {
				if (!isVertical) {
					fPainter.setLocation(x + (i % lineWidth) * width, y + rowCount * 80);
				}
				else {
					fPainter.setLocation(x + rowCount * 100, y + (i % lineWidth) * height);
				}
				fPainter.paint();
	//Sets the size of field painters after their paint() to override the size set in their paint()
				fPainter.setSize(width, height);
			}
		}
	}
}
