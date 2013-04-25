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
 * Class: CommandListModel
 * 
 * Description:
 * ListModel implementation behind the CommandList GUI class.
 */

package com.ebay.testdemultiplexer.gui;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;

public class CommandListModel implements ListModel {
	
	/** Reference to active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/**
	 * Create a new CommandListModel. Requires the active TestDeviceManager.
	 * @param manager Active TestDeviceManager.
	 */
	public CommandListModel(TestDeviceManager manager) {
		
		this.manager = manager;
	}
	
	// -------------------------------------------------------------------------
	// Methods required by ListModel
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	public void addListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int arg0) {

		return manager.getCommandRecorder().getCommand(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return manager.getCommandRecorder().getRecorderLength();
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	public void removeListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub
	}

}
