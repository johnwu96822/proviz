package viz.views.util;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.painters.Painter;
import viz.painters.VizPainterManager;
import viz.runtime.IVizVariable;
import viz.swing.VComponent;
import viz.views.VizCanvas;

/**
 * TODO Make MethodPainter selectable, too
 * Handles the click-and-drag action of a mouse on a network component.
 * @author Jo-Han Wu
 */
public class ComponentListener implements MouseMotionListener, MouseListener {

	private Point pressedPoint = null;
	private Component pressedComponent = null;
	private Painter selectedPainter = null;
	private boolean isRightClick = false;
	private Painter origin = null;
	private VizCanvas canvas;  	
	ArrayList<Painter> moved = new ArrayList<Painter>();  
	
	public ComponentListener(VizCanvas canvas) {
		this.canvas = canvas;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent event) {
		if (ProViz.getAnimationController().getState() == AnimationController.State.running) {
			return;
		}
		Painter current = null;
    this.pressedComponent = (Component) event.getSource();
    this.pressedPoint = event.getPoint();
    this.eraseAliasBackground();
    
    
  //Find which selectedPainter is being pressed on
    VizPainterManager vpm = ProViz.getVPM();
    for (Painter pa : vpm.getPrimitivePainters()) {
    	if (pressedComponent == pa.getComponent()) {
    		current = pa.redirectMouseSelection();
    		this.origin = pa;
    		break;
    	}
    }
    if (current == null) {
    	for (List<Painter> painters : vpm.getUniqueObjectTable().values()) {
    		for (Painter pa : painters) {
        	if (pressedComponent == pa.getComponent()) {
        		current = pa.redirectMouseSelection();
        		this.origin = pa;
        		break;
        	}
    		}
    	}
    }
  	if (current == null) {
  		this.pressedComponent = null;
  		this.pressedPoint = null;
  		this.origin = null;
  		if (this.selectedPainter != null) {
  			this.selectedPainter.highlightSelectErase();
  			this.selectedPainter = null;
  		}
  		return;
  	}
		this.selectedPainter = current;
  	//System.out.println(selectedPainter + " " + selectedPainter.getAllVariableNames() + " " + selectedPainter.getLocation());
  	//	System.out.println(origin + " " + origin.getAllVariableNames() + " " + origin.getLocation());
  	if (event.getButton() == MouseEvent.BUTTON3) {
  		this.isRightClick = true;
  	}
	//If the selectedComponents in NetworkManager already contains this network 
	//component, it means this mouse press event is to drag the whole previous
	//selected component(s), not selecting it. Otherwise this is a new selection,
	//so add this network component to the selectedComponents.
		if (!canvas.getSelection().contains(this.selectedPainter)) {
			canvas.clearSelection();		
			//Highlight the selected network component.
			this.selectedPainter.highlightSelect(Color.GREEN);
			canvas.getSelection().add(this.selectedPainter);
		}
  }
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent event) {
		if (ProViz.getAnimationController().getState() == AnimationController.State.running || 
				this.pressedPoint == null) {
			return;
		}
		//x and y are the movements
  	int x = event.getX() - this.pressedPoint.x;
  	int y = event.getY() - this.pressedPoint.y;
  	//boolean hasLine = false;
		int destX = selectedPainter.getLocation().x + x;
		int destY = selectedPainter.getLocation().y + y;
	//Restrict the movement within VizCanvas's size
		if (x < 0) {
			if (destX < 0) {
				destX = 0;
			}
		}
		if (y < 0) {
			if (destY < 0) {
				destY = 0;
			}
		}
		if (x > 0) {
			if (destX > selectedPainter.getCanvas().getWidth() - selectedPainter.getWidth()) {
				destX = selectedPainter.getCanvas().getWidth() - selectedPainter.getWidth();
			}
		}
		if (y > 0) {
			if (destY > selectedPainter.getCanvas().getHeight() - selectedPainter.getHeight()) {
				destY = selectedPainter.getCanvas().getHeight() - selectedPainter.getHeight();
			}
		}
  //If right click, then move that single component
  	if (this.isRightClick) {
  		if (this.selectedPainter != null) {
  			this.selectedPainter.setLocation(destX, destY);
  		}
  		//hasLine = selectedPainter.hasConnector();
  	}
  	else {
  		int dx = destX - selectedPainter.getLocation().x;
  		int dy = destY - selectedPainter.getLocation().y;
			for (Painter selectedPainter : canvas.getSelection()) {
				//location = selectedPainter.getLocation();
	//Always call move only on the root selectedPainter
				Painter parent = selectedPainter.getRootPainter();				
				if (!moved.contains(parent)) {
					Painter.move(parent, dx, dy);
					moved.add(parent);
				}
				//hasLine = selectedPainter.hasConnector();
			}
			moved.clear();
		}
  	canvas.scrollRectToVisible(new Rectangle(selectedPainter.getLocation().x, selectedPainter.getLocation().y, 
  			selectedPainter.getWidth(), selectedPainter.getHeight()));
  	//if (hasLine) {
  		canvas.repaint();
  	/*}
  	else {
  		canvas.validate();
  	}*/
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent event) {
		if (ProViz.getAnimationController().getState() == AnimationController.State.running) {
			return;
		}
		if (canvas.getSelection().size() == 1) {
			this.highlightAliasObjects();
		}
		/*if (!event.isPopupTrigger()) {
			System.out.println("WHAT THE");
			NetworkManager.getInstance().clearSelected();
		}*/
		//System.out.println(NetworkManager.getInstance().getSelectedComponents());
//		if (event.isPopupTrigger() && this.nc != null && 
//				AnimationController.getState() == AnimationController.STOP) {
			//ComponentDragger.highlight((JLabel) event.getSource());
//			(new ComponentPopupMenu(this.nc)).show(event.getComponent(), event.getX(), event.getY());
//		}
		this.isRightClick = false;
    this.pressedComponent = null;
    this.pressedPoint = null;
	} //end mouseReleased

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent event) {
	}
	
	public void eraseAliasBackground() {
		if (this.selectedPainter != null) {
			IVizVariable var = this.selectedPainter.getVariable();
			if (var != null && var.isObject() && !var.isNull()) {
				List<Painter> list = ProViz.getVPM().getAliasingPainters(var.getUniqueObjectID());
				if (list != null) {
					for (Painter alias : list) {
						if (alias != this.selectedPainter) {
							alias.highlightSelectErase();
						}
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {
		if (ProViz.getAnimationController().getState() == AnimationController.State.running) {
			return;
		}
		//Clears previously selected items in order to accept new selected items.
		if (event.getButton() == MouseEvent.BUTTON3) {//!event.isPopupTrigger()) {
			//System.out.println("Right button clicked!");
  		canvas.getSelection().clear();
			canvas.getSelection().add(this.selectedPainter);
			PainterRightClickMenu menu = new PainterRightClickMenu(this.selectedPainter);
			Component comp = selectedPainter.getComponent();
			if (comp instanceof VComponent) {
				//((VComponent) comp).rotate(0.1, 0, 0);
			}
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
		else {
			canvas.getSelection().clear();
			canvas.getSelection().add(this.selectedPainter);
			//highlightAliasObjects();
		}
		if (/*AnimationController.getState() == AnimationController.STOP && */this.selectedPainter != null) {
			if (event.getClickCount() == 1) {
				//this.painter.highlightSelect();
				//this.nc.getLabel().setBorder(BorderFactory.createLineBorder(Color.MAGENTA));
				//Highlighter.highlight(selectedPainter);
			}
			//Double click
			if (event.getClickCount() == 2) {
//				if (selectedPainter instanceof Workstation) {
//          Workstation workstation = (Workstation) selectedPainter;
//          SendQueueDisplay queue = new SendQueueDisplay(workstation);
//          queue.setVisible(true);
					//WorkstationDialog wDialog = new WorkstationDialog(workstation);
					//wDialog.setVisible(true);
//				}
			} 
		} //end if
		//String nameOfComponent = comp.getName();
  	//NetworkComponent ncomp = NetworkManager.getInstance().findComponent(nameOfComponent);
		//if (AnimationController.getState() == AnimationController.RUNNING) {
    //	this.nc.send(null);//, new Integer(1));
		//}
		//System.out.println(NetworkManager.getInstance().getSelectedComponents());
	}
	/**
	 * 
	 */
	public void highlightAliasObjects() {
		if (this.selectedPainter != null) {
			IVizVariable var = this.selectedPainter.getVariable();
			if (var.isObject() && !var.isNull()) {
				List<Painter> list = ProViz.getVPM().getAliasingPainters(var.getUniqueObjectID());
				for (Painter alias : list) {
					if (alias != this.selectedPainter) {
						alias.highlightSelect(Color.CYAN);
						System.out.println(alias.getAllVariableNames() + " " + var.getUniqueObjectID());
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	
	public void mouseEntered(MouseEvent event) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent event) {
	}
	
	/**
	 * DON'T EVER USE THIS METHOD! For VizCanvas use only!
	 * @param selectedPainter
	 */
	public void setPainter(Painter painter) {
		this.selectedPainter = painter;
	}
} //end class
