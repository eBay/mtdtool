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
 * Class: CalibrationData
 * 
 * Description: 
 * Container for the data about a device specific calibration and related 
 * values.
 */

package com.ebay.testdemultiplexer.device.calibration;

public class CalibrationData {

	private int startX;
	private int startY;
	private int endX;
	private int endY;
	
	/**
	 * Default constructor.
	 * @param startX Starting x position in device screen coordinates.
	 * @param startY Starting y position in device screen coordinates.
	 * @param endX Ending x position in device screen coordinates.
	 * @param endY Ending y position in device screen coordinates.
	 */
	public CalibrationData(int startX, int startY, int endX, int endY) {
		
		setData(startX, startY, endX, endY);
	}
	
	/**
	 * Set calibration data
	 * @param startX Starting x position in device screen coordinates.
	 * @param startY Starting y position in device screen coordinates.
	 * @param endX Ending x position in device screen coordinates.
	 * @param endY Ending y position in device screen coordinates.
	 */
	public void setData(int startX, int startY, int endX, int endY) {
		
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
	
	/**
	 * Get the starting x position in screen coordinates.
	 * @return Starting x position.
	 */
	public int getStartX() {
		return startX;
	}
	
	/**
	 * Get the starting y position in screen coordinates.
	 * @return Starting y position.
	 */
	public int getStartY() {
		return startY;
	}

	/**
	 * Get the ending x position in screen coordinates.
	 * @return Ending x position.
	 */
	public int getEndX() {
		return endX;
	}
	
	/**
	 * Get the ending y position in screen coordinates.
	 * @return Ending y position.
	 */
	public int getEndY() {
		return endY;
	}
}
