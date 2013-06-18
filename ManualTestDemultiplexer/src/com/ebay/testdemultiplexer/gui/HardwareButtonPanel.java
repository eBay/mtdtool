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
 * Class: HardwareButtonPanel
 * 
 * Description:
 * GUI class for providing UI device hardware button elements. These include
 * the standard Android hardware buttons (back, menu, search, home, up, down,
 * left, right and select). Also includes frequently used operations such as
 * screen capture, bug report, unlock device etc.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.commands.PressCommand;
import com.ebay.testdemultiplexer.device.commands.RestartDeviceCommand;
import com.ebay.testdemultiplexer.device.commands.ShellCommand;
import com.ebay.testdemultiplexer.device.commands.ToggleAirplaneModeCommand;
import com.ebay.testdemultiplexer.device.commands.UnlockDeviceCommand;

public class HardwareButtonPanel extends JPanel implements ActionListener {

	/** File where default command data is saved to. */
	private static final String STORED_CMD_FILE = "shellCommands.scf";
	
	/** Read shell command file error message. */
	private static final String READ_ERROR_MSG = 
			"Error reading from shellCommands.scf. Please make sure the file " +
			"exists in the same directory as the MTD executable.";
	
	/** Read shell command file error title. */
	private static final String READ_ERROR_TITLE = 
			"Read Shell Command File Error";
	
	/** Default bugreport error message. */
	private static final String OUTPUT_ERROR_MSG = 
			"Error writing bugreport file. Please make sure that the " +
			"location is valid and that write access is correct.";
	
	/** Bugreport error message title. */
	private static final String OUTPUT_ERROR_TITLE = "Bugreport Error";
	
	/** Reference to the device manager. */
	private TestDeviceManager manager;
	
	/** Set the dimensions of every button in the panel. */
	private Dimension buttonDimensions = new Dimension(60, 60);
	
	/** The hardware home button. */
	private JButton homeButton;
	
	/** The hardware search button. */
	private JButton searchButton;
	
	/** The hardware menu button. */
	private JButton menuButton;
	
	/** The hardware back button. */
	private JButton backButton;
	
	/** The DPAD down hardware button. */
	private JButton downButton;
	
	/** The DPAD right hardware button. */
	private JButton rightButton;
	
	/** The DPAD select hardware button. */
	private JButton enterButton;
	
	/** The DPAD left hardware button. */
	private JButton leftButton;
	
	/** The DPAD up hardware button. */
	private JButton upButton;
	
	/** The screen capture button. */
	private JButton screenCaptureButton;
	
	/** The save bug report button. */
	private JButton bugReportButton;
	
	/** Execute a raw adb command. */
	private JButton adbRawButton;
	
	/** Unlock device button. */
	private JButton unlockDeviceButton;
	
	/** Toggle airplane mode on devices. */
	private JButton toggleAirplaneMode;
	
	/** Refresh the UIAutomation dump stored with each device. */
	private JButton refreshUiAutomation;
	
	/** Standard shell commands to make available in the shell command dialog.*/
	private Object[] standardShellCommands;
	
	/**
	 * Create a new instance of the panel with the specified TestDeviceManager.
	 *
	 * @param manager Active TestDeviceManager reference.
	 */
	public HardwareButtonPanel(TestDeviceManager manager) {
		
		this.manager = manager;
		
		initialize();
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Setup the GUI.
	 */
	private void initialize() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Add the hardware button label.
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		
		JLabel hardwareButtonLabel = new JLabel();
		hardwareButtonLabel.setText("Hardware Buttons");
		labelPanel.add(hardwareButtonLabel);
		this.add(labelPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		// -------------------------------------
		// Add the first button column panel. (DPAD Buttons)
		// -------------------------------------
		JPanel buttonPanelDPAD = new JPanel();
		buttonPanelDPAD.setLayout(
				new BoxLayout(buttonPanelDPAD, BoxLayout.Y_AXIS));
		
		// Add the up button
		JPanel topRow = new JPanel();
		topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
		
		upButton = new JButton();
		Icon upIcon = new ImageIcon(getClass().getResource(
				"/graphics/up.png"));
		upButton.setIcon(upIcon);
		upButton.setSize(buttonDimensions);
		upButton.setMinimumSize(buttonDimensions);
		upButton.setPreferredSize(buttonDimensions);
		upButton.setMaximumSize(buttonDimensions);
		upButton.setToolTipText("Up DPad Button");
		upButton.addActionListener(this);
		
		topRow.add(upButton);
		buttonPanelDPAD.add(topRow);
		
		JPanel buttonPanelDPADMiddleRow = new JPanel();
		buttonPanelDPADMiddleRow.setLayout(
				new BoxLayout(buttonPanelDPADMiddleRow, BoxLayout.X_AXIS));
		
		// Add the left button
		leftButton = new JButton();
		Icon leftIcon = new ImageIcon(getClass().getResource(
				"/graphics/back.png"));
		leftButton.setIcon(leftIcon);
		leftButton.setSize(buttonDimensions);
		leftButton.setMinimumSize(buttonDimensions);
		leftButton.setPreferredSize(buttonDimensions);
		leftButton.setMaximumSize(buttonDimensions);
		leftButton.setToolTipText("Left DPad Button");
		leftButton.addActionListener(this);
		buttonPanelDPADMiddleRow.add(leftButton);
				
		// Add the enter button
		enterButton = new JButton();
		Icon enterIcon = new ImageIcon(getClass().getResource(
				"/graphics/enter.png"));
		enterButton.setIcon(enterIcon);
		enterButton.setSize(buttonDimensions);
		enterButton.setMinimumSize(buttonDimensions);
		enterButton.setPreferredSize(buttonDimensions);
		enterButton.setMaximumSize(buttonDimensions);
		enterButton.setToolTipText("Center DPad Button");
		enterButton.addActionListener(this);
		buttonPanelDPADMiddleRow.add(enterButton);
				
		// Add the right button
		rightButton = new JButton();
		Icon rightIcon = new ImageIcon(getClass().getResource(
				"/graphics/forward.png"));
		rightButton.setIcon(rightIcon);
		rightButton.setSize(buttonDimensions);
		rightButton.setMinimumSize(buttonDimensions);
		rightButton.setPreferredSize(buttonDimensions);
		rightButton.setMaximumSize(buttonDimensions);
		rightButton.setToolTipText("Right DPad Button");
		rightButton.addActionListener(this);
		buttonPanelDPADMiddleRow.add(rightButton);
		
		buttonPanelDPAD.add(buttonPanelDPADMiddleRow);
		
		// Add the down button
		JPanel bottomRow = new JPanel();
		bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
		
		downButton = new JButton();
		Icon downIcon = new ImageIcon(getClass().getResource(
				"/graphics/down.png"));
		downButton.setIcon(downIcon);
		downButton.setSize(buttonDimensions);
		downButton.setMinimumSize(buttonDimensions);
		downButton.setPreferredSize(buttonDimensions);
		downButton.setMaximumSize(buttonDimensions);
		downButton.setToolTipText("Down DPad Button");
		downButton.addActionListener(this);
		downButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		downButton.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		bottomRow.add(downButton);
		buttonPanelDPAD.add(bottomRow);
		buttonPanel.add(buttonPanelDPAD);
		
		buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		
		// -------------------------------------
		// Add the second button column panel. (Other hardware buttons)
		// -------------------------------------
		
		JPanel secondColumn = new JPanel();
		secondColumn.setLayout(new BoxLayout(secondColumn, BoxLayout.Y_AXIS));
		
		JPanel secondColumnFirstRow = new JPanel();
		secondColumnFirstRow.setLayout(
				new BoxLayout(secondColumnFirstRow, BoxLayout.X_AXIS));
		
		// Add the home button.
		homeButton = new JButton();
		Icon homeIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_home.png"));
		homeButton.setIcon(homeIcon);
		homeButton.setSize(buttonDimensions);
		homeButton.setMinimumSize(buttonDimensions);
		homeButton.setPreferredSize(buttonDimensions);
		homeButton.setMaximumSize(buttonDimensions);
		homeButton.setToolTipText("Home");
		homeButton.addActionListener(this);
		secondColumnFirstRow.add(homeButton);
		
		// Add the back button
		backButton = new JButton();
		Icon backIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_back.png"));
		backButton.setIcon(backIcon);
		backButton.setSize(buttonDimensions);
		backButton.setMinimumSize(buttonDimensions);
		backButton.setPreferredSize(buttonDimensions);
		backButton.setMaximumSize(buttonDimensions);
		backButton.setToolTipText("Back");
		backButton.addActionListener(this);
		secondColumnFirstRow.add(backButton);
		
		// Add the menu button
		menuButton = new JButton();
		Icon menuIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_more.png"));
		menuButton.setIcon(menuIcon);
		menuButton.setSize(buttonDimensions);
		menuButton.setMinimumSize(buttonDimensions);
		menuButton.setPreferredSize(buttonDimensions);
		menuButton.setMaximumSize(buttonDimensions);
		menuButton.setToolTipText("Menu");
		menuButton.addActionListener(this);
		secondColumnFirstRow.add(menuButton);
		
		secondColumn.add(secondColumnFirstRow);
		
		// Add the second row
		JPanel secondColumnSecondRow = new JPanel();
		secondColumnSecondRow.setLayout(
				new BoxLayout(secondColumnSecondRow, BoxLayout.X_AXIS));
		
		// Add the search button
		searchButton = new JButton();
		Icon searchIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_find.png"));
		searchButton.setIcon(searchIcon);
		searchButton.setSize(buttonDimensions);
		searchButton.setMinimumSize(buttonDimensions);
		searchButton.setPreferredSize(buttonDimensions);
		searchButton.setMaximumSize(buttonDimensions);
		searchButton.setToolTipText("Search");
		searchButton.addActionListener(this);
		secondColumnSecondRow.add(searchButton);
	
		// Add bugreport button
		bugReportButton = new JButton();
		Icon bugReportIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_emoticons.png"));
		bugReportButton.setIcon(bugReportIcon);
		bugReportButton.setSize(buttonDimensions);
		bugReportButton.setMinimumSize(buttonDimensions);
		bugReportButton.setPreferredSize(buttonDimensions);
		bugReportButton.setMaximumSize(buttonDimensions);
		bugReportButton.setToolTipText("Bug Report");
		bugReportButton.addActionListener(this);
		secondColumnSecondRow.add(bugReportButton);
		
		// Add screen capture button
		screenCaptureButton = new JButton();
		Icon captureIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_camera.png"));
		screenCaptureButton.setIcon(captureIcon);
		screenCaptureButton.setSize(buttonDimensions);
		screenCaptureButton.setMinimumSize(buttonDimensions);
		screenCaptureButton.setPreferredSize(buttonDimensions);
		screenCaptureButton.setMaximumSize(buttonDimensions);
		screenCaptureButton.setToolTipText("Capture Screen");
		screenCaptureButton.addActionListener(this);
		secondColumnSecondRow.add(screenCaptureButton);
		
		secondColumn.add(secondColumnSecondRow);
		
		// Add the third row
		JPanel secondColumnThirdRow = new JPanel();
		secondColumnThirdRow.setLayout(
				new BoxLayout(secondColumnThirdRow, BoxLayout.X_AXIS));
		
		// Add the restart button.
		JButton restartButton = new JButton();
		Icon restartIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_refresh.png"));
		restartButton.setIcon(restartIcon);
		restartButton.setSize(buttonDimensions);
		restartButton.setMinimumSize(buttonDimensions);
		restartButton.setPreferredSize(buttonDimensions);
		restartButton.setMaximumSize(buttonDimensions);
		restartButton.setToolTipText("Restart Devices");
		restartButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				int result =  JOptionPane.showConfirmDialog(
								null, 
								"Are you sure you want to restart devices?",
								"Confirm Restart Devices", 
								JOptionPane.YES_NO_OPTION, 
								JOptionPane.QUESTION_MESSAGE);

				if (result == 0) {
					
					JOptionPane.showMessageDialog(
							null, 
							"If the devices do not reattach after restart, " +
							"please issue an adb kill-server followed by an " +
							"adb devices command in the terminal.", 
							"Adb Connection Hint", 
							JOptionPane.INFORMATION_MESSAGE);

					// In order to have the render loop not run off the rails,
					// we leave it running but set the signaling device to -1.
					// This will cause the renderer to release the device it
					// is using to render. Give 2 seconds for the disconnect
					// before issuing the command.
					manager.setSignalingDeviceIndex(-1);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					RestartDeviceCommand restartCommand = 
							new RestartDeviceCommand();
					manager.executeCommand(restartCommand);
				}
			}
		});		
		secondColumnThirdRow.add(restartButton);
		
		// Add adb raw execute button
		adbRawButton = new JButton();
		Icon adbIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_manage.png"));
		adbRawButton.setIcon(adbIcon);
		adbRawButton.setSize(buttonDimensions);
		adbRawButton.setMinimumSize(buttonDimensions);
		adbRawButton.setPreferredSize(buttonDimensions);
		adbRawButton.setMaximumSize(buttonDimensions);
		adbRawButton.setToolTipText("Execute Raw ADB Command");
		adbRawButton.addActionListener(this);
		secondColumnThirdRow.add(adbRawButton);
		
		// Add the unlock device button
		unlockDeviceButton = new JButton();
		Icon unlockIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_login.png"));
		unlockDeviceButton.setIcon(unlockIcon);
		unlockDeviceButton.setSize(buttonDimensions);
		unlockDeviceButton.setMinimumSize(buttonDimensions);
		unlockDeviceButton.setPreferredSize(buttonDimensions);
		unlockDeviceButton.setMaximumSize(buttonDimensions);
		unlockDeviceButton.setToolTipText("Unlock devices");
		unlockDeviceButton.addActionListener(this);
		secondColumnThirdRow.add(unlockDeviceButton);
		
		// Add the third row to the second column
		secondColumn.add(secondColumnThirdRow);
		
		// Add the fourth row
		JPanel secondColumnFourthRow = new JPanel();
		secondColumnFourthRow.setLayout(
				new BoxLayout(secondColumnFourthRow, BoxLayout.X_AXIS));
		
		toggleAirplaneMode = new JButton();
		Icon airplaneModeIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_send.png"));
		toggleAirplaneMode.setIcon(airplaneModeIcon);
		toggleAirplaneMode.setSize(buttonDimensions);
		toggleAirplaneMode.setMinimumSize(buttonDimensions);
		toggleAirplaneMode.setPreferredSize(buttonDimensions);
		toggleAirplaneMode.setMaximumSize(buttonDimensions);
		toggleAirplaneMode.setToolTipText("Toggle airplane mode");
		toggleAirplaneMode.addActionListener(this);
		secondColumnFourthRow.add(toggleAirplaneMode);
		
		refreshUiAutomation = new JButton();
		Icon refreshUIAutomationIcon = new ImageIcon(getClass().getResource(
				"/graphics/ic_menu_set_as.png"));
		refreshUiAutomation.setIcon(refreshUIAutomationIcon);
		refreshUiAutomation.setSize(buttonDimensions);
		refreshUiAutomation.setMinimumSize(buttonDimensions);
		refreshUiAutomation.setPreferredSize(buttonDimensions);
		refreshUiAutomation.setMaximumSize(buttonDimensions);
		refreshUiAutomation.setToolTipText("Refresh UIAutomation Dump");
		refreshUiAutomation.addActionListener(this);
		secondColumnFourthRow.add(refreshUiAutomation);
		
		// Add the fourth row to the second column.
		secondColumn.add(secondColumnFourthRow);
		
		buttonPanel.add(secondColumn);
		
		this.add(buttonPanel);
		
		loadDefaultShellCommands();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == homeButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_HOME", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == searchButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_SEARCH", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == menuButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_MENU", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == backButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_BACK", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == downButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_DPAD_DOWN", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == rightButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_DPAD_RIGHT", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == enterButton) {
				
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_DPAD_CENTER", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == leftButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_DPAD_LEFT", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);
			
		} else if (e.getSource() == upButton) {
			
			PressCommand pressCommand = new PressCommand(
					"KEYCODE_DPAD_UP", TouchPressType.DOWN_AND_UP);
			manager.executeCommand(pressCommand);

		} else if (e.getSource() == screenCaptureButton) {
		
			KeyboardInputManager.interrupt();
			
			int retVal;
	
		    JFileChooser fc = new JFileChooser();
		    FileNameExtensionFilter fileFilter = 
		    		new FileNameExtensionFilter("PNG", "png");
	
		    fc.addChoosableFileFilter(fileFilter);
		    retVal = fc.showSaveDialog(this);
	
		    if (retVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = fc.getSelectedFile().getAbsolutePath();
		    	
		    	if (!filePath.endsWith(".png")) {
		    		filePath = filePath + ".png";
		    	}
		    	
		    	IChimpImage image = manager.getDeviceAt(
		    			manager.getSignalingDeviceIndex()).
		    			getIChimpDevice().takeSnapshot();
				
				BufferedImage imageBuffer = image.createBufferedImage();

		    	File imgFile = new File(filePath);
		    	try {
					ImageIO.write(imageBuffer, "png", imgFile);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    }
		    
		    KeyboardInputManager.resume();
		    
		} else if (e.getSource() == bugReportButton) {
			
			generateBugReport();
		
		} else if (e.getSource() == adbRawButton) {
		
			executeRawAdb();
			
		} else if (e.getSource() == unlockDeviceButton) {
			
			UnlockDeviceCommand command = new UnlockDeviceCommand();
			manager.executeCommand(command);
			
		} else if (e.getSource() == toggleAirplaneMode) {
			
			ToggleAirplaneModeCommand command = new ToggleAirplaneModeCommand();
			manager.executeCommand(command);
			
		} else if (e.getSource() == refreshUiAutomation) {
			
			for (int i = manager.getTotalDeviceCount()-1; i >= 0; i--) {
				manager.getDeviceAt(i).getUIViewTreeManager().dumpUIHierarchy();
			}
			
		}
		
	}
	
	/**
	 * Execute raw adb shell commands on each device.
	 */
	private void executeRawAdb() {
		
		KeyboardInputManager.interrupt();
		
		ComboBoxDialog dialog = new ComboBoxDialog(null, standardShellCommands);
		String shellCommandValue = (String) dialog.showDialog();
		
		if (shellCommandValue != null) {
			ShellCommand shellCommand = new ShellCommand(shellCommandValue);
			manager.executeCommand(shellCommand);
		}
	
		KeyboardInputManager.resume();
		
	}
	
	/**
	 * Generate the bugreport file. This is almost identical to adb bugreport
	 * except we have the ability to expand this out and add/remove any number
	 * of sections to the report as needed.
	 */
	private void generateBugReport() {
		
		// Ask the user where to save the report.
		KeyboardInputManager.interrupt();
		
		int retVal;

	    JFileChooser fc = new JFileChooser();
	    FileNameExtensionFilter fileFilter = 
	    		new FileNameExtensionFilter("Text", "txt");

	    fc.addChoosableFileFilter(fileFilter);
	    retVal = fc.showSaveDialog(this);
	    
	    String filePath = null;

	    if (retVal == JFileChooser.APPROVE_OPTION) {
	    	filePath = fc.getSelectedFile().getAbsolutePath();
	    	
	    	if (!filePath.endsWith(".txt")) {
	    		filePath = filePath + ".txt";
	    	}
	    }
	    
	    KeyboardInputManager.resume();
	    
	    // Generate the actual bug report file.
	    if (filePath == null) {
	    	return;
	    }
		
		TestDevice device = 
				manager.getDeviceAt(manager.getSignalingDeviceIndex());
		
		if (device == null) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				OUTPUT_ERROR_MSG, 
    				OUTPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
			return;
		}
		
		File file = new File(filePath);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				OUTPUT_ERROR_MSG, 
    				OUTPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
			
			e.printStackTrace();
			
			return;
		}
		
		String endCap = "------";
		
		String[] keys = {
		"MEMORY INFO",
		"CPU INFO",
		"PROCRANK",
		"VIRTUAL MEMORY STATS",
		"VMALLOC INFO",
		"SLAB INFO",
		"ZONEINFO",
		"MAIN LOG",
		"SYSTEM LOG",
		"VM TRACES JUST NOW",
		"VM TRACES AT LAST ANR",
		"EVENT LOG",
		"RADIO LOG",
		"NETWORK INTERFACES",
		"NETWORK ROUTES",
		"ARP CACHE",
		"SYSTEM PROPERTIES",
		"KERNEL LOG",
		"KERNEL WAKELOCKS",
		"KERNEL CPUFREQ",
		"VOLD DUMP",
		"SECURE CONTAINERS",
		"PROCESSES",
		"PROCESSES AND THREADS",
		"LIBRANK",
		"BINDER FAILED TRANSACTION LOG",
		"BINDER TRANSACTION LOG",
		"BINDER TRANSACTIONS",
		"BINDER STATS",
		"BINDER PROCESS STATE",
		"FILESYSTEMS & FREE SPACE",
		"PACKAGE SETTINGS",
		"PACKAGE UID ERRORS",
		"LAST KMSG",
		"LAST RADIO LOG",
		"LAST PANIC CONSOLE",
		"LAST PANIC THREADS",
		"DUMPSYS"};
		
		String[] values = {
		"cat /proc/meminfo",
		"top -n 1 -d 1 -m 30 -t",
		"procrank",
		"cat /proc/vmstat",
		"cat /proc/vmallocinfo",
		"cat /proc/slabinfo",
		"cat /proc/zoneinfo",
		"logcat -b main -v time -d *:v",
		"logcat -b system -v time -d *:v",
		"cat /data/anr/traces.txt.bugreport",
		"cat /data/anr/traces.txt",
		"logcat -b events -v time -d *:v",
		"logcat -b radio -v time -d *:v",
		"netcfg",
		"cat /proc/net/route",
		"cat /proc/net/arp",
		"cat /system/build.prop",
		"dmesg",
		"cat /proc/wakelocks",
		"cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state",
		"vdc dump",
		"vdc asec list",
		"ps -P",
		"ps -t -p -P",
		"librank",
		"cat /proc/binder/failed_transaction_log",
		"cat /proc/binder/transaction_log",
		"cat /proc/binder/transactions",
		"cat /proc/binder/stats",
		"sh -c cat /proc/binder/proc/* -P",
		"df",
		"cat /data/system/packages.xml",
		"cat /data/system/uiderrors.txt",
		"cat /proc/last_kmsg",
		"parse_radio_log /proc/last_radio_log",
		"cat /data/dontpanic/apanic_console",
		"cat /data/dontpanic/apanic_threads",
		"dumpsys"};
		
		// Header bits and pieces
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		String manufacturer = device.getIChimpDevice().getSystemProperty("ro.product.manufacturer");
		String model = device.getIChimpDevice().getSystemProperty("ro.product.model");
		String build = device.getIChimpDevice().getSystemProperty("ro.build.description");
		
		String header = 
				"========================================================\n" +
				"== dumpstate CPS Super Blend (BJY): " + dateFormat.format(cal.getTime()) + "\n" +
				"========================================================\n\n"+
				"Model: " + model + "\n" +
				"Manufacturer: " + manufacturer + "\n" +
				"Build: " + build + "\n\n";

		try {
			writer.write(header);
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				OUTPUT_ERROR_MSG, 
    				OUTPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
			
			e.printStackTrace();
			
			return;
		}
		
		// Now loop over the map and print the info	
		for (int i = 0; i < keys.length; i++) {
			
			String output = 
					endCap + " " + keys[i] + " (" + 
							values[i] + ") " + endCap + "\n";
			
			String result = device.getIChimpDevice().shell(values[i] );
			
			if (result != null) {
				output += result;
			}
			output += "\n\n";
			
			try {
				writer.write(output);
			} catch (IOException e) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				OUTPUT_ERROR_MSG, 
	    				OUTPUT_ERROR_TITLE, 
	    				JOptionPane.ERROR_MESSAGE);
				
				e.printStackTrace();
				
				return;
			}
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				OUTPUT_ERROR_MSG, 
    				OUTPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
			
			e.printStackTrace();
			
			return;
		}
		
	}
	
	/**
	 * Open the saved shell command data file and sidepocket the info.
	 */
	private void loadDefaultShellCommands() {
		
		File file = new File(STORED_CMD_FILE);
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
		ArrayList<String> shellCommandList = new ArrayList<String>();
		
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
			
			shellCommandList.add(dataLine);
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
		
		// Only initialize standardShellCommands if we have something to
		// initialize it with. We will check for null elsewhere.
		if (shellCommandList.size() > 0) {
			standardShellCommands = shellCommandList.toArray();
		}
		
	}
}
