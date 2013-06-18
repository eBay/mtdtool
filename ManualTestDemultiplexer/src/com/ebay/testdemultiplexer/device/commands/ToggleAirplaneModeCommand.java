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
 * Class: ToggleAirplaneModeCommand
 * 
 * Description: 
 * Definition of a toggle airplane mode event for any given device. Requires the
 * ToggleAirplaneMode.apk to execute. This is an adjacent project 
 * (ToggleAirplaneMode).
 */

package com.ebay.testdemultiplexer.device.commands;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class ToggleAirplaneModeCommand extends DeviceCommand implements
		CommandSerializer, CommandDeserializer {
	
	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "TOGGLE_AIRPLANE_MODE_COMMAND";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 1;
	
	/**
	 * Default constructor.
	 */
	public ToggleAirplaneModeCommand() {
		
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
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#execute(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void execute(TestDevice device) {

		// Try first by installing ToggleAirplaneMode.apk if not already 
		// installed.
		// Then run ToggleAirplaneMode to change the airplane mode setting.
		String result = device.getIChimpDevice().shell(
				"pm path com.ebay.toggleairplanemode");
		
		if (result.equals("")) {
			device.getIChimpDevice().installPackage("ToggleAirplaneMode.apk");
		}
		
		device.getIChimpDevice().startActivity(
				null, 
				null, 
				null, 
				null, 
				new ArrayList<String>(), 
				new HashMap<String, Object>(), 
				"com.ebay.toggleairplanemode/.ToggleAirplaneModeActivity", 
				0);
		
		// Wait for the activity to launch.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		device.getIChimpDevice().press("KEYCODE_BACK", TouchPressType.DOWN_AND_UP);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return SERIALIZED_KEY;
	}
}
