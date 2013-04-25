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
 * Class: PackageOperationButtonPanel
 * 
 * Description:
 * GUI class for providing the package management UI elements. Allows for apk
 * install, uninstall and start activity.
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
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.commands.InstallPackageCommand;
import com.ebay.testdemultiplexer.device.commands.RemovePackageCommand;
import com.ebay.testdemultiplexer.device.commands.StartActivityCommand;

public class PackageOperationButtonPanel 
	extends JPanel implements ActionListener {
	
	/** File where startActivity data is saved to. */
	private static final String START_ACTIVITY_FILE = "startActivity.txt";
	
	/** Default value for start activity field. */
	private String defaultStartActivity = 
			"Please enter start activity";
	
	/** Read startActivity file error message. */
	private static final String READ_ERROR_MSG = 
			"Optional startActivity.txt file not found. To use, please make " +
			"sure the file exists in the same directory as the MTD executable.";
	
	/** Read startActivity file error title. */
	private static final String READ_ERROR_TITLE = 
			"Read Start Activity File Error";

	/** Reference to active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Install apk button. */
	private JButton installButton;
	
	/** Uninstall apk button. */
	private JButton uninstallButton;
	
	/** Start activity button. */
	private JButton startActivityButton;
	
	/**
	 * Create a new instance of PackageOperationButtonPanel.
	 * @param manager Active TestDeviceManager.
	 */
	public PackageOperationButtonPanel(TestDeviceManager manager) {
		this.manager = manager;
		
		initialize();
	}
	
	// -------------------------------------------------------------------------
	// Methods required by ActionListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == installButton) {
			
			KeyboardInputManager.interrupt();
			
			int retVal;

		    JFileChooser fc = new JFileChooser();
		    FileNameExtensionFilter fileFilter = 
		    		new FileNameExtensionFilter("APK File", "apk");

		    fc.addChoosableFileFilter(fileFilter);
		    retVal = fc.showOpenDialog(this);

		    if (retVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = fc.getSelectedFile().getAbsolutePath();
		    	
		    	InstallPackageCommand installCommand = 
		    			new InstallPackageCommand(filePath);
		    	manager.executeCommand(installCommand);
		    }
			
		    KeyboardInputManager.resume();
			
		} else if (e.getSource() == uninstallButton) {
			
			KeyboardInputManager.interrupt();
			
			String packageName = JOptionPane.showInputDialog(
				null, 
				"Please provide the package name to be uninstalled.", 
				"Uninstall Package", 
				JOptionPane.PLAIN_MESSAGE);
			
			if (packageName == null || packageName.isEmpty()) {
				return;
			}
			
			RemovePackageCommand removeCommand = new RemovePackageCommand(packageName);
			manager.executeCommand(removeCommand);
			
			KeyboardInputManager.resume();
			
		} else if (e.getSource() == startActivityButton) {
			
			KeyboardInputManager.interrupt();
			
			String componentVal = (String) JOptionPane.showInputDialog(
				null, 
				"Please provide the component name to start.\n" +
				"For example: com.yourcompany/.YourActivity", 
				"Start Activity", 
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				defaultStartActivity);
			
			if (componentVal == null || componentVal.isEmpty()) {
				return;
			}
			
			StartActivityCommand startCommand = 
					new StartActivityCommand(
							null, 
							null, 
							null, 
							null, 
							new ArrayList<String>(), 
							new HashMap<String, Object>(), 
							componentVal, 
							0);
			
			manager.executeCommand(startCommand);
			
			KeyboardInputManager.resume();
			
		}
		
	}
	
	// -------------------------------------------------------------------------
	// Private Methods
	// -------------------------------------------------------------------------
	
	/**
	 * Setup the UI panel.
	 */
	private void initialize() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		
		JLabel label = new JLabel("Package Operations");
		labelPanel.add(label);
		
		this.add(labelPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		installButton = new JButton("Install APK");
		installButton.addActionListener(this);
		buttonPanel.add(installButton);
		
		uninstallButton = new JButton("Uninstall APK");
		uninstallButton.addActionListener(this);
		buttonPanel.add(uninstallButton);
		
		startActivityButton = new JButton("Start Activity");
		startActivityButton.addActionListener(this);
		buttonPanel.add(startActivityButton);
		
		this.add(buttonPanel);
		
		loadDefaultStartActivity();
	}
	
	/**
	 * Open the saved startActivity data file and sidepocket the info.
	 */
	private void loadDefaultStartActivity() {
		
		File file = new File(START_ACTIVITY_FILE);
		if (!file.exists()) {
			return;
		}
		
		FileReader fileReader = null;
		
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		// Begin parsing
		String dataLine = "";
		
		while (dataLine != null) {
			
			// Read the next line.
			try {
				dataLine = bufferedReader.readLine();
			} catch (IOException e) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				READ_ERROR_MSG, 
	    				READ_ERROR_TITLE, 
	    				JOptionPane.ERROR_MESSAGE);
	    		
				e.printStackTrace();
				
				break;
			}
			
			// Because we are going in without reading the first line in the
			// file we need to check the case when nothing is there.
			if (dataLine == null) {
				break;
			}
			
			// Only read in the first line. We ignore anything else.
			defaultStartActivity = dataLine;
			break;
		}
		
		try {
			bufferedReader.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				READ_ERROR_MSG, 
    				READ_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
			
			e.printStackTrace();
		}
		
		try {
			fileReader.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				READ_ERROR_MSG, 
    				READ_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
			
			e.printStackTrace();
		}
	}

}
