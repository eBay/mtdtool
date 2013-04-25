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
 * Interface: TestDeviceConnectionListener
 * 
 * Description: 
 * Interface for implementing TestDevice action listeners.
 * Notifies of device connections, changes and removals from TestDeviceManager.
 */

package com.ebay.testdemultiplexer.connection;

public interface TestDeviceConnectionListener {

	/**
	 * Notifies listener of add device events.
	 * @param device Newly added TestDevice.
	 */
	public void onDeviceAddedEvent(TestDevice device);
	
	/**
	 * Notifies listener of change device events.
	 * @param device Modified TestDevice.
	 */
	public void onDeviceChangeEvent(TestDevice device);
	
	/**
	 * Notifies listener of remove device events.
	 * @param device TestDeivce that was removed.
	 */
	public void onDeviceRemovedEvent(TestDevice device);
}
