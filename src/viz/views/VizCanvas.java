package viz.views;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import viz.ProViz;
import viz.animation.AnimationController;
import viz.painters.IPainter;
import viz.painters.Painter;
import viz.painters.PainterWithNoComponent;
import viz.swing.VLayeredPane;
import viz.views.util.ComponentListener;
import viz.views.util.IConnectable;

/**
 * 
 * If VizCanvas extends JPanel, after adding each component, a repaint() must be called
 * @author JW
 *
 */
public class VizCanvas extends VLayeredPane implements IConnectable {//JPanel {
	private List<IPainter> paintersToBePainted = new ArrayList<IPainter>();
	private List<Painter> selection = new ArrayList<Painter>();

	private Point pressLoc = new Point(0, 0);
	private Point dragLoc = new Point(0, 0);
	private int mouseButton = 0;
	private ComponentListener componentListener = null;
	
	private VizCanvasMouseListener mouseListener = new VizCanvasMouseListener();
	private VizCanvasMouseMotionListener mouseMotionListener = new VizCanvasMouseMotionListener();
	
	private ConnectorManager connectorManager = new ConnectorManager(this);
	
	//private String text = null;
	private Hashtable<IPainter, DrawingText> texts = new Hashtable<IPainter, DrawingText>();
	
	private double scale = 1;

	public VizCanvas() {
		this(null);
	}
	
	public void removeMouseListeners() {
		this.removeMouseListener(mouseListener);
		this.removeMouseMotionListener(mouseMotionListener);
	}
	
	public VizCanvas(ComponentListener listener) {
		this.setLayout(null);
		this.addMouseListener(new VizCanvasMouseListener());
		this.addMouseMotionListener(new VizCanvasMouseMotionListener());
		if (listener != null) {
			this.componentListener = listener;
		}
		else {
			this.componentListener = new ComponentListener(this);
		}
	}
	
	/**
	 * @return the connectorManager
	 */
	public ConnectorManager getConnectorManager() {
		return connectorManager;
	}

	/**
	 * @return the componentListener
	 */
	public ComponentListener getComponentListener() {
		return componentListener;
	}
	
	/**
	 * Sets the component listener for all painters on this canvas. This method must
	 * be called before any painter is added to this canvas.
	 * @param listener
	 */
	public void setComponentListener(ComponentListener listener) {
		this.componentListener = listener;
	}
	
/*	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
		g2.scale(2, 2);
		super.paint(g2);
	}*/

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(this.getFont().deriveFont(16f));
		synchronized (this) {
			for (DrawingText dText : this.texts.values()) {
				g.drawString(dText.getText(), dText.getLocation().x, dText.getLocation().y);
			}
		}
		Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    scale = ProViz.getInstance().getZoom();
    g2.scale(scale, scale);
	  // draw rectangle if mouse is pressed and dragged.
		if (mouseButton == MouseEvent.BUTTON1 
				&& ProViz.getAnimationController().getState() != AnimationController.State.running) {
			g.setColor(Color.blue);
			g.drawRect((int) Math.min(pressLoc.x, dragLoc.x),
						     (int) Math.min(pressLoc.y, dragLoc.y),
						     (int) Math.abs(pressLoc.x - dragLoc.x),
					       (int) Math.abs(pressLoc.y - dragLoc.y));
			g.setColor(Color.black);
		}
		synchronized (connectorManager) {
			//System.out.println("No. of connectors: " + connectorManager.getConnectors().size());
			for (Connector line : connectorManager.getConnectors()) {
				line.refresh();
				g.drawLine(line.getPoint1().x, line.getPoint1().y, line.getPoint2().x, line.getPoint2().y);
			}
		}
		if (ProViz.getAnimationController().getState() != AnimationController.State.stop) {
			synchronized (this) {
				for (IPainter painter : this.paintersToBePainted) {
					painter.draw(g);
				}
			}
		}
	}
	
	/**
	 * @param text the text to set
	 */
	public synchronized void drawText(IPainter key, String text, int x, int y) {
		this.texts.put(key, new DrawingText(text, x, y));
		repaint();
	}

	/**
	 * @return the text
	 */
	public String getText(Object key) {
		return texts.get(key).getText();
	}
	
	public synchronized void removeText(Object key) {
		this.texts.remove(key);
		repaint();
	}

	public synchronized void addPainterToBePainted(IPainter painter) {
		if (!this.paintersToBePainted.contains(painter)) {
			this.paintersToBePainted.add(painter);
		}
	}
	
	public synchronized boolean removePainterToBePainted(IPainter painter) {
		return this.paintersToBePainted.remove(painter);
	}
	
	/**
	 * Gets the painters selected by the user with mouse.
	 * @return the list of selected painters
	 */
	public List<Painter> getSelection() {
		return selection;
	}
	
	/**
	 * Clears the list of selected painters.
	 */
	public void clearSelection() {
		for (Painter painter : selection) {
			painter.highlightSelectErase();
		}
		selection.clear();
	}
	
	/**
	 * Adds a newly selected painter to the list of selected painters and highlights
	 * the painter by calling Painter.highlightSelect().
	 * @param painter
	 */
	public void addSelection(Painter painter) {
		if (!selection.contains(painter)) {
			selection.add(painter);
			painter.highlightSelect(Color.MAGENTA);
		}
	}

	public void clearAll() {
		//AnimationController.getInstance().clear();
		//AnimationToolBar.getInstance().setStopState();
		this.connectorManager.clear();
		this.selection.clear();
		synchronized (this) {
			this.paintersToBePainted.clear();
			this.texts.clear();
		}
		this.removeAll();
		//this.repaint();
	}

	/**
	 * Used by mouse listeners.
	 * @return
	 */
	private VizCanvas getVizCanvas() {
		return this;
	}
	
	public void eraseHighlights() {
		componentListener.eraseAliasBackground();
		clearSelection();
	}
	
	private class VizCanvasMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {
			if (ProViz.getAnimationController().getState() != AnimationController.State.running) {
				eraseHighlights();
			}
			pressLoc.x = (int) (e.getX() / scale);
			pressLoc.y = (int) (e.getY() / scale);
			dragLoc.x = pressLoc.x;
			dragLoc.y = pressLoc.y;
			mouseButton = e.getButton();
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e)	{}

		public void mouseReleased(MouseEvent e)
		{
			if (mouseButton == MouseEvent.BUTTON3 
					|| ProViz.getAnimationController().getState() == AnimationController.State.running) {
				pressLoc.x = 0;
				pressLoc.y = 0;
				dragLoc.x = 0;
				dragLoc.y = 0;
				mouseButton = 0;
				return;
			}
			int mx = (int) (e.getX() / scale);
			int my = (int) (e.getY() / scale);
				int lx = (mx < pressLoc.x) ? mx : pressLoc.x;
				int rx = (lx == mx) ? pressLoc.x : mx;
				int uy = (my < pressLoc.y) ? my : pressLoc.y;
				int by = (uy == my) ? pressLoc.y : my;
				int x, xx, y, yy;
				for (Painter painter : ProViz.getVPM().getPrimitivePainters()) {
					if (painter.getCanvas() != getVizCanvas()) {
						continue;
					}
					x = painter.getLocation().x;
					xx = x + painter.getWidth();
					y = painter.getLocation().y;
					yy = y + painter.getHeight();
				//Checks if the painter is inside the rectangle.
					
					if (!(painter instanceof PainterWithNoComponent)) {
						if ((xx >= lx) && (yy >= uy) && (x < rx) && (y < by) && painter.redirectMouseSelection() == painter) {
							//Adds the component within selection rectangle to NetworkManager
							addSelection(painter);
						}
					}
				}
				for (List<Painter> painters : ProViz.getVPM().getUniqueObjectTable().values()) {
					for (Painter painter : painters) {
						if (painter.getCanvas() != getVizCanvas()) {
							continue;
						}
						x = painter.getLocation().x;
						xx = x + painter.getWidth();
						y = painter.getLocation().y;
						yy = y + painter.getHeight();
						//Checks if the painter is inside the rectangle.
						if (!(painter instanceof PainterWithNoComponent)) {
							if ((xx >= lx) && (yy >= uy) && (x < rx) && (y < by) && painter.redirectMouseSelection() == painter) {
								//Adds the component within selection rectangle to NetworkManager
								addSelection(painter);
							}
						}
					}
				}
				if (e.isPopupTrigger()) {
					//(new ComponentPopupMenu(null)).show(e.getComponent(), e.getX(), e.getY());
				}
				if (selection.size() == 1) {
					componentListener.setPainter(selection.get(0));
					componentListener.highlightAliasObjects();
				}
				else {
	//PainterWithNoComponent will not be selectable
					for (Painter p : selection) {
						ProViz.println(p.toString());
					}
				}
				//System.out.println(NetworkManager.getInstance().getSelectedComponents());
				pressLoc.x = 0;
				pressLoc.y = 0;
				dragLoc.x = 0;
				dragLoc.y = 0;
				mouseButton = 0;
				repaint();
		} //end mouseReleased
	} //end NetworkSpaceMouseListener
	
	private class VizCanvasMouseMotionListener implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {
			if (mouseButton == MouseEvent.BUTTON3) {
				int mx = (int) (e.getX() / scale);
				int my = (int) (e.getY() / scale);
				//System.out.println("Right click drag");
				Rectangle currentRect = getVisibleRect();
				Rectangle rect = new Rectangle(0, 0, 1, 1);
				if (mx > dragLoc.x) {
					rect.x = (int) currentRect.getMaxX() + (mx - dragLoc.x) / 2 + 1;
				}
				else if (mx < dragLoc.x) {
					rect.x = currentRect.x - 2 - (dragLoc.x - mx) / 2;
				}
				else {
					rect.x = currentRect.x;
				}
				if (my > dragLoc.y) {
					rect.y = (int) currentRect.getMaxY() + (my - dragLoc.y) / 2 + 1;
				}
				else if (my < dragLoc.y) {
					rect.y = currentRect.y - 2 - (dragLoc.y - my) / 2;
				}
				else {
					rect.y = currentRect.y;
				}
				scrollRectToVisible(rect);
				dragLoc.x = mx;
				dragLoc.y = my;
				repaint();
				return;
			}
			if (ProViz.getAnimationController().getState() == AnimationController.State.running) {
				return;
			}
			int mx = (int) (e.getX() / scale);
			int my = (int) (e.getY() / scale);
			int lx = (mx < pressLoc.x) ? mx : pressLoc.x;
			int rx = (lx == mx) ? pressLoc.x : mx;
			int uy = (my < pressLoc.y) ? my : pressLoc.y;
			int by = (uy == my) ? pressLoc.y : my;
			int x, xx, y, yy;
			for (Painter painter : ProViz.getVPM().getPrimitivePainters()) {
				if (painter.getCanvas() != getVizCanvas()) {
					continue;
				}
				x = painter.getLocation().x;
				xx = x + painter.getWidth();
				y = painter.getLocation().y;
				yy = y + painter.getHeight();
			//Checks if the network component is inside the rectangle.
				if ((xx >= lx) && (yy >= uy) && (x < rx) && (y < by) && painter.redirectMouseSelection() == painter) {
					painter.highlightSelect(Color.MAGENTA);
				}
				else {
					painter.highlightSelectErase();
				}
			}
			for (List<Painter> painters : ProViz.getVPM().getUniqueObjectTable().values()) {
				for (Painter painter : painters) {
					if (painter.getCanvas() != getVizCanvas()) {
						continue;
					}
					x = painter.getLocation().x;
					xx = x + painter.getWidth();
					y = painter.getLocation().y;
					yy = y + painter.getHeight();
				//Checks if the network component is inside the rectangle.
					if ((xx >= lx) && (yy >= uy) && (x < rx) && (y < by) && painter.redirectMouseSelection() == painter) {
						painter.highlightSelect(Color.MAGENTA);
					}
					else {
						painter.highlightSelectErase();
					}
				}
			}
			dragLoc.x = mx;
			dragLoc.y = my;

		  Rectangle r = new Rectangle(mx, my, 1, 1);
		    
		  scrollRectToVisible(r);
			repaint();
		}
		public void mouseMoved(MouseEvent e) {} //end mouseMoved
	} //end NetworkingSpaceMouseMotionListener
}

/**
 * @author JW
 *
 */
class DrawingText {
	private String text;
	private Point location;
	
	public DrawingText(String text, int x, int y) {
		this.text = text;
		location = new Point(x, y);
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * @return the location
	 */
	public Point getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(Point location) {
		this.location = location;
	}
}
