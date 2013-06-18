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
 * Class: TouchCommand
 * 
 * Description: 
 * Definition of a mouse click event for any given device.
 */

package com.ebay.testdemultiplexer.device.commands;

import java.awt.Point;

import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.uiautomator.UIViewTreeManager;
import com.ebay.testdemultiplexer.uiautomator.UIViewTreeNode;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class TouchCommand extends DeviceCommand implements 
	CommandSerializer, CommandDeserializer {
	
	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "TOUCH_COMMAND";
	
	/** Null string used for deserialization and serialization. */
	private static final String NULL_STRING = "null";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 5;
	
	/** X axis scaled position of touch. */
	private float xScale;
	
	/** Y axis scaled position of touch. */
	private float yScale;
	
	/** TouchPressType to apply to touch action. */
	private TouchPressType pressType;
	
	/** 
	 * Optional unique ID that can be used to click on specific elements on
	 * other devices. If set, this will supercede any click location on that
	 * device.
	 */
	private String uniqueUiAutomationId;
	
	/**
	 * Default constructor should only be used when deserializing data.
	 */
	public TouchCommand() {
		
	}
	
	/**
	 * Create a new TouchCommand.
	 * @param xScale X axis scale position of touch (0.0 - 1.0 inclusive).
	 * @param yScale Y axis scale position of touch (0.0 - 1.0 inclusive).
	 * @param pressType TouchPressType to apply to touch command.
	 * @param uniqueUiAutomationId ID of the view clicked. Can be null.
	 */
	public TouchCommand(float xScale, float yScale, TouchPressType pressType, String uniqueUiAutomationId) {
		this.xScale = xScale;
		this.yScale = yScale;
		this.pressType = pressType;
		this.uniqueUiAutomationId = uniqueUiAutomationId;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#execute(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void execute(TestDevice device) {
		
		int finalXPos = -1;
		int finalYPos = -1;
		UIViewTreeManager uiViewTreeManager = device.getUIViewTreeManager();	
		
		if (uiViewTreeManager.deviceSupportsUIAutomation() && uniqueUiAutomationId != null) {
/*			
			uiViewTreeManager.dumpUIHierarchy(device);
			uiViewTreeManager.waitForNewRootNode();
			uiViewTreeManager.printUIHierarchy();
*/			UIViewTreeNode node = 
					uiViewTreeManager.makeNodeVisible(uniqueUiAutomationId);
			
			if (node != null) {
				System.out.println("Touch command executing on: "+node.getUniqueID());
				Point clickableCenter = node.getClickableCenter();
				finalXPos = clickableCenter.x;
				finalYPos = clickableCenter.y;
				System.out.println("Clicking at: "+finalXPos+" "+finalYPos);
			}

/*			
			uiViewTreeManager.getNodeAtID(device, "0000010006500");
			System.out.println("step 1");
//			uiViewTreeManager.getNodeAtID(device, "000001000200000");
//			System.out.println("step 2");
			uiViewTreeManager.getNodeAtID(device, "0000010002003120");
			System.out.println("step 3");
			uiViewTreeManager.getNodeAtID(device, "000001000200000");
			System.out.println("step 4");
*/				
		}
		
		if (finalXPos == -1 || finalYPos == -1) {
			// Get the top left offset position as a percentage change of the
			// screen size. Add this to the calibrated width scale factor and
			// that should be our click location.
			
			Point topLeftCorner = device.getCalibratedTopLeftPoint();
			int xLeftCorner = 
					(int) ((float)topLeftCorner.x/(float)device.getScreenWidth());
			
			int yLeftCorner = 
					(int) ((float)topLeftCorner.y/(float)device.getScreenHeight());
			
			int xCalibrationPos = (int) (device.getCalibratedWidth() * xScale);
			int yCalibrationPos = (int) (device.getCalibratedHeight() * yScale);
			
			finalXPos = xLeftCorner + xCalibrationPos;
			finalYPos = yLeftCorner + yCalibrationPos;
			
		}
		
		device.getIChimpDevice().touch(finalXPos, finalYPos, pressType);
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer#deserializeCommand(java.lang.String)
	 */
	public boolean deserializeCommand(String data) {
		
		String[] tokens = 
				data.split(TestDemultiplexerConstants.SERIAL_SEPARATOR);
		
		// Adding in legacy support for NUM_SERIAL_TOKENS-1.
		// Unlikely it is needed, but trying to
		// be nice to anyone that was an early adopter.
		if (tokens.length != NUM_SERIAL_TOKENS || 
				tokens.length != (NUM_SERIAL_TOKENS-1)) {
			return false;
		} else if (!tokens[0].equals(SERIALIZED_KEY)) {
			return false;
		}
		
		xScale = Float.valueOf(tokens[1]);
		yScale = Float.valueOf(tokens[2]);
		pressType = TouchPressType.fromIdentifier(tokens[3]);
		uniqueUiAutomationId = tokens[4];
		
		if (uniqueUiAutomationId.equals(NULL_STRING)) {
			uniqueUiAutomationId = null;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer#serializeCommand()
	 */
	public String serializeCommand() {
		
		String serialized = SERIALIZED_KEY;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += xScale;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += yScale;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += pressType.getIdentifier();
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		if (uniqueUiAutomationId != null) {
			serialized += uniqueUiAutomationId;
		} else {
			serialized += NULL_STRING;
		}
		
		return serialized;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return SERIALIZED_KEY;
	}
}
