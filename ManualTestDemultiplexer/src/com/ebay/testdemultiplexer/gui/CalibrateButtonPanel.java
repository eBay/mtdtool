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
 * Class: CalibrateButtonPanel
 * 
 * Description:
 * GUI class for providing the UI calibration elements. Sets up a JPanel that
 * can be added to a larger layout.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.calibration.CalibrationIO;

public class CalibrateButtonPanel extends JPanel implements ActionListener {

	/** Reference to the active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Calibrate the selected device screen. */
	private JButton calibrateButton;
	
	/** Reset the calibration on the selected device screen. */
	private JButton resetCalibrationButton;
	
	/** Reference to the device table. */
	private DeviceTable deviceTable;
	
	/**
	 * Create a new instance of the button panel with a reference to the 
	 * active TestDeviceManager.
	 * @param manager Active TestDeviceManager.
	 */
	public CalibrateButtonPanel(TestDeviceManager manager) {
		
		this.manager = manager;
		
		initialize();
	}
	
	// -------------------------------------------------------------------------
	// Required by ActionListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == calibrateButton) {
			
			ScreenDisplay.doCalibration();
			
		} else if (e.getSource() == resetCalibrationButton) {
			
			// Store the calibration data.
			CalibrationIO.addCalibrationData(
					manager.getDeviceAt(
							manager.getSignalingDeviceIndex()).
							getIDevice().getSerialNumber(), 
							0,
							0,
							(manager.getDeviceAt(
									manager.getSignalingDeviceIndex())).
									getScreenWidth(),
							(manager.getDeviceAt(
									manager.getSignalingDeviceIndex())).
									getScreenHeight());
			
			manager.getDeviceAt(
					manager.getSignalingDeviceIndex()).clearCalibrationData();
		}			
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Setup the GUI for this panel.
	 */
	private void initialize() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		
		JLabel label = new JLabel("Calibration and Device Management");
		labelPanel.add(label);
		
		this.add(labelPanel);
		
		JPanel buttonPanel1 = new JPanel();
		buttonPanel1.setLayout(new BoxLayout(buttonPanel1, BoxLayout.X_AXIS));
		
		calibrateButton = new JButton();
		calibrateButton.setText("Calibrate Screen");
		calibrateButton.addActionListener(this);
		buttonPanel1.add(calibrateButton);
		
		resetCalibrationButton = new JButton();
		resetCalibrationButton.setText("Reset Calibration");
		resetCalibrationButton.addActionListener(this);
		buttonPanel1.add(resetCalibrationButton);
		
		this.add(buttonPanel1);
		
		deviceTable = new DeviceTable(manager);
		
		this.add(deviceTable.getScrollPane());
	}
}
