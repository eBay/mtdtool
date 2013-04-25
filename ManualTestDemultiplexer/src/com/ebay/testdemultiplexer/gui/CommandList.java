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
 * Class: CommandList
 * 
 * Description:
 * GUI class for presenting the command list. Sets up a JList that can be added
 * to a container for displaying the command history of user inputs.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.commands.DeviceCommand;
import com.ebay.testdemultiplexer.device.commands.recorder.CommandRecorderListener;

public class CommandList extends JList implements CommandRecorderListener {
	
	/** Reference to the active TestDeviceManager. */
	private TestDeviceManager manager;
	
	/** Holds the list data. */
	private Vector<String> listData = new Vector<String>();

	/** Hold the list view in a scroll pane. */
	private JScrollPane commandScrollList;
	
	/** 
	 * Tracks the number of commands in the lists. Resets to 0 when list
	 * is cleared.
	 */
	private int counter = 0;
	
	/**
	 * Create a new instance of the CommandList using the active 
	 * TestDeviceManager.
	 * @param mananger Active TestDeviceManager.
	 * @param manager 
	 */
	public CommandList(TestDeviceManager manager) {
		
		super(new CommandListModel(manager));
		this.manager = manager;
		this.manager.getCommandRecorder().addCommandRecorderListener(this);
		initialize();
	}
	
	/**
	 * Get this Component with a JScrollPane.
	 * @return This list within a JScrollPane.
	 */
	public JScrollPane getScrollPane() {
		return commandScrollList;
	}

	// -------------------------------------------------------------------------
	// Methods required by CommandRecorderListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandRecorderListener#addCommand(com.ebay.testdemultiplexer.device.commands.DeviceCommand)
	 */
	public void addCommand(DeviceCommand command) {
		counter++;
		listData.add(""+counter+" "+command.toString());
		this.setListData(listData);
		commandScrollList.revalidate();
		commandScrollList.repaint();

		this.ensureIndexIsVisible(listData.size()-1);
	}

	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandRecorderListener#commandRecorderCleared()
	 */
	public void commandRecorderCleared() {
		counter = 0;
		listData.clear();
		this.setListData(listData);
		commandScrollList.revalidate();
		commandScrollList.repaint();
	}
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.device.commands.recorder.CommandRecorderListener#commandExecuted(int)
	 */
	public void commandExecuted(int index) {
		
		this.setSelectionInterval(index, index);
		this.ensureIndexIsVisible(index);
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Setup the GUI.
	 */
	private void initialize() {
		
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setLayoutOrientation(JList.VERTICAL);
		
		Dimension scrollSize = new Dimension(
				DeviceTable.TABLE_WIDTH+20, DeviceTable.TABLE_HEIGHT+20);
		commandScrollList = new JScrollPane();
		commandScrollList.setSize(scrollSize);
		commandScrollList.setMinimumSize(scrollSize);
		commandScrollList.setPreferredSize(scrollSize);
		commandScrollList.setMaximumSize(scrollSize);
		commandScrollList.getViewport().add(this);
	}

}
