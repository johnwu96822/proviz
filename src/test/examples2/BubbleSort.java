package test.examples2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import viz.annotation.DViz;
import viz.annotation.Viz;
//import viz.annotation.DViz;

@SuppressWarnings("unused")
/*
@Viz ("lib.ExamplePainter")
class Example {
  @Viz ("lib.StringPainter")
  private String sField;
  private int iField;
  @Viz ("lib.MethodAPainter")
  void methodA(@DViz String sParam) {
    @Viz ("lib.MyExamplePainter")
    Example local1;
    @DViz
    Example local2;
   }
  }
*/
public class BubbleSort extends Sort {	

	
	public BubbleSort(int size) {
		super(size);
	}

	public static void main(String[] args) {
		/*@Viz ("viz.painters.lib.ArrayPainter_Painter")
		String[] s = null;//{"hi", "no"};
		s = new String[]{"oh", "my"};
		ArrayList<String> aList = new ArrayList<String>();
		aList.add(null);*/
		//@Viz ("test.painters.LinkedListPainter_Draw")
		//@Viz ("viz.painters.lib.LinkedListPainter_Painter")
		//@Viz ("viz.painters.java.linkedlist.LinkedListPainter")
		long begin = System.currentTimeMillis();
		@DViz
		LinkedList<String> list = null;
		list = new LinkedList<String>();
		list.add("Test 1");
		list.remove();
		list.add("Test 2");
		list.add("Test 3");
		list.remove();
		list.add("Test 4");
		list.add("Test 5");
		list.add("Test 6");
		list.remove(2);
		list.add(3, "Test");
		list.add(1, "John");
		list.clear();
		list.push("Hi);");
		
		
		@Viz ("test.painters.SortPainter")
		Sort bs = new BubbleSort(10);
		BubbleSort.printArray(bs.getArray());
		bs.sort();
		BubbleSort.printArray(bs.getArray());
		T t = new T(10);
		System.out.println(t.getClass().getSuperclass().getSuperclass());
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
	}

	public void sort() {
		for (@Viz ("test.painters.IndexPainterI") int i = array.length - 1; i > 0; i--) {			
			for (@Viz ("test.painters.IndexPainter") int j = 0; j < i; j++) {
				if (array[j] > array[j + 1]) {
					swap(j, j + 1);
				}
			}
		}
		/*@SuppressWarnings("unused")
		@DViz
		int[] arr = array;
		array = null;
		arr[0] = 100;
		array = arr;
		int k = 0;*/
	}
}
class T extends BubbleSort {

	public T(int size) {
		super(size);
		// TODO Auto-generated constructor stub
	}
	
}