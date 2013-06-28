package viz.painters.java.hashtable;

import viz.ProViz;
import viz.painters.Painter;
import viz.painters.java.array.ArrayPainter;
import viz.runtime.Change;
import viz.runtime.IVizVariable;
import viz.views.VizCanvas;

/**
 * Uses ArrayPainter to show the actual data structure used in Hashtable
 * @author JW
 *
 */
public class InternalHashtablePainter extends HashtablePainter {

	public InternalHashtablePainter(IVizVariable vvar, VizCanvas canvas) {
		super(vvar, canvas);
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	protected void addToCanvas_userImp() {
		if (!this.getVariable().isNull()) {
			Painter table = this.getFieldPainter("table");
			table.setLocation(this.getLocation().x, this.getLocation().y);
			if (!(table instanceof viz.painters.java.array.ArrayPainter)) {
				table = ProViz.getVPM().switchOver(table, "viz.painters.java.array.ArrayPainter", true, false, false);
				if (table instanceof ArrayPainter) {
					((ArrayPainter) table).setVertical(true);
				}
			}
			table.addToCanvas();
		}
		/*int arraySize = dummyTable.getFieldPainters().size();
		int count = Integer.parseInt(this.getVariable().getField("count").getValueAsString());
		int vPos = 0;
		int x = this.getLocation().x;
		int y = this.getLocation().y;
		for (int i = 0; i < arraySize; i++) {
			Painter entryPainter = dummyTable.getFieldPainter("[" + i + "]");
			if (!entryPainter.getVariable().isNull()) {
//Position this painter and add it to canvas; any element chained after this will be
//added to canvas by the HEntryPainter
				entryPainter.setLocation(x, y + vPos);
				vPos += entryPainter.getHeight();
//Position chained elements
				Painter nextPainter = entryPainter.getFieldPainter("next");
				while (!nextPainter.getVariable().isNull()) {
					nextPainter.setLocation(x, y + vPos);
					System.out.println("Adding next: " + nextPainter.getVariable().getField("key").getValueAsString());
					vPos += nextPainter.getHeight();
					count--;
					nextPainter = nextPainter.getFieldPainter("next");
				}
				entryPainter.addToCanvas();
				count--;
			}
			if (count < 1) {
				break;
			}
		}*/
  //10.05.23 - Changed to non-ArrayPainter implementation
		/*
		IVizVariable var = this.getVariable();
		if (!this.getVariable().isNull()) {
	//table is a Hashtable&Entry<K,V> array
			IVizVariable table = var.getField("table");
			this.addEventGenerator(table);
			table.addListener(this);
			List<IVizVariable> fields = table.getFields();
			//this.arrayLength = fields.size();
			for (IVizVariable field : fields) {
	//Each field is table[i]
				this.addEventGenerator(field);
				field.addListener(this);
				if (!field.isNull()) {
	//The variable contains a Hashtable&Entry<K,V>
					IVizVariable key = field.getField("key");
					IVizVariable value = field.getField("value");
					Painter pKey = VizPainterManager.getInstance().createFieldPainter(Vizes.DEFAULT_TYPE, key, this);
					Painter pValue = VizPainterManager.getInstance().createFieldPainter(Vizes.DEFAULT_TYPE, value, this);
					this.keys.add(pKey);
					this.values.add(pValue);
					IVizVariable next = field.getField("next");
					while (!next.isNull()) {
						key = field.getField("key");
						value = field.getField("value");
						pKey = VizPainterManager.getInstance().createFieldPainter(Vizes.DEFAULT_TYPE, key, this);
						pValue = VizPainterManager.getInstance().createFieldPainter(Vizes.DEFAULT_TYPE, value, this);
						this.keys.add(pKey);
						this.values.add(pValue);
						next = field.getField("next");
					}
				}
			}
			this.setLocation(getCanvas().getVisibleRect().width / 2 - this.getWidth() / 2, 
					getCanvas().getVisibleRect().height / 2 - this.getHeight() / 2);
		}
		paint();
		for (Painter key : keys) {
			key.addToCanvas();
		}
		for (Painter value : values) {
			value.addToCanvas();
		}*/
	}

	@Override
	protected void destroy_userImp() {
	}

	@Override
	public void handleChange(Change change, IVizVariable source) {
		this.addToCanvas();
	}

	@Override
	protected void paint_userImp() {
		this.getFieldPainter("table").paint();
	}
}
