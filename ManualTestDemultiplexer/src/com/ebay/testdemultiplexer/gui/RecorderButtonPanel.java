/**
 * Copyright 2012-2013 eBay Software Foundation - All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============================================================================
 * 
 * @author Benjamin Yarger <byarger@ebay.com>
 * 
 * Class: RecorderButtonPanel
 * 
 * Description:
 * GUI class for providing the command recorder UI elements. Allows for 
 * recording, playback, saving and loading of command sequences.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;

public class RecorderButtonPanel extends JPanel 
	implements ItemListener, ListSelectionListener, ActionListener {

	/** Reference to the active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Set the dimensions of every button in the panel. */
	private Dimension buttonDimensions = new Dimension(60, 60);
	
	/** Toggles the recorder on and off. */
	private JToggleButton recordButton;
	
	/** Plays the next command. */
	private JButton playButton;
	
	/** Reset the play index to 0. */
	private JButton stopButton;
	
	/** Open the save dialog and then write the command list to file. */
	private JButton saveButton;
	
	/** Load a saved command list from file. */
	private JButton loadButton;
	
	/** Clear the command list. */
	private JButton clearButton;
	
	/** The CommandList JList component with optional JScrollPane. */
	private CommandList commandList;
	
	/**
	 * Creates an instance of the RecorderPanel with a reference to the 
	 * active TestDeviceManager.
	 * 
	 * @param manager Active TestDeviceManager reference.
	 */
	public RecorderButtonPanel(TestDeviceManager manager) {
		
		this.manager = manager;
		
		initialize();
	}
	
	// -------------------------------------------------------------------------
	// Required by ListSelectionListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	// -------------------------------------------------------------------------
	// Required by ActionListener
	// -------------------------------------------------------------------------
		
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		 if (e.getSource() == clearButton) {
			
			manager.getCommandRecorder().clearRecorder();
			
		} else if (e.getSource() == stopButton) {
			
			manager.rewindCommandPlayback();
			
		} else if (e.getSource() == saveButton) {
			
			KeyboardInputManager.interrupt();
			
			int retVal;

		    JFileChooser fc = new JFileChooser();
		    FileNameExtensionFilter fileFilter = 
		    		new FileNameExtensionFilter("Test Record File", "trf");


		    fc.addChoosableFileFilter(fileFilter);
		    retVal = fc.showSaveDialog(this);

		    if (retVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = fc.getSelectedFile().getAbsolutePath();
		    	
		    	if (!filePath.endsWith(".trf")) {
		    		filePath = filePath + ".trf";
		    	}
		    	
		    	manager.getCommandRecorder().writeCommandsToFile(filePath);
		    }
		    
		    KeyboardInputManager.resume();
			
		} else if (e.getSource() == loadButton) {
			
			KeyboardInputManager.interrupt();
			
			manager.getCommandRecorder().clearRecorder();
			
			int retVal;

		    JFileChooser fc = new JFileChooser();
		    FileNameExtensionFilter fileFilter = 
		    		new FileNameExtensionFilter("Test Record File", "trf");

		    fc.addChoosableFileFilter(fileFilter);
		    retVal = fc.showOpenDialog(this);

		    if (retVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = fc.getSelectedFile().getAbsolutePath();
		    	manager.getCommandRecorder().loadCommandsFromFile(filePath);
		    }
			
		    KeyboardInputManager.resume();
		    
		}  else if (e.getSource() == playButton) {
			
			// If selected, turn off the recorder (status and via manager)
			recordButton.setSelected(false);
			manager.getCommandRecorder().stopRecorder();
			manager.playCommands();
			
		}
	}
	
	// -------------------------------------------------------------------------
	// Required by ItemListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getItem() == recordButton) {
			
			if (e.getStateChange() == ItemEvent.SELECTED) {
				manager.getCommandRecorder().startRecorder();
				((JToggleButton)e.getItem()).setToolTipText(
						"Command Recorder Running");
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				manager.getCommandRecorder().stopRecorder();
				((JToggleButton)e.getItem()).setToolTipText(
						"Command Recorder Stopped");
			}
			
		}
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Setup the UI.
	 */
	private void initialize() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// Add the record button label.
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		
		JLabel recordButtonLabel = new JLabel();
		recordButtonLabel.setText("Command Recorder Buttons");
		labelPanel.add(recordButtonLabel);
		this.add(labelPanel);
		
		// Add the button panel.
		JPanel buttonPanelA = new JPanel();
		buttonPanelA.setLayout(new BoxLayout(buttonPanelA, BoxLayout.X_AXIS));
		
		// Add the record button
		recordButton = new JToggleButton();
		Icon recordOnIcon = new ImageIcon(getClass().getResource(
				"/graphics/indicator_code_lock_point_area_red.png"));
		Icon recordOffIcon = new ImageIcon(getClass().getResource(
				"/graphics/indicator_code_lock_point_area_red_holo.png"));
		recordButton.setIcon(recordOffIcon);
		recordButton.setDisabledIcon(recordOffIcon);
		recordButton.setPressedIcon(recordOnIcon);
		recordButton.setSelectedIcon(recordOnIcon);
		recordButton.setSize(buttonDimensions);
		recordButton.setMinimumSize(buttonDimensions);
		recordButton.setPreferredSize(buttonDimensions);
		recordButton.setMaximumSize(buttonDimensions);
		recordButton.setToolTipText("Record Commands");
		recordButton.setSelected(true);
		recordButton.addItemListener(this);
		buttonPanelA.add(recordButton);
		
		// Add the play button
		playButton = new JButton();
		Icon playIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_media_play.png"));
		playButton.setIcon(playIcon);
		playButton.setSize(buttonDimensions);
		playButton.setMinimumSize(buttonDimensions);
		playButton.setPreferredSize(buttonDimensions);
		playButton.setMaximumSize(buttonDimensions);
		playButton.setToolTipText("Play Next Command");
		playButton.addActionListener(this);
		buttonPanelA.add(playButton);
		
		// Add the stop button
		stopButton = new JButton();
		Icon stopIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_media_stop.png"));
		stopButton.setIcon(stopIcon);
		stopButton.setSize(buttonDimensions);
		stopButton.setMinimumSize(buttonDimensions);
		stopButton.setPreferredSize(buttonDimensions);
		stopButton.setMaximumSize(buttonDimensions);
		stopButton.setToolTipText("Stop Playback");
		stopButton.addActionListener(this);
		buttonPanelA.add(stopButton);	
		
		// Add the clear playlist button
		clearButton = new JButton();
		Icon clearIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_clear_playlist.png"));
		clearButton.setIcon(clearIcon);
		clearButton.setSize(buttonDimensions);
		clearButton.setMinimumSize(buttonDimensions);
		clearButton.setPreferredSize(buttonDimensions);
		clearButton.setMaximumSize(buttonDimensions);
		clearButton.setToolTipText("Clear the Command List");
		clearButton.addActionListener(this);
		buttonPanelA.add(clearButton);
		
		// Add the save button
		saveButton = new JButton();
		Icon saveIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_save.png"));
		saveButton.setIcon(saveIcon);
		saveButton.setSize(buttonDimensions);
		saveButton.setMinimumSize(buttonDimensions);
		saveButton.setPreferredSize(buttonDimensions);
		saveButton.setMaximumSize(buttonDimensions);
		saveButton.setToolTipText("Save the Command List");
		saveButton.addActionListener(this);
		buttonPanelA.add(saveButton);
		
		// Add the load button
		loadButton = new JButton();
		Icon loadIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_archive.png"));
		loadButton.setIcon(loadIcon);
		loadButton.setSize(buttonDimensions);
		loadButton.setMinimumSize(buttonDimensions);
		loadButton.setPreferredSize(buttonDimensions);
		loadButton.setMaximumSize(buttonDimensions);
		loadButton.setToolTipText("Load Command List");
		loadButton.addActionListener(this);
		buttonPanelA.add(loadButton);
		
		this.add(buttonPanelA);
		
		commandList = new CommandList(manager);
		
		this.add(commandList.getScrollPane());
	}
}
