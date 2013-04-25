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
 * Class: ManualTestDemultiplexer
 * 
 * Description: 
 * Entry point for the Manual Test Demultiplexer. Initializes adb connection
 * in the TestDeviceManager and displays the main window.
 * 
 * Expectation is that the Android SDK is installed. The following Android SDK
 * jars are required to build the project:
 * 
 * ddmlib.jar
 * chimpchat.jar
 * sdklib.jar
 * guava-13.0.1.jar
 * 
 * Expectation that the environment variable ADB_HOME is set to the path for 
 * adb. For example:
 * export ADB_HOME=/Developer/android-sdk-macosx/platform-tools/adb
 * 
 * There is a dependency on DeviceUnlock and ToggleAirplaneMode projects. To
 * use those features of the MTD tool, you will need to build those projects
 * first and include those apks.
 */

package com.ebay.testdemultiplexer;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.calibration.CalibrationIO;
import com.ebay.testdemultiplexer.gui.MainWindow;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class ManualTestDemultiplexer {
	
	// Missing environment variable - message.
	private static final String MISSING_ENVIRONMENT_VARIABLE_MSG = 
			"Could not find environment variable ADB_HOME. Please make sure " +
			"you have defined ADB_HOME in your environment variable list.";
	
	// Missing environment variable - title.
	private static final String MISSING_ENVIRONMENT_VARIABLE_MSG_TITLE = 
			"MISSING ENVIRONMENT VARIABLE";
	
	// Environment variable exists but is not set correctly - message.
	private static final String ENVIRONMENT_VARIABLE_NOT_SET_MSG =
			"ADB_HOME was not set correctly. Please check the value of " +
			"ADB_HOME in your environment variable list.";
	
	// Environment variable exists but is not set correctly - title.
	private static final String ENVIRONMENT_VARIABLE_NOT_SET_MSG_TITLE = 
			"ADB_HOME NOT SET";
	
	// Environment variable does not reference a legitimate path - message.
	private static final String BAD_ENVIRONMENT_VARIABLE_PATH_MSG =
			"The path specified by ADB_HOME is not a legitimate path. Please " +
			"make sure the path referenced by ADB_HOME correctly references " +
			"adb executable.";
	
	// Environment variable does not reference a legitimate path - title.
	private static final String BAD_ENVIRONMENT_VARIABLE_PATH_TITLE =
			"INVALID ADB_HOME PATH";

	/**
	 * Entry point for standard execution.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Get the menu name right for Mac.
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	    System.setProperty(
	    		"com.apple.mrj.application.apple.menu.about.name", 
	    		TestDemultiplexerConstants.APP_NAME);
		
	    // Call this now to have it load the calibration profile.
	    // TestDeviceManager will make calls against CalibrationIO to see if
	    // calibration data exists.
	    CalibrationIO.getInstance();
	    
		String adbPath = findAdb();
		TestDeviceManager manager = new TestDeviceManager(adbPath);	
		manager.initializeADBConnection();
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final MainWindow mainWindow = new MainWindow(manager);
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	mainWindow.setVisible(true);
            }
        });
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------

	/**
	 * Get the adb path from the environment variable ADB_HOME.
	 * @return adb path.
	 */
    private static String findAdb() {
    	
    	String adbPath = null;
    	
    	// Initially try to retrieve the ADB_HOME environment variable.
    	try {
    		adbPath = System.getenv("ADB_HOME");
    	} catch(Exception e) {

    		JOptionPane.showMessageDialog(
    				null, 
    				MISSING_ENVIRONMENT_VARIABLE_MSG, 
    				MISSING_ENVIRONMENT_VARIABLE_MSG_TITLE, 
    				JOptionPane.WARNING_MESSAGE);
    		
    		e.printStackTrace();
    		System.exit(1);
    	}
    	
    	// Make sure ADB_HOME is a legitimate path.
    	try {
    		
	    	File f = new File(adbPath);
	    	
	    	// Make sure it exists, it contains adb and is executable.
	    	if (!f.exists()) {
	    		
	    		JOptionPane.showMessageDialog(
	    				null, 
	    				BAD_ENVIRONMENT_VARIABLE_PATH_MSG, 
	    				BAD_ENVIRONMENT_VARIABLE_PATH_TITLE, 
	    				JOptionPane.WARNING_MESSAGE);
	    		
	    		System.exit(1);
	    		
	    	} else if (!f.canExecute()) {

	    		JOptionPane.showMessageDialog(
	    				null, 
	    				BAD_ENVIRONMENT_VARIABLE_PATH_MSG, 
	    				BAD_ENVIRONMENT_VARIABLE_PATH_TITLE, 
	    				JOptionPane.WARNING_MESSAGE);
	    		
	    		System.exit(1);
	    		
	    	} else if (!f.getName().contains("adb")) {

	    		JOptionPane.showMessageDialog(
	    				null, 
	    				BAD_ENVIRONMENT_VARIABLE_PATH_MSG, 
	    				BAD_ENVIRONMENT_VARIABLE_PATH_TITLE, 
	    				JOptionPane.WARNING_MESSAGE);
	    		
	    		System.exit(1);
	    		
	    	}
	    	
    	} catch(NullPointerException npe) {
    		
    		JOptionPane.showMessageDialog(
    				null, 
    				ENVIRONMENT_VARIABLE_NOT_SET_MSG, 
    				ENVIRONMENT_VARIABLE_NOT_SET_MSG_TITLE, 
    				JOptionPane.WARNING_MESSAGE);
    		
    		System.exit(1);
    	}
       
    	return adbPath;
    }
}
