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
 * Class: ScreenDisplay
 * 
 * Description:
 * GUI class for providing the render area to display the screen of the 
 * currently selected device. Handles input events and runs on an update 
 * thread to not lock up the UI thread.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener;
import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.calibration.CalibrationIO;
import com.ebay.testdemultiplexer.device.commands.DragCommand;
import com.ebay.testdemultiplexer.device.commands.TouchCommand;
import com.ebay.testdemultiplexer.uiautomator.UIViewTreeManager;
import com.ebay.testdemultiplexer.uiautomator.UIViewTreeNode;

public class ScreenDisplay extends Thread implements 
	MouseListener, TestDeviceConnectionListener {

	/** 
	 * This is the display area. Repeatedly drawing the screen capture as 
	 * an icon. Could be improved upon with canvas in the future.
	 */
	private JLabel display = new JLabel();
	
	/** Requested display width. */
	private static final int DISPLAY_WIDTH = 390;
	
	/** Requested display height. */
	private static final int DISPLAY_HEIGHT = 850;
	
	/** Delay in milliseconds between view refresh. */
	private static final int DELAY = 125;
	
	/** 
	 * Timer limit for click consideration. Anything longer is a long press.
	 * Time is stored in nano-seconds.
	 */
	private static final Long CLICK_LIMIT = 150000000L;
	
	/** Reference to TestDeviceManager used by app. */
	private TestDeviceManager manager;
	
	/** Image grabbed from focused device. */
	private BufferedImage currentImage;
	
	/** Scaled version of currentImage that is rendered to the view. */
	private BufferedImage scaledImage;
	
	/** Time stamp of mouse down event. Stored in nanoseconds. */
	private long mouseDownTimeStamp;
	
	/** Time stamp of mouse up event. Stored in nanoseconds. */
	private long mouseUpTimeStamp;
	
	/** The location of the mouse down event click. */
	private Point mouseDownPoint;
	
	/** The location of the mouse up event click. */
	private Point mouseUpPoint;
	
	/** Location of the first calibration point. */
	private Point firstCalibrationPoint = new Point();
	
	/** Location of the second calibration point. */
	private Point secondCalibrationPoint = new Point();
	
	/** 
	 * Flag when calibration should be performed. Need to improve upon this.
	 */
	private static boolean doCalibration = false;
	
	/**
	 * Track the nubmer of clicks in the calibration routine.
	 */
	private static int calibrationCounter = 0;
	
	/** Keep the render thread running. */
	private boolean runRenderer = false;
	
	/**
	 * Create a new ScreenDisplay. Requires an instance of TestDeviceManager.
	 * @param manager Reference to TestDeviceManager.
	 */
	public ScreenDisplay(TestDeviceManager manager) {
		
		this.manager = manager;
		
		initialize();
	}
	
	/**
	 * Get the display component.
	 * @return Display component.
	 */
	public JLabel getDisplay() {
		return display;
	}

	/**
	 * Stop the rendering loop.
	 */
	public void stopRendering() {
		runRenderer = false;
	}
	
	/**
	 * Flag the process to do a calibration on the current device.
	 */
	public static void doCalibration() {
		
		int selection = JOptionPane.showConfirmDialog(
                null, 
				"Please click on the upper left corner to set the " +
				"initial calibration coordinate.", 
				"First Calibration Point",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);

		if (selection == JOptionPane.OK_OPTION) {
			calibrationCounter = 0;
			doCalibration = true;
		}

	}
	
	// -------------------------------------------------------------------------
	// Methods required by MouseListener
	// -------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		
		// Capture data about mouse down presses.
		// This information is used later for drag command issuing.
		// Also capture the time so we can reproduce an equivalently timed
		// drag.
		mouseDownTimeStamp = System.nanoTime();
		mouseDownPoint = e.getPoint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		
		// This is where a lot of goodness happens!
		// On mouse released, there are a few things we have to look for.
		// First is the calibration cycle. If we are in a calibration cycle
		// (doCalibration == true) we will capture the following two clicks
		// as the top left and bottom right of the calibrated screen space.
		// This data is then stored per device as a calibrated region in
		// device screen coordinates.
		// If we are not performing a calibration, then we are issuing touch
		// or drag commands based on the duration of the mouse down to mouse
		// up cycle.
		if (doCalibration) {
			
			if (manager.getSignalingDeviceIndex() < 0) {
				
				JOptionPane.showMessageDialog(
						null, 
						"Please select a device to calibrate first.", 
						"No Device Selected", 
						JOptionPane.WARNING_MESSAGE);
				
				doCalibration = false;
				return;
			}
			
			if (calibrationCounter == 0) {
				
				// Get the first calibration point and then display the next
				// set of instructions. Also increment the calibrationCounter
				// so we know which step in the calibration we are on.
				firstCalibrationPoint = e.getPoint();
				calibrationCounter++;
				
				JOptionPane.showMessageDialog(
						null, 
						"Please click on the bottom right corner to set the " +
						"second calibration coordinate.", 
						"Second Calibration Point", 
						JOptionPane.INFORMATION_MESSAGE);
				
			} else if (calibrationCounter == 1) {
				
				// Get the second calibration point and then display the
				// calibration completed notification. Save the calibration
				// data to the specific device that was calibrated.
				// We have to do a little bit of math to convert the windowed
				// screen coordinates to device coordinates. All calibration
				// data is stored in device screen coordinates.
				secondCalibrationPoint = e.getPoint();
				
				int fullSizeWidth = 
						manager.getDeviceAt(
								manager.getSignalingDeviceIndex()).
								getScreenWidth();
				
				int fullSizeHeight = 
						manager.getDeviceAt(
								manager.getSignalingDeviceIndex()).
								getScreenHeight();
				
				// Convert to device screen coordinates.
				Point scaledFirstPoint = new Point();
				
				scaledFirstPoint.x = (int) (
						(float)firstCalibrationPoint.x / 
						(float)display.getWidth() * 
						fullSizeWidth);
				
				scaledFirstPoint.y = (int) (
						(float)firstCalibrationPoint.y / 
						(float)display.getHeight() * 
						fullSizeHeight);
				
				// Convert to device screen coordinates.
				Point scaledSecondPoint = new Point();
				
				scaledSecondPoint.x = (int) (
						(float)secondCalibrationPoint.x / 
						(float)display.getWidth() * 
						fullSizeWidth);
				
				scaledSecondPoint.y = (int) (
						(float)secondCalibrationPoint.y / 
						(float)display.getHeight() * 
						fullSizeHeight);
				
				// Store the calibration data.
				CalibrationIO.addCalibrationData(
						manager.getDeviceAt(
								manager.getSignalingDeviceIndex()).
								getIDevice().getSerialNumber(), 
								scaledFirstPoint.x,
								scaledFirstPoint.y,
								scaledSecondPoint.x,
								scaledSecondPoint.y);
				
				// Set the currently displayed device's calibrated data.
				manager.getDeviceAt(
						manager.getSignalingDeviceIndex()).setCalibrationData(
								scaledFirstPoint.x,
								scaledFirstPoint.y,
								scaledSecondPoint.x,
								scaledSecondPoint.y);
				
				// Show the final notification.
				JOptionPane.showMessageDialog(
						null, 
						"The device is now calibrated.", 
						"Calibration Complete", 
						JOptionPane.INFORMATION_MESSAGE);
				
				// Do everything possible to make sure we exit this path of
				// execution until the user presses the calibrate button again.
				calibrationCounter++;
				doCalibration = false;
				
			} else {
				doCalibration = false;
			}
			
			return;
		}
		
		// Grab the final time stamp and get the difference between the
		// mouse down and mouse up time stamps. Determine if the user clicked
		// or drag the mouse based on the duration of the mouse down process.
		mouseUpTimeStamp = System.nanoTime();
		Long difference = mouseUpTimeStamp - mouseDownTimeStamp;

		// Grab the mouse up location.
		mouseUpPoint = e.getPoint();
	
		if (difference < CLICK_LIMIT) {

			// For clicks, transform the mouse up into a percentage of 
			// view width and height to use in calculating touch events on all
			// other devices. Issue the touch command using this scale factor.
			float scaleX = (float)mouseUpPoint.x/(float)display.getWidth();
			float scaleY = (float)mouseUpPoint.y/(float)display.getHeight();

			TestDevice currentDevice = 
					manager.getDeviceAt(manager.getSignalingDeviceIndex());
		
			// Try to get the ID of the widget that was clicked.
			// Get the current device's UIViewTreeManager, then get the click
			// location in UIAutomation coordinates. Then get the node/widget
			// at that location. Pass the id of this node to the TouchCommand
			// to look up on all devices the touch command is applied to.
			String viewID = null;
			
			UIViewTreeManager uiViewTreeManager = 
					currentDevice.getUIViewTreeManager();

			if (uiViewTreeManager != null && uiViewTreeManager.deviceSupportsUIAutomation()) {
				
				Point clickPoint = 
						uiViewTreeManager.getUiAutomationClickLocation(
								scaleX, scaleY);
				
				UIViewTreeNode node = uiViewTreeManager.getViewAtLocation(
						clickPoint.x, clickPoint.y);
				
				if (node != null) {
					viewID = node.getUniqueID();
				}
			}
			
			TouchCommand touchCommand = 
					new TouchCommand(
							scaleX, 
							scaleY, 
							TouchPressType.DOWN_AND_UP,
							viewID);
			
			manager.executeCommand(touchCommand);
		
		} else {
			
			// For drags, transform the mouse down and mouse up into a 
			// percentage of view widths and heights to use in calculating
			// drags on all other devices. Issue the drag command using these
			// scale factors.
			float startScaleX = (float)mouseDownPoint.x/(float)display.getWidth();
			float startScaleY = (float)mouseDownPoint.y/(float)display.getHeight();

			float endScaleX = (float)mouseUpPoint.x/(float)display.getWidth();
			float endScaleY = (float)mouseUpPoint.y/(float)display.getHeight();
			
			DragCommand dragCommand = 
					new DragCommand(
							startScaleX, 
							startScaleY, 
							endScaleX, 
							endScaleY, 
							difference/1000000);
			
			manager.executeCommand(dragCommand);		
		}
	}
	
	/**
	 * Render loop.
	 */
	@Override
	public void run() {
		
		runRenderer = true;
		
		while(runRenderer) {
			
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			int index = manager.getSignalingDeviceIndex();
			
			// If no device is set as the signaling device, bail out.
			// But first clear the render view.
			if (index < 0) {
				display.setIcon(null);
				continue;
			}
			
			// In case we cannot connect with the device, bail out.
			if (manager.getDeviceAt(index) == null) {
				continue;
			}
			
			IChimpImage snapshot = manager.getDeviceAt(index).getScreenCapture();
				
			if (snapshot != null) {
				
				Point topLeft = 
						manager.getDeviceAt(index).getCalibratedTopLeftPoint();
			
				int calibratedWidth = 
						manager.getDeviceAt(index).getCalibratedWidth();
				int calibratedHeight =
						manager.getDeviceAt(index).getCalibratedHeight();
		
				snapshot = snapshot.getSubImage(
						topLeft.x, topLeft.y, calibratedWidth, calibratedHeight);
			}
			
			// In case we didn't get a snapshot, bail out.
			if (snapshot == null) {
				continue;
			}
			
			currentImage = snapshot.createBufferedImage();

			if (scaledImage == null) {
				scaledImage = new BufferedImage(
						display.getWidth(), 
						display.getHeight(), 
						BufferedImage.TYPE_INT_ARGB);
			}
			
			Graphics2D g = scaledImage.createGraphics();
			g.drawImage(
					currentImage, 
					0, 
					0, 
					scaledImage.getWidth(), 
					scaledImage.getHeight(), 
					null);
			
			g.dispose();
			
			display.setIcon(new ImageIcon(scaledImage));
			
		}
	}
	
	// -------------------------------------------------------------------------
	// Required by TestDeviceConnectionListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener#onDeviceAddedEvent(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void onDeviceAddedEvent(TestDevice device) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener#onDeviceChangeEvent(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void onDeviceChangeEvent(TestDevice device) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener#onDeviceRemovedEvent(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void onDeviceRemovedEvent(TestDevice device) {
		
		if (device == manager.getDeviceAt(manager.getSignalingDeviceIndex())) {
			stopRendering();
			this.interrupt();
		}
	}
	
	// -------------------------------------------------------------------------
	// Private method
	// -------------------------------------------------------------------------
	
	/**
	 * Initialize the JList per our requirements.
	 */
	private void initialize() {
		
		Dimension dimension = new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT);
		display.setPreferredSize(dimension);
		display.setMaximumSize(dimension);
		display.setMinimumSize(dimension);
		display.setBackground(Color.WHITE);
		display.setText("Screen capture render space");
		
		display.addMouseListener(this);
	}
}
