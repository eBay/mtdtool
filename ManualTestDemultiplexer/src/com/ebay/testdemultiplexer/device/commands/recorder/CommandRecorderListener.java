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
 * Interface: CommandRecorderListener
 * 
 * Description: 
 * Respond to changes in the CommandRecorder.
 */

package com.ebay.testdemultiplexer.device.commands.recorder;

import com.ebay.testdemultiplexer.device.commands.DeviceCommand;

public interface CommandRecorderListener {

	/** 
	 * Respond to a command being added to the recorder. 
	 */
	public void addCommand(DeviceCommand command);
	
	/** 
	 * Respond to the recorder being cleared. 
	 */
	public void commandRecorderCleared();
	
	/** 
	 * Respond to the recorder executing a command at the given index. 
	 */
	public void commandExecuted(int index);
}
