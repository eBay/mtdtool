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
 * Class: PressCommand
 * 
 * Description: 
 * Definition of a physical button press event for any given device.
 */

package com.ebay.testdemultiplexer.device.commands;

import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class PressCommand extends DeviceCommand implements 
	CommandSerializer, CommandDeserializer {
	
	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "PRESS_COMMAND";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 4;

	/** Physical button to press. May be null depending on constructor used. */
	private PhysicalButton button;
	
	/** Name of button to press. May be null depending on constructor used. */
	private String buttonName;
	
	/** Type of press to use with button press. */
	private TouchPressType touchType;
	
	/**
	 * Default constructor should only be used when deserializing data.
	 */
	public PressCommand() {
		
	}
	
	/**
	 * Create a new Press command.
	 * @param button Physical button to press.
	 * @param touchType Type of button press.
	 */
	public PressCommand(PhysicalButton button, TouchPressType touchType) {
		
		this.button = button;
		this.buttonName = null;
		this.touchType = touchType;
	}
	
	/**
	 * Create a new Press command.
	 * @param buttonName Name of button to press.
	 * @param touchType Type of button press.
	 */
	public PressCommand(String buttonName, TouchPressType touchType) {
		
		this.button = null;
		this.buttonName = buttonName;
		this.touchType = touchType;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#execute(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void execute(TestDevice device) {

		if (button != null) {
			device.getIChimpDevice().press(button, touchType);
		} else {
			device.getIChimpDevice().press(buttonName, touchType);
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
		
		if (tokens[1].equals("null")) {
			button = null;
		} else {
			button = PhysicalButton.valueOf(tokens[1]);
		}
		
		buttonName = tokens[2];
		if (buttonName.equals("null")) {
			buttonName = null;
		}
		
		touchType = TouchPressType.fromIdentifier(tokens[3]);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer#serializeCommand()
	 */
	public String serializeCommand() {
		
		String serialized = SERIALIZED_KEY;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (button != null) {
			serialized += button.getKeyName();
		} else {
			serialized += "null";
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (buttonName != null) {
			serialized += buttonName;
		} else {
			serialized += "null";
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		serialized += touchType.getIdentifier();
		
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
