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
 * Class: CommandRecorder
 * 
 * Description: 
 * Record commands to be able to replay them, save them and reload them later.
 */

package com.ebay.testdemultiplexer.device.commands.recorder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.ebay.testdemultiplexer.device.commands.DeviceCommand;
import com.ebay.testdemultiplexer.device.commands.DragCommand;
import com.ebay.testdemultiplexer.device.commands.InstallPackageCommand;
import com.ebay.testdemultiplexer.device.commands.PressCommand;
import com.ebay.testdemultiplexer.device.commands.RemovePackageCommand;
import com.ebay.testdemultiplexer.device.commands.RestartDeviceCommand;
import com.ebay.testdemultiplexer.device.commands.ShellCommand;
import com.ebay.testdemultiplexer.device.commands.StartActivityCommand;
import com.ebay.testdemultiplexer.device.commands.ToggleAirplaneModeCommand;
import com.ebay.testdemultiplexer.device.commands.TouchCommand;
import com.ebay.testdemultiplexer.device.commands.TypeCommand;
import com.ebay.testdemultiplexer.device.commands.UnlockDeviceCommand;

public class CommandRecorder {
	
	// Error message for output file access issues.
	private static final String OUTPUT_ERROR_MSG = 
			"An error occurred accessing output file. " +
			"Please make sure that the file location " +
			"is valid and has write access.";
	
	// Error title for output file access issues.
	private static final String OUTPUT_ERROR_TITLE = 
			"Output File Access Error";
	
	// Error message for writing to output file.
	private static final String WRITE_ERROR_MSG =
			"An error occurred while writing to file.";
	
	// Error title for writing to output file.
	private static final String WRITE_ERROR_TITLE =
			"Write Error";
	
	// Error message for closing output file.
	private static final String CLOSE_ERROR_MSG = 
			"An error occurred while closing the file.";
	
	// Error title for closing output file.
	private static final String CLOSE_ERROR_TITLE = 
			"Close File Error";
	
	// Error message for input file access issues.
	private static final String INPUT_ERROR_MSG = 
			"An error ocurred accessing the input file. " +
			"Please make sure that the file location is valid " +
			"and has read access.";
	
	// Error title for input file access issues.
	private static final String INPUT_ERROR_TITLE =
			"Input File Access Error";
	
	// Error message for reading from input issues.
	private static final String READ_ERROR_MSG = 
			"An error occurred while reading from file.";
	
	// Error title for reading from input issues.
	private static final String READ_ERROR_TITLE =
			"Read Error";

	/** Holds all of the commands recorded. */
	private ArrayList<DeviceCommand> commands;
	
	/** Tracks the state of the recorder. */
	private boolean isRecording = true;
	
	/** Registered listeners. */
	private ArrayList<CommandRecorderListener> listeners;
	
	/**
	 * Create a new recorder.
	 */
	public CommandRecorder() {
		
		commands = new ArrayList<DeviceCommand>();
		listeners = new ArrayList<CommandRecorderListener>();
	}
	
	/**
	 * Add a CommandRecorderListener to be notified of recorder changes.
	 * @param listener CommandRecorderListener to notify of recorder changes.
	 */
	public void addCommandRecorderListener(CommandRecorderListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove a CommandRecorderListener from the list of listeners.
	 * @param listener CommandRecorderListener to remove from list of listeners.
	 */
	public void removeCommandRecorderListener(CommandRecorderListener listener){
		listeners.remove(listener);
	}
	
	/** 
	 * Start the recorder. 
	 */
	public void startRecorder() {
		isRecording = true;
	}
	
	/** 
	 * Stop the recorder. 
	 */
	public void stopRecorder() {
		isRecording = false;
	}
	
	/**
	 * See if the recorder is running.
	 * @return True if recording, false otherwise.
	 */
	public boolean isRecording() {
		return isRecording;
	}
	
	/**
	 * Clear the recorder.
	 */
	public void clearRecorder() {
		commands.clear();
		notifyListenersOfClearedRecorder();
	}
	
	/**
	 * Add a command to the recorder.
	 * @param command Command to add to the recorder.
	 * @param force Set to true to force adding command to recorder even when
	 * not recording.
	 */
	public void addCommand(DeviceCommand command, boolean force) {
		
		if (isRecording || force) {
			commands.add(command);
			notifyListenersOfCommandAdded(command);
		}
	}
	
	/**
	 * Get the command at the specified index.
	 * @param index Index to retrieve.
	 * @return Command at the index.
	 */
	public DeviceCommand getCommand(int index) {
		
		if (index < 0 || index >= commands.size()) {
			return null;
		}
		return commands.get(index);
	}
	
	/**
	 * Get the number of commands in the recorder.
	 * @return Number of commands in recorder.
	 */
	public int getRecorderLength() {
		return commands.size();
	}
	
	/**
	 * Load commands from file.
	 * @param filePath Path to command file to open.
	 * @return True if successful, false otherwise.
	 */
	public boolean loadCommandsFromFile(String filePath) {
		
		FileReader file;
		BufferedReader reader;
		
		// Open the file for reading.
		try {
			file = new FileReader(filePath);
			reader = new BufferedReader(file);
		} catch (FileNotFoundException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				INPUT_ERROR_MSG, 
    				INPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
			e.printStackTrace();
			return false;
		}
		
		// Begin parsing the commands out of the file.
		DeviceCommand command;
		
		String dataToken = "";
		
		while (dataToken != null) {
			
			// Read the next line.
			try {
				dataToken = reader.readLine();
			} catch (IOException e) {
				
				JOptionPane.showMessageDialog(
	    				null, 
	    				READ_ERROR_MSG, 
	    				READ_ERROR_TITLE, 
	    				JOptionPane.ERROR_MESSAGE);
	    		
				e.printStackTrace();
				
				return false;
			}
			
			// Because we are going in without reading the first line in the
			// file we need to check the case when nothing is there.
			if (dataToken == null) {
				break;
			}
			
			// Build the correct command based on the line data.
			if (dataToken.startsWith(DragCommand.SERIALIZED_KEY)) {
				command = new DragCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(InstallPackageCommand.SERIALIZED_KEY)) {
				command = new InstallPackageCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(PressCommand.SERIALIZED_KEY)) {
				command = new PressCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(RemovePackageCommand.SERIALIZED_KEY)) {
				command = new RemovePackageCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(RestartDeviceCommand.SERIALIZED_KEY)) {
				command = new RestartDeviceCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(ShellCommand.SERIALIZED_KEY)) {
				command = new ShellCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(StartActivityCommand.SERIALIZED_KEY)) {
				command = new StartActivityCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(ToggleAirplaneModeCommand.SERIALIZED_KEY)) {
				command = new ToggleAirplaneModeCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(TouchCommand.SERIALIZED_KEY)) {
				command = new TouchCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(TypeCommand.SERIALIZED_KEY)) {
				command = new TypeCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else if (dataToken.startsWith(UnlockDeviceCommand.SERIALIZED_KEY)) {
				command = new UnlockDeviceCommand();
				((CommandDeserializer)command).deserializeCommand(dataToken);
			} else {
				System.out.println("Could not load line token: "+dataToken);
				return false;
			}
			
			// Add the command to the command list.
			addCommand(command, true);
		}
		
		return true;
	}
	
	/**
	 * Write the commands to file.
	 * @param filePath The path to the output file to write.
	 */
	public void writeCommandsToFile(String filePath) {
		
		FileWriter file;
		BufferedWriter writer;
		
		// Open the file for writing.
		try {
			file = new FileWriter(filePath);
			writer = new BufferedWriter(file);
		} catch (IOException e) {

			JOptionPane.showMessageDialog(
    				null, 
    				OUTPUT_ERROR_MSG, 
    				OUTPUT_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
			
			e.printStackTrace();
			return;
		}
		
		// Iterate over all of the commands and write them to file.
		Iterator<DeviceCommand> iterator = commands.iterator();
		DeviceCommand command;
		
		while (iterator.hasNext()) {
			
			command = iterator.next();
			
			if (command instanceof CommandSerializer) {
				try {
					writer.write(
							((CommandSerializer)command).serializeCommand());
					writer.write("\n");
				} catch (IOException e) {
					
					JOptionPane.showMessageDialog(
		    				null, 
		    				WRITE_ERROR_MSG, 
		    				WRITE_ERROR_TITLE, 
		    				JOptionPane.ERROR_MESSAGE);
		    		
					e.printStackTrace();
					return;
				}
			}
		}
		
		// Close the file buffer.
		try {
			writer.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				CLOSE_ERROR_MSG, 
    				CLOSE_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
			e.printStackTrace();
			return;
		}
		
		// Close the file.
		try {
			file.close();
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(
    				null, 
    				CLOSE_ERROR_MSG, 
    				CLOSE_ERROR_TITLE, 
    				JOptionPane.ERROR_MESSAGE);
    		
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Notify listeners that a command was executed.
	 * @param index Index of command that was executed.
	 */
	public void notifyListenerOfCommandExecuted(int index) {
		
		Iterator<CommandRecorderListener> iterator = listeners.iterator();
		
		while (iterator.hasNext()) {
			
			CommandRecorderListener listener = iterator.next();
			listener.commandExecuted(index);
		}
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Notify listeners of a command being added to the recorder.
	 * @param command Command that was added to the recorder.
	 */
	private void notifyListenersOfCommandAdded(DeviceCommand command) {
		
		Iterator<CommandRecorderListener> iterator = listeners.iterator();
		
		while (iterator.hasNext()) {
			
			CommandRecorderListener listener = iterator.next();
			listener.addCommand(command);
		}
	}
	
	/**
	 * Notify listeners of the command recorder being cleared.
	 */
	private void notifyListenersOfClearedRecorder() {
		
		Iterator<CommandRecorderListener> iterator = listeners.iterator();
		
		while (iterator.hasNext()) {
			
			CommandRecorderListener listener = iterator.next();
			listener.commandRecorderCleared();
		}
	}

}
