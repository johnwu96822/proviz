package viz.runtime;

import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
public class EventListener implements IJDIEventListener {

	@Override
	public boolean handleEvent(Event event, JDIDebugTarget target,
			boolean suspendVote, EventSet eventSet) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eventSetComplete(Event event, JDIDebugTarget target,
			boolean suspend, EventSet eventSet) {
		// TODO Auto-generated method stub
		
	}

}
