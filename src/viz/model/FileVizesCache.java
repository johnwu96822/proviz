package viz.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages cache vizes parsed for all the opened files. 
 * This class is mainly used by VizAnnotationExplorer. 
 * @author John
 * Created on Mar 6, 2006, 2006
 */
public class FileVizesCache extends VizMapStorage {
	/* A file may have more than one TypeVizes. That is the outer map, which maps a file's 
   * path to its TypeVizes. The inner map allows fast access to TypeVizes based on the
   * name of the TypeViz. <FilePath, <ClassName, TypeViz>> 
   */
  private Map<String, Map<String, TypeViz>> fileToTypeViz = new HashMap<String, Map<String, TypeViz>>();
  
  /**
   * Removes a top level (class/file level) TypeViz from Vizes but does not remove
   * the TypeViz from the file table. removeTypeFromFie() must be called to remove
   * the TypeViz from the file table.
   * @param tv The removed TypeViz; null if 
   * @return The previous TypeViz that is removed, or null tv is not in Vizes.
   */
  public TypeViz removeTypeViz(TypeViz tv) {
  	TypeViz rv = super.removeTypeViz(tv);
  	if (rv != null) {
  		this.removeTypeFromFile(rv);
  	}
  	return rv;
  }
  
  /**
   * Clears everything in Vizes.
   */
  public void clearAll() {
  	super.clearAll();
  	clearFileTable();
  }
  
  /*
  public viz.VizBase viz (String Path) 
      throws IllegalArgumentException {
    try {
      return null;
    } catch (Exception error) {
      throw new IllegalArgumentException (error.getMessage ());
    }
  }
  */

  /**
   * Adds a TypeViz and the path of the file which contains that TypeViz.
   * @param filePath Path of the file that the TypeViz belongs to. It is 
   * composed by IPath.toOSString() method.
   * @param tv The TypeViz to be added.
   */
  public void addTypeToFile(String filePath, TypeViz tv) {
  	if (tv == null) {
  		return;
  	}
  //First time adding this file, so create a new ArrayList.
  	if (!this.fileToTypeViz.containsKey(filePath)) {
  		this.fileToTypeViz.put(filePath, new HashMap<String, TypeViz>());
  	}
  	Map<String, TypeViz> map = this.fileToTypeViz.get(filePath);
  	map.put(tv.getFullName(), tv);
  }
  
  /**
   * Gets the TypeVizes in a file.
   * @param filePath Path of a file, composed by IPath.toOSString() method.
   * @return An array of TypeVizes that are in the file. null if the file
   * hasn't been loaded to Vizes.
   */
  public TypeViz[] getFile(String filePath) {
  	Map<String, TypeViz> map = this.fileToTypeViz.get(filePath);
  	if (map != null) {
  		return map.values().toArray(new TypeViz[0]);
  	}
  	return null;
  }
  
  public String[] getFiles() {
  	return this.fileToTypeViz.keySet().toArray(new String[0]);
  }
  
  /**
   * Removes one file-to-TypeVizes information from Vizes.
   * @param filePath Path of the file to be removed, composed from IPath.toOSString().
   * @return Previously existed association of TypeViz and its name.
   */
  public Map<String, TypeViz> removeFile(String path) {
  	return this.fileToTypeViz.remove(path);
  }
  
  /**
   * Removes a TypeViz from a file. Although it is acceptable that TypeVizes with the
   * same name (ID) to be in different files, Vizes internal structure would allow only
   * one of them to be stored, as the addOrReplace method replaces previously existing
   * TypeViz witht the same name. 
   * @param tv The top-level TypeViz to be removed.
   */
  public void removeTypeFromFile(TypeViz tv) {
  	Set<Map.Entry<String, Map<String, TypeViz>>> set = this.fileToTypeViz.entrySet();
  	for (Map.Entry<String, Map<String, TypeViz>> entry : set) {
  		Map<String, TypeViz> types = entry.getValue();
  		types.remove(tv.getFullName());
  		break;      //Can break here because current implementation would not allow
  		            //more than one TypeVizes with the same ID to exist in Vizes.
  	}
  }
  
  /**
   * Clears only the file-TypeViz(s) association in Vizes.
   */
  public void clearFileTable() {
  	this.fileToTypeViz.clear();
  }
}
