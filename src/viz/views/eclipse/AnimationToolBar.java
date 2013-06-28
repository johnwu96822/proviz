package viz.views.eclipse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.eclipse.core.resources.ResourcesPlugin;

import viz.ProViz;
import viz.VizMonitor;
import viz.VizPlayer;
import viz.animation.AnimationController;

/**
 * Defines the toolbar for controlling ProViz animations.
 * @author JW
 *
 */
public class AnimationToolBar extends JToolBar {
  //private static AnimationToolBar theInstance; 
  private JButton bResume;
  private JButton bStop;
  private JButton bPause;
  private JButton bNext;
  //private JButton bStep;
  
  private JButton sInto;
  private JButton sOver;
  private JButton sReturn;
  
  private AnimationActionListener listener = new AnimationActionListener();
  private AnimationController aController;
  private JButton playerButton;
  private VizPlayer vizPlayer = null;
  public static final String RESUME = "Resume";
  public static final String PAUSE = "Pause";
  public static final String STOP = "Stop";
  //public static final String STEP = "Step";
	public static final String PLAY = "Play";
	public static final String NEXT = "Next";
	
	private String fileToPlay = "recording.viz";
  
  public AnimationToolBar(AnimationController ac) {
  	this.aController = ac;
    this.add(new JLabel("Animation"));
    this.add(aController.getCheckBox());

    this.addSeparator();
    this.addSeparator();
    this.addSeparator();
    this.bResume = new JButton(new ImageIcon("image/Start16.jpg"));
    this.bResume.setActionCommand(RESUME);
    this.bResume.setToolTipText("Start/Resume");
    this.bResume.addActionListener(listener);
    this.add(this.bResume);

    this.bNext = new JButton(new ImageIcon("image/Next16.jpg"));
    this.bNext.setActionCommand(NEXT);
    this.bNext.setToolTipText("Step until there is a graphical change");
    this.bNext.addActionListener(listener);
    this.add(this.bNext);
    
    //this.bStep = new JButton(new ImageIcon("image/Step16.jpg"));
    //this.bStep.setActionCommand(STEP);
    //this.bStep.setToolTipText("Step Forward");
    //this.bStep.addActionListener(listener);
    //this.add(this.bStep);
    
    this.bPause = new JButton(new ImageIcon("image/Pause16.jpg"));
    this.bPause.setActionCommand(PAUSE);
    this.bPause.setToolTipText(PAUSE);
    this.bPause.addActionListener(listener);
    this.add(this.bPause);    
    
    this.bStop = new JButton(new ImageIcon("image/Stop16.jpg"));
    this.bStop.setActionCommand(STOP);
    this.bStop.setToolTipText("Stop Program");
    this.bStop.addActionListener(listener);
    bStop.setEnabled(false);
    this.add(this.bStop);   
    
    this.addSeparator();
    
    this.sInto = new JButton("Step Into");
    this.sInto.addActionListener(listener);
    this.add(this.sInto);   
    
    this.sOver = new JButton("Step Over");
    this.sOver.addActionListener(listener);
    this.add(this.sOver);   

    this.sReturn = new JButton("Step Return");
    this.sReturn.addActionListener(listener);
    this.add(this.sReturn);
    
    this.addSeparator();
    
    this.add(new JLabel("FAST"));

    this.add(aController.getSpeedMeter());
    
    this.add(new JLabel("SLOW"));
    this.addSeparator();
    
    this.playerButton = new JButton("Viz Player");
    playerButton.setActionCommand(PLAY);
    this.playerButton.setToolTipText("Plays a recorded Viz log file");
    this.playerButton.addActionListener(listener);
    this.add(this.playerButton);   
    this.setFloatable(false);
  	this.setRollover(true);
  	this.setStopState();
  }
  
  /**
   * Sets the buttons to the state when Start/Resume button is pressed.
   */
  public void setResumeState() {
    this.bResume.setEnabled(false);
    this.bPause.setEnabled(true);
    this.bStop.setEnabled(true);
    //this.bStep.setEnabled(true);
    this.bNext.setEnabled(true);
    this.sInto.setEnabled(true);
    this.sOver.setEnabled(true);
    this.sReturn.setEnabled(true);
    this.playerButton.setEnabled(false);
  }

  /**
   * Sets the buttons to the state when visualization is stopped.
   */
  public void setStopState() {
    this.bResume.setEnabled(false);
    this.bPause.setEnabled(false);
    this.bStop.setEnabled(false);
    //this.bStep.setEnabled(false);
    this.bNext.setEnabled(false);
    this.sInto.setEnabled(false);
    this.sOver.setEnabled(false);
    this.sReturn.setEnabled(false);
    this.playerButton.setEnabled(true);
  }

  /**
   * Sets the buttons to the state when the visualization is being stepped.
   */
  public void setStepState() {
    this.bResume.setEnabled(true);
    this.bPause.setEnabled(false);
    this.bStop.setEnabled(true);
    //this.bStep.setEnabled(true);
    this.bNext.setEnabled(true);
    this.sInto.setEnabled(true);
    this.sOver.setEnabled(true);
    this.sReturn.setEnabled(true);
    this.playerButton.setEnabled(false);
  }

  /**
   * Sets the buttons to the state when the visualization is in-between steps.
   */
  public void setIntermediateState() {
    this.bResume.setEnabled(false);
    this.bPause.setEnabled(false);
    this.bStop.setEnabled(true);
    //this.bStep.setEnabled(false);
    this.bNext.setEnabled(false);
    this.sInto.setEnabled(false);
    this.sOver.setEnabled(false);
    this.sReturn.setEnabled(false);
    this.playerButton.setEnabled(false);
  }
  

  /**
   * The action listener for buttons in AnimationToolBar.
   * @author JW
   */
  class AnimationActionListener implements ActionListener {
  	@Override
  	public void actionPerformed(ActionEvent e) {
  		ProViz.getVPM().getCanvas().eraseHighlights();
  		if (e.getActionCommand().equals(NEXT)) {
  			//aController.go(false, true);
  			aController.go(AnimationController.State.next);
  		}
  		/*else if (e.getActionCommand().equals(STEP)) {
  			//aController.go(false, false);
  			aController.go(AnimationController.STEPPING);
  		}*/
  		else if (e.getActionCommand().equals(RESUME)) {
  			//aController.go(true, false);
  			ProViz.getInstance().setStatusLine("");
  			aController.go(AnimationController.State.running);
  		}
  		else if (e.getActionCommand().equals(PAUSE)) {
  			//VizMonitor.getInstance().setProceedStep(false);
  			aController.pause();
  		}
  		else if (e.getActionCommand().equals(STOP)) {
  			aController.stop();
  		}
  		else if (e.getActionCommand().equals("Step Into")) {
  			aController.go(AnimationController.State.stepping);
  		}
  		else if (e.getActionCommand().equals("Step Over")) {
  			aController.go(AnimationController.State.stepOver);
  		}
  		else if (e.getActionCommand().equals("Step Return")) {
  			aController.go(AnimationController.State.stepReturn);
  		}
  		else if (e.getActionCommand().equals(PLAY)) {
			//Do not proceed when VizPlayer is in action, which means that the VizController in AnimationController
			//is not this VizMonitor, and that the animation state is not STOP.
				if (ProViz.getAnimationController().getVizController() == VizMonitor.getInstance() &&
						ProViz.getAnimationController().getState() != AnimationController.State.stop) {
					return;
				}
  			try {
  				JFileChooser chooser;
  				try {
  					chooser = new JFileChooser(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
  					//filePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + "/recording.log";
  				}
  				catch (Exception chooserEx) {
  					chooser = new JFileChooser("./");
  					//filePath = "recording.log";
  				}
  				//.getProject(projectName).getLocation().toOSString());
  				chooser.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File f) {
							return f.getName().endsWith(".viz") || f.isDirectory();
						}

						@Override
						public String getDescription() {
							return "ProViz Recordings";
						}
  					
  				});
		  		chooser.setSelectedFile(new File(fileToPlay));
		  		int returnVal = chooser.showOpenDialog(ProViz.getInstance().getFrame());

		  		//String filePath;
		  		//.getProject(projectName).getFile("recording.log").getLocation().toOSString();
		  		if (returnVal == JFileChooser.APPROVE_OPTION) {
		  			fileToPlay = chooser.getSelectedFile().getAbsolutePath();
		  		}
		  		else {
		  			return;
		  		}
		  		ProViz.println(fileToPlay);
  				vizPlayer = new VizPlayer(fileToPlay);//rootPath.toOSString() + "/Visualization/recording.log");
  				vizPlayer.begin();
  			} catch (FileNotFoundException fe) {
  				//ProViz.errprintln(fe);
  				JOptionPane.showMessageDialog(ProViz.getInstance().getFrame(), "File not found!", "ProViz", JOptionPane.ERROR_MESSAGE);
  			} catch (Exception ex) {
  				ProViz.errprintln(ex);
  			}
  		}
  	}
  }

	/**
	 * Enables or disables the Viz Player button.
	 * @param enable true to enable; false to disable.
	 */
	public void enablePlayerButton(boolean enable) {
		this.playerButton.setEnabled(enable);
	}

} //end class
