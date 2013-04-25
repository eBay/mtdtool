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
 * Class: StartActivityCommand
 * 
 * Description: 
 * Definition of a start apk activity event for any given device.
 */

package com.ebay.testdemultiplexer.device.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandDeserializer;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer;
import com.ebay.testdemultiplexer.util.TestDemultiplexerConstants;

public class StartActivityCommand implements 
	DeviceCommand, CommandSerializer, CommandDeserializer {

	/** Serialization key identifier. */
	public static final String SERIALIZED_KEY = "START_ACTIVITY_COMMAND";
	
	/** Number of serialized tokens to expect. */
	private static final int NUM_SERIAL_TOKENS = 9;
	
	/** URI for the Intent (can be null). */
	private String uri;
	
	/** Action for the Intent (can be null). */
	private String action;
	
	/** Data URI for the Intent (can be null). */
	private String data;
	
	/** Mime type for the Intent (can be null). */
	private String mimeType;
	
	/** Category names for the Intent (can be null). */
	private Collection<String> categories;
	
	/** Extras to add to the Intent. */
	private Map<String, Object> extras;
	
	/** Component of the Intent (can be null). */
	private String component;
	
	/** Flags for the Intent. */
	private int flags;
	
	/**
	 * Default constructor should only be used when deserializing data.
	 */
	public StartActivityCommand() {
		
	}
	
	/**
	 * Create a Start Activity command.
	 * @param uri The URI for the Intent (Can be null).
	 * @param action The action for the Intent (Can be null).
	 * @param data The data URI for the Intent (Can be null).
	 * @param mimeType The mime type for the Intent (Can be null).
	 * @param categories The category names for the Intent.
	 * @param extras The extras to add to the Intent. (NOT SERIALIZABLE)
	 * @param component The component of the Intent (Can be null).
	 * @param flags The flags for the Intent.
	 */
	public StartActivityCommand(
			String uri, 
			String action, 
			String data, 
			String mimeType, 
			Collection<String> categories, 
			Map<String, Object> extras, 
			String component, 
			int flags) {
		
		this.uri = uri;
		this.action = action;
		this.data = data;
		this.mimeType = mimeType;
		this.categories = categories;
		this.extras = extras;
		this.component = component;
		this.flags = flags;
	}
	
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.DeviceCommand#executeCommand(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void executeCommand(TestDevice device) {
		device.getIChimpDevice().startActivity(
				uri, 
				action, 
				data, 
				mimeType, 
				categories, 
				extras, 
				component, 
				flags);
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
		
		this.uri = tokens[1];
		if (this.uri.equals("null")){
			this.uri = null;
		}
		
		this.action = tokens[2];
		if (this.action.equals("null")) {
			this.action = null;
		}
		
		this.data = tokens[3];
		if (this.data.equals("null")) {
			this.data = null;
		}

		this.mimeType = tokens[4];
		if (this.mimeType.equals("null")) {
			this.mimeType = null;
		}
		
		String categoryString = tokens[5];
		String[] categoryTokens = categoryString.split(",");
		categories = new ArrayList<String>();
		categories.clear();
		for (int i = 0; i < categoryTokens.length; i++) {
			if (categoryTokens[i].length() > 0)
				categories.add(categoryTokens[i]);
		}
		
		// TODO: deserialize more sophisticated serialization input
		this.extras = new HashMap<String, Object>();
		
		this.component = tokens[7];
		if (this.component.equals("null")) {
			this.component = null;
		}
		
		this.flags = Integer.valueOf(tokens[8]);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandSerializer#serializeCommand()
	 */
	public String serializeCommand() {
		
		String serialized = SERIALIZED_KEY;
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (uri == null) {
			serialized += "null";
		} else {
			serialized += uri;
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (action == null) {
			serialized += "null";
		} else {
			serialized += action;
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (data == null) {
			serialized += "null";
		} else {
			serialized += data;
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (mimeType == null) {
			serialized += "null";
		} else {
			serialized += mimeType;
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		Iterator<String> iterator = categories.iterator();
		while (iterator.hasNext()) {
			
			serialized += iterator.next();
			
			if (iterator.hasNext()) {
				serialized += ",";
			}
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		// TODO: make a more sophisticated serialization of extras.
		serialized += "null";
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		if (component == null) {
			serialized += "null";
		} else {
			serialized += component;
		}
		
		serialized += TestDemultiplexerConstants.SERIAL_SEPARATOR;
		
		serialized += flags;
		
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
