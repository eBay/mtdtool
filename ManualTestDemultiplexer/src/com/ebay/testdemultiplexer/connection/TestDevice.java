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
 * Class: TestDevice
 * 
 * Description: 
 * Represents a device. Contains a reference to IDevice and IChimpDevice
 * representations of the physical device with methods to access both.
 */

package com.ebay.testdemultiplexer.connection;

import java.awt.Point;

import javax.swing.JOptionPane;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.ddmlib.IDevice;
import com.ebay.testdemultiplexer.device.calibration.CalibrationData;
import com.ebay.testdemultiplexer.uiautomator.UIViewTreeManager;

public class TestDevice {
	
	// Missing environment variable - message.
	private static final String MISSING_SCREEN_DIMENSION_MSG = 
			"Unable to obtain screen dimensions for %s with serial number %s. "+
			"It is recommended that you retry connecting the device to see if "+
			"this information becomes available. If it does not, please " +
			"disconnect the device.";
	
	// Missing environment variable - title.
	private static final String MISSING_SCREEN_DIMENSION_TITLE = 
			"NO SCREEN DIMENSIONS REPORTED";
	
	/** Possible device grouping labels . */
	private static final String[] GROUPING = {"", "A", "B", "C", "D"};
	
	/** Current grouping label assigned to this device. */
	private int groupIndex;
	
	/** Instance of IDevice. */
	private IDevice device;

	/** Instance of IChimpDevice, typically implemented by AdbChimpDevice. */
	private IChimpDevice chimpDevice;
	
	/** Formatted device info of all properties available about a device. */
	private String formattedDeviceInfo;
	
	/** Device specific instance of its UIAutomation View Tree Manager. */
	private UIViewTreeManager uiViewTreeManager;
	
	/** 
	 * Width of the device screen in device coordinates. Once initialized, this
	 * value never changes.
	 */
	private int screenWidth;
	
	/** 
	 * Height of the device screen in device coordinates. Once initialized, this
	 * value never changes.
	 */
	private int screenHeight;
	
	/** Top left calibration point in device coordinates. */
	private Point calibratedTopLeft = new Point();
	
	/** Bottom right calibration point in device coordinates. */
	private Point calibratedBottomRight = new Point();
	
	/** Calibrated width in device coordinates. */
	private int calibratedWidth;
	
	/** Calibrated height in device coordinates. */
	private int calibratedHeight;
	
	/** Serial number of the device. */
	private String serialNumber = null;
	
	/** Model name of the device. */
	private String modelName = null;
	
	/** Android OS version number. */
	private String androidOS = null;
	
	/** 
	 * Tracks if the device is receiving input from demultiplexer. 
	 * True by default 
	 */
	private boolean receivingInput;
	
	/**
	 * Default constructor.
	 * @param device Device instance to manage.
	 */
	public TestDevice(IDevice device) {
		
		this.groupIndex = 0;
		this.device = device;
		this.chimpDevice = new AdbChimpDevice(device);
		this.receivingInput = true;
		this.uiViewTreeManager = new UIViewTreeManager(this);
		this.uiViewTreeManager.dumpUIHierarchy();

		// Perform the initialization procedures to extract data from the 
		// device.
		extractDeviceInfo();
		extractScreenDimensions();
		clearCalibrationData();
	}
	
	/**
	 * Release the IDevice and IChimpDevice references.
	 * Do this only when done with the device.
	 */
	public void dispose() {
		chimpDevice.dispose();
		chimpDevice = null;
		device = null;
	}
	
	/**
	 * Return the managed IDevice instance.
	 * @return IDevice instance.
	 */
	public IDevice getIDevice() {
		return device;
	}
	
	/**
	 * Return the managed IChimpDevice instance.
	 * @return IDevice instance.
	 */
	public IChimpDevice getIChimpDevice() {
		return chimpDevice;
	}
	
	/**
	 * Get the UIViewTreeManager instance tied to this device.
	 * @return UIViewTreeManager instance.
	 */
	public UIViewTreeManager getUIViewTreeManager() {
		return uiViewTreeManager;
	}
	
	/**
	 * Get the formatted device properties. Will report all available device 
	 * properties.
	 * @return Formatted device properties.
	 */
	public String getFullFormattedInfo() {
		
		return formattedDeviceInfo;
	}
	
	/**
	 * Retrieve the screen width of the device.
	 * @return Screen width.
	 */
	public int getScreenWidth() {
		return screenWidth;
	}
	
	/**
	 * Retrieve the screen height of the device.
	 * @return Screen height.
	 */
	public int getScreenHeight() {
		return screenHeight;
	}
	
	/**
	 * Set the state of receivingInput. If true, device will receive input
	 * messages. If false, messages will be blocked.
	 * @param receivingInput True to receive messages, false to block.
	 */
	public void setReceivingInput(boolean receivingInput) {
		this.receivingInput = receivingInput;
	}
	
	/**
	 * Check if the device is receiving messages or blocking them.
	 * @return True if receiving messages. False otherwise.
	 */
	public boolean getReceivingInput() {
		return receivingInput;
	}
	
	/**
	 * Get the grouping string for the device.
	 * @return Assigned grouping value.
	 */
	public String getGrouping() {
		
		return GROUPING[groupIndex];
	}
	
	/**
	 * Increment the grouping index of the device. If >= total number of 
	 * groupings reset to 0.
	 */
	public void incrementGrouping() {
		
		groupIndex++;
		
		if (groupIndex >= GROUPING.length) {
			groupIndex = 0;
		}
	}
	
	/**
	 * Take a screen capture and return the IChimpImage.
	 * @return IChimpImage of screen captured.
	 */
	public IChimpImage getScreenCapture() {
		
		return chimpDevice.takeSnapshot();
	}
	
	/**
	 * Get the model name of the device.
	 * @return Device model name.
	 */
	public String getModelName() {
		
		// We side pocket the info once we get valid data to avoid the 
		// cost and risk of calling again to the device for this info.
		if (modelName == null) {
			modelName = device.getProperty("ro.product.model");
		}
		
		return modelName;
	}
	
	/**
	 * Get the android OS version number.
	 * @return Version number String.
	 */
	public String getAndroidOSVersion() {
		
		if (androidOS == null) {
			androidOS = device.getProperty("ro.build.version.release");
		}
		
		return androidOS;
	}
	
	/**
	 * Get the serial number of the device.
	 * @return Device serial number.
	 */
	public String getSerialNumber() {
		return serialNumber;
	}
	
	/**
	 * See if the serial numbers match.
	 * @param device TestDevice to check against.
	 * @return True if the same, false otherwise.
	 */
	public boolean matchesSerialNumber(TestDevice device) {
		
		return (this.getSerialNumber().equals(device.getSerialNumber()));
	}
	
	/**
	 * Reset the calibration data to the full size of the device screen.
	 */
	public void clearCalibrationData() {
		
		calibratedTopLeft.setLocation(0, 0);
		calibratedBottomRight.setLocation(screenWidth, screenHeight);
		
		calibratedWidth = screenWidth;
		calibratedHeight = screenHeight;
	}
	
	/**
	 * Set the calibration data.
	 * @param data CalibrationData object with all the goodies to add.
	 */
	public void setCalibrationData(CalibrationData data) {
		
		calibratedTopLeft.setLocation(data.getStartX(), data.getStartY());
		calibratedBottomRight.setLocation(data.getEndX(), data.getEndY());
		
		calibratedWidth = data.getEndX() - data.getStartX();
		calibratedHeight = data.getEndY() - data.getStartY();
	}
	
	/**
	 * Set the calibration data. All values are expected to be in device 
	 * coordinates, not UI coordinates.
	 * @param startX Start x position in device coordinates.
	 * @param startY Start y position in device coordinates.
	 * @param endX End x position in device coordinates.
	 * @param endY End y position in device coordinates.
	 */
	public void setCalibrationData(int startX, int startY, int endX, int endY) {
		
		// Data expected in screen capture coords (full screen size)
		calibratedTopLeft.setLocation(startX, startY);
		calibratedBottomRight.setLocation(endX, endY);
		
		calibratedWidth = endX - startX;
		calibratedHeight = endY - startY;
	}
	
	/**
	 * Retrieve the calibrated top left point in device coordinates.
	 * @return Top left point.
	 */
	public Point getCalibratedTopLeftPoint() {
		return calibratedTopLeft;
	}
	
	/**
	 * Retrieve the calibrated bottom right point in device coordinates.
	 * @return Bottom right point.
	 */
	public Point getCalibratedBottomRightPoint() {
		return calibratedBottomRight;
	}
	
	/**
	 * Get the calibrated width in device coordinates.
	 * @return Calibrated width.
	 */
	public int getCalibratedWidth() {
		return calibratedWidth;
	}
	
	/**
	 * Get the calibrated height in device coordinates.
	 * @return Calibrated height.
	 */
	public int getCalibratedHeight() {
		return calibratedHeight;
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Extract all of the device properties from the device and format it 
	 * for human consumption.
	 * 
	 * Also collect the device serial number.
	 */
	private void extractDeviceInfo() {
		
		serialNumber = device.getSerialNumber();
		modelName = device.getProperty("ro.product.model");

	}
	
	/**
	 * Extract the dimensions of the device screen.
	 */
	private void extractScreenDimensions() {
		
		IChimpImage screen = chimpDevice.takeSnapshot();
		
		// We have found that on certain devices they will report as online,
		// however, they are not fully loaded yet. If we get screen back as
		// null, then sleep for a little bit and try again. If we still don't
		// get the screen then allow the NPE to be thrown so we know about
		// which device is having problems.
		if (screen == null) {
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			screen = chimpDevice.takeSnapshot();
		}
		
		if (screen == null) {
			
    		JOptionPane.showMessageDialog(
    				null, 
    				String.format(MISSING_SCREEN_DIMENSION_MSG, 
    						device.getName(), 
    						device.getSerialNumber()), 
    				MISSING_SCREEN_DIMENSION_TITLE, 
    				JOptionPane.WARNING_MESSAGE);
			
			screenHeight = 0;
			screenWidth = 0;
		} else {
			screenHeight = screen.getBufferedImage().getHeight();
			screenWidth = screen.getBufferedImage().getWidth();
		}
	}
}
