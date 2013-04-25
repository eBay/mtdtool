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
 * Class: MainWindow
 * 
 * Description:
 * Principle JFrame that is the GUI window. All components are added to this
 * window to create the UI.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class MainWindow extends JFrame {
	
	/** Width of the window. */
	private static final int WINDOW_WIDTH = 925;
	
	/** Height of the window. */
	private static final int WINDOW_HEIGHT = 850;
	
	/** Reference to TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Render area to show the device screen. */
	private ScreenDisplay display;
	
	/**
	 * Create the main window. Requires a reference to the active 
	 * TestDeviceManager.
	 * @param manager Active TestDeviceManager.
	 */
	public MainWindow(TestDeviceManager manager) {
		
		super();
		
		this.manager = manager;
		initialize();
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Setup the window and UI.
	 */
	private void initialize() {
		
		Dimension windowDimension = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
		
		this.setTitle(TestDemultiplexerConstants.APP_NAME);
		this.setPreferredSize(windowDimension);
		this.setMaximumSize(windowDimension);
		this.setMinimumSize(windowDimension);
		this.setResizable(false);
		this.setBackground(Color.GRAY);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints previewPanelConstraints = new GridBagConstraints();
		previewPanelConstraints.fill = GridBagConstraints.BOTH;
		previewPanelConstraints.gridx = 0;
		previewPanelConstraints.gridy = 0;
		previewPanelConstraints.weightx = 0.5;
		this.add(getPreviewPanel(), previewPanelConstraints);
		
		GridBagConstraints controlPanelConstraints = new GridBagConstraints();
		controlPanelConstraints.fill = GridBagConstraints.BOTH;
		controlPanelConstraints.gridx = 1;
		controlPanelConstraints.gridy = 0;
		controlPanelConstraints.weightx = 0.5;
		this.add(getControlPanel(), controlPanelConstraints);
		
		// This magic bit of code is called when the window closes. Exists
		// to shutdown the TestDeviceManager adb connection.
		Runtime.getRuntime().addShutdownHook(new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				System.out.println("Shutdown process...");
				manager.disconnect();
			}
			
		});
	
		KeyboardInputManager keyboardInputManager = 
				new KeyboardInputManager(manager);
		KeyboardFocusManager.
			getCurrentKeyboardFocusManager().
			addKeyEventDispatcher(keyboardInputManager);
		
		ProgressDialogWindow.getInstance(this);
		
		display.start();
	}
	
	/**
	 * Get the preview panel which contains the ScreenDisplay for rendering
	 * the view of a single device.
	 * @return JPanel with screen display.
	 */
	private JPanel getPreviewPanel() {
		
		JPanel previewPanel = new JPanel(new BorderLayout());
		
		display = new ScreenDisplay(manager);
		
		previewPanel.add(display.getDisplay(), BorderLayout.CENTER);
		
		return previewPanel;
	}
	
	/**
	 * Get the control panel which contains the buttons for controlling the 
	 * device inputs, saving and loading step lists etc.
	 * @return JPanel with control buttons.
	 */
	private JPanel getControlPanel() {
	
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		// Add the hardware button panel.
		HardwareButtonPanel hardwareButtonPanel = 
				new HardwareButtonPanel(manager);
		controlPanel.add(hardwareButtonPanel);
		
		// Add the package operations panel.
		PackageOperationButtonPanel packageButtonPanel =
				new PackageOperationButtonPanel(manager);
		controlPanel.add(packageButtonPanel);
		
		// Add the recorder button panel.
		RecorderButtonPanel recorderButtonPanel = 
				new RecorderButtonPanel(manager);
		controlPanel.add(recorderButtonPanel);
		
		// Add the calibration button panel (includes the device table)
		CalibrateButtonPanel calibrationButtonPanel = 
				new CalibrateButtonPanel(manager);
		controlPanel.add(calibrationButtonPanel);
		
		// Add the login file panel.
		LoginFilePanel loginPanel = new LoginFilePanel(manager);
		controlPanel.add(loginPanel);
		
		return controlPanel;
	}
}
