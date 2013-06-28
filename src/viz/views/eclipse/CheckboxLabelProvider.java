package viz.views.eclipse;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import viz.model.FieldViz;
import viz.model.VariableViz;
import viz.runtime.IVizVariable;

public class CheckboxLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText(Object element) {
		String rv = element.toString();
		if (element instanceof VariableViz) {
			rv = element.toString();
		}
		else if (element instanceof FieldViz) {
			rv = IVizVariable.THIS_PREFIX + ((FieldViz) element).getSimpleName();
		}
		return rv;
	}
}
