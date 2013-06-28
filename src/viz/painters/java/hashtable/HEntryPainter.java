package viz.painters.java.hashtable;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * @author JW
 *
 */
public class HEntryPainter extends Painter {
	private VizCanvas container;
	private boolean isAdded = false;
	
	public HEntryPainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
		container = new VizCanvas(this.getCanvas().getComponentListener());
		container.removeMouseListeners();
		this.setDefaultBorderColor(Color.black);
	}

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			Painter key = this.getFieldPainter("key");
			Painter value = this.getFieldPainter("value");
			paint();			
			if (!isAdded) {
				this.getCanvas().add(this.container);
				this.container.setBorder(BorderFactory.createLineBorder(this.getDefaultBorderColor()));
				isAdded = !isAdded;
			}
			key.setCanvas(this.container);
			key.setRedirectMouseSelection(this);
			key.setTooltipsVariablePortion("key");
			value.setCanvas(this.container);
			value.setRedirectMouseSelection(this);
			value.setTooltipsVariablePortion("value");
			key.addToCanvas();
			value.addToCanvas();
			Painter nextField = getFieldPainter("next");
			if (nextField != null && !nextField.getVariable().isNull()) {
				nextField.addToCanvas();
			}
		}
		else {
			HashtablePainter hPainter = this.getHashtablePainter();
			if (hPainter != null && hPainter instanceof InternalHashtablePainter) {
				paint();
				this.container.drawText(this, "null", 25, 15);
				if (!isAdded) {
					this.getCanvas().add(this.container);
					this.container.setBorder(BorderFactory.createLineBorder(this.getDefaultBorderColor()));
					isAdded = !isAdded;
				}
			}
		}
	}

	@Override
	protected void destroy_userImp() {			
		isAdded = false;
	}

	public HashtablePainter getHashtablePainter() {
		Painter parent = this;
	//Not checking for null. Can't be!
		while (parent.getParent() != null && !(parent instanceof HashtablePainter)) {
			parent = parent.getParent();
		}
		return (parent instanceof HashtablePainter) ? (HashtablePainter) parent : null;
	}
	
	@Override
	public void handleChange(Change change, IVizVariable source) {
		if (change == Change.TO_NULL) {
			isAdded = false;
			this.destroy();
		}
		addToCanvas();
		getRootPainter().paint();
	}
	
	@Override
	protected void paint_userImp() {
		if (!this.getVariable().isNull()) {
			int width = this.getHashtablePainter().getCellWidth();
			int height = this.getHashtablePainter().getCellHeight();
			/*if (this.panel == null) {
				this.panel = new VizCanvas(this.getCanvas().getComponentListener());
			}*/
			this.container.setBounds(this.getLocation().x, this.getLocation().y, width * 2 + 2, height + 2);	
		  Painter key = this.getFieldPainter("key");
			//key.setLocation(getLocation().x, getLocation().y);
		  key.setLocation(1, 1);
			key.setSize(width, height);
			Painter value = this.getFieldPainter("value");
			//value.setLocation(getLocation().x + key.getWidth(), getLocation().y);
			value.setLocation(1 + width, 1);
			value.setSize(width, height);
	//Hook up a connector with the next element, if any
			Painter nextPainter = this.getFieldPainter("next");
			HashtablePainter hPainter = this.getHashtablePainter();
			if (!nextPainter.getVariable().isNull() && hPainter != null) {
				if (hPainter.isShowChains() || hPainter instanceof InternalHashtablePainter) {
					nextPainter.setLocation(this.getLocation().x + this.getWidth() + 20, this.getLocation().y);
					if (getCanvas().getConnectorManager().getConnector(this, nextPainter) == null) {
	//Sets up the next painter before the connector is established
						nextPainter.paint();
						getCanvas().getConnectorManager().hookUsUp(this, nextPainter);
						getCanvas().repaint();
					}
				}
				else {
					getCanvas().getConnectorManager().removeConnector(getCanvas().getConnectorManager().getConnector(this, nextPainter));
				}
			}
		}
		else {
			HashtablePainter hPainter = this.getHashtablePainter();
			if (hPainter != null && hPainter instanceof InternalHashtablePainter) {
				this.container.setBounds(this.getLocation().x, this.getLocation().y, 
						this.getHashtablePainter().getCellWidth() * 2 
						+ 2, this.getHashtablePainter().getCellHeight() + 2);	
			}
		}
	}	
	
	@Override
	public JComponent getComponent() {
		return this.container;
	}

	/* (non-Javadoc)
	 * @see viz.painters.Painter#switchedToThisPainter()
	 *
	@Override
	public void switchedToThisPainter() {
		Painter key = this.getFieldPainter("key");
		if (key != null) {
			key.destroy();
		}
		Painter value = this.getFieldPainter("value");
		if (value != null) {
			value.destroy();
		}
		super.switchedToThisPainter();
	}*/
}
