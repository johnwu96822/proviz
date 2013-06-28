package viz.painters.graphics;

import java.awt.Color;

public class ColorIterator {
	private static Color[] colorTable;
	{
		colorTable = new Color[10];
		colorTable[0] = Color.red.brighter();
		colorTable[1] = Color.orange;
		colorTable[2] = Color.yellow.brighter();
		colorTable[3] = Color.green.brighter();
		colorTable[4] = Color.cyan.brighter();
		colorTable[5] = Color.blue.brighter();
		colorTable[6] = Color.magenta.brighter();
		colorTable[7] = Color.darkGray.brighter();
		colorTable[8] = Color.pink.brighter();
		colorTable[9] = Color.gray.brighter();
	}
	private int index;
	
	public ColorIterator() {
		index = (int) (Math.random() * 10);
	}
	
	public ColorIterator(int index) {
		this.index = index;
	}
	
	public Color next() {
		Color color = colorTable[index++];
		index %= 10;
		return color;
	}
	
	public Color get(int index) {
		return colorTable[index % 10];
	}
}
