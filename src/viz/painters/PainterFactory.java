package viz.painters;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.TreeMap;

import viz.ProViz;
import viz.IVizController;
import viz.model.Association;
import viz.model.TypeViz;
import viz.model.VariableVizBase;
import viz.model.VizMapModel;
import viz.painters.method.MethodPainter;
import viz.runtime.IVizVariable;
import viz.runtime.VizStackFrame;
import viz.views.VizCanvas;

public class PainterFactory {
	public static final String ARRAY_KEYWORD = "[]";
	private static TreeMap<String, Class<?>> classList = new TreeMap<String, Class<?>>();

	public static void clear() {
		classList.clear();
	}
	/**
	 * @param asso
	 * @param var
	 * @param canvas
	 * @return null if painter creation is unsuccessful
	 */
	public static Painter createPainter(Association asso, IVizVariable var, VizCanvas canvas, Point startingLoc) {
		String vizName = VizMapModel.DEFAULT_TYPE;
		Point startLoc = null;
		if (asso != null && asso.getVizCount() > 0) {
			vizName = asso.getCurrentViz().getPainterName();
	//Added 10.09.14
			startLoc = asso.getCurrentViz().getStartingLocation();
		}
	//Checks what listeners on fields the default TypeViz wants to register
		List<String> dependingVars = null;
	//////////////////////Handle @DViz///////////////////////////////
		if (vizName.equalsIgnoreCase(VizMapModel.DEFAULT_TYPE)) {
	//Gets the type of the variable for TypeViz lookup
			String actualType;
	//If var is null, use var's declared type; otherwise use its actual tyype
			if (var.isNull()) {
				actualType = var.getType();
		//If var is an element of an array, find out the element's type, which is var's parent's
		//declared type without '[]'
				if (var.getParent() != null) {
					String parentActualType = var.getParent().getActualType();
					if (parentActualType.indexOf(ARRAY_KEYWORD) != -1) {
						actualType = parentActualType.substring(0, parentActualType.indexOf('['));
					}
				}
			}
			else {
				actualType = var.getActualType();
			}
	//Find out the painter name which should be used to visualize var
			TypeViz typeViz = VizMapModel.getInstance().findTypeViz_runtime(actualType);
			if (typeViz != null && typeViz.getVizCount() > 0) {
				vizName = typeViz.getCurrentViz().getPainterName();
	//Added 10.09.14
				if (startLoc == null) {
					startLoc = typeViz.getCurrentViz().getStartingLocation();
				}
				dependingVars = typeViz.getDependingVars();
			}
			else {
				if (actualType.indexOf(ARRAY_KEYWORD) != -1) {
					vizName = "viz.painters.java.array.ArrayPainter";
				}
				else {
					if (var.isPrimitive()) {
						vizName = "viz.painters.lib.PrimitivePainter";
					}
					else {
						vizName = VizMapModel.getInstance().getLibraryViz(actualType);
					}
				}
			}
		}
	//Create the painter
		Painter painter = null;
		if (vizName != null) {
			painter = reflectionCreatePainter(vizName, var, canvas);
			if (painter == null) {
				return null;
			}
			if (startLoc != null) {
				ProViz.println("PainterFactory: setting painter starting location");
				//painter.setLocation(startLoc.x, startLoc.y);
				startingLoc.x = startLoc.x;
				startingLoc.y = startLoc.y;
			}
			//parent.setParent(parent);
			if (dependingVars != null) {
				for (String dVar : dependingVars) {
//Prefix a '*' to indicate these are fields to register listener to
					//ProViz.println("*" + dVar);
					painter.addDependentVar("*" + dVar);
				}
			}
			if (asso != null) {
				for (String dVar : asso.getDependingVars()) {
					painter.addDependentVar(dVar);
				}
			}
		}
		return painter;
	}

	/**
	 * @param var
	 * @param canvas
	 * @param vizName
	 * @param painter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Painter reflectionCreatePainter(String vizName, IVizVariable var, VizCanvas canvas) {
		Painter painter = null;
		Constructor con = null;
		try {
			con = Class.forName(vizName).getConstructors()[0];
			painter = (Painter) con.newInstance(var, canvas);
		} catch (ClassNotFoundException e1) {
			if (classList.containsKey(vizName)) {
				try {
					con = classList.get(vizName).getConstructors()[0];
					painter = (Painter) con.newInstance(var, canvas);
					return painter;
				} catch (Exception ex) {
					ProViz.errprintln(ex);
				}
			}
			IVizController controller = ProViz.getAnimationController().getVizController();
			Class clas = controller.loadClass(vizName);
			if (clas != null) {
				classList.put(vizName, clas);
				con = clas.getConstructors()[0];
				if (con != null) {
					try {
						painter = (Painter) con.newInstance(var, canvas);
					} catch (Exception ex) {
						ProViz.errprintln(ex);
					}
				}
			}
		} catch (Exception e3) {
			ProViz.errprintln(e3);
		}
		return painter;
	}
	
	/**
	 * Gets the exact name of the viz class to be created as a painter by extracting
	 * from a VariableViz object.
	 * @param vvb
	 * @param var
	 * @return
	 */
	protected static String getVisualizationName(VariableVizBase vvb, IVizVariable var) {
		String vizName = vvb.getCurrentViz().getPainterName();
		return getVisualizationName(vizName, var);
	}
	
	/**
	 * Checks if the viz class name is null or Vizes.DEFAULT_TYPE. If so, returns the
	 * default viz class string; otherwise return the original string in the parameter.
	 * @param vizName If null or is Vizes.DEFAULT_TYPE, the default viz class string
	 * will be returned.
	 * @param var The variable we are creating a painter for.
	 * @return A viz class string that will be the type of the painter.
	 */
	protected static String getVisualizationName(String vizName, IVizVariable var) {
		if (vizName == null || vizName.equalsIgnoreCase(VizMapModel.DEFAULT_TYPE)) {
			String actualType = var.getActualType();
			TypeViz typeViz = VizMapModel.getInstance().findTypeViz_runtime(actualType);
			if (typeViz != null && typeViz.getVizCount() > 0) {
				vizName = typeViz.getCurrentViz().getPainterName();
			}
			else {
				vizName = VizMapModel.getInstance().getLibraryViz(actualType);
			}
		}
		return vizName;
	}
	
	/**
	 * Creates a custom painter based on the name of the painter type, vizName. If vizName is 
	 * null or "1D" (the default Viz), the painter type will be the annotation of the variable's 
	 * actual class, or the library type if the actual class is not in the Viz model.
	 * @param vizName
	 * @param var
	 * @param canvas
	 * @return
	 */
	public static Painter createCustomPainter(String vizName, IVizVariable var, VizCanvas canvas) {
		Painter painter = null;
		vizName = getVisualizationName(vizName, var);
		if (vizName != null) {
			painter = reflectionCreatePainter(vizName, var, canvas);
		}
		return painter;
	}

	/**
	 * Creates a method painter.
	 * @param methodVCName Name of the method painter.
	 * @param stackFrame The stack frame representing the method.
	 * @param canvas The canvas this method painter should paint on.
	 * @return The newly created method painter; null if failed to create one.
	 */
	@SuppressWarnings("unchecked")
	public static MethodPainter createMethodPainter(String methodVCName, VizStackFrame stackFrame, VizCanvas canvas) {
		MethodPainter painter = null;
		Constructor con = null;
		try {
			con = Class.forName(methodVCName).getConstructors()[0];
			painter = (MethodPainter) con.newInstance(stackFrame, canvas);
		} catch (ClassNotFoundException e1) {
			if (classList.containsKey(methodVCName)) {
				try {
					con = classList.get(methodVCName).getConstructors()[0];
					painter = (MethodPainter) con.newInstance(stackFrame, canvas);
					return painter;
				} catch (Exception ex) {
					ProViz.errprintln(ex);
				}
			}
			IVizController controller = ProViz.getAnimationController().getVizController();
			Class clas = controller.loadClass(methodVCName);
			if (clas != null) {
				classList.put(methodVCName, clas);
				con = clas.getConstructors()[0];
				if (con != null) {
					try {
						painter = (MethodPainter) con.newInstance(stackFrame, canvas);
					} catch (Exception ex) {
						ProViz.println(methodVCName);
						ProViz.errprintln(ex);
					}
				}
			}
		} catch (Exception e3) {
			ProViz.errprintln(e3);
		}
		return painter;
	}
}
