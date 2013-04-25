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
 * Class: TestDeviceCommandExecutionThread
 * 
 * Description: 
 * Threaded execution of a command against the list of attached devices. The 
 * command will only be executed on devices that are targeted for receiving
 * input. Long running command executions will display a progress dialog for 
 * the user.
 */

package com.ebay.testdemultiplexer.connection;

import java.util.ArrayList;

import com.ebay.testdemultiplexer.device.commands.DeviceCommand;
import com.ebay.testdemultiplexer.device.commands.DragCommand;
import com.ebay.testdemultiplexer.device.commands.InstallPackageCommand;
import com.ebay.testdemultiplexer.device.commands.RemovePackageCommand;
import com.ebay.testdemultiplexer.device.commands.StartActivityCommand;
import com.ebay.testdemultiplexer.device.commands.ToggleAirplaneModeCommand;
import com.ebay.testdemultiplexer.device.commands.UnlockDeviceCommand;
import com.ebay.testdemultiplexer.gui.ProgressDialogWindow;

public class TestDeviceCommandExecutionThread extends Thread {

	/** DeviceCommand to execute. */
	private DeviceCommand command;
	
	/** List of the devices to execute command on. */
	private ArrayList<TestDevice> devices;
	
	/** Tracks the execution state of the thread. */
	private boolean running = false;
	
	/** 
	 * Track the progress of the thread's execution of the command on each
	 * device.
	 */
	private int progressCounter = 0;
	
	/**
	 * Default constructor.
	 * @param command Command to execute.
	 * @param devices Device list to execute against.
	 */
	public TestDeviceCommandExecutionThread(
			DeviceCommand command,
			ArrayList<TestDevice> devices) {
		
		this.command = command;
		this.devices = new ArrayList<TestDevice>(devices);
		running = true;
	}
	
	/**
	 * Check if the thread is running.
	 * @return True if running, false otherwise.
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Get the progress of the thread.
	 * @return Float percentage of progress complete.
	 */
	public float getProgress() {
		
		return ((float)progressCounter/(float)devices.size()*100.0f);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		
		if (command instanceof InstallPackageCommand || 
				command instanceof RemovePackageCommand || 
				command instanceof StartActivityCommand || 
				command instanceof ToggleAirplaneModeCommand ||
				command instanceof UnlockDeviceCommand ||
				command instanceof DragCommand) {
			
			ProgressDialogWindow.getInstance().setProgress(0);
			ProgressDialogWindow.getInstance().setVisible(true);
			ProgressDialogWindow.getInstance().setMessage(
					"Please wait, executing\n"+command.toString()+ "...");
		}

		for (int i = devices.size()-1; i >= 0; i--) {

			TestDevice tDevice = devices.get(i);
			
			if (tDevice.getReceivingInput()) {
				
				try {
					command.executeCommand(tDevice);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
			progressCounter++;
			ProgressDialogWindow.getInstance().setProgress((int)getProgress());
		}
		
		running = false;
		ProgressDialogWindow.getInstance().setVisible(false);
	}
}
