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
 * Class: UIViewSAXParser
 * 
 * Description: 
 * SAX parser for the UIAutomation dump file generated. Creates the 
 * UIViewTreeNode hierarchy and passes back the root node when finished.
 */

package com.ebay.testdemultiplexer.uiautomator;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UIViewSAXParser extends DefaultHandler {
	
	/** Root node of UI hierarchy. */
	private UIViewTreeNode rootNode;
	
	/** Parent stack to track nodes for parenting with. */
	private Stack<UIViewTreeNode> parentStack;
	
	/** Listener to call with the new root node. */
	private UIViewSAXParserListener listener;
	
	/** Local path to UIAutomation xml file to path. */
	private String localFilePath;
	
	/**
	 * Create a new UIAutomation xml parser.
	 * @param localFilePath Path to xml file to parse.
	 * @param listener Listener to call with the new root node.
	 */
	public UIViewSAXParser(
			String localFilePath, UIViewSAXParserListener listener) {
		this.localFilePath = localFilePath;
		this.listener = listener;	
	}
	
	/**
	 * Start the parsing operation. Caller should wait for the 
	 * UIViewSAXParserListener call to be made with the root node.
	 */
	public void beginParsing() {
		
		rootNode = null;
		parentStack = new Stack<UIViewTreeNode>();
		
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = saxParserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			notifyListener();
			return;
		} catch (SAXException e) {
			e.printStackTrace();
			notifyListener();
			return;
		}
		File file = new File(localFilePath);
		try {
			saxParser.parse(file, this);
		} catch (SAXException e) {
			e.printStackTrace();
			notifyListener();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			notifyListener();
			return;
		}
	}
	
	// -------------------------------------------------------------------------
	// DefaultHandler Overridden Methods
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		System.out.println("UNEXPECTED CHARACTERS ENCOUNTERED BY SAX PARSER");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		notifyListener();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (!parentStack.isEmpty()) {
			parentStack.pop();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(
			String uri, 
			String localName, 
			String qName,
			Attributes attributes) throws SAXException {
		
		// The root element is the hierarchy node, but that doesn't have any 
		// data. We only care about node elements.
		if (!qName.equals(UIViewTreeNode.NODE_ELEMENT)) {
			return;
		}
		
		boolean naf = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_NOT_ACCESSABILITY_FRIENDLY));
		
		int index = Integer.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_INDEX));
		
		String uniqueID = createUniqueID(index);
		
		String text = attributes.getValue(UIViewTreeNode.U_TEXT);
		
		String classReference = attributes.getValue(
				UIViewTreeNode.U_CLASS_REFERENCE);
		
		String packageName = attributes.getValue(UIViewTreeNode.U_PACKAGE);
		
		String contentDescription = attributes.getValue(
				UIViewTreeNode.U_CONTENT_DESC);
		
		boolean isCheckable = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_CHECKABLE));
		
		boolean isChecked = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_CHECKED));
		
		boolean isClickable = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_CLICKABLE));
		
		boolean isEnabled = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_ENABLED));
		
		boolean isFocusable = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_FOCUSABLE));
		
		boolean isFocused = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_FOCUSED));
		
		boolean isScrollable = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_SCROLLABLE));
		
		boolean isLongClickable = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_LONG_CLICKABLE));
		
		boolean isPasswordField = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_PASSWORD));
		
		boolean isSelected = Boolean.valueOf(
				attributes.getValue(
						UIViewTreeNode.U_SELECTED));
		
		String rawBounds = attributes.getValue(UIViewTreeNode.U_BOUNDS);
		int topLeftX = 0;
		int topLeftY = 0;
		int bottomRightX = 0;
		int bottomRightY = 0;
		
		if (rawBounds != null) {
			rawBounds = rawBounds.substring(1);
			rawBounds = rawBounds.substring(0, rawBounds.length()-1);
			String[] boundsData = rawBounds.split("]\\[");
			String[] topLeft = boundsData[0].split(",");
			String[] bottomRight = boundsData[1].split(",");
			
			topLeftX = Integer.valueOf(topLeft[0]);
			topLeftY = Integer.valueOf(topLeft[1]);
			bottomRightX = Integer.valueOf(bottomRight[0]);
			bottomRightY = Integer.valueOf(bottomRight[1]);
		}
		
		UIViewTreeNode newNode = new UIViewTreeNode(
				naf, 
				index, 
				uniqueID, 
				text, 
				classReference, 
				packageName, 
				contentDescription, 
				isCheckable, 
				isChecked, 
				isClickable, 
				isEnabled, 
				isFocusable, 
				isFocused, 
				isScrollable, 
				isLongClickable, 
				isPasswordField, 
				isSelected, 
				topLeftX,
				topLeftY,
				bottomRightX,
				bottomRightY);
		
		if (!parentStack.isEmpty()) {
			newNode.setParent(parentStack.peek());
			parentStack.peek().addChild(newNode);
		}
		
		if (rootNode == null) {
			rootNode = newNode;
		}
		
		parentStack.push(newNode);
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Create a unique ID for a node that does not yet have a parent but will
	 * be parented to the node on the top of the stack.
	 * @param index Index value of the new node.
	 * @return Unique id starting with the index of the oldest parent node and
	 * including the index value of the new node.
	 */
	private String createUniqueID(int index) {
		
		String id = "";
		
		if (!parentStack.isEmpty()) {
			id = createUniqueID(parentStack.peek());
		}
		id = id + index;
		return id;
	}
	
	/**
	 * Create a unique ID inclusive of the specified node and that includes
	 * all of the parents derived from that node.
	 * @param node Node to start with.
	 * @return Unique id starting with the index of the oldest parent node.
	 */
	private String createUniqueID(UIViewTreeNode node) {
		
		String id = "";
		
		if (node.getParent() != null) {
			id = createUniqueID(node.getParent());
		}

		id = id + node.getIndex();
		return id;
	}
	
	/**
	 * Notify the listener that parsing is done. Hand back the root node.
	 */
	private void notifyListener() {
		listener.doneParsingXML(rootNode);
	}
}
