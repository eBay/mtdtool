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
 * Class: CalibrationIO
 * 
 * Description: 
 * Singleton utility to read calibration data from file, report back calibration
 * values for a specific serial number. Also takes new calibration data and
 * will write it to a file.
 */

package com.ebay.testdemultiplexer.device.calibration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;

import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class CalibrationIO {
	
	// Error dialog message displayed for read calibration file errors.
	private static final String READ_ERROR_MSG = 
			"Unable to read from calibration file.";
	
	// Error dialog title displayed for read calibration file errors.
	private static final String READ_ERROR_TITLE =
			"Calibration Read Error";
	
	// Error dialog message displayed for write calibration file errors.
	private static final String WRITE_ERROR_MSG = 
			"Unable to write to calibration file.";
	
	// Error dialog title displayed for write calibration file errors.
	private static final String WRITE_ERROR_TITLE =
			"Calibration Write Error";

	/** Static instance of CalibrationIO. */
	private static CalibrationIO calibrationIO;
	
	/** File where data is saved to. */
	private static final String STORED_FILE = "calibrationData.cdf";
	
	/** Calibration data mapped to device serial numbers. */
	private static HashMap<String, CalibrationData> dataMap = 
			new HashMap<String, CalibrationData>();
	
	/** Expected number of serialized tokens. */
	private static final int NUM_SERIAL_TOKENS = 5;
	
	/** Private singleton constructor. */
	private CalibrationIO() {
		
		// Pull in the calibration data we have.
		loadCalibrationData();
	}
	
	/**
	 * Get the singleton instance of CalibrationIO.
	 * @return CalibrationIO singleton instance.
	 */
	public static CalibrationIO getInstance() {
		
		if (calibrationIO == null) {
			calibrationIO = new CalibrationIO();
		}
		
		return calibrationIO;
	}
	
	/**
	 * Add calibration data to the store.
	 * @param serialNo Device serial number.
	 * @param startX Starting x position in device screen coordinates.
	 * @param startY Starting y position in device screen coordinates.
	 * @param endX Ending x position in device screen coordinates.
	 * @param endY Ending y position in device screen coordinates.
	 */
	public static void addCalibrationData(
			String serialNo, int startX, int startY, int endX, int endY) {
		
		CalibrationData data = new CalibrationData(startX, startY, endX, endY);
		dataMap.put(serialNo, data);
		
		// Always immediately write data to file and save. That way we never
		// loose data and don't have to prompt the user to do it.
		writeCalibrationData();
	}
	
	/**
	 * Get the calibration data tied to the given serial number.
	 * @param serialNo Serial number to look-up.
	 * @return Calibration data or null if none exists.
	 */
	public static CalibrationData getCalibrationData(String serialNo) {
		
		return dataMap.get(serialNo);
	}
	
	// -------------------------------------------------------------------------
	// Private methods.
	// -------------------------------------------------------------------------
	
	/**
	 * Open the saved calibration data file and parse out the calibration info.
	 */
	private static void loadCalibrationData() {
		
		dataMap.clear();
		
		File file = new File(STORED_FILE);
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
		String dataToken = "";
		
		while (dataToken != null) {
			
			// Read the next line.
			try {
				dataToken = bufferedReader.readLine();
			} catch (IOException e) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				READ_ERROR_MSG, 
	    				READ_ERROR_TITLE, 
	    				JOptionPane.ERROR_MESSAGE);
	    		
	    		System.out.println(READ_ERROR_MSG);
	    		
				e.printStackTrace();
				
				break;
			}
			
			// Because we are going in without reading the first line in the
			// file we need to check the case when nothing is there.
			if (dataToken == null) {
				break;
			}
			
			String[] tokens = 
					dataToken.split(
							TestDemultiplexerConstants.SERIAL_SEPARATOR);
			
			if (tokens.length != NUM_SERIAL_TOKENS) {
				continue;
			}
			
			int startX = Integer.valueOf(tokens[1]);
			int startY = Integer.valueOf(tokens[2]);
			int endX = Integer.valueOf(tokens[3]);
			int endY = Integer.valueOf(tokens[4]);
			
			dataMap.put(
					tokens[0], new CalibrationData(startX, startY, endX, endY));
		}
		
		try {
			bufferedReader.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				READ_ERROR_MSG, 
    				READ_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
    		System.out.println(READ_ERROR_MSG);
			
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
    		
    		System.out.println(READ_ERROR_MSG);
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the calibration data to file.
	 */
	private static void writeCalibrationData() {
		
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(STORED_FILE, false);
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				WRITE_ERROR_MSG, 
    				WRITE_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
    		System.out.println(WRITE_ERROR_MSG);
			
			e.printStackTrace();
		}
		
		BufferedWriter writer = new BufferedWriter(fileWriter);
		
		Object[] keys = dataMap.keySet().toArray();
		
		for (int i = 0; i < keys.length; i++) {
			
			String serialNo = (String) keys[i];
			
			CalibrationData data = dataMap.get(serialNo);
			
			String serialized = serialNo;
			serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
			serialized += data.getStartX();
			serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
			serialized += data.getStartY();
			serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
			serialized += data.getEndX();
			serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
			serialized += data.getEndY();
			serialized += "\n";
			
			try {
				writer.write(serialized);
			} catch (IOException e) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				WRITE_ERROR_MSG, 
	    				WRITE_ERROR_TITLE, 
	    				JOptionPane.ERROR_MESSAGE);
	    		
	    		System.out.println(WRITE_ERROR_MSG);
				
				e.printStackTrace();
			}
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				WRITE_ERROR_MSG, 
    				WRITE_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
    		System.out.println(WRITE_ERROR_MSG);
    		
			e.printStackTrace();
		}
		
		try {
			fileWriter.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				WRITE_ERROR_MSG, 
    				WRITE_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
    		System.out.println(WRITE_ERROR_MSG);
    		
			e.printStackTrace();
		}
	}
}
