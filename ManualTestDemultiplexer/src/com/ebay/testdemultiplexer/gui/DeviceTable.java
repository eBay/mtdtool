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
 * Class: DeviceTable
 * 
 * Description:
 * GUI JTable with device connection information. Has a method to retrieve a
 * JScrollPane wrapper for the table.
 * 
 * GUI class for providing UI device table elements. This table lists the
 * connected devices and facilitates device targeting operations, active view
 * selection, getting device info and device grouping.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.android.ddmlib.IDevice;
import com.ebay.testdemultiplexer.connection.TestDevice;
import com.ebay.testdemultiplexer.connection.TestDeviceManager;

public class DeviceTable extends JTable implements MouseListener {
	
	/** Table width. */
	public static int TABLE_WIDTH = 360;
	
	/** Table height. */
	public static int TABLE_HEIGHT = 130;
	
	/** Reference to the active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Scroll pane wrapper around table. */
	private JScrollPane deviceScrollList;
	
	/** Enable/Disable all menu item option. */
	private JMenuItem enableDisableAll;
	
	/** Enable/Disable all except selected menu item option. */
	private JMenuItem enableDisableAllButSelect;
	
	/** Enable/Disable all belong to group. */
	private JMenuItem enableDisableGroup;
	
	/** Enable/Disable all but those belonging to group. */
	private JMenuItem enableDisableAllButGroup;
	
	/** Pop up dialog with device specific info. */
	private JMenuItem getDeviceInfo;

	/**
	 * Create a new DeviceTable with a reference to the active
	 * TestDeviceManager.
	 * @param manager Active TestDeviceManager.
	 */
	public DeviceTable(TestDeviceManager manager) {
		
		super(new DeviceTableModel(manager));
		
		this.manager = manager;
		
		// Set this to single selection mode and set the width of each of the
		// columns.
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.getColumnModel().getColumn(0).setPreferredWidth(150);
		this.getColumnModel().getColumn(1).setPreferredWidth(20);
		this.getColumnModel().getColumn(2).setPreferredWidth(30);
		
		// Setup the right click menu options
		JPopupMenu popUpMenu = new JPopupMenu();
		enableDisableAll = new JMenuItem("Enable/Disable All");
		enableDisableAll.addMouseListener(this);
		popUpMenu.add(enableDisableAll);
		
		enableDisableAllButSelect = 
				new JMenuItem("Enable/Disable All But Selected");
		enableDisableAllButSelect.addMouseListener(this);
		popUpMenu.add(enableDisableAllButSelect);
		
		enableDisableGroup = new JMenuItem("Enable/Disable Group");
		enableDisableGroup.addMouseListener(this);
		popUpMenu.add(enableDisableGroup);
		
		enableDisableAllButGroup = 
				new JMenuItem("Enable/Disable All Other Groups");
		enableDisableAllButGroup.addMouseListener(this);
		popUpMenu.add(enableDisableAllButGroup);
		
		getDeviceInfo = new JMenuItem("Get Device Info");
		getDeviceInfo.addMouseListener(this);
		popUpMenu.add(getDeviceInfo);
		
		this.setComponentPopupMenu(popUpMenu);
		
		// Setup the scroll pane for the table
		Dimension deviceScrollSize = new Dimension(
				DeviceTable.TABLE_WIDTH+20, DeviceTable.TABLE_HEIGHT+20);
		deviceScrollList = new JScrollPane();
		deviceScrollList.setSize(deviceScrollSize);
		deviceScrollList.setMinimumSize(deviceScrollSize);
		deviceScrollList.setPreferredSize(deviceScrollSize);
		deviceScrollList.setMaximumSize(deviceScrollSize);
		deviceScrollList.getViewport().add(this);
		
		// Respond to cell clicking.
		addMouseListener(this);

	}
	
	/**
	 * Get the scroll pane container for this list.
	 * @return JScrollPane component wrapping this JList.
	 */
	public JScrollPane getScrollPane() {
		return deviceScrollList;
	}

	// -------------------------------------------------------------------------
	// Methods required by MouseListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		
		if (e.getSource() == enableDisableAll) {
			
			boolean state = setTrueOrFalse(-1);
			setDeviceInputStates(state, -1);
			
		} else if (e.getSource() == enableDisableAllButSelect) {
			
			int selected = manager.getSignalingDeviceIndex();
			boolean state = setTrueOrFalse(selected);
			setDeviceInputStates(state, selected);
			
		} else if (e.getSource() == enableDisableGroup) {
			
			toggleDeviceGroupInputStates(false);
			
		} else if (e.getSource() == enableDisableAllButGroup) {
			
			toggleDeviceGroupInputStates(true);
			
		} else if (e.getSource() == getDeviceInfo) {
			
			int selected = manager.getSignalingDeviceIndex();
			
			// Avoid Null Pointer Exception.
			if (selected < 0) {
				return;
			}
			
			IDevice device = manager.getDeviceAt(selected).getIDevice();
			
			
			String deviceInfo = 
					"Device info for: " + 
					device.getProperty("ro.product.model") + "\n";
			
			deviceInfo +=
					"Manufacturer: " + 
					device.getProperty("ro.product.manufacturer") + "\n";
			
			deviceInfo +=
					"Serial no: " +
					manager.getDeviceAt(selected).getSerialNumber() + "\n";
			
			deviceInfo += 
					"Hardware: " + 
					device.getProperty("ro.hardware") + "\n";
			
			deviceInfo += "CPU: " +
					device.getProperty("ro.product.cpu.abi") + "\n";
			
			deviceInfo += "LCD Density: " +
					device.getProperty("ro.sf.lcd_density") + "\n";
			
			deviceInfo += "Screen Width: " +
					manager.getDeviceAt(selected).getScreenWidth() + "\n";
			
			deviceInfo += "Screen Height: " +
					manager.getDeviceAt(selected).getScreenHeight() + "\n";
			
			deviceInfo += "Android OS: " +
					device.getProperty("ro.build.version.release") + "\n";
			
			deviceInfo += "Country Code: " +
					device.getProperty("ro.csc.country_code") + "\n";
			
			deviceInfo += "Carrier: " +
					device.getProperty("ro.product.brand") + "\n";
			
			String netcfgData = 
					manager.getDeviceAt(selected).getIChimpDevice().shell(
							"netcfg");
			deviceInfo += "\nnetcfg:\n" + netcfgData + "\n";
			
			// Print all properties to console.
			if (device.getProperties() != null) {
				Map<String, String> properties = 
						(Map<String, String>) device.getProperties();
				
				Object[] keys = properties.keySet().toArray();
				
				for (int i = 0; i < keys.length; i++) {
					System.out.println("--- "+(String)keys[i]+ " :: "+
							properties.get((String)keys[i]));
				}
			}
			
			JOptionPane.showMessageDialog(
					null, 
					deviceInfo, 
					"Device Properties", 
					JOptionPane.INFORMATION_MESSAGE);
			
			
		} else if (e.getButton() == MouseEvent.BUTTON1) {
		
			int row = this.rowAtPoint(e.getPoint());
	        int col = this.columnAtPoint(e.getPoint());
	        
	        // Always set the device to the selected row.
	        manager.setSignalingDeviceIndex(row);
	        
	        if (col == 1) {
	        	manager.toggleReceivingInput(row);
	        } else if (col == 2) {
	        	manager.incrementDeviceGrouping(row);
	        }
		}
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------

	/**
	 * Set the receiving input state for all devices with the possible 
	 * exception of the device at indexException.
	 * @param state State to set devices to (True or False)
	 * @param indexException Optional device index to not include in setting
	 * change.
	 */
	private void setDeviceInputStates(boolean state, int indexException) {
		
		for (int i = manager.getTotalDeviceCount()-1; i >= 0; i--) {
			
			if (i == indexException) {
				continue;
			}
			
			if (manager.getDeviceAt(i).getReceivingInput() != state) {
				manager.toggleReceivingInput(i);
			}
		}
	}
	
	/**
	 * Toggle the device group input state for either the selected device
	 * group, or optionally the other groups besides the selected device's 
	 * group.
	 * @param others True to set other groups, false to set selected device's
	 * group.
	 */
	private void toggleDeviceGroupInputStates(boolean others) {
		
		// Avoid null pointer exception.
		if (manager.getSignalingDeviceIndex() < 0) {
			return;
		}
		
		String selectedGrouping = 
				manager.getDeviceAt(
						manager.getSignalingDeviceIndex()).getGrouping();
		
		TestDevice tmpDevice = null;
		
		boolean state = false;
		
		// Loop through all devices and determine if they should be set to
		// true or false. If any are false, set all to true.
		for (int i = manager.getTotalDeviceCount()-1; i >= 0; i--) {
			
			tmpDevice = manager.getDeviceAt(i);
			
			if (others) {
				
				if (!tmpDevice.getGrouping().equals(selectedGrouping)) {
					
					if (tmpDevice.getReceivingInput() == false) {
						state = true;
						break;
					}
				}
				
			} else {
				
				if (tmpDevice.getGrouping().equals(selectedGrouping)) {
					
					if (tmpDevice.getReceivingInput() == false) {
						state = true;
						break;
					}
				}
			}
		}
		
		// Loop through all devices and set them to state.
		for (int i = manager.getTotalDeviceCount()-1; i >= 0; i--) {
			
			tmpDevice = manager.getDeviceAt(i);
			
			if (others) {
				
				if (!tmpDevice.getGrouping().equals(selectedGrouping)) {
					
					if (tmpDevice.getReceivingInput() != state) {
						manager.toggleReceivingInput(i);
					}
				}
				
			} else {
				
				if (tmpDevice.getGrouping().equals(selectedGrouping)) {
					
					if (tmpDevice.getReceivingInput() != state) {
						manager.toggleReceivingInput(i);
					}
				}
			}
		}
	}
	
	/**
	 * Here we look for any device with receiving input set to false.
	 * If we find one, then we are setting all to true, so we return True.
	 * If we don't find one set to false, then we set all to false so 
	 * we return False.
	 * @param indexException Optional device index to not include in check
	 * @return True to set all to true, false to set all to false.
	 */
	private boolean setTrueOrFalse(int indexException) {
		
		// Work backwards through the loop in the event that one or more
		// of the devices are disconnected we still catch them all.
		for (int i = manager.getTotalDeviceCount()-1; i >= 0; i--) {
			
			if (i == indexException) {
				continue;
			}
			
			if (!manager.getDeviceAt(i).getReceivingInput()) {
				// We found our device set to false, so set all to true.
				return true;
			}
		}
		
		// We did not find any devices set to false, so set all to false.
		return false;
	}
}
