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
 * Class: LoginFilePanel
 * 
 * Description:
 * GUI class for providing UI login file elements. The login file reads a key
 * value pair file of inputs that can be used for things like unique logins
 * across all devices or unique search strings across all devices. The key 
 * concept here is that it makes it possible to read key value pairs from
 * file and apply each pair uniquely to all devices.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.commands.TypeCommand;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class LoginFilePanel extends JPanel implements ActionListener {
	
	/** GUI header label. */
	private static final String DEFAULT_LABEL_TEXT = "Login File Buttons";
	
	/** Error message that no login details were found. */
	private static final String EMPTY_RECORDS_MSG = 
			"No login details exist. Please load a valid login file.";
	
	/** Error title that no login details were found. */
	private static final String EMPTY_RECORDS_TITLE =
			"No Login Details";
	
	/** Error message related to reading the input file. */
	private static final String INPUT_ERROR_MSG = 
			"A problem occurred reading the login file. Please make sure " +
			"that the file exists and has appropriate permissions.";
	
	/** Error title related to reading the input file. */
	private static final String INPUT_ERROR_TITLE = "Error Reading Login File";
	
	/** 
	 * Error message that the login file does not contain enough key value
	 * pairs for the number of devices attached.
	 */
	private static final String LOGIN_LIMIT_MSG = 
			"The login file does not contain enough logins for all of the " +
			"attached devices. Proceeding with the logins we have. Please " +
			"manually enter logins for the remaining devices.";
	
	/**
	 * Error title that the login file does not contain enough key value
	 * pairs for the number of devices attached.
	 */
	private static final String LOGIN_LIMIT_TITLE = "Not Enough Logins";
	
	/** Reference to the active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Label for the panel. */
	private JLabel label;
	
	/** Load the login file button. */
	private JButton loadLoginFileButton;
	
	/** Apply the usernames. */
	private JButton applyUsernamesButton;
	
	/** Apply the passwords. */
	private JButton applyPasswordsButton;
	
	/** List of usernames in same order as passwords. */
	private ArrayList<String> usernames;
	
	/** List of passwords in same order as usernames. */
	private ArrayList<String> passwords;
	
	/**
	 * Create a new LoginFilePanel with a reference to the active 
	 * TestDeviceManager.
	 * @param manager Active TestDeviceManager reference.
	 */
	public LoginFilePanel(TestDeviceManager manager) {
		this.manager = manager;
		usernames = new ArrayList<String>();
		passwords = new ArrayList<String>();
		
		initialize();
	}

	// -------------------------------------------------------------------------
	// Methods requried by ActionListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == loadLoginFileButton) {
			
			KeyboardInputManager.interrupt();
			
			manager.getCommandRecorder().clearRecorder();
			
			int retVal;

		    JFileChooser fc = new JFileChooser();
		    FileNameExtensionFilter fileFilter = 
		    		new FileNameExtensionFilter("Text File", "txt");

		    fc.addChoosableFileFilter(fileFilter);
		    retVal = fc.showOpenDialog(this);

		    if (retVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = fc.getSelectedFile().getAbsolutePath();
		    	loadLoginsFromFile(filePath);
		    }
			
		    KeyboardInputManager.resume();
			
		} else if (e.getSource() == applyUsernamesButton) {
			
			if (usernames.size() == 0) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				EMPTY_RECORDS_MSG, 
	    				EMPTY_RECORDS_TITLE, 
	    				JOptionPane.WARNING_MESSAGE);
				
				return;
			}
			
			enterUsernames();
			
		} else if (e.getSource() == applyPasswordsButton) {
			
			if (passwords.size() == 0) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				EMPTY_RECORDS_MSG, 
	    				EMPTY_RECORDS_TITLE, 
	    				JOptionPane.WARNING_MESSAGE);
				
				return;
			}
			
			enterPasswords();
		}
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Layout the panel.
	 */
	private void initialize() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// Add the label
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		
		label = new JLabel(DEFAULT_LABEL_TEXT);
		labelPanel.add(label);
		
		this.add(labelPanel);
		
		// Add the buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		loadLoginFileButton = new JButton();
		loadLoginFileButton.setText("Load File");
		loadLoginFileButton.addActionListener(this);
		buttonPanel.add(loadLoginFileButton);
		
		applyUsernamesButton = new JButton();
		applyUsernamesButton.setText("Apply Username");
		applyUsernamesButton.addActionListener(this);
		buttonPanel.add(applyUsernamesButton);
		
		applyPasswordsButton = new JButton();
		applyPasswordsButton.setText("Apply Password");
		applyPasswordsButton.addActionListener(this);
		buttonPanel.add(applyPasswordsButton);
		
		this.add(buttonPanel);
	}
	
	/**
	 * Load the login information from file.
	 * @param filePath File path to read in.
	 * @return True if data loaded successfully, false otherwise.
	 */
	private boolean loadLoginsFromFile(String filePath) {
		
		label.setText(DEFAULT_LABEL_TEXT);
		
		usernames.clear();
		passwords.clear();
		
		File fileObj = new File(filePath);
		
		FileReader file;
		BufferedReader reader = null;
		
		// Open the file for reading.
		try {
			file = new FileReader(filePath);
			reader = new BufferedReader(file);
		} catch (FileNotFoundException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				INPUT_ERROR_MSG, 
    				INPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
			e.printStackTrace();
			
			return false;
		}
		
		// Begin parsing the commands out of the file.		
		String dataToken = "";
		
		while (dataToken != null) {
			
			// Read the next line.
			try {
				dataToken = reader.readLine();
			} catch (IOException e) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				INPUT_ERROR_MSG, 
	    				INPUT_ERROR_TITLE, 
	    				JOptionPane.ERROR_MESSAGE);
	    		
				e.printStackTrace();
				
				try {
					reader.close();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
				
				return false;
			}
			
			// Because we are going in without reading the first line in the
			// file we need to check the case when nothing is there.
			if (dataToken == null) {
				break;
			}
			
			String[] tokens = 
					dataToken.split(
							TestDemultiplexerConstants.SERIAL_SEPARATOR);
			
			if (tokens.length != 2) {
				
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return false;
			}
			
			usernames.add(tokens[0]);
			passwords.add(tokens[1]);

		}
		
		label.setText(DEFAULT_LABEL_TEXT + " : " + fileObj.getName());
		
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	
	/**
	 * Enter usernames, each device will get a unique username from the 
	 * login file.
	 */
	private void enterUsernames() {
		
		int numDevices = manager.getTotalDeviceCount();
		TestDevice device = null;
		
		// Catch case where there aren't enough logins.
		if (numDevices > usernames.size()) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				LOGIN_LIMIT_MSG, 
    				LOGIN_LIMIT_TITLE, 
    				JOptionPane.WARNING_MESSAGE);
			
			numDevices = usernames.size();
		}
		
		for (int i = (numDevices-1); i >= 0; i--) {
			
			device = manager.getDeviceAt(i);
			
			if (!device.getReceivingInput()) {
				continue;
			}
			
			// We do not store these commands because each one is unique per
			// device. Create the type command and immediately execute on the
			// device.
			TypeCommand typeCommand = new TypeCommand(usernames.get(i));
			typeCommand.executeCommand(device);
		}
	}
	
	/**
	 * Enter passwords, each device will get a unique password from the login
	 * file.
	 */
	private void enterPasswords() {
		
		int numDevices = manager.getTotalDeviceCount();
		TestDevice device = null;
		
		// Catch case where there aren't enough logins.
		if (numDevices > passwords.size()) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				LOGIN_LIMIT_MSG, 
    				LOGIN_LIMIT_TITLE, 
    				JOptionPane.WARNING_MESSAGE);
    		
			numDevices = passwords.size();
		}
		
		for (int i = (numDevices-1); i >= 0; i--) {
			
			device = manager.getDeviceAt(i);
			
			if (!device.getReceivingInput()) {
				continue;
			}
			
			// We do not store these commands because each one is unique per
			// device. Create the type command and immediately execute on the
			// device.
			TypeCommand typeCommand = new TypeCommand(passwords.get(i));
			typeCommand.executeCommand(device);
		}
	}
	
}
