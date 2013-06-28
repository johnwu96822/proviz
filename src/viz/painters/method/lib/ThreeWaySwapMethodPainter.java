package viz.painters.method.lib;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JLabel;

import viz.ProViz;
import viz.animation.motion.move.LinearPath;
import viz.animation.motion.move.Path;
import viz.animation.motion.move.CurvePath;
import viz.animation.motion.Direction;
import viz.animation.motion.Motion;
import viz.painters.java.array.IntBarPainter;
import viz.painters.method.MethodAction;
import viz.painters.method.MethodPainter;
import viz.painters.Painter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;
import viz.swing.VPanel;
import viz.views.VizCanvas;


/**
 * Visualizes a swap method with parameters being an array and two indices whose
 * names must be what's returned by getArray(), getIndex1(), and getIndex2(). 
 * TODO Currently only works with IntBarPaiinter as the elements for
 * swapping.
 * @author JW
 *
 */
public abstract class ThreeWaySwapMethodPainter extends MethodPainter {
	private JLabel tempLabel;
	private VPanel iBarPanel;
	private VPanel jBarPanel;

	private Painter arrayPainter = null;
	private Painter iPainter = null;
	private Painter jPainter = null;
	
	public ThreeWaySwapMethodPainter(VizStackFrame stackFrame, VizCanvas canvas) {
		super(stackFrame, canvas);
	//Listens to the NEW change event on the "temp" variable
		this.addBeforeAction(getTempVariable(), new MethodAction(Change.NEW) {
			public void run(IVizVariable var) {
				tempDeclared(var);
			}
		});
	}
	
	/**
	 * Gets the variable name of the array being swapped. This array variable should
	 * be the parameter 
	 * @return
	 */
	public abstract String getArray();
	
	/**
	 * Gets the name of the first index variable for swapping.
	 * @return
	 */
	public abstract String getIndex1();
	/**
	 * Gets the name of the second index variable for swapping.
	 * @return
	 */
	public abstract String getIndex2();
	/**
	 * Gets the name of temporary variable used to facilitate the swapping. 
	 * @return
	 */
	public abstract String getTempVariable();
	
	/**
	 * @param temp
	 */
	private void tempDeclared(IVizVariable temp) {
		IVizVariable array = this.getStackFrame().getVariable(getArray());
		arrayPainter = ProViz.getVPM().getPainter(array);
		if (arrayPainter != null) {
			int iIndex = Integer.parseInt(this.getStackFrame().getVariable(getIndex1()).getValueAsString());
			int jIndex = Integer.parseInt(this.getStackFrame().getVariable(getIndex2()).getValueAsString());
			iPainter = arrayPainter.getFieldPainter("[" + iIndex + "]");
			jPainter = arrayPainter.getFieldPainter("[" + jIndex + "]");
			if (iPainter == null || jPainter == null) {
				return;
			}
			tempLabel = new JLabel(getTempVariable());
			tempLabel.setBounds(arrayPainter.getLocation().x + arrayPainter.getWidth() / 2, 100, 30, 20);
			getCanvas().add(tempLabel);
			
			iBarPanel = new BarPanel((IntBarPainter) iPainter);
			iBarPanel.setBounds(iPainter.getLocation().x, iPainter.getLocation().y, iPainter.getWidth(), iPainter.getHeight());
			getCanvas().add(iBarPanel);
			getCanvas().moveToFront(iBarPanel);
			
			Point loc = new Point(tempLabel.getLocation().x, tempLabel.getLocation().y 
					+ tempLabel.getHeight());
			Path path = new LinearPath(iBarPanel, iPainter.getLocation(), loc, null);
			ProViz.getAnimationController().scheduleAnimation(iPainter, 
					new Motion[] {path}, null);
			this.addBeforeAction(iPainter.getVariable().getName(), new MethodAction(Change.VALUE_CHANGED) {
				public void run(IVizVariable var) {
					jToI();
				}
			});
		}
	}
	
	private void jToI() {
		jBarPanel = new BarPanel((IntBarPainter) jPainter);
		jBarPanel.setBounds(jPainter.getLocation().x, jPainter.getLocation().y, jPainter.getWidth(), jPainter.getHeight());
		getCanvas().add(jBarPanel);
		getCanvas().moveToFront(jBarPanel);
		Point loc = new Point(iPainter.getLocation().x, jPainter.getLocation().y);
		Path path = new CurvePath(jBarPanel, jPainter.getLocation(), loc, Direction.DOWN);
		ProViz.getAnimationController().animateNow(false, path);
		this.addBeforeAction(jPainter.getVariable().getName(), new MethodAction(Change.VALUE_CHANGED) {
			public void run(IVizVariable var) {
				tempToJ();
			}
		});
	}
	
	private void tempToJ() {
		Point loc = new Point(jPainter.getLocation().x, jPainter.getLocation().y);
		Path path = new LinearPath(iBarPanel, iBarPanel.getLocation(), loc, null);
		ProViz.getAnimationController().animateNow(false, path);
	}
	
	@Override
	public void methodReturned() {
		if (iBarPanel != null) {
			this.getCanvas().remove(iBarPanel);
		}
		if (this.tempLabel != null) {
			this.getCanvas().remove(tempLabel);
		}
		if (this.jBarPanel != null) {
			this.getCanvas().remove(jBarPanel);
		}
	}

	@Override
	public void methodInvoked() {
	}

	/* (non-Javadoc)
	 * @see viz.painters.method.MethodPainter#shouldContinueMethod()
	 */
	@Override
	public boolean shouldContinueMethod() {
		return true;
	}
	
}

class BarPanel extends VPanel {
	private int value;
	private String text;
	private IntBarPainter painter;
	private int baseWidth = 10;
	public BarPanel(IntBarPainter painter) {
		super();
		this.painter = painter;
		value = Integer.parseInt(painter.getVariable().getValueAsString());
		text = painter.getVariable().getValueAsString();
	}
	
	public void paint(Graphics g) {
		if (painter.isVertical()) {
			if (value >= 0) {
				g.fillRect(5, 15, baseWidth, this.getHeight());
				g.drawString(text, 5, 12);
			}
			else {
				g.fillRect(5, 0, baseWidth, this.getHeight() - 15);
				g.drawString(text, 5, this.getHeight() - 3);
			}
		}
		else {
			if (value >= 0) {
				g.fillRect(0, 5, this.getWidth() - 15, baseWidth);
				g.drawString(text, this.getWidth() - 12, 12);
			}
			else {
				g.fillRect(15, 5, this.getWidth() - 15, baseWidth);
				g.drawString(text, 3, 12);
			}
		}
	}
}
