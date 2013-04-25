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
 * Class: ComboBoxDialog
 * 
 * Description:
 * Presents a dialog window with combo box for selecting the adb shell commands
 * to execute. The resulting selection is returned when the user pressed OK.
 */

package com.ebay.testdemultiplexer.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ComboBoxDialog extends JDialog implements ActionListener {
	
	/** Combo box that contains the predefined shell commands. */
	private JComboBox comboBox;
	
	/** OK Button to execute the shell command. */
	private JButton okButton;
	
	/** Cancel button to bail. */
	private JButton cancelButton;
	
	/** Width of the combo box. */
	private static final int COMBO_BOX_WIDTH = 350;
	
	/** Height of the combo box. */
	private static final int COMBO_BOX_HEIGHT = 30;
	
	/** Width of the dialog. */
	private static final int DIALOG_WIDTH = 400;
	
	/** Height of the dialog. */
	private static final int DIALOG_HEIGHT = 130;
	
	/** The string of the command selected to be executed. */
	private String result;
	
	/**
	 * Default constructor.
	 * @param parent Parent component
	 * @items ComboBox items to show
	 */
	public ComboBoxDialog(JFrame parent, Object[] items) {
		
		super(parent, true);
		init(items);
	}
	
	/**
	 * Display the dialog and allow the user to select or modify the command.
	 * This is a blocking operation, so the result that is selected will be
	 * returned from this operation.
	 * @return Return the result chosen by the user.
	 */
	public Object showDialog() {
		this.setVisible(true);
		return result;
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Initialize the layout for the combo box.
	 */
	private void init(Object[] items) {
		
		this.setTitle("Shell Command");
		this.setResizable(false);
		this.setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		this.setMaximumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		this.setPreferredSize(
				new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JPanel mainWindow = new JPanel();
		mainWindow.setLayout(new BoxLayout(mainWindow, BoxLayout.Y_AXIS));
		mainWindow.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		
		JLabel message = new JLabel(
				"Please enter or select your shell command.");
		
		mainWindow.add(Box.createVerticalStrut(10));
	
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
		messagePanel.add(message);
		mainWindow.add(messagePanel);
		
		mainWindow.add(Box.createVerticalStrut(5));
		
		comboBox = new JComboBox(items);
		comboBox.setEditable(true);
		comboBox.addActionListener(this);
		comboBox.setPreferredSize(
				new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
		comboBox.setMinimumSize(
				new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
		comboBox.setMaximumSize(
				new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
		
		JPanel comboPane = new JPanel();
		comboPane.setLayout(new BoxLayout(comboPane, BoxLayout.X_AXIS));
		comboPane.add(comboBox);
		mainWindow.add(comboPane);
		
		mainWindow.add(Box.createVerticalStrut(5));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		
		buttonPane.add(cancelButton);
		buttonPane.add(okButton);
		
		mainWindow.add(buttonPane);
		
		mainWindow.add(Box.createVerticalStrut(5));
		
		this.add(mainWindow);
		this.pack();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == okButton) {
			result = (String) comboBox.getEditor().getItem();
			setVisible(false);
			dispose();
		} else if (e.getSource() == cancelButton) {
			setVisible(false);
			dispose();
		}
	}
}
