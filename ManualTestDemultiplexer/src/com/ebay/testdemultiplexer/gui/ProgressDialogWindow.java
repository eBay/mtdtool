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
 * Class: ProgressDialogWindow
 * 
 * Description:
 * Singleton for displaying a dialog window with progress bar for showing 
 * progress of long running tasks.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressDialogWindow extends JDialog {
	
	/** Static class instance. */
	private static ProgressDialogWindow dialogWindow = null;
	
	/** Dialog label. */
	private static JLabel message = null;
	
	/** Dialog progress bar. */
	private static JProgressBar progress = null;
	
	/** Internal const to set the progress bar minimum value. */
	private static final int PROGRESS_MINIMUM = 0;
	
	/** Internal const to set the progress bar maximum value. */
	private static final int PROGRESS_MAXIMUM = 100;
	
	/** Internal const to set the progress bar initial value. */
	private static final int PROGRESS_INITIAL_VALUE = 0;
	
	/** Width of the progress bar. */
	private static final int PROGRESS_WIDTH = 350;
	
	/** Height of the progress bar. */
	private static final int PROGRESS_HEIGHT = 20;
	
	/** Width of the dialog box. */
	private static final int DIALOG_WIDTH = 400;
	
	/** Height of the dialog box. */
	private static final int DIALOG_HEIGHT = 100;
	
	/**
	 * Private constructor.
	 */
	private ProgressDialogWindow(JFrame parent) {
		super(parent);
	}
	
	/**
	 * Get the instance of this singleton class and set the parent JFrame.
	 * This should be called the first time during execution to set, otherwise 
	 * you will not be able to change it.
	 * @param parent JFrame to parent dialog to.
	 * @return Singleton instance.
	 */
	public static ProgressDialogWindow getInstance(JFrame parent) {
		
		if (dialogWindow == null) {
			dialogWindow = new ProgressDialogWindow(parent);
			dialogWindow.setLocationRelativeTo(parent);
			init();
		}
		
		return dialogWindow;
	}
	
	/**
	 * Get the instance of this singleton class.
	 * @return Singleton instance.
	 */
	public static ProgressDialogWindow getInstance() {
		
		if (dialogWindow == null) {
			dialogWindow = new ProgressDialogWindow(null);
			init();
		}
		
		return dialogWindow;
	}
	
	/**
	 * Set the progress value of the progress bar.
	 * @param value Percent complete value between 0 - 100 inclusive.
	 */
	public void setProgress(int value) {
		progress.setValue(value);
	}
	
	/**
	 * Set the message of the progress dialog.
	 * @param value Dialog message.
	 */
	public void setMessage(String value) {
		message.setText(value);
	}
	
	/**
	 * Set the dialog box title.
	 * @param title Dialog box title.
	 */
	public void setMessageTitle(String title) {
		dialogWindow.setTitle(title);
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Initialize the layout for the progress dialog.
	 */
	private static void init() {
		
		dialogWindow.setTitle("Progress");
		dialogWindow.setResizable(false);
		dialogWindow.setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		dialogWindow.setMaximumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		dialogWindow.setPreferredSize(
				new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		dialogWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JPanel mainWindow = new JPanel();
		mainWindow.setLayout(new BoxLayout(mainWindow, BoxLayout.Y_AXIS));
		mainWindow.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		
		message = new JLabel("Processing, please wait...");
		
		progress = new JProgressBar(PROGRESS_MINIMUM, PROGRESS_MAXIMUM);
		progress.setValue(PROGRESS_INITIAL_VALUE);
		progress.setPreferredSize(
				new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
		progress.setMinimumSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
		progress.setMaximumSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
		
		mainWindow.add(Box.createVerticalStrut(10));
	
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
		messagePanel.add(message);
		mainWindow.add(messagePanel);
		
		mainWindow.add(Box.createVerticalStrut(5));
		
		JPanel progressPane = new JPanel();
		progressPane.setLayout(new BoxLayout(progressPane, BoxLayout.X_AXIS));
		progressPane.add(progress);
		mainWindow.add(progressPane);
		
		mainWindow.add(Box.createVerticalStrut(5));
		
		dialogWindow.add(mainWindow);
		dialogWindow.pack();
	}
}
