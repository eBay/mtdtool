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
 * Interface: DeviceCommand
 * 
 * Description: 
 * Interface that defines the command execution method for all MTD commands.
 */

package com.ebay.testdemultiplexer.device.commands;

import com.ebay.testdemultiplexer.connection.TestDevice;

public abstract class DeviceCommand {

	/**
	 * Execute the command on the TestDevice's IChimpDevice.
	 * @param device TestDevice to execute on.
	 */
	public void executeCommand(TestDevice device) {
		
		// Execute the command.
		execute(device);
		
		// Get the updated view hierarchy. Wait for new root node before
		// requesting another one. Avoids multiple threads running
		// concurrently when user is jamming on buttons etc.
		if (device.getUIViewTreeManager().deviceSupportsUIAutomation()) {
			device.getUIViewTreeManager().waitForNewRootNode();
			device.getUIViewTreeManager().dumpUIHierarchy();
		}
		
	}
	
	/**
	 * Perform command specific operations on TestDevice's IChimpDevice.
	 * @param device TestDevice to execute on.
	 */
	protected abstract void execute(TestDevice device);
}
