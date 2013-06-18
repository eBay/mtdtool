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
 * Class: UnlockDeviceCommand
 * 
 * Description: 
 * Definition of a device unlock event for any given device. Expectation is
 * that the DeviceUnlock.apk is available. This comes from the DeviceUnlock
 * project which is an adjacent project. In order for device unlock to work 
 * all Android devices should be setup with the pin code 1234.
 */

package com.ebay.testdemultiplexer.device.commands;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class UnlockDeviceCommand extends DeviceCommand implements 
	CommandSerializer, CommandDeserializer {
	
	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "UNLOCK_DEVICE_COMMAND";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 1;
	
	/**
	 * Default constructor.
	 */
	public UnlockDeviceCommand() {
		
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#execute(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void execute(TestDevice device) {

		// Try first by installing DeviceUnlock.apk if not already installed.
		// Then run DeviceUnlock to unlock the device.
		// Then try unlocking any pin security. Expect pin to be 1234.
		String result = device.getIChimpDevice().shell(
				"pm path com.ebay.deviceunlock");
		
		if (result.equals("")) {
			device.getIChimpDevice().installPackage("DeviceUnlock.apk");
		}
		
		device.getIChimpDevice().startActivity(
				null, 
				null, 
				null, 
				null, 
				new ArrayList<String>(), 
				new HashMap<String, Object>(), 
				"com.ebay.deviceunlock/.MainActivity", 
				0);
		
		// Wait for the activity to launch.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		device.getIChimpDevice().press("KEYCODE_BACK", TouchPressType.DOWN_AND_UP);
		device.getIChimpDevice().shell("input text 1234");
		device.getIChimpDevice().shell("input keyevent 66");
		
	}
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer#deserializeCommand(java.lang.String)
	 */
	public boolean deserializeCommand(String data) {
		
		String[] tokens = 
				data.split(TestDemultiplexerConstants.SERIAL_SEPARATOR);
		
		if (tokens.length != NUM_SERIAL_TOKENS) {
			return false;
		} else if (!tokens[0].equals(SERIALIZED_KEY)) {
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer#serializeCommand()
	 */
	public String serializeCommand() {
		
		String serialized = SERIALIZED_KEY;
		
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
