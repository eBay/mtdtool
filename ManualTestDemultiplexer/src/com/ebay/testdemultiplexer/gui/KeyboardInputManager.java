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
 * Class: KeyboardInputManager
 * 
 * Description:
 * Processes keyboard input and issues PressCommands or TypeCommands 
 * depending on the input.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import com.android.chimpchat.core.TouchPressType;
import com.ebay.testdemultiplexer.connection.TestDeviceManager;
import com.ebay.testdemultiplexer.device.commands.PressCommand;
import com.ebay.testdemultiplexer.device.commands.TypeCommand;

public class KeyboardInputManager implements KeyEventDispatcher {

	/** Reference to the TestDeviceManager being used. */
	private TestDeviceManager manager;
	
	/** 
	 * Track if we should be processing input. 
	 * This is not ideal and should be corrected.
	 */
	private static boolean processInput = true;
	
	/**
	 * Hack to prevent enter key input from dialog boxes.
	 */
	private static boolean skipInput = false;
	
	/**
	 * Create a new KeyboardInputManager with a reference to TestDeviceManager.
	 * @param manager Reference to TestDeviceManager being used.
	 */
	public KeyboardInputManager(TestDeviceManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Interrupt key input handling.
	 */
	public static void interrupt() {
		processInput = false;
	}
	
	/**
	 * Resume key input handling.
	 */
	public static void resume() {
		processInput = true;
		skipInput = true;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
	 */
	public boolean dispatchKeyEvent(KeyEvent e) {
		
		if (!processInput) {
			return false;
		} else if (skipInput) {
			skipInput = false;
			return false;
		}
		
		// Depending on the key event process accordingly. Special key presses
		// like the arrow keys are identified separately and as press commands.
		// Return/enter, spacebar and delete are all also special cased 
		// PressCommands instead of TypeCommands because of how ADB processes
		// these type of inputs.
		if (e.getID() == KeyEvent.KEY_TYPED) {
			
			char c = e.getKeyChar();
			
			if (c == '\n') {
				
				PressCommand pressCommand = 
						new PressCommand(
								"KEYCODE_ENTER", 
								TouchPressType.DOWN_AND_UP);
				manager.executeCommand(pressCommand);

			} else if (c == ' ') {
				
				PressCommand pressCommand = 
						new PressCommand(
								"KEYCODE_SPACE", 
								TouchPressType.DOWN_AND_UP);
				manager.executeCommand(pressCommand);

			} else if (c == '\b') {
				
				PressCommand pressCommand = 
						new PressCommand(
								"KEYCODE_DEL", 
								TouchPressType.DOWN_AND_UP);
				manager.executeCommand(pressCommand);
				
			} else {
				
				// Default is to just send the keypress through.
				TypeCommand typeCommand = new TypeCommand(""+c);
				manager.executeCommand(typeCommand);
				
			}
			
        } else {
        	
            int keyCode = e.getKeyCode();
            
            if (keyCode == 37) {
            	
            	PressCommand pressCommand = 
            			new PressCommand(
            					"KEYCODE_DPAD_LEFT", 
            					TouchPressType.DOWN_AND_UP);
            	manager.executeCommand(pressCommand);
            	
            } else if (keyCode == 38) {
            	
            	PressCommand pressCommand = 
            			new PressCommand(
            					"KEYCODE_DPAD_UP", 
            					TouchPressType.DOWN_AND_UP);
            	manager.executeCommand(pressCommand);
            	
            } else if (keyCode == 39) {
            	
            	PressCommand pressCommand = 
            			new PressCommand(
            					"KEYCODE_DPAD_RIGHT", 
            					TouchPressType.DOWN_AND_UP);
            	manager.executeCommand(pressCommand);
            	
            } else if (keyCode == 40) {
            	
            	PressCommand pressCommand = 
            			new PressCommand(
            					"KEYCODE_DPAD_DOWN", 
            					TouchPressType.DOWN_AND_UP);
            	manager.executeCommand(pressCommand);
            }
        }
		
		e.consume();
		return true;
	}

}
