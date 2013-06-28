package test.examples2;

import viz.annotation.Viz;

public class InsertionSort extends Sort {

	public InsertionSort(int size) {
		super(size);
	}
	
	public static void main(String[] args) {
		@Viz ("test.painters.SortPainter")
		Sort bs = new InsertionSort(15);
		Sort.printArray(bs.getArray());
		bs.sort();
		Sort.printArray(bs.getArray());
	}
	
	@Override
	public void sort() {
		for (@Viz ("test.painters.IndexPainter") int i = 1; i < array.length; i++) {
			for (@Viz ("test.painters.IndexPainter") int j = i; j > 0; j--) {
				if (array[j] < array[j - 1]) {
					swap(j, j - 1);
				}
				else {
					break;
				}
			}
		}
	}

}
