package viz.painters.lib;

import java.awt.Color;

import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

public abstract class HighlightPainter extends PainterWithNoComponent {
	protected Color color = Color.yellow;
	private Color original = null;
	public HighlightPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}
	
	public abstract Painter getPainterForHighlight();
	
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	protected void addToCanvas_userImp() {
		Painter painter = this.getPainterForHighlight();
		if (painter != null && painter.getComponent() != null) {
			original = painter.getComponent().getBackground();
			paint();
		}
	}

	@Override
	protected void destroy_userImp() {
		Painter painter = this.getPainterForHighlight();
		if (painter != null && painter.getComponent() != null && original != null) {
			painter.getComponent().setBackground(original);
		}
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
	}

	@Override
	protected void paint_userImp() {
		Painter painter = this.getPainterForHighlight();
		if (painter != null && painter.getComponent() != null) {
			painter.getComponent().setBackground(color);
		}
	}
}
