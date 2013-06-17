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
 * Class: TestDeviceManager
 * 
 * Description: 
 * Manages all of the connected devices. Looks for new connections and creates
 * the correct wrappers around each device connection. Also manages changes to
 * device state and device disconnections. Facilitates execution of input 
 * commands against each device.
 */

package com.ebay.testdemultiplexer.connection;

import java.util.ArrayList;
import java.util.Iterator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.ebay.testdemultiplexer.device.calibration.CalibrationData;
import com.ebay.testdemultiplexer.device.calibration.CalibrationIO;
import com.ebay.testdemultiplexer.device.commands.DeviceCommand;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandRecorder;

public class TestDeviceManager extends Thread implements IDeviceChangeListener {
	
	/** Path to adb executable. */
	private String adbPath;
	
	/** Index of the device that is providing the signaling input. */
	private int signalingDeviceIndex = -1;
	
	/** List of devices being managed. */
	private ArrayList<TestDevice> devices;
	
	/** List of listeners we need to notify of connection change events. */
	private ArrayList<TestDeviceConnectionListener> connectionListeners;
	
	/** Debug bridge instance. Used to query connected devices. */
	private AndroidDebugBridge bridge;
	
	/** Command recorder for tracking commands, and reading and writing them. */
	private CommandRecorder recorder;
	
	/** Current command step index being played (executed). */
	private int playbackIndex = 0;

	/**
	 * Default constructor.
	 * @param adbPath Path to adb executable.
	 */
	public TestDeviceManager(String adbPath) {
		
		connectionListeners = new ArrayList<TestDeviceConnectionListener>();
		
		devices = new ArrayList<TestDevice>();
		recorder = new CommandRecorder();
		
		this.adbPath = adbPath;
	}

	/**
	 * Initializes the AndroidDebugBridge and registers the TestDeviceManager
	 * with the AndroidDebugBridge device change listener. It is recommended
	 * that addConnectionListener(TestDeviceConnectionListener) be called with 
	 * all listeners before calling this method.
	 */
	public void initializeADBConnection() {
		
		// Get a device bridge instance. Initialize, create and restart.
		try {
			AndroidDebugBridge.init(false);
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			System.out.println("The IllegalStateException is not a show " +
			"stopper. It has been handled. This is just debug spew." +
			" Please proceed.");
		}

		bridge = AndroidDebugBridge.getBridge();
		
		if (bridge == null) {
	        bridge = AndroidDebugBridge.createBridge(
	                adbPath, 
	                false);
		}
		
		// Add the existing devices to the list of devices we are tracking.
		if (bridge.isConnected() && bridge.hasInitialDeviceList()) {
			IDevice[] connectedDevices = bridge.getDevices();
			
			for (int i = 0; i < connectedDevices.length; i++) {
				
				TestDevice tDevice = new TestDevice(connectedDevices[i]);
				
				devices.add(tDevice);
				notifyListenersAddedDevice(tDevice);
			}
		}
        
        AndroidDebugBridge.addDeviceChangeListener(this);
	}
	
	/**
	 * Shutdown the AndroidDebugBridge and clean up all connected devices.
	 */
	public void disconnect() {
		
		Iterator<TestDevice> iterator = devices.iterator();
		
		while (iterator.hasNext()) {
			
			TestDevice device = iterator.next();
			device.dispose();
		}
		
		AndroidDebugBridge.removeDeviceChangeListener(this);
		AndroidDebugBridge.terminate();
	}
	
	
	/**
	 * Add a new connection listener to the listener list. The listener will
	 * be notified of changes to the devices (connected, changed, disconnected).
	 * This should be called before initializeADBConnection().
	 * @param listener New listener to add.
	 */
	public void addConnectionListener(TestDeviceConnectionListener listener) {
		connectionListeners.add(listener);
		notifyNewListener(listener);
	}
	
	/**
	 * Get the total number of devices being managed.
	 * @return Total number of managed devices.
	 */
	public synchronized int getTotalDeviceCount() {
		
		return devices.size();
	}
	
	/**
	 * Get the TestDevice at the given index.
	 * @param index Index to retrieve.
	 * @return TestDevice located at that index, or null if not found.
	 */
	public synchronized TestDevice getDeviceAt(int index) {
		
		if (index < 0 || index >= devices.size()) {
			return null;
		}
		
		return devices.get(index);
	}
	
	/**
	 * Get the index of the specified TestDevice.
	 * @param device TestDevice index to retrieve.
	 * @return Index of TestDevice.
	 */
	public synchronized int getDeviceIndex(TestDevice device) {
		
		TestDevice tmpDevice;
		
		for (int i = 0; i < devices.size(); i++) {
			
			tmpDevice = devices.get(i);
			
			if (device.matchesSerialNumber(tmpDevice)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Toggle the state of receiving input for the device at the specified
	 * index. Notifies all listeners of device state change.
	 * 
	 * @param index Index of device to toggle receiving input state.
	 */
	public synchronized void toggleReceivingInput(int index) {
		
		if (index < 0) {
			return;
		} else if (index >= devices.size()) {
			return;
		}
		
		devices.get(index).setReceivingInput(
				!devices.get(index).getReceivingInput());
		
		notifyListenersChangeDevice(devices.get(index));
	}
	
	/**
	 * Increment the grouping index assigned to the device at specified index.
	 * @param index Index of the device to increment grouping for.
	 */
	public void incrementDeviceGrouping(int index) {
		
		if (index >= 0 && index < devices.size()) {
			devices.get(index).incrementGrouping();
			notifyListenersChangeDevice(devices.get(index));
		}
	}
	
	/**
	 * Set the index of the device that should be used for signaling.
	 * @param index Index of device to use for signaling.
	 */
	public void setSignalingDeviceIndex(int index) {
		this.signalingDeviceIndex = index;
		
		if (index >= 0 && index < devices.size()) {
			notifyListenersChangeDevice(devices.get(index));
		}
	}
	
	/**
	 * Get the index of the device that should be used for signaling.
	 * @return The index of the device to use for signaling.
	 */
	public int getSignalingDeviceIndex() {
		return signalingDeviceIndex;
	}
	
	/**
	 * Execute a DeviceCommand on the test devices receiving input and add it
	 * to the CommandRecorder.
	 * @param command Command to execute on devices receiving input.
	 */
	public synchronized void executeCommand(DeviceCommand command) {
		
		if (command == null) {
			return;
		}
		
		recorder.addCommand(command, false);
		
		TestDeviceCommandExecutionThread cmdThread = 
				new TestDeviceCommandExecutionThread(command, devices);
		cmdThread.start();		
		
		recorder.notifyListenerOfCommandExecuted(
				recorder.getRecorderLength()-1);		
	}
	
	/**
	 * Playback the next command in the command list. Reset to zero after
	 * last command is played.
	 */
	public synchronized void playCommands() {
		
		DeviceCommand command = recorder.getCommand(playbackIndex);
		
		if (command == null) {
			return;
		}

		TestDeviceCommandExecutionThread cmdThread = 
				new TestDeviceCommandExecutionThread(command, devices);
		cmdThread.start();		
		
		recorder.notifyListenerOfCommandExecuted(playbackIndex);

		playbackIndex++;
		
		if (playbackIndex >= recorder.getRecorderLength()) {
			playbackIndex = 0;
		}
	}
	
	/**
	 * Rewind the playback steps to 0 and update the gui.
	 */
	public void rewindCommandPlayback() {
		playbackIndex = 0;
		recorder.notifyListenerOfCommandExecuted(playbackIndex);
	}
	
	/**
	 * Access the command recorder instance.
	 * @return CommandRecorder instance.
	 */
	public CommandRecorder getCommandRecorder() {
		return recorder;
	}

	// -------------------------------------------------------------------------
	// Required by IDeviceChangeListener
	// -------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener#deviceConnected(com.android.ddmlib.IDevice)
	 */
	public void deviceConnected(IDevice arg0) {
		
		int counter = 0;
		int COUNTER_LIMIT = 5;
		
		// If the device is offline, wait for a period of time for it to come
		// online. If it never does come online, then do not try to add
		// the device.
		while (arg0.isOffline() && counter < COUNTER_LIMIT) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;
		}
		
		if (counter >= COUNTER_LIMIT) {
			return;
		}
		
		// Add the device.
		TestDevice tDevice = new TestDevice(arg0);
		
		// Attempt to get existing calibration data and add it to the TestDevice
		CalibrationData calibrationData = 
				CalibrationIO.getCalibrationData(arg0.getSerialNumber());
		
		if (calibrationData != null) {
			tDevice.setCalibrationData(calibrationData);
		}
		
		devices.add(tDevice);
		notifyListenersAddedDevice(tDevice);
	}
	
	/* (non-Javadoc)
	 * @see com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener#deviceChanged(com.android.ddmlib.IDevice, int)
	 */
	public void deviceChanged(IDevice arg0, int changeMask) {
		
		Iterator<TestDevice> iterator = devices.iterator();
		boolean inDeviceList = false;
		
		while(iterator.hasNext()) {
		
			TestDevice device = iterator.next();
			
			if (device.getSerialNumber().equals(arg0.getSerialNumber())) {
				notifyListenersChangeDevice(device);
				inDeviceList = true;
				break;
			}
		}
		
		// If the device changed state, check to see if it is offline or online
		// and handle accordingly.
		if (changeMask == IDevice.CHANGE_STATE) {
			
			if (arg0.isOffline() && inDeviceList) {
				
				// Remove it from the device list.
				for (int i = 0; i < devices.size(); i++) {
					
					TestDevice device = devices.get(i);
					
					if (device.getSerialNumber().equals(arg0.getSerialNumber())) {
						notifyListenersRemovedDevice(device);
						
						if (getDeviceIndex(device) == signalingDeviceIndex) {
							setSignalingDeviceIndex(-1);
						}
						
						device.dispose();
						devices.remove(device);
					}
				}
				
			} else if (arg0.isOnline() && !inDeviceList) {
			
				// Add the device.
				TestDevice tDevice = new TestDevice(arg0);
				
				// Attempt to get existing calibration data and add it to the 
				// TestDevice
				CalibrationData calibrationData = 
						CalibrationIO.getCalibrationData(arg0.getSerialNumber());
				
				if (calibrationData != null) {
					tDevice.setCalibrationData(calibrationData);
				}
				
				devices.add(tDevice);
				notifyListenersAddedDevice(tDevice);
				
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener#deviceDisconnected(com.android.ddmlib.IDevice)
	 */
	public void deviceDisconnected(IDevice arg0) {

		for (int i = 0; i < devices.size(); i++) {
			
			TestDevice device = devices.get(i);
			
			if (device.getSerialNumber().equals(arg0.getSerialNumber())) {
				notifyListenersRemovedDevice(device);
				
				if (getDeviceIndex(device) == signalingDeviceIndex) {
					setSignalingDeviceIndex(-1);
				}
				
				device.dispose();
				devices.remove(device);
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Notify a newly added listener of all existing devices.
	 * @param listener Listener that was just added.
	 */
	private synchronized void notifyNewListener(
			TestDeviceConnectionListener listener) {
		
		Iterator<TestDevice> iterator = devices.iterator();
		
		while(iterator.hasNext()) {
			
			TestDevice device = iterator.next();
			listener.onDeviceAddedEvent(device);
		}
	}
	
	/**
	 * Notify all of the listeners of the device that was added.
	 * @param device Device that was added.
	 */
	private synchronized void notifyListenersAddedDevice(TestDevice device) {
		
		Iterator<TestDeviceConnectionListener> iterator = 
				connectionListeners.iterator();
		
		while(iterator.hasNext()) {
			
			TestDeviceConnectionListener listener = iterator.next();
			listener.onDeviceAddedEvent(device);
		}
	}
	
	/**
	 * Notify all of the listeners of the device that changed status.
	 * @param device Device that changed status.
	 */
	private synchronized void notifyListenersChangeDevice(TestDevice device) {
		
		Iterator<TestDeviceConnectionListener> iterator = 
				connectionListeners.iterator();
		
		while(iterator.hasNext()) {
			
			TestDeviceConnectionListener listener = iterator.next();
			listener.onDeviceChangeEvent(device);
		}
	}
	
	/**
	 * Notify all of the listeners of the device that was removed.
	 * @param device Device that was removed.
	 */
	private synchronized void notifyListenersRemovedDevice(TestDevice device) {
		
		Iterator<TestDeviceConnectionListener> iterator =
				connectionListeners.iterator();
		
		while(iterator.hasNext()) {
			
			TestDeviceConnectionListener listener = iterator.next();
			listener.onDeviceRemovedEvent(device);
		}
	}
	
}
