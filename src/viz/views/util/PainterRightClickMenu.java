package viz.views.util;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import viz.ProViz;
import viz.model.Visualization;
import viz.model.VizMapModel;
import viz.painters.Painter;
import viz.painters.lib.StringPainter;
import viz.painters.lib.VariablePainter;
import viz.runtime.IVizVariable;

/**
 * Represents the right-click menu that appears when each painter's graphical component
 * is right-clicked. It uses the getActions() and getActionListener() methods in Painter
 * to create menu entries and perform actions. 
 * @author JW
 */
public class PainterRightClickMenu extends JPopupMenu {
  //private Painter painter;
	public PainterRightClickMenu(Painter painter) {
		super();
		//this.painter = painter;
		JMenuItem item;
		if (painter.getActionCommands() != null && painter.getRightClickActionListener() != null) {
			for (String action : painter.getActionCommands()) {
				item = new JMenuItem(action);
				item.setActionCommand(action);
				item.addActionListener(painter.getRightClickActionListener());
				add(item);
			}
		}
		this.addSeparator();
		String[] actions = {"Repaint"};
	//Adding ProViz system commands that are universal to all painters
		for (String action : actions) {
			item = new JMenuItem(action);
			item.setActionCommand(action);
			item.addActionListener(painter.getSystemListener());
			add(item);
		}
		this.addSeparator();
		JMenu submenu = new JMenu("Switch Painter");
		for (Visualization viz : this.getSwitchablePainterNames(painter)) {
			item = new JMenuItem(new SwitchPainterAction(viz, painter));
			//item.setAction(action);
			item.setText(viz.getPainterName());
			submenu.add(item);
		}
		if (submenu.getMenuComponentCount() == 0) {
			submenu.setEnabled(false);
		}
		this.add(submenu);
		Painter parent = painter.getParent();
		while (parent != null) {
			submenu = new JMenu("Switch Painter: " + parent.getVariable().getName());
			for (Visualization viz : this.getSwitchablePainterNames(parent)) {
				item = new JMenuItem(new SwitchPainterAction(viz, parent));
				//item.setAction(action);
				item.setText(viz.getPainterName());
				submenu.add(item);
			}
			if (submenu.getMenuComponentCount() > 0) {
				this.add(submenu);
			}
			parent = parent.getParent();
		}
		if (!(painter instanceof StringPainter)) {
			item = new JMenuItem(new SwitchPainterAction(new Visualization("viz.painters.lib.StringPainter"), painter));
			item.setText("StringPainter (w/ Caution!");
			this.add(item);
		}
		if (!(painter instanceof VariablePainter)) {
			item = new JMenuItem(new SwitchPainterAction(new Visualization("viz.painters.lib.VariablePainter"), painter));
			item.setText("VariablePainter (w/ Caution!");
			this.add(item);
		}
	}
	
	class SwitchPainterAction extends AbstractAction {
		private Visualization viz;
		private Painter switchingPainter;
		public SwitchPainterAction(Visualization viz, Painter painter) {
			this.viz = viz;
			this.switchingPainter = painter;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			ProViz.getVPM().switchOver(switchingPainter, viz.getPainterName(), false, true, true);//, null);
		}
		/**
		 * @return the viz
		 */
		public Visualization getViz() {
			return viz;
		}
	}

	/**
	 * @param painter
	 * @return
	 */
	public ArrayList<Visualization> getSwitchablePainterNames(Painter painter) {
		ArrayList<Visualization> rv = new ArrayList<Visualization>();
		String original = painter.getClass().getName();
		//System.out.println("Switchable painter - original - " + original);
		IVizVariable var = painter.getVariable();
		Visualization[] vizs = null;
	//Finds all switchable painters in the variable's annotation and its type class' annotations
		if (var.isLocalVariable()) {
			vizs = VizMapModel.getInstance().findVariableVCs_runtime(var.getStackFrame().getMethodID(), var.getName());
		}
		else {
	//var is a field
			vizs = VizMapModel.getInstance().findFieldVCs_runtime(var.getParent().getActualType(), var.getName());
		}
		ArrayList<String> temp = new ArrayList<String>();
		for (Visualization viz : vizs) {
			String name = viz.getPainterName();
			if (!name.equals(original) && !name.equals("1D") && !temp.contains(name)) {
					rv.add(viz);
					temp.add(name);
				
			}
		}
		//TODO Inheritance going up the hierarchy
		Visualization[] tVizs = VizMapModel.getInstance().findTypeVCs_runtime(var.getActualType());
		for (Visualization viz : tVizs) {
			String name = viz.getPainterName();
			if (!name.equals(original) && !temp.contains(name)) {
				rv.add(viz);
				temp.add(name);
			}
		}
		return rv;
	}
}
