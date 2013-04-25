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
 * Class: DeviceTableModel
 * 
 * Description:
 * TableModel implementation behind the DeviceTable GUI class.
 */

package com.ebay.testdemultiplexer.gui;

import javax.swing.table.AbstractTableModel;

import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener;
import com.ebay.testdemultiplexer.connection.TestDeviceManager;

public class DeviceTableModel extends AbstractTableModel 
	implements TestDeviceConnectionListener {

	/** Column headers. */
	private static final String[] COLUMN_HEADERS = 
		{"Device", "Target", "Group"};
	
	/** Reference to TestDeviceManager we are basing table data on. */
	private TestDeviceManager manager;
	
	/**
	 * Create a new DeviceTableModel. Requires the active TestDeviceManager.
	 * @param manager Active TestDeviceManager.
	 */
	public DeviceTableModel(TestDeviceManager manager) {
		
		super();
		this.manager = manager;
		this.manager.addConnectionListener(this);
	}

	// -------------------------------------------------------------------------
	// Methods required/overridden in AbstractTableModel
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {

		return COLUMN_HEADERS[column];
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_HEADERS.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return manager.getTotalDeviceCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		
		TestDevice device = manager.getDeviceAt(row);
		
		if (device == null) {
			return null;
		}
		
		switch (column) {
		
		case 0:
			
			String label = "";
			String name = device.getModelName();
			String os = device.getAndroidOSVersion();
			
			if (name != null && os != null) {
				label = name + " ("+os+")";
			} else {
				label = device.getSerialNumber();
			}
			
			return label;
			
		case 1:
			return Boolean.toString(device.getReceivingInput());
			
		case 2:
			return device.getGrouping();
			
		default:
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// Handlers for TestDeviceConnectionListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener#onDeviceAddedEvent(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void onDeviceAddedEvent(TestDevice device) {
		this.fireTableRowsInserted(
				manager.getTotalDeviceCount()-1, 
				manager.getTotalDeviceCount()-1);
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener#onDeviceChangeEvent(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void onDeviceChangeEvent(TestDevice device) {
		
		int row = manager.getDeviceIndex(device);
		
		this.fireTableRowsUpdated(row, row);
		
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.connection.TestDeviceConnectionListener#onDeviceRemovedEvent(com.ebay.testdemultiplexer.connection.TestDevice)
	 */
	public void onDeviceRemovedEvent(TestDevice device) {
		
		int row = manager.getDeviceIndex(device);
		this.fireTableRowsDeleted(row, row);
	}
	
}
