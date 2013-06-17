package com.ebay.testdemultiplexer.uiautomator;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.ebay.testdemultiplexer.connection.TestDevice;

public class ThreadedUIViewTreeParser extends Thread implements 
	UIViewSAXParserListener {

	private TestDevice device;
	
	private ThreadedUIViewTreeParserListener listener;
	
	private UIViewTreeNode rootNode;
	
	private boolean isRunning;
	
	private static final String UIAUTOMATOR_PATH = "/system/bin/uiautomator";
	
	private static final String DUMP_COMMAND = "dump";
	
	private static final String XML_DEVICE_PATH = "/sdcard/uidump.xml";
	
	private static final String UIAUTOMATOR_DUMP_RESULT = 
			"UI hierchary dumped to: /sdcard/uidump.xml";
	
	private String XML_LOCAL_PATH = null;
	
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
		device.getIChimpDevice().shell("rm "+XML_DEVICE_PATH);
		
		// Get UI XML Snapshot
		result = device.getIChimpDevice().shell(
				UIAUTOMATOR_PATH + " " + DUMP_COMMAND + " " + XML_DEVICE_PATH);
		if (!result.trim().equals(UIAUTOMATOR_DUMP_RESULT)) {
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
