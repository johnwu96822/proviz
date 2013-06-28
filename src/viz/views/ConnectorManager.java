package viz.views;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import viz.views.util.IConnectable;

/**
 * Stores and manages all connectors in ProViz. There can be only one connector between
 * two connectables.
 * @author Jo-Han Wu
 */
public class ConnectorManager {
  private List<Connector> connectors = new ArrayList<Connector>();
  //private Hashtable<IConnectable, Hashtable<IConnectable, Connector>> conTable = 
  //	new Hashtable<IConnectable, Hashtable<IConnectable, Connector>>();
  private VizCanvas canvas = null;
  
  private Hashtable<IConnectable, ArrayList<Connector>> cTable = 
  	new Hashtable<IConnectable, ArrayList<Connector>>();
  
  public ConnectorManager(VizCanvas canvas) {
  	this.canvas = canvas;
  }

	/**
	 * Connects two connectables by their <i>center</i> positions with a connector.
	 * @param connectable1 Cannot be null
	 * @param connectable2 Cannot be null
	 * @return
	 */
	public Connector hookUsUp(IConnectable connectable1, IConnectable connectable2) {
		/*Connector connector = new Connector(connectable1, connectable2);
		//painter1.addConnector(painter2, connector);
		//painter2.addConnector(painter1, connector);
		this.addConnector(connector);
		putInTable(connectable1, connectable2, connector);
		putInTable(connectable2, connectable1, connector);
		//if (canvas != null) {
			//canvas.repaint();
		//}
		return connector;*/
		return hookUsUp(connectable1, connectable2, null);
	}

	/**
	 * Connects two connectables by their <i>center</i> positions with a connector.
	 * @param connectable1 Cannot be null
	 * @param connectable2 Cannot be null
	 * @return
	 */
	public Connector hookUsUp(IConnectable connectable1, IConnectable connectable2, String id) {
		Connector existing = this.getConnector(connectable1, connectable2, id);
		if (existing != null) {
			this.removeConnector(existing);
		}
		Connector connector = new Connector(connectable1, connectable2, id);
		//painter1.addConnector(painter2, connector);
		//painter2.addConnector(painter1, connector);
		this.addConnector(connector);
		putInTable(connectable1, connectable2, connector);
		putInTable(connectable2, connectable1, connector);
		//if (canvas != null) {
			//canvas.repaint();
		//}
		return connector;
	}
	
	private void putInTable(IConnectable target, IConnectable other, Connector connector) {
		/*if (!this.conTable.containsKey(target)) {
			Hashtable<IConnectable, Connector> list = new Hashtable<IConnectable, Connector>();
			list.put(other, connector);
			conTable.put(target, list);
		}
		else {
			this.conTable.get(target).put(other, connector);
		}*/
		if (!this.cTable.containsKey(target)) {
			ArrayList<Connector> list = new ArrayList<Connector>();
			list.add(connector);
			cTable.put(target, list);
		}
		else {
			cTable.get(target).add(connector);
		}
	}
	
  /**
   * Adds a cable to the manager.
   * @param cable Connector to be added.
   * @return
   */
	private synchronized Connector addConnector(Connector cable) {
		if (cable != null) {
  		this.connectors.add(cable);
		}
		return cable;
	}
	
	/**
	 * Gets all connectors in the ConnectorManager.
	 * @return A Map of connectors.
	 */
	public List<Connector> getConnectors() {
		return this.connectors;
	}
	
	/**
	 * Clears all connectors in the ConnectorManager and removes all connectors from all
	 * painters.
	 *
	 */
	public void clear() {
		for (Connector con : connectors.toArray(new Connector[0])) {
			removeConnector(con);
		}
		this.connectors.clear();
		//this.conTable.clear();
		this.cTable.clear();
	}
	
	/**
	 * @param connector
	 * @param canvas
	 */
	public synchronized void removeConnector(Connector connector) {
		if (connector != null) {
			//connector.getComponent1().removeConnector(connector.getComponent2());
			//connector.getComponent2().removeConnector(connector.getComponent1());

			//this.conTable.get(connector.getComponent1()).remove(connector);
			//this.conTable.get(connector.getComponent2()).remove(connector);
			removeFromTable(connector.getComponent1(), connector.getComponent2(), connector);
			removeFromTable(connector.getComponent2(), connector.getComponent1(), connector);
			if (this.connectors.remove(connector) && canvas != null) {
				canvas.repaint();
			}
		}
	}
	
	private void removeFromTable(IConnectable painter, IConnectable other, Connector connector) {
		/*Hashtable<IConnectable, Connector> list = this.conTable.get(painter);
		list.remove(other);
		if (list.isEmpty()) {
			this.conTable.remove(painter);
		}*/
		ArrayList<Connector> list = cTable.get(painter);
		list.remove(connector);
		if (list.isEmpty()) {
			cTable.remove(painter);
		}
	}
	
	/**
	 * Removes all connectors associated with a painter
	 * @param painter
	 */
	public synchronized void removeAll(IConnectable painter) {
		for (Connector connector : getConnectors(painter)) {
			removeConnector(connector);
		}
	}
	public Connector getConnector(IConnectable thisPainter, IConnectable other) {
		/*Hashtable<IConnectable, Connector> list = this.conTable.get(thisPainter);
		if (list != null) {
			return list.get(other);
		}*/
		return getConnector(thisPainter, other, null);
	}

	public synchronized Connector getConnector(IConnectable thisPainter, IConnectable other, String id) {
		/*Hashtable<IConnectable, Connector> list = this.conTable.get(thisPainter);
		if (list != null) {
			return list.get(other);
		}*/
		ArrayList<Connector> list = cTable.get(thisPainter);
		if (list != null) {
			if (id != null) {
				for (Connector connector : list) {
					if (connector.getTheOtherPainter(thisPainter) == other && id.equals(connector.getId())) {
						return connector;
					}
				}
			}
			else {
				for (Connector connector : list) {
					if (connector.getTheOtherPainter(thisPainter) == other && connector.getId() == null) {
						return connector;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets a copy of all connectors in this painter. It returns an array copy, so the actual
	 * storage can be modified by iterating through this array without concurrent modification.
	 * This is used in removeAll() to remove connectors of a IConnectable.
	 * @return the connectors
	 */
	public Connector[] getConnectors(IConnectable connectable) {
		/*Hashtable<IConnectable, Connector> list = this.conTable.get(connectable);
		if (list != null) {
			return list.values().toArray(new Connector[0]);
		}
		return new Connector[0];*/
		ArrayList<Connector> list = cTable.get(connectable);
		if (list != null) {
			return list.toArray(new Connector[0]);
		}
		return new Connector[0];
	}

	/**
	 * Sets the connector's point on thisConnectable relative to its location.
	 * @param thisConnectable
	 * @param other
	 * @param x
	 * @param y
	 */
	public void setConnectorPoint(IConnectable thisConnectable, IConnectable other, int x, int y) {
		this.setConnectorPoint(thisConnectable, other, null, x, y);
	}
	
	/**
	 * Sets the connector's point on thisConnectable relative to its location.
	 * @param thisConnectable
	 * @param other
	 * @param x
	 * @param y
	 */
	public void setConnectorPoint(IConnectable thisConnectable, IConnectable other, String id, int x, int y) {
		Connector connector = getConnector(thisConnectable, other, id);
		if (connector != null) {
			connector.setRelativeLocation(thisConnectable, x, y);
		}
	}
}
