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
 * Class: DragCommand
 * 
 * Description: 
 * Definition of a drag event for any given device. This is a mouse-press, 
 * mouse-drag, mouse-release user input event.
 */

package com.ebay.testdemultiplexer.device.commands;

import java.awt.Point;

import javax.vecmath.Point2f;

import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class DragCommand extends DeviceCommand implements 
	CommandSerializer, CommandDeserializer {

	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "DRAG_COMMAND";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 6;
	
	/** Starting point for drag as a scale percentage of the screen size. */
	private Point2f startScaleFactor;
	
	/** End point for drag as a scale percentage of the screen size. */
	private Point2f endScaledFactor;
	
	/** Number of milliseconds drag should take. */
	private long ms;
	
	/**
	 * Default constructor should only be used when deserializing data.
	 */
	public DragCommand() {
		
	}
	
	/**
	 * Create a new DragCommand. The number of steps interpolated between points
	 * is calculated at 1 steps per 1 unit distance.
	 * @param startXScale Initial x position scale factor (0.0 - 1.0 inclusive).
	 * @param startYScale Initial y position scale factor (0.0 - 1.0 inclusive).
	 * @param endXScale Final x position scale factor (0.0 - 1.0 inclusive).
	 * @param endYScale Final y position scale factor (0.0 - 1.0 inclusive).
	 * @param ms Duration of drag in milliseconds (1000 ms = 1 sec).
	 */
	public DragCommand(
			float startXScale, 
			float startYScale, 
			float endXScale, 
			float endYScale, 
			long ms) {
		
		this.startScaleFactor = new Point2f(startXScale, startYScale);
		this.endScaledFactor = new Point2f(endXScale, endYScale);
		this.ms = ms;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#execute(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void execute(TestDevice device) {
		
		// Number of steps the drag should interpolate over.
		int steps;

		// Get the top left offset position as a percentage change of the
		// screen size. Add this to the calibrated width scale factor and
		// that should be our click locations.
		
		Point topLeftCorner = device.getCalibratedTopLeftPoint();
		
		int xLeftCorner = 
				(int) ((float)topLeftCorner.x/(float)device.getScreenWidth());
		
		int yLeftCorner = 
				(int) ((float)topLeftCorner.y/(float)device.getScreenHeight());
		
		
		// Calculate the first click point
		int xACalibrationPos = (int) (device.getCalibratedWidth() * startScaleFactor.x);
		int yACalibrationPos = (int) (device.getCalibratedHeight() * startScaleFactor.y);
		
		Point startTmp = 
				new Point(
						xLeftCorner + xACalibrationPos, 
						yLeftCorner + yACalibrationPos);
		
		// Calculate the second click point
		int xBCalibrationPos = (int) (device.getCalibratedWidth() * endScaledFactor.x);
		int yBCalibrationPos = (int) (device.getCalibratedHeight() * endScaledFactor.y);

		Point endTmp = 
				new Point(
						xLeftCorner + xBCalibrationPos, 
						yLeftCorner + yBCalibrationPos);
		
		// Calculate the number of steps based on distance.
		double distance = startTmp.distance(endTmp);
		steps = (int) (distance * 0.25);
		
		if (steps <= 0) {
			steps = 1;
		}
		
		device.getIChimpDevice().drag(
				startTmp.x, startTmp.y, endTmp.x, endTmp.y, steps, ms);
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer#deserializeCommand(java.lang.String)
	 */
	public boolean deserializeCommand(String data) {
		
		startScaleFactor = new Point2f();
		endScaledFactor = new Point2f();
		
		String[] tokens = 
				data.split(TestDemultiplexerConstants.SERIAL_SEPARATOR);
		
		if (tokens.length != NUM_SERIAL_TOKENS) {
			return false;
		} else if (!tokens[0].equals(SERIALIZED_KEY)) {
			return false;
		}
		
		this.startScaleFactor.set(
				Float.parseFloat(tokens[1]), 
				Float.parseFloat(tokens[2]));
		
		this.endScaledFactor.set(
				Float.parseFloat(tokens[3]), 
				Float.parseFloat(tokens[4]));
		
		this.ms = Long.parseLong(tokens[5]);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer#serializeCommand()
	 */
	public String serializeCommand() {
		
		String serialized = SERIALIZED_KEY;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += startScaleFactor.x;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += startScaleFactor.y;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += endScaledFactor.x;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += endScaledFactor.y;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += ms;
		
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
