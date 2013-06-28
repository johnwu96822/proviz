package test.examples2;

import java.util.Random;

import viz.annotation.Viz;
import viz.annotation.DViz;
@Viz ("test.painters.SortPainter")
public abstract class Sort {
	//@Viz ("viz.painters.java.array.ArrayPainter")
	@DViz
	protected int[] array;

	public abstract void sort();
	
	public Sort(int size) {
		this.array = new int[Math.abs(size)];
		for (int i = 0; i < array.length; i++) {
			array[i] = i + 1;
		}
		randomize(this.array);
	}
	
	public void randomize(/*@Viz ("viz.painters.lib.ArrayPainter_Draw, viz.painters.lib.ArrayPainter_Comp")*/ int[] array) {
		Random random = new Random();
		int rand;
		for (int i = array.length - 1; i >= 0; i--) {
			rand = random.nextInt(i + 1);
			swap(i, rand);
		}
	}
	
	@Viz ("test.painters.method.SwapMethod")
	public void swap(int i, int j) {
		/*//@DViz
		int[] test = array;
		int temp = test[i];
		test[i] = test[j];
		array[j] = temp;*/
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;	}

	public int[] getArray() {
		return this.array;
	}
	
	public static void printArray(int[] array) {
		for (int i : array) {
			System.out.print(i + " ");
		}
		System.out.println();
	}
}
