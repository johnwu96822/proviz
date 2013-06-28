package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import viz.VizPlayer;
import viz.annotation.DViz;

public class Test implements Serializable {
	public String s1 = "ki";
	public transient String s2 = "not saved";
	public Test kk;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		try {
			int i = 0;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("recording.log")));
			while (true) {
				System.out.println(ois.readObject());
				i = ois.readInt();
				System.out.println(i);
				if (i == 0) {
					System.out.println(ois.readObject());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		@DViz
		ArrayList<String> al = new ArrayList<String>();
		al.add("1");
		al.add("2");
		al.add("3");
		al.add("4");
		al.add("5");
		al.add("6");
		al.add("7");
		al.add("8");
		al.add("9");
		al.add("10");
		al.add("11");
		al.add("12");
		al.add(3, "13");
		String tet = "Testing ehllwo\n\nkei";
		ObjectOutputStream oos = null;
		Test test1 = new Test();
		test1.s1 = "oh my";
		Test test = new Test();
		test.kk = test1;
		test1.kk = test;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File("testing.txt")));
			//oos.writeObject(al);
			//oos.writeObject(tet);
			oos.writeObject(test);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(new File("testing.txt")));
			//System.out.println(ois.readObject());
			//System.out.println(ois.readObject());
			test = (Test) ois.readObject();
			ois.close();
			System.out.println(test.s1);
			test.s2 = "aaaaaaaaa";
			System.out.println(test.s2);
			System.out.println(test.kk.s1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		@DViz
		//HashMap<String, String> hMap = new HashMap<String, String>();
		Hashtable<String, String> hMap = new Hashtable<String, String>();
		hMap.put("1", "a");
		hMap.put("2", "b");
		hMap.put("3", "c");
		hMap.put("4", "d");
		hMap.put("5", "e");
		hMap.put("6", "f");
		hMap.put("7", "g");
		hMap.put("8", "h");
		hMap.put("9", "i");
		hMap.put("10", "j");
		hMap.put("11", "k");
		hMap.put("12", "l");
		hMap.put("13", "m");
		hMap.put("14", "n");
		hMap.put("15", "o");
		hMap = null;
		@DViz
		String i = null;
		i = "HIELI";
		@DViz
		//HashMap<String, String> hTable = new HashMap<String, String>();
		TreeMap<String, String> hTable = new TreeMap<String, String>();
		//Hashtable<String, String> hTable = new Hashtable<String, String>();
		hTable.put("1", "a");
		hTable.put("2", "b");
		hTable.put("3", "c");
		hTable.put("4", "d");
		hTable.put("5", "e");
		hTable.put("6", "f");
		hTable.put("7", "g");
		hTable.put("8", "h");
		hTable.put("9", "i");
		hTable.put("10", "j");
		hTable.put("11", "k");
		hTable.put("12", "l");
		hTable.put("13", "m");
		hTable.put("14", "n");
		hTable.put("15", "o");
		hTable.put("1", "m");
		hTable.put("1", "n");
		hTable.remove("12");*/
	}

}
