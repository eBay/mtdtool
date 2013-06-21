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
 * Class: ThreadedUIViewTreeParser
 * 
 * Description: 
 * Threaded class that handles the instantiation of the UIViewSAXParser and
 * performs the parsing of the UIAutomation dump file independent of the current
 * execution thread.
 */

package com.ebay.testdemultiplexer.uiautomator;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.ebay.testdemultiplexer.connection.TestDevice;

public class ThreadedUIViewTreeParser extends Thread implements 
	UIViewSAXParserListener {
	
	/** Path of the UIAutomation executable on the physical device. */
	private static final String UIAUTOMATOR_PATH = "/system/bin/uiautomator";
	
	/** UIAutomation command to execute. */
	private static final String DUMP_COMMAND = "dump";
	
	/** Location on the physical device to dump the xml file. */
	private static final String XML_DEVICE_PATH = "/sdcard/uidump.xml";
	
	/** Shell response to identify that the dump operation was successful. */
	private static final String UIAUTOMATOR_DUMP_RESULT = 
			"UI hierchary dumped to: /sdcard/uidump.xml";
	
	/** 
	 * Local path to the pulled xml file. Saved with the device serial number 
	 * appended so it is unique.
	 */
	private String XML_LOCAL_PATH = null;

	/** TestDevice to pull the UIAutomation dump file from. */
	private TestDevice device;
	
	/** Listener to notify when parsing is complete. */
	private ThreadedUIViewTreeParserListener listener;
	
	/** Root node from the UIViewSAXParser. */
	private UIViewTreeNode rootNode;
	
	/** Execution state flag for the thread loop. */
	private boolean isRunning;
	
	/**
	 * Create a new threaded parser.
	 * @param device Device to parse view from.
	 * @param listener Listener to notify when parsing is finished.
	 */
	public ThreadedUIViewTreeParser(
			TestDevice device, ThreadedUIViewTreeParserListener listener) {
		rootNode = null;
		isRunning = false;
		this.device = device;
		this.listener = listener;
		XML_LOCAL_PATH = "./uidump"+device.getSerialNumber()+".xml";
	}
	
	/**
	 * Check if the thread is running.
	 * @return Will return true if the thread has been started and is still 
	 * running. False otherwise.
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	// -------------------------------------------------------------------------
	// Methods required by Thread
	// -------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public synchronized void run() {
		
		// The operations performed at the top of this thread are the same
		// shell commands executed in ScreenshotAction.java of the uiautomator
		// module. See run() implementation of ScreenshotAction.java for more
		// information.
		
		isRunning = true;
		String result;
		
		// Confirm that uiautomator exists on the device. Requires API level
		// 16 or newer.
		result = device.getIChimpDevice().shell("ls "+UIAUTOMATOR_PATH);
		if (!result.trim().equals(UIAUTOMATOR_PATH)) {
			doCleanup();
			return;
		}
		
		// Cleanup any existing UI XML Snapshot
		result = device.getIChimpDevice().shell("rm "+XML_DEVICE_PATH);
		
		// Get UI XML Snapshot
		try {
			result = device.getIChimpDevice().shell(
				UIAUTOMATOR_PATH + " " + DUMP_COMMAND + " " + XML_DEVICE_PATH);
		} catch (Exception e) {
			System.out.println("MTD Tool failed to get the UIAutomation dump.");
			e.printStackTrace();
		}
		
		if (result == null || !result.trim().equals(UIAUTOMATOR_DUMP_RESULT)) {
			doCleanup();
			return;
		}
		
		// Pull the output file.
		try {
			device.getIDevice().pullFile(XML_DEVICE_PATH, XML_LOCAL_PATH);
		} catch (SyncException e) {
			e.printStackTrace();
			doCleanup();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			doCleanup();
			return;
		} catch (AdbCommandRejectedException e) {
			e.printStackTrace();
			doCleanup();
			return;
		} catch (TimeoutException e) {
			e.printStackTrace();
			doCleanup();
			return;
		}
		
		// Parse the output file.
		UIViewSAXParser parser = new UIViewSAXParser(XML_LOCAL_PATH, this);
		parser.beginParsing();
	}
	
	// -------------------------------------------------------------------------
	// Methods required by UIViewSAXParserListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.util.UIViewSAXParserListener#doneParsingXML(com.ebay.testdemultiplexer.util.UIViewTreeNode)
	 */
	@Override
	public void doneParsingXML(UIViewTreeNode rootNode) {
		this.rootNode = rootNode;
		doCleanup();
	}

	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Do any cleanup required to stop execution of the thread and set all
	 * fields their expected values for post operation analysis.
	 */
	private void doCleanup() {
		isRunning = false;
		notifyListener();
	}
	
	/**
	 * Send the notification that parsing is finished and hand back the root
	 * node and device serial number. RootNode may be null.
	 */
	private void notifyListener() {
		listener.doneParsingTreeView(rootNode, device.getSerialNumber());
	}
}
