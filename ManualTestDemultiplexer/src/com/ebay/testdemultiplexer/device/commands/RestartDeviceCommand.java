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
 * Class: RestartDeviceCommand
 * 
 * Description: 
 * Definition of a restart device event for any given device.
 */

package com.ebay.testdemultiplexer.device.commands;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.TimeoutException;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class RestartDeviceCommand implements 
	DeviceCommand, CommandSerializer, CommandDeserializer {
	
	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "RESTART_COMMAND";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 1;
	
	/**
	 * Default constructor.
	 */
	public RestartDeviceCommand() {
		
	}
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#executeCommand(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void executeCommand(TestDevice device) {
		
		try {
			device.getIDevice().reboot(null);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		return SERIALIZED_KEY;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return SERIALIZED_KEY;
	}

}
