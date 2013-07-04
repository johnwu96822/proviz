package viz.painters;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.animation.motion.move.Path;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;
import viz.views.util.PainterSystemActionListener;

/**
 * The base class of all painters in ProViz.
 * @author JW
 */
public abstract class Painter extends AbstractPainter implements IValueChangedListener {
//	private VizCanvas canvas;
	
	private String previousValue = null;
	private String uniqueID = null;
	
	private String tooltipsFirstSeg;
	private boolean iHaveTooltipsControl = true;
	
	//TODO Can a painter has two parent painters?
	private Painter parent = null;
	
	//connectors is never null
	//private Map<Painter, Connector> connectors = new Hashtable<Painter, Connector>();

	private Map<String, Painter> fieldPainters = null;

	//Instance variables in this list would have names prefixed by "this."
	//Fields to listen to have '*' prefixed by the field name
	private List<String> dependingVariables = null;
	private List<IVizVariable> eventGenerators = new LinkedList<IVizVariable>();
	
	private List<IVizVariable> variablesToThisPainter = new LinkedList<IVizVariable>();

	private Color defaultBorderColor = null;
	private Painter redirectMouseSelection;
	private PainterSystemActionListener systemListener; 
	
	//private boolean isAdded = false;
	private boolean shouldCreateFieldPainters = true;

	/**
	 * It is recommended to set the location in the constructor rather than in addToCanvas(). But
	 * subclasses of Painter be cautious about calling setLocation(...) in the constructor because
	 * of the redirecting call to the component, and the component might not have been initialized 
	 * yet in the constructor. So make sure the component is initialized before calling setLocation().
	 * @param vvar
	 * @param canvas
	 */
	public Painter(IVizVariable vvar, VizCanvas canvas) {
		this.previousValue = vvar.getValueAsString();
		this.uniqueID = vvar.getUniqueObjectID();
		this.addVariableToThisPainter(vvar);
		this.canvas = canvas;
		this.redirectMouseSelection = this;
		this.systemListener = new PainterSystemActionListener(this);
		this.tooltipsFirstSeg = this.getAllVariableNames();
	}
	
	/**
	 * Paints this variable/object and should be able to do it any time. The paint method
	 * should be able to display the state of the object by painting the whole object/variable 
	 * statically (without animation). 
	 * User must impelement this method, but it should never be called to paint the object.
	 * Always call paint() instead.
	 * 
	 * If this method does not modify the screen, should set 
	 * AnimationController.screenModified to false.
	 */
	protected abstract void paint_userImp();

	/*public JComponent getComponent() {
		if (this.getVariable().isNull() && this.nullLabel != null) {
			return this.nullLabel;
		}
		else {
			return getComponent_userImp();
		}
	}*/

	/**
	 * Initializes the painter and adds this painter to the canvas. 
	 * 
	 * If this method does not modify the screen, should set 
	 * AnimationController.screenModified to false.
	 */
	protected abstract void addToCanvas_userImp();

	/**
	 * Any painter should handle change events in this method. No Change.NEW event from any variable
	 * of this painter will reach this method, so painters do not need to worry about that case.
	 * @param change
	 * @param source
	 */
	public abstract void handleChange(Change change, IVizVariable source);	

	/**
	 * Gets the graphical component associated with this painter. If this painter does not
	 * paint a component, return null. If a subclass decides to return null for this method,
	 * it must also override the geographical methods, including getLocation(), setLocation(),
	 * setSize(), getWidth(), and getHeight().
	 * @return A graphical component; null if this painter does not have one.
	 */
	public abstract JComponent getComponent();
	
	/**
	 * If this method does not modify the screen, may set 
	 * AnimationController.screenModified to false.
	 */
	protected abstract void destroy_userImp();

	/*protected void paintNull() {
		if (this.nullLabel == null) {
			this.destroy();
//Needs to get the location before creating the nullLabel because of how getLocation() is written
			Point location = this.getLocation();
			this.nullLabel = new JLabel(this.getAllVariableNames() + " = null");
			this.nullLabel.setBounds(location.x, location.y, 30, 20);
			this.nullLabel.addMouseListener(canvas.getComponentListener());
			this.nullLabel.addMouseMotionListener(canvas.getComponentListener());
			this.getCanvas().add(this.nullLabel);
		}
		return;
	}
	
	protected void eraseNull() {
		if (this.nullLabel != null) {
			this.getCanvas().remove(this.nullLabel);
			this.nullLabel = null;
		}
	}*/
	/**
	 * Paints the current state of the painter. Should not update the state of this painter;
	 * in other words, does not update fields for library painters.
	 * @param newValue
	 */
	public final void paint() {
		/*if (this.getVariable().isNull()) {
			paintNull();
		}
		else {
			eraseNull();
		}*/
		//if (isAdded) {
		AnimationController.screenModified = true;
			this.paint_userImp();
		//Update the value string with current value
			this.previousValue = this.getVariable().getValueAsString();
			this.uniqueID = this.getVariable().getUniqueObjectID();
		//}
	}
	
	/*
	 * @return the isAdded
	 *
	public boolean isAdded() {
		return isAdded;
	}
	
	protected void setIsAdded(boolean boo) {
		this.isAdded = boo;
	}*/

	/**
	 * Initializes the graphical component to be added to the canvas and calls
	 * addToCanvas_userImp().
	 */
	public final void addToCanvas() {
		/*if (this.getVariable().isNull()) {
			this.paint();
			return;
		}*/
		AnimationController.screenModified = true;
			super.addToCanvas();
			if (getComponent() != null) {
				this.getComponent().setToolTipText(this.tooltipsFirstSeg + " = " 
						+ this.getVariable().getValueAsString());
				//11.05.17 Comment out
				//this.highlightSelectErase(); 

				//this.getComponent().setDoubleBuffered(true);
				//}
			}
			addToCanvas_userImp();
	}

	/**
	 * Removes the graphical component from the canvas and then calls the user's destroy() method.
	 * This takes a painter off the screen, but the painter should be able to be add back to canvas
	 * again via addToCanvas().
	 * This method is called only by VPM.
	 */
	public final void destroy() {
		/*if (this.nullLabel != null) {
			this.getCanvas().remove(this.nullLabel);
			this.nullLabel = null;
		}*/
		//ProViz.println("Painter: Destroying " + this.getVariable().getName() + " " + this);
		//if (isAdded) {
			//isAdded = false;
		AnimationController.screenModified = true;
			destroy_userImp();
			super.destroy();
		//}
	}

	/**
	 * Sets the canvas that this painter should be added to. The canvas must be set correctly
	 * before the painter is added to the canvas in addToCanvas(). Also, if the canvas is used
	 * as a simple container for this painter and the mouse event should be redirected to the
	 * container, one must use setRedirectMouseSelection() to set the container painter to handle
	 * mouse events.
	 * @param canvas the canvas to set
	 */
	public void setCanvas(VizCanvas canvas) {
		if (canvas != null) {
			this.canvas = canvas;
			if (this.hasFieldPainter()) {
				for (Painter field : this.getFieldPainters().values()) {
					field.setCanvas(canvas);
				}
			}
		}
	}

	public String getPreviousValue() {
		return this.previousValue;
	}
	
	/**
	 * NEW and REMOVED events coming from this painter's own variable(s) will not occur in handleChange(),
	 * as they are filtered in this method.
	 * @see viz.painters.IValueChangedListener#systemHandleChange(viz.runtime.Change, viz.runtime.IVizVariable)
	 */
	public final void systemHandleChange(Change change, IVizVariable source) {
	//10.06.21 Filtered out REMOVED
		if ((change == Change.NEW || change == Change.REMOVED) && this.containsVariable(source)) {
			return;
		}
		ProViz.println("Firing change event on: " + source.getStackFrame().getMethodID() + " " + source.getName() + " " 
				+ source.getUniqueObjectID() + " - " + source.getValueAsString() 
				+ " - " + change);
		if (this.getComponent() != null) {
			this.getComponent().setToolTipText(tooltipsFirstSeg + ": " + this.getVariable().getValueAsString());
		}
		this.handleChange(change, source);
	}
	
	/*
	 * Moves the entire painter tree that the given painter belongs to to the given coordinate.
	 * @param x
	 * @param y
	 *
	public static void moveAllTo(Painter painter, int x, int y) {
		moveAll(painter, x - painter.getLocation().x, y - painter.getLocation().y);
	}*/
	
	/*
	 * Moves the entire painter tree the given painter belongs to by the given distance.
	 * @param painter
	 * @param dx
	 * @param dy
	 *
	public static void moveAll(Painter painter, int dx, int dy) {
		while (painter != null) {
			painter = painter.getParent();
		}
		move(painter, dx, dy);
	}*/
	
	//TODO private LinkedList<Painter> graphicalListeners = null;
	
	//public void addGraphicalListener(Painter listener) {
		
	//}

	/**
	 * Defines how this painter should move when the move motion is executed in animation.
	 * For example, if the painter has other graphical components, they should be moved as well.
	 * @param next The next point that the painter is moving to.
	 * @param path The motion path that's used to move this painter.
	 */
	public void moveMotion(Point next, Path path) {
		Painter.moveTo(this, next.x, next.y);
	}
	
	/**
	 * @return
	 */
	public boolean isLocationRedirected() {
		return false;
	}
	
	/**
	 * Moves the given painter and all field painters under it by the given distance.
	 * Any painter (particularly subclasses of ParasitePainter) whose isLocationRedirected()
	 * returns true will not be moved.
	 * @param painter
	 * @param x
	 * @param y
	 */
	public static void move(Painter painter, int dx, int dy) {
		if (!painter.isLocationRedirected()) {
			painter.setLocation(painter.getLocation().x + dx, painter.getLocation().y + dy);
		}
		if (painter.hasFieldPainter()) {
			for (Painter field : painter.getFieldPainters().values()) {
				if (field.redirectMouseSelection == field) {
					move(field, dx, dy);
				}
			}
		}
	}

	/**
	 * Moves the given painter and all field painters under it to the given coordinate.
	 * @param painter
	 * @param x
	 * @param y
	 */
	public static void moveTo(Painter painter, int x, int y) {
		Point loc = painter.getLocation();
		move(painter, x - loc.x, y - loc.y);
	}
	
	protected final void addDependentVar(String var) {
		if (this.dependingVariables == null) {
			this.dependingVariables = new ArrayList<String>();
		}
		if (var != null && !this.dependingVariables.contains(var)) {
			this.dependingVariables.add(var);
		}
	}
	
	protected final boolean removeDependentVar(String var) {
		if (this.dependingVariables == null) {
			return false;
		}
		return this.dependingVariables.remove(var);
	}
	
	protected final void clearDependentVars() {
		if (this.dependingVariables != null) {
			this.dependingVariables.clear();
		}
	}
	
	protected final List<String> getDependentVars() {
		return this.dependingVariables;
	}	
	/**
	 * @return the parent
	 */
	public Painter getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Painter parent) {
		this.parent = parent;
	}
	
	/**
	 * This method will replace the existing field painter that has the same name as varName
	 * @param varName
	 * @param field
	 * @return The replaced field painter, if any
	 */
	protected synchronized Painter addOrReplaceFieldPainter(String varName, Painter field) {
		if (this.fieldPainters == null) {
			this.fieldPainters = new Hashtable<String, Painter>();
		}
		if (varName != null && field != null) {
			field.setParent(this);
			return this.fieldPainters.put(varName, field);
		}
		return null;
	}
	
	/**
	 * Removes the field painter from this painter by the name of the field.
	 * @param varName Name of the field, which is unique under an object.
	 * @return
	 */
	protected synchronized Painter removeFieldPainter(String varName) {
		if (this.fieldPainters != null) {
			Painter removed = this.fieldPainters.remove(varName);
			if (removed != null) {
				//System.out.println("Field painter removed 2: " + varName + " " + removed.getUniqueID());
				removed.setParent(null);
			}
			return removed;
		}
		return null;
	}
	
	/**
	 * Gets all field painters of this painter, which can be null.
	 * @return A map of <variable name, painter>
	 */
	public Map<String, Painter> getFieldPainters() {
		return this.fieldPainters;
	}
	
	/**
	 * Gets a field painter by the name of the field.
	 * @param varName Name of the field
	 * @return A field painter
	 */
	public synchronized Painter getFieldPainter(String varName) {
		if (this.fieldPainters != null) {
			return this.fieldPainters.get(varName);
		}
		return null;
	}
	
	/**
	 * Checks if this painter has any field painter.
	 * @return true if this painter has field painter; false otherwise.
	 */
	public synchronized boolean hasFieldPainter() {
		return (this.fieldPainters != null) && (!this.fieldPainters.isEmpty());
	}

	/**
	 * @return the uniqueID
	 */
	public String getUniqueID() {
		return uniqueID;
	}

	/**
	 * @param uniqueID the uniqueID to set
	 */
	protected void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	/**
	 * @param previousValue the previousValue to set
	 */
	protected void setPreviousValue(String valueString) {
		this.previousValue = valueString;
	}
	
	/**
	 * Adds a variable that is using this painter to this painter.
	 * @param var A variable that uses this painter.
	 */
	protected void addVariableToThisPainter(IVizVariable var) {
		if (var != null && !this.variablesToThisPainter.contains(var)) {
			//this.variablesToThisPainter.add(var);
	//11.02.14 Put the newer variable in the front of the list so that
	//getVariable() returns the aliasing variable closer to the top stack frame.
			this.variablesToThisPainter.add(0, var);
			this.addEventGenerator(var);
			var.addListener(this);
			if (this.getComponent() != null && this.iHaveTooltipsControl) {
				this.getComponent().setToolTipText(this.getAllVariableNames() + " = " + this.getVariable().getValueAsString());
			}
		}
	}
	
	/**
	 * Removes a variable that is using this painter.
	 * @param var
	 * @return
	 */
	protected synchronized boolean removeVariableToThisPainter(IVizVariable var) {
		this.removeEventGenerator(var);
		var.removeListener(this);
		boolean removed = this.variablesToThisPainter.remove(var);
	//Updates the tooltips only if iHaveTooltipsControl == true
		if (!this.variablesToThisPainter.isEmpty() && this.iHaveTooltipsControl) {
			if (this.getComponent() != null) {
				this.getComponent().setToolTipText(this.getAllVariableNames() 
						+ " = " + this.getVariable().getValueAsString());
			}
		}
		return removed;
	}
	
	/*
	 * Removes
	 *
	protected void removeAllVariables() {
		for (IVizVariable var : this.variablesToThisPainter) {
			this.removeEventGenerator(var);
			var.removeListener(this);
		}
	}*/
	
	/**
	 * Tooltips for a painter is by default the list of variables using this painter.
	 * Once this method is called, the caller will have to control the tooltipsFirstSeg all
	 * the way.
	 * @param tooltipsFirstSeg the tooltipsFirstSeg to set
	 */
	public void setTooltipsVariablePortion(String tooltips) {
		this.iHaveTooltipsControl = false;
		this.tooltipsFirstSeg = tooltips;
		if (this.getComponent() != null) {
			this.getComponent().setToolTipText(tooltips + ": " + this.getVariable().getValueAsString());
		}
	}

	/**
	 * Gets the list of variables that are using this painter.
	 * @return
	 */
	public List<IVizVariable> getVariablesToThisPainter() {
		return this.variablesToThisPainter;
	}
	
	/**
	 * Gets the first variable that uses this painter.
	 * @return
	 */
	public synchronized IVizVariable getVariable() {
		if (!this.variablesToThisPainter.isEmpty()) {
			return this.variablesToThisPainter.get(0);
		}
		return null;
	}
	/**
	 * Checks to see if a variable is using this painter.
	 * @param var
	 * @return
	 */
	public boolean containsVariable(IVizVariable var) {
		return this.variablesToThisPainter.contains(var);
	}

	/* (non-Javadoc)
	 * @see viz.runtime.IValueChangedListener#addEventGenerator(viz.runtime.VizVariable)
	 */
	@Override
	public void addEventGenerator(IVizVariable var) {
		if (var != null && !this.eventGenerators.contains(var)) {
			this.eventGenerators.add(var);
		}
	}
	
	/* (non-Javadoc)
	 * @see viz.runtime.IValueChangedListener#removeEventGenerator(viz.runtime.IEventGenerator)
	 */
	@Override
	public boolean removeEventGenerator(IVizVariable var) {
		return this.eventGenerators.remove(var);
	}
	
	/* (non-Javadoc)
	 * @see viz.runtime.IValueChangedListener#getEventGenerators()
	 */
	@Override
	public List<IVizVariable> getEventGenerators() {
		return this.eventGenerators;
	}
	
	private Color background = null;
	/**
	 * Highlights the graphical object painted by this painter by setting the border to
	 * pink. This is used when the graphical object is selected by the mouse.
	 */
	public void highlightSelect(Color color) {
		if (getComponent() != null) {
			getComponent().setBorder(BorderFactory.createLineBorder(color));
			background = getComponent().getBackground();
			getComponent().setBackground(color);
		}
	}
	
	/**
	 * Erases the highlighting of the graphical object painted by this painter by setting 
	 * the border to back to the default color. This is used when the graphical object is 
	 * selected by the mouse.
	 */
	public void highlightSelectErase() {
		if (getComponent() != null) {
			if (this.getDefaultBorderColor() == null) {
				getComponent().setBorder(null);
			}
			else {
				getComponent().setBorder(BorderFactory.createLineBorder(this.getDefaultBorderColor()));
			}
			//if (background != null) {
				//getComponent().setBackground(background);
				getComponent().setBackground(null);
			//}
			getComponent().repaint();
		}
	}
	
	/**
	 * Gets the default border color of this painter. By default, the border color is null. 
	 * setDefaultBorderColor() can be used to change the default border color.
	 * @return
	 */
	public Color getDefaultBorderColor() {
		return this.defaultBorderColor;
	}
	
	/**
	 * Sets the default border color 
	 * @param color
	 */
	public void setDefaultBorderColor(Color color) {
		this.defaultBorderColor  = color;
		if (this.getComponent() != null) {
			this.highlightSelectErase();
		}
	}

	/**
	 * Gets all the actions associated with this painter. By default the parent's action
	 * commands are returned.
	 * @return
	 */
	public String[] getActionCommands() {
		/*String[] rv = new String[2];
		rv[0] = "ProViz Option 1";
		rv[1] = "ProViz Option 2";
		return rv;*/
		return getParent() != null ? getParent().getActionCommands() : null;
	}

	/**
	 * Gets ProViz's system listener, which handles common operations for all painters.
	 * @return
	 */
	public final ActionListener getSystemListener() {
		return this.systemListener;
	}
	
	/**
	 * Gets the names of all variables that are pointing to this painter and puts them into
	 * a string. The names are separated by a ","
	 * @return
	 */
	public String getAllVariableNames() {
		StringBuffer sb = new StringBuffer(getVariable().getName());
		int i = 1;
		while (i < variablesToThisPainter.size()) {
			sb.append(", " + variablesToThisPainter.get(i).getName());
			i++;
		}
		return sb.toString();
	}
	
	/**
	 * 09.10.08 Gets the copies of field painters as an array. This method is used to avoid
	 * concurrent modification problems. For example, if one is to remove all field painters 
	 * using VPM's removePainter() call, he needs to iterate through this duplicated list of
	 * field painters.
	 * @return 
	 */
	public synchronized Painter[] getCopiesOfFieldPainters() {
		if (this.fieldPainters != null) {
			return this.fieldPainters.values().toArray(new Painter[0]);
		}
		return null;
	}
	
	public static String simplifyClassName(String declaredType) {
		int index = declaredType.indexOf('<');//.lastIndexOf('.');
		if (index != -1) {
			declaredType = simplify(declaredType.substring(0, index)) + '<' + 
				simplifyClassName(declaredType.substring(index + 1, declaredType.length() - 1)) + '>';
		}
		else {
			declaredType = simplify(declaredType);
		}
		return declaredType;
	}
	
	/**
	 * Gets the simple class name, the last segment of a class path after the dot "."
	 * @param type
	 * @return
	 */
	private static String simplify(String type) {
		int last = type.lastIndexOf('.');
		if (last != 1) {
			type = type.substring(last + 1);
		}
		return type;
	}

	/**
	 * Certain painters do all the painting without the need of field painters.
	 * Then they should return false for this method for efficiency.
	 * @return the shouldCreateFieldPainters
	 */
	public boolean shouldCreateFieldPainters() {
		return shouldCreateFieldPainters;
	}

	/**
	 * When set to false, VPM will not create any field painter for this painter. This method
	 * must be called in the constructor of a subclass painter, otherwise it will not work.
	 * @param shouldCreateFieldPainters the shouldCreateFieldPainters to set
	 */
	public void setShouldCreateFieldPainters(boolean shouldCreateFieldPainters) {
		this.shouldCreateFieldPainters = shouldCreateFieldPainters;
	}
	
	/**
	 * Mouse selection events generated from this painter will be redirected to the painter 
	 * returned by this method.
	 * @return
	 * @see setRedirectMouseSelection(Painter)
	 */
	public Painter redirectMouseSelection() {
		return this.redirectMouseSelection;
	}
	
	/**
	 * Redirects the mouse selection events generated from this painter to the given painter. 
	 * The mouse redirection is for painters that are put in a container. The container should 
	 * redirect child painter's mouse selection to itself so that the container responds to mouse
	 * events instead of the child painters. But of course, this takes out the individual
	 * mouse activity for child painters. Redirection will propagate to down to all field painters.
	 * @param painter
	 */
	public void setRedirectMouseSelection(Painter painter) {
		this.redirectMouseSelection = painter;
		if (this.hasFieldPainter()) {
			for (Painter field : this.getFieldPainters().values()) {
				field.setRedirectMouseSelection(painter);
			}
		}
	}
	
	/**
	 * Gets the root painter of the painter tree that contains this painter. The root
	 * painter must visualize a local variable.
	 * @return
	 */
	public Painter getRootPainter() {
		Painter painter = this;
		while (painter.getParent() != null) {
			painter = painter.getParent();
		}
		return painter;
	}
  ///////////////////////// To Be Overridden //////////////////////////

	/**
	 * Gets the action listener for this painter. By default if there is a parent painter, the
	 * parent painter's listener is returned; otherwise null. For use by PainterRightClickMenu.
	 * @return
	 */
	public ActionListener getRightClickActionListener() {
		//return new PainterSystemActionListener();
		return getParent() != null ? getParent().getRightClickActionListener() : null;//new PainterSystemActionListener(); 
	}
	
	/**
	 * Handles any needed task AFTER a painter is switched to THIS type of painter.
	 * The default implementation calls addToCanvas() to add the newly switched painter to
	 * its canvas.
	 * 
	 * Because addToCanvas() is generally used in the method, painters overriding this method
	 * may either not call addToCanvas(), or they need to destroy its field painters prior to 
	 * addToCanvas().
	 */
	public void switchedToThisPainter() {
		this.addToCanvas();
	}

	/**
	 * Sets the size of the graphical component of this painter.
	 * @param x
	 * @param y
	 */
	public void setSize(int x, int y) {		
		if (getComponent() != null ) {
			getComponent().setSize(x, y);
		}
	}
	
	public boolean voo() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see viz.painters.IPainter#getInstanceVariable(java.lang.String)
	 */
	public IVizVariable getInstanceVariable(String varName) {
		IVizVariable thisVar = this.getVariable().getStackFrame().getVariable("this");
		if (thisVar != null) {
			return thisVar.getField(varName);
		}
		return null;
	}
	
	/**
	 * Gets a local variable in the same stack frame as this.getVariable().
	 * @param varName
	 * @return
	 */
	public IVizVariable getLocalVariable(String varName) {
		return this.getVariable().getStackFrame().getVariable(varName);
	}
}
