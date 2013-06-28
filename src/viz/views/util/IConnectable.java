package viz.views.util;

import java.awt.Point;

public interface IConnectable {
	//connectors is never null
	//private Map<IConnectable, Connector> connectors = new Hashtable<IConnectable, Connector>();
	
	public abstract int getWidth();
	public abstract int getHeight();
	
	public abstract Point getLocation();
	
	///////////////////////// Connectors ///////////////////////////////
	/*
	public void addConnector(IConnectable otherPainter, Connector line) {
		if (line != null && otherPainter != null) {
			this.connectors.put(otherPainter, line);
		}
	}
	
	public Connector removeConnector(IConnectable otherPainter) {
		return this.connectors.remove(otherPainter);
	}
	
	public boolean hasConnector() {
		return !this.connectors.isEmpty();
	}
	
	public Connector getConnector(IConnectable otherPainter) {
		return this.connectors.get(otherPainter);
	}

	/**
	 * Gets a copy of all connectors in this painter. Since it returns an array copy,
	 * the array can be used to remove connectors one by one without concurrent modification.
	 * @return the connectors
	 *
	public Connector[] getConnectors() {
		return connectors.values().toArray(new Connector[0]);
	}

	public void setConnectorPoint(IConnectable otherPainter, int x, int y) {
		Connector connector = getConnector(otherPainter);
		connector.setRelativeLocation(this, x, y);
	}*/
}
