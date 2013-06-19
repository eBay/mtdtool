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
 * Class: UIViewTreeNode
 * 
 * Description: 
 * Data representation of a UIViewTreeNode. UIViewTreeNode are the data 
 * encapsulation of an individual node in the UIAutomation dump file.
 */

package com.ebay.testdemultiplexer.uiautomator;

import java.awt.Point;
import java.util.ArrayList;

public class UIViewTreeNode {
	
	/** XML element flag : node */
	public static final String NODE_ELEMENT = "node";
	
	/** XML attribute flag : index */
	public static final String U_NOT_ACCESSABILITY_FRIENDLY = "NAF";
	
	/** XML attribute flag : index */
	public static final String U_INDEX = "index";
	
	/** XML attribute flag : text */
	public static final String U_TEXT = "text";
	
	/** XML attribute flag : class */
	public static final String U_CLASS_REFERENCE = "class";
	
	/** XML attribute flag : package */
	public static final String U_PACKAGE = "package";
	
	/** XML attribute flag : content-desc */
	public static final String U_CONTENT_DESC = "content-desc";
	
	/** XML attribute flag : checkable */
	public static final String U_CHECKABLE = "checkable";
	
	/** XML attribute flag: checked */
	public static final String U_CHECKED = "checked";
	
	/** XML attribute flag: clickable */
	public static final String U_CLICKABLE = "clickable";
	
	/** XML attribute flag : enabled */
	public static final String U_ENABLED = "enabled";
	
	/** XML attribute flag : focusable */
	public static final String U_FOCUSABLE = "focusable";
	
	/** XML attribute flag : focused */
	public static final String U_FOCUSED = "focused";
	
	/** XML attribute flag : scrollable */
	public static final String U_SCROLLABLE = "scrollable";
	
	/** XML attribute flag : long-clickable */
	public static final String U_LONG_CLICKABLE = "long-clickable";
	
	/** XML attribute flag : password */
	public static final String U_PASSWORD = "password";
	
	/** XML attribute flag : selected */
	public static final String U_SELECTED = "selected";
	
	/** XML attribute flag : bounds */
	public static final String U_BOUNDS = "bounds";

	/** Flag for Not Accessibility Friendly (NAF). */
	private boolean NAF;
	
	/** Child index value. */
	private int index;
	
	/** 
	 * The unique ID is the combination of all of the indexes from root node to
	 * this node. For example, 00120 is a valid ID.
	 */
	private String uniqueID;
	
	/**
	 * Text data stored in node.
	 */
	private String text;
	
	/**
	 * Class implmentation for the node.
	 */
	private String classReference;
	
	/**
	 * Package name for the application under test.
	 */
	private String packageName;
	
	/**
	 * Accessibility content description field.
	 */
	private String contentDescription;
	
	/**
	 * Flag for checkable.
	 */
	private boolean isCheckable;
	
	/**
	 * Flag for if checked.
	 */
	private boolean isChecked;
	
	/**
	 * Flag for is clickable.
	 */
	private boolean isClickable;
	
	/**
	 * Flag for is enabled.
	 */
	private boolean isEnabled;
	
	/**
	 * Flag for is focusable.
	 */
	private boolean isFocusable;
	
	/**
	 * Flag for is focused.
	 */
	private boolean isFocused;
	
	/**
	 * Flag for is scrollable.
	 */
	private boolean isScrollable;
	
	/**
	 * Flag for is long clickable.
	 */
	private boolean isLongClickable;
	
	/**
	 * Flag for is password field.
	 */
	private boolean isPasswordField;
	
	/**
	 * Flag for is selected.
	 */
	private boolean isSelected;
	
	/**
	 * Top left bounds coordinate.
	 */
	private Point topLeftBounds;
	
	/**
	 * Bottom right bounds coordinate.
	 */
	private Point bottomRightBounds;
	
	/**
	 * Reference to parent node.
	 */
	private UIViewTreeNode parent;
	
	/**
	 * Reference to all children nodes.
	 */
	private ArrayList<UIViewTreeNode> children;

	/**
	 * Creates a new UIViewTreeNode. Data should be generated from UIAutomation
	 * calls.
	 * @param NAF Not Automation Friendly Flag
	 * @param index Index value
	 * @param uniqueID Unique ID generated from index values back to root node
	 * @param text Text value
	 * @param classReference Class reference
	 * @param packageName Package name for activity
	 * @param contentDescription Content description for node
	 * @param isCheckable Is checkable flag
	 * @param isChecked Is checked flag
	 * @param isClickable Is clickable flag
	 * @param isEnabled Is enabled flag
	 * @param isFocusable Is focusable flag
	 * @param isFocused Is focused flag
	 * @param isScrollable Is scrollable flag
	 * @param isLongClickable Is long clickable flag
	 * @param isPasswordField Is password field flag
	 * @param isSelected Is selected flag
	 * @param topLeftX Top left bounds X coordinate
	 * @param topLeftY Top left bounds Y cooridnate
	 * @param bottomRightX Bottom right bounds X coordiante
	 * @param bottomRightY Bottom right bounds Y coordinate
	 */
	public UIViewTreeNode(
			boolean NAF, 
			int index, 
			String uniqueID, 
			String text, 
			String classReference, 
			String packageName, 
			String contentDescription, 
			boolean isCheckable, 
			boolean isChecked, 
			boolean isClickable,
			boolean isEnabled,
			boolean isFocusable,
			boolean isFocused,
			boolean isScrollable,
			boolean isLongClickable,
			boolean isPasswordField,
			boolean isSelected,
			int topLeftX,
			int topLeftY,
			int bottomRightX,
			int bottomRightY) {
		
		this.NAF = NAF;
		this.index = index;
		this.uniqueID = uniqueID;
		this.text = text;
		this.classReference = classReference;
		this.packageName = packageName;
		this.contentDescription = contentDescription;
		this.isCheckable = isCheckable;
		this.isChecked = isChecked;
		this.isClickable = isClickable;
		this.isEnabled = isEnabled;
		this.isFocusable = isFocusable;
		this.isFocused = isFocused;
		this.isScrollable = isScrollable;
		this.isLongClickable = isLongClickable;
		this.isPasswordField = isPasswordField;
		this.isSelected = isSelected;
		this.topLeftBounds = new Point(topLeftX, topLeftY);
		this.bottomRightBounds = new Point(bottomRightX, bottomRightY);
		
		parent = null;
		children = new ArrayList<UIViewTreeNode>();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Note that this implementation does not compare the parent or children.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof UIViewTreeNode)) {
			return false;
		}
		
		UIViewTreeNode node = (UIViewTreeNode) obj;
		
		if (!node.getUniqueID().equals(uniqueID)) {
			return false;
		} else if (node.getIndex() != index) {
			return false;
		} else if (node.isCheckable != isCheckable) {
			return false;
		} else if (node.isChecked != isChecked) {
			return false;
		} else if (node.isClickable != isClickable) {
			return false;
		} else if (node.isEnabled != isEnabled) {
			return false;
		} else if (node.isFocusable != isFocusable) {
			return false;
		} else if (node.isFocused != isFocused) {
			return false;
		} else if (node.isLongClickable != isLongClickable) {
			return false;
		} else if (node.isPasswordField != isPasswordField) {
			return false;
		} else if (node.isScrollable != isScrollable) {
			return false;
		} else if (node.isSelected != isSelected) {
			return false;
		} else if (node.NAF != NAF) {
			return false;
		} else if (!node.text.equals(text)) {
			return false;
		} else if (!node.classReference.equals(classReference)) {
			return false;
		} else if (!node.packageName.equals(packageName)) {
			return false;
		} else if (!node.contentDescription.equals(contentDescription)) {
			return false;
		} else if (!node.topLeftBounds.equals(topLeftBounds)) {
			return false;
		} else if (!node.bottomRightBounds.equals(bottomRightBounds)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Set the parent node.
	 * @param parent Parent node.
	 */
	public void setParent(UIViewTreeNode parent) {
		this.parent = parent;
	}
	
	/**
	 * Get the parent node.
	 * @return Parent node.
	 */
	public UIViewTreeNode getParent() {
		return parent;
	}
	
	/**
	 * Add a child.
	 * @param child Child to add.
	 */
	public void addChild(UIViewTreeNode child) {
		this.children.add(child);
	}
	
	/**
	 * Remove a specific child.
	 * @param child Child to remove from list of children.
	 */
	public void removeChild(UIViewTreeNode child) {
		this.children.remove(child);
	}
	
	/**
	 * Remove all children.
	 */
	public void removeChildren() {
		this.children.clear();
	}
	
	/**
	 * Get the number of children.
	 * @return Number of children.
	 */
	public int getNumberOfChildren() {
		return children.size();
	}
	
	/**
	 * Get the child at the specified index.
	 * @param index Index to retrieve.
	 * @return Child at specified index.
	 */
	public UIViewTreeNode getChildAtIndex(int index) {
		return children.get(index);
	}

	/**
	 * Get the Not Accessibility Friendly flag.
	 * @return True if Not Accessibility Friendly, false otherwise.
	 */
	public boolean getNAF() {
		return NAF;
	}
	
	/**
	 * Get the index for this node. The index is a signature of is ordering
	 * amongst its siblings.
	 * @return Node index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Get the unique ID for this node. The unique ID is an accumulation of
	 * the index values from the root node to this node. For example, 00120
	 * would be a valid ID and is unique in the hierarchy.
	 * @return Unique ID.
	 */
	public String getUniqueID() {
		return uniqueID;
	}
	
	/**
	 * Get the text for this node.
	 * @return Text for this node.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Get the class reference for this node.
	 * @return Class reference for this node.
	 */
	public String getClassReference() {
		return classReference;
	}
	
	/**
	 * Get the package name of the running activity.
	 * @return Package name of running activity.
	 */
	public String getPackageName() {
		return packageName;
	}
	
	/**
	 * Get the accessibility content description.
	 * @return Content description for node.
	 */
	public String getContentDescription() {
		return contentDescription;
	}
	
	/**
	 * Get the is checkable flag state.
	 * @return True if checkable, false otherwise.
	 */
	public boolean getIsCheckable() {
		return isCheckable;
	}
	
	/**
	 * Get the is checked flag state.
	 * @return True if checked, false otherwise.
	 */
	public boolean getIsChecked() {
		return isChecked;
	}
	
	/**
	 * Get the is clickable flag state.
	 * @return True if clickable, false otherwise.
	 */
	public boolean getIsClickable() {
		return isClickable;
	}
	
	/**
	 * Get the enabled flag state.
	 * @return True if enabled, false otherwise.
	 */
	public boolean getIsEnabled() {
		return isEnabled;
	}
	
	/**
	 * Get the is focusable flag state.
	 * @return True if focusable, false otherwise.
	 */
	public boolean getIsFocusable() {
		return isFocusable;
	}
	
	/**
	 * Get the is focused flag state.
	 * @return True if focused, false otherwise.
	 */
	public boolean getIsFocused() {
		return isFocused;
	}
	
	/**
	 * Get the is scrollable flag state.
	 * @return True if scrollable, false otherwise.
	 */
	public boolean getIsScrollable() {
		return isScrollable;
	}
	
	/**
	 * Get the is long clickable flag state.
	 * @return True if long clickable, false otherwise.
	 */
	public boolean getIsLongClickable() {
		return isLongClickable;
	}
	
	/**
	 * Get the is password field flag state.
	 * @return True if password field, false otherwise.
	 */
	public boolean getIsPasswordField() {
		return isPasswordField;
	}
	
	/**
	 * Get the is selected flag state.
	 * @return True if selected, false otherwise.
	 */
	public boolean getIsSelected() {
		return isSelected;
	}
	
	/**
	 * Get the top left bounds coordinate.
	 * @return Top left bounds coordinate.
	 */
	public Point getTopLeftBounds() {
		return topLeftBounds;
	}

	/**
	 * Get the bottom right bounds coordinate.
	 * @return Bottom right bounds coordinate.
	 */
	public Point getBottomRightBounds() {
		return bottomRightBounds;
	}
	
	/**
	 * Get the width of the node.
	 * @return Width of the node.
	 */
	public int getWidth() {
		return (bottomRightBounds.x - topLeftBounds.x);
	}
	
	/**
	 * Get the height of the node.
	 * @return height of the node.
	 */
	public int getHeight() {
		return (bottomRightBounds.y - topLeftBounds.y);
	}
	
	/**
	 * Get the center point of the node.
	 * @return Center point of the node.
	 */
	public Point getCenter() {
		return new Point(
				topLeftBounds.x + getWidth()/2, 
				topLeftBounds.y + getHeight()/2);
	}
	
	/**
	 * Get the center of the clickable region on screen.
	 * @return Clickable center on screen.
	 */
	public Point getClickableCenter() {
		
		UIViewTreeNode root = this;
		
		while (root.getParent() != null) {
			root = root.getParent();
		}
		
		int maxWidth = root.getWidth();
		int maxHeight = root.getHeight();
		
		Point newTopLeftPoint = new Point(topLeftBounds);
		Point newBottomRightPoint = new Point(bottomRightBounds);
		
		newTopLeftPoint.x = Math.max(Math.min(newTopLeftPoint.x, maxWidth), 0);
		newTopLeftPoint.y = Math.max(Math.min(newTopLeftPoint.y, maxHeight), 0);
		
		newBottomRightPoint.x = Math.max(Math.min(newBottomRightPoint.x, maxWidth), 0);
		newBottomRightPoint.y = Math.max(Math.min(newBottomRightPoint.y, maxHeight), 0);
		
		Point newCenterPoint = new Point(
				newBottomRightPoint.x - newTopLeftPoint.x, 
				newBottomRightPoint.y - newTopLeftPoint.y);
		
		if (newTopLeftPoint.x == topLeftBounds.x) {
			newCenterPoint.x = newCenterPoint.x + topLeftBounds.x;
		}
		
		if (newTopLeftPoint.y == topLeftBounds.y) {
			newCenterPoint.y = newCenterPoint.y + topLeftBounds.y;
		}
		
		return newTopLeftPoint;
	}
}
