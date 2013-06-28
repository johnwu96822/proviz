package viz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import viz.animation.AnimationController;
import viz.painters.VizPainterManager;
import viz.runtime.VizRuntime;
import viz.views.eclipse.VizSetEditor;

/**
 * The singleton class containing the ProViz application. It can construct a Java frame given
 * by another user interface program. It is designed this way so that it can construct the 
 * frame given by Eclipse's AWTView. A program that wants to use ProViz must have a VizController
 * and set it to the AnimationController when the visualization begins. 
 * @author JW
 */
public class ProViz {
	public static final String INIT_STATUS_TEXT = "Remember to load the annotations into ProViz Editor prior to visualization";
	private static ProViz theInstance = null;
	private Frame frame;
	private JSlider zoomSlider;
	private static JTextArea tArea;
	private static JScrollPane areaScrollPane;
	private VizRuntime vizRuntime;
	private static VizPainterManager vpm;
	private static AnimationController aController;
	private static VizSetEditor vizSetEditor = null;
	private JScrollPane workspacePane;
  private JComboBox<String> stackDisplay;
  private JLabel statusLine;
	
	private ProViz() {
		aController = new AnimationController();
		vpm = new VizPainterManager();
		vizRuntime = new VizRuntime();
	}
	
	/*public VizPlayer initializePlayer(String path) throws FileNotFoundException, IOException {
		return new VizPlayer(path);
	}*/
	
	public static ProViz getInstance() {
		if (theInstance == null) {
			theInstance = new ProViz();
		}
		return theInstance;
	}
	
	public void setPreferredSize(Dimension dim) {
		vpm.getCanvas().setPreferredSize(dim);
		workspacePane.validate();
	}
	
	public void constructFrame(Frame frame) {
		this.frame = frame;
		
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(new File("mytest.txt")));
			ps.println("test");
			ps.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		final int value1x = 20;
		JToolBar mainToolBar = new JToolBar();
		zoomSlider = new JSlider(1, 101, value1x);
		zoomSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				vpm.getCanvas().repaint();
			}
		});
    this.zoomSlider.setMajorTickSpacing(10);
    this.zoomSlider.setPaintTicks(true); 
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		//labelTable.put(new Integer(1), new JLabel("0.1x"));
		labelTable.put(new Integer(1), new JLabel("0.05x"));
		labelTable.put(new Integer(value1x), new JLabel("1x"));
		labelTable.put(new Integer(100), new JLabel("5x"));
		this.zoomSlider.setLabelTable(labelTable);
		this.zoomSlider.setPaintLabels(true);
		
		JButton resetZoom = new JButton("Reset");
		resetZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoomSlider.setValue(value1x);
			}
		});

		mainToolBar.add(zoomSlider);
		mainToolBar.add(resetZoom);
	//TODO remove this
		zoomSlider.setToolTipText("Not working yet!");
		zoomSlider.setEnabled(false);
		resetZoom.setEnabled(false);
		
		JPanel aToolBarPane = new JPanel();
		aToolBarPane.add(aController.getToolBar());
		//aToolBarPane.add(mainToolBar);
		frame.setLayout(new BorderLayout());
		frame.add(aToolBarPane, BorderLayout.NORTH);
		
    vpm.getCanvas().setPreferredSize(new Dimension(2000, 2000));
	  workspacePane = new JScrollPane(vpm.getCanvas());
	  workspacePane.getViewport().setDoubleBuffered(true);
    //workspacePane.setPreferredSize(new Dimension(2000, 2000));
	  //frame.add(workspacePane, BorderLayout.CENTER);
	  
    stackDisplay = new JComboBox<String>();
    stackDisplay.setBackground(Color.WHITE);
    JPanel middlePanel = new JPanel(new BorderLayout());
    middlePanel.add(workspacePane, "Center");
    JPanel methodView = new JPanel(new BorderLayout());
    methodView.add(new JLabel(" Method: "), "West");
    methodView.add(stackDisplay, "Center");
    middlePanel.add(methodView, "North");
	  
    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tArea.setText("");
				statusLine.setText("");
			}
    });
    JPanel messagePanel = new JPanel(new BorderLayout());
    JPanel northPanel = new JPanel(new BorderLayout());
    statusLine = new JLabel(INIT_STATUS_TEXT);
    statusLine.setOpaque(true);
    statusLine.setBackground(Color.BLACK);
    statusLine.setForeground(Color.WHITE);
    northPanel.add(statusLine, "Center");
    northPanel.add(clearButton, "East");
    messagePanel.add(northPanel, "North");
    
    tArea = new JTextArea();
	  tArea.setFont(tArea.getFont().deriveFont(12f));
	  areaScrollPane = new JScrollPane(tArea);
    areaScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    areaScrollPane.setPreferredSize(new Dimension(200, 150));
    messagePanel.add(areaScrollPane, "Center");
    JSplitPane southPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
	  		middlePanel, messagePanel);
    southPane.setOneTouchExpandable(true);
    southPane.setResizeWeight(0.8);
    
    //frame.add(areaScrollPane, BorderLayout.SOUTH);
    frame.add(southPane, BorderLayout.CENTER);
	}
	
	public static void println(String str) {
		System.out.println(str);
		tArea.append(str + "\n");
		areaScrollPane.getVerticalScrollBar().setValue(tArea.getHeight());
	}

	/*public static void print(String str) {
		tArea.append(str);
		areaScrollPane.getVerticalScrollBar().setValue(tArea.getHeight());
	}*/

	public static void errprintln(String msg) {
		System.err.println(msg);
		//tArea.append("<<--------------------------\n");
		tArea.append("######### " + msg + "\n");
		/*TODO Thread access error again
		MessageDialog.openInformation(
				vizSetEditor.getSite().getShell(),//.getTreeViewer().getControl().getShell(),
				"Viz Explorer",
				msg);*/
		//tArea.append("-------------------------->>\n");
		areaScrollPane.getVerticalScrollBar().setValue(tArea.getHeight());
	}
	

	public static void errprintln(Exception e) {
		e.printStackTrace();
		StackTraceElement[] stack = e.getStackTrace();
		tArea.append("<<--------------------------\n");
		tArea.append(e + "\n");
		for (StackTraceElement ele : stack) {
			tArea.append("\t" + ele.toString() + "\n");
		}
		tArea.append("-------------------------->>\n");
		areaScrollPane.getVerticalScrollBar().setValue(tArea.getHeight());
	}
	/**
	 * @return the frame
	 */
	public Frame getFrame() {
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	protected void setFrame(Frame frame) {
		this.frame = frame;
	}

	/**
	 * @return the zoomSlider
	 */
	public double getZoom() {
		return (double) zoomSlider.getValue() / 20;
	}

	/**
	 * @return the vpm
	 */
	public static VizPainterManager getVPM() {
		return vpm;
	}

	/**
	 * @return the vizRuntime
	 */
	public VizRuntime getVizRuntime() {
		return vizRuntime;
	}

	/**
	 * @return the aController
	 */
	public static AnimationController getAnimationController() {
		return aController;
	}
	
	/**
	 * Clears VPM, VizRuntime, and AnimationController
	 */
	public void clearAll() {
		vpm.clearAll();
		vizRuntime.clearAll();
		aController.clear();
		clearStackDisplay();
		setStatusLine(INIT_STATUS_TEXT);
	}
	

	/**
	 * @return the vizSetEditor
	 */
	public VizSetEditor getVizSetEditor() {
		return vizSetEditor;
	}

	/**
	 * @param vizSetEditor the vizSetEditor to set
	 */
	public void setVizSetEditor(VizSetEditor vizSetEditor) {
		ProViz.vizSetEditor = vizSetEditor;
	}
	
	/**
	 * Pushes the given method name to the top of the method stack
	 * display and also displays it
	 * @param methodName
	 */
	public void pushMethodDisplay(String methodName) {
		stackDisplay.insertItemAt(methodName, 0);
		stackDisplay.setSelectedIndex(0);
	}
	
	/**
	 * Pops the top method off the method stack display
	 */
	public void popMethodDisplay() {
		stackDisplay.removeItemAt(0);
	}
	
	/**
	 * Removes all in the method stack display
	 */
	public void clearStackDisplay() {
		stackDisplay.removeAllItems();
	}
	
	/**
	 * Displays the given text to ProViz view's status line
	 * @param text
	 */
	public void setStatusLine(String text) {
		this.statusLine.setText(text);
		this.statusLine.setToolTipText(text);
	}
}
