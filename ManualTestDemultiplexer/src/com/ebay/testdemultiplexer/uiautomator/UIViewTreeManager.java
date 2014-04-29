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
 * Class: UIViewTreeManager
 * 
 * Description: 
 * Provides all of the tree processing and node level operations required to
 * perform operations such as specific widget selection and node finding. Every
 * TestDevice instance has an instance of this class that manages the
 * interaction with the UIAutomation library.
 */

package com.ebay.testdemultiplexer.uiautomator;

import java.awt.Point;
import java.util.ArrayList;

import com.ebay.testdemultiplexer.connection.TestDevice;

public class UIViewTreeManager implements ThreadedUIViewTreeParserListener {
	
	/** Root node of the view. */
	private UIViewTreeNode rootNode;
	
	/** Optional drag directions to try and locate a view. */
	public enum DRAG_DIRECTION {UP, DOWN, LEFT, RIGHT};
	
	/** Fixed drag duration in seconds */
	private static int DRAG_DURATION = 1000;
	
	/** 
	 * Side pocketed list of nodes that are occluding the target node examined
	 * in isViewOccluded(). This list is cleared and then populated for every
	 * call to isViewOccluded().
	 */
	private ArrayList<UIViewTreeNode> occlusionNodeList;
	
	/** 
	 * Tracks when the root node is ready after requesting a UIAutomation dump.
	 * It is possible for the root node to come back as null.
	 */
	private boolean isRootNodeReady;
	
	/** 
	 * Reference to the TestDevice all UIAutomation calls should be executed
	 * against.
	 */
	private TestDevice device;
	
	/** List of nodes used for picking analysis See getViewAtLocation(). */
	ArrayList<UIViewTreeNode> pickedNodes = new ArrayList<UIViewTreeNode>();
	
	/**
	 * Create a new UIViewTreeManager.
	 */
	public UIViewTreeManager(TestDevice device) {
		this.device = device;
		rootNode = null;
		isRootNodeReady = false;
		occlusionNodeList = new ArrayList<UIViewTreeNode>();
	}
	
	/**
	 * Test if the device supports UIAutomation. UIAutomation is available only
	 * on API level 16+.
	 * @return True if UIAutomation is supported, false otherwise.
	 */
	public boolean deviceSupportsUIAutomation() {
		
		if (true)
			return false;
		
		//adb shell cat /system/build.prop | grep ro.build.version.sdk
		String properties = 
				device.getIChimpDevice().shell("cat /system/build.prop");
		int index = properties.indexOf("ro.build.version.sdk");
		if (index == -1) {
			return false;
		}
		
		properties = properties.substring(index);
		properties = properties.substring(0, properties.indexOf("\n"));
		properties = properties.substring(properties.indexOf("=")+1);
		properties = properties.trim();
		
		int apiVersion = Integer.valueOf(properties);
		
		if (apiVersion < 16) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the click location in UiAutomation coordinates.
	 * @param scaleX Horizontal percentage of original click.
	 * @param scaleY Vertical percentage of original click.
	 * @return New UiAutomation click point.
	 */
	public Point getUiAutomationClickLocation(float scaleX, float scaleY) {
		
		UIViewTreeNode rootNode = getRootNode();
		
		return new Point(
				(int)(rootNode.getWidth()*scaleX), 
				(int)(rootNode.getHeight()*scaleY));
	}
	
	/**
	 * Dump the UI hierarchy using UIAutomation. Parse the XML into a data
	 * structure for future reference. Do this on a separate thread as not to
	 * block operations. Call waitForNewRootNode() to block until done. Any
	 * calls to getRootNode() will automatically block until done.
	 */
	public void dumpUIHierarchy() {
		
		if (!deviceSupportsUIAutomation()) {
			return;
		}
		
		isRootNodeReady = false;
		rootNode = null;
		
		ThreadedUIViewTreeParser parser = 
				new ThreadedUIViewTreeParser(device, this);
		parser.start();
	}
	
	/**
	 * Wait until the new root node is ready and then continue operations.
	 * Optional way to block until the root node is ready after a UIAutomation
	 * dump.
	 */
	public void waitForNewRootNode() {
		
		while (!this.isRootNodeReady()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This node should be called before calling any other method after 
	 * requesting a UIAutomatioin dump. This will return true when the dump
	 * is complete and the root node is ready.
	 * @return True if the new root node is ready. False otherwise.
	 */
	public synchronized boolean isRootNodeReady() {
		return isRootNodeReady;
	}
	
	/**
	 * Print the full UI Hierarchy to stdout.
	 */
	public void printUIHierarchy() {
		printUIHierarchy(getRootNode());
	}
	
	/**
	 * Get the root node of the tree. Will always block until the root node
	 * is ready following a dump request.
	 * @return Root node of the tree.
	 */
	public UIViewTreeNode getRootNode() {
		
		waitForNewRootNode();
		return rootNode;
	}
	
	/**
	 * Make the node visible in the active view. This will make sure the view
	 * is visible and not occluded by any other view. Be sure to request the
	 * root node again after calling this operation as it may have forced a new
	 * drop of the UIAutomation hierarchy.
	 * @param id Unique ID of view to make visible.
	 * @return Node requested.
	 */
	public UIViewTreeNode makeNodeVisible(String id) {
		
		UIViewTreeNode node = getNodeAtID(id);
		
		if (node == null) {
			return null;
		}

		if (isViewOccluded(node, getRootNode())) {
			waitForNewRootNode();
			unOccludeView(node);
			waitForNewRootNode();
			dumpUIHierarchy();
			waitForNewRootNode();
		}
		
		node = getNodeAtID(id);
		
		if (isViewOccluded(node, getRootNode())) {
			UIViewTreeNode tmpNode = unOccludeView(node);
			if (tmpNode != null) {
				node = tmpNode;
			}
		}
		
		return node;
	}
			
	/**
	 * Get the shallowest child node that contains the click location. Typically
	 * this will be a clickable node. The only exception would be in the case
	 * where we find a ListView that is clickable. In that case we look for
	 * the widget the user actually clicked, even though it isn't clickable.
	 * @param xPos X axis screen click location.
	 * @param yPos Y axis screen click location.
	 * @return Shallowest node that contains the click location, or null if
	 * not found.
	 */
	public UIViewTreeNode getViewAtLocation(int xPos, int yPos) {
		
		pickedNodes.clear();
		UIViewTreeNode topNode = null;
		getClickableViewsAtLocation(getRootNode(), xPos, yPos);
		
		if (pickedNodes.size() > 0) {
			topNode = pickedNodes.get(0);
		}
		
		// Find the shallowest node by unique ID length.
		for (int i = 1; i < pickedNodes.size(); i++) {
			if (pickedNodes.get(i).getUniqueID().length() < 
					topNode.getUniqueID().length()) {
				topNode = pickedNodes.get(i);
			}
		}
		
		// If the topNode class reference is android.widget.ListView, then
		// return the actual child node clicked, even though it isn't clickable.
		// We do this so we can correctly select the location in the list view.
		if (topNode != null &&
				topNode.getClassReference().equals("android.widget.ListView")) {
			
			pickedNodes.clear();
			getAllViewsAtLocation(topNode, xPos, yPos);
			
			if (pickedNodes.size() > 0) {
				topNode = pickedNodes.get(0);
			}
			
			// Find the deepest node by unique ID length.
			for (int i = 1; i < pickedNodes.size(); i++) {
				if (pickedNodes.get(i).getUniqueID().length() > 
						topNode.getUniqueID().length()) {
					topNode = pickedNodes.get(i);
				}
			}
		}
		
		return topNode;
	}
	
	/**
	 * Get the view center for the node specified by its unique ID.
	 * @param id ID requested. This is the unique ID of the node.
	 * @return Center point of the view.
	 */
	public Point getViewCenterByID(String id) {
		UIViewTreeNode node = getNodeAtID(id);
		return node.getCenter();
	}
	
	/**
	 * Get the top left bounds by the specified ID.
	 * @param id ID requested. This is the unique ID of the node.
	 * @return Top left bounds coordinate of the view.
	 */
	public Point getViewTopLeftBoundsByID(String id) {
		UIViewTreeNode node = getNodeAtID(id);
		return node.getTopLeftBounds();
	}
	
	/**
	 * Get the bottom right bounds by the specified ID.
	 * @param id ID requested. This is the unique ID of the node.
	 * @return Bottom right bounds coordinate of the view.
	 */
	public Point getViewBottomRightBoundsByID(String id) {
		UIViewTreeNode node = getNodeAtID(id);
		return node.getBottomRightBounds();
	}
	
	/**
	 * Determine if two trees reference the same tree. Compares both node's 
	 * addresses and all of their children to determine if the trees are the 
	 * same by address.
	 * @param treeOne First tree to compare.
	 * @param treeTwo Second tree to compare.
	 * @return True if they are the same, false otherwise.
	 */
	public boolean areSameHierarchy(
			UIViewTreeNode treeOne, 
			UIViewTreeNode treeTwo) {
	
		if (!treeOne.equals(treeTwo)) {
			return false;
		} else if (treeOne.getNumberOfChildren() != 
				treeTwo.getNumberOfChildren()) {
			return false;
		}
		
		for (int i = 0; i < treeOne.getNumberOfChildren(); i++) {
			
			if (!areSameHierarchy(
					treeOne.getChildAtIndex(i), 
					treeTwo.getChildAtIndex(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * See if two nodes are the same based on their properties, with the
	 * exception of their position and bounds information which can vary from
	 * device to device.
	 * @param nodeA First node to compare.
	 * @param nodeB Second node to compare.
	 * @return True if all properties, except position and bounds, match. False
	 * otherwise.
	 */
	public boolean areNodesTheSameMinusPosition(
			UIViewTreeNode nodeA, 
			UIViewTreeNode nodeB) {
		
		if (!nodeA.getUniqueID().equals(nodeB.getUniqueID())) {
			return false;
		} else if (nodeA.getIndex() != nodeB.getIndex()) {
			return false;
		} else if (nodeA.getIsCheckable() != nodeB.getIsCheckable()) {
			return false;
		} else if (nodeA.getIsChecked() != nodeB.getIsChecked()) {
			return false;
		} else if (nodeA.getIsClickable() != nodeB.getIsClickable()) {
			return false;
		} else if (nodeA.getIsEnabled() != nodeB.getIsEnabled()) {
			return false;
		} else if (nodeA.getIsFocusable() != nodeB.getIsFocusable()) {
			return false;
		} else if (nodeA.getIsFocused() != nodeB.getIsFocused()) {
			return false;
		} else if (nodeA.getIsLongClickable() != nodeB.getIsLongClickable()) {
			return false;
		} else if (nodeA.getIsPasswordField() != nodeB.getIsPasswordField()) {
			return false;
		} else if (nodeA.getIsScrollable() != nodeB.getIsScrollable()) {
			return false;
		} else if (nodeA.getIsSelected() != nodeB.getIsSelected()) {
			return false;
		} else if (nodeA.getNAF() != nodeB.getNAF()) {
			return false;
		} else if (!nodeA.getText().equals(nodeB.getText())) {
			return false;
		} else if (!nodeA.getClassReference().equals(nodeB.getClassReference())) {
			return false;
		} else if (!nodeA.getPackageName().equals(nodeB.getPackageName())) {
			return false;
		} else if (!nodeA.getContentDescription().equals(nodeB.getContentDescription())) {
			return false;
		}
		
		return true;
	}

 	// -------------------------------------------------------------------------
	// Methods required by ThreadedUIViewTreeParserListener
	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see com.ebay.testdemultiplexer.util.ThreadedUIViewTreeParserListener#doneParsingTreeView(com.ebay.testdemultiplexer.util.UIViewTreeNode, java.lang.String)
	 */
	@Override
	public void doneParsingTreeView(
			UIViewTreeNode rootNode,
			String deviceSerialNo) {
		isRootNodeReady = true;
		this.rootNode = rootNode;
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * See if the view is occluded by another view that is not in the direct
	 * parent/child path. If it is, the global occlusionNodeList variable will
	 * include the occluding nodes.
	 * @param targetNode Node whose view we want to examine for possible
	 * occlusion.
	 * @param possibleOcclusionNode Test node that we want to begin testing
	 * against for possible occlusion cases. Typically, this should be the
	 * root node.
	 * @return True if there is an occlusion, false otherwise.
	 */
	private boolean isViewOccluded(
			UIViewTreeNode targetNode, UIViewTreeNode possibleOcclusionNode) {
		
		occlusionNodeList.clear();
		doOcclusionCheck(targetNode, possibleOcclusionNode);
		if (occlusionNodeList.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Called by isViewOccluded. This does all of the hard work. Do not
	 * call this directly. Instead call, isViewOccluded. This performs a check
	 * of the tree descending from possibleOcclusionNode to see if any of the
	 * nodes in that tree occlude/collide with the targetNode.
	 * @param targetNode Node whose view we want to examine for possible
	 * occlusion.
	 * @param possibleOcclusionNode Test node that we want to begin testing
	 * against for possible occlusion cases. Typically, this should be the
	 * root node.
	 * @return True if there is an occlusion, false otherwise.
	 */
	private void doOcclusionCheck(
			UIViewTreeNode targetNode, UIViewTreeNode possibleOcclusionNode) {
		
		// Check for the possible occlusion node's existence in the direct
		// parent or child path. If it is not, then consider it for occlusion
		// testing.
		if (!isChildDecendentOfParent(targetNode, possibleOcclusionNode) &&
				!isChildDecendentOfParent(possibleOcclusionNode, targetNode)) {
			
			Point targetNodeTopLeft = targetNode.getTopLeftBounds();
			Point targetNodeBottomRight = targetNode.getBottomRightBounds();
			
			Point occlusionTestTopLeft = 
					possibleOcclusionNode.getTopLeftBounds();
			Point occlusionTestBottomRight = 
					possibleOcclusionNode.getBottomRightBounds();
			
			// If all of these are true, then we are intersecting.
			// See this cool demo to understand what this is doing:
			// http://silentmatt.com/rectangle-intersection/
			if (targetNodeTopLeft.x < occlusionTestBottomRight.x &&
					targetNodeBottomRight.x > occlusionTestTopLeft.x &&
					targetNodeTopLeft.y < occlusionTestBottomRight.y &&
					targetNodeBottomRight.y > occlusionTestTopLeft.y) {
				occlusionNodeList.add(possibleOcclusionNode);
			}
		}
		
		for (int i = 0; i < possibleOcclusionNode.getNumberOfChildren(); i++) {
			doOcclusionCheck(targetNode, possibleOcclusionNode.getChildAtIndex(i));
		}
	}
	
	/**
	 * Get the first scrollable node below the common parent. Taking into 
	 * consideration all of the occlusion nodes added to the occlusionNodeList
	 * by the doOcclusionCheck() method, find the single scrollable node that
	 * will allow the adjustment of our node to scroll and not move the other
	 * occluding nodes. Do this by looking for the common parent between the
	 * nodeToScroll and all occluding nodes and then find the first scrollable
	 * parent to the nodeToScroll that is below the common parent.
	 * @param nodeToScroll Node to scroll.
	 * @return Scrollable node, or null if not found.
	 */
	private UIViewTreeNode getScrollableNodeBelowCommonParent(
			UIViewTreeNode nodeToScroll) {
		
		ArrayList<UIViewTreeNode> scrollNodeCandidateList = 
				new ArrayList<UIViewTreeNode>();
		
		for (int x = 0; x < occlusionNodeList.size(); x++) {
			
			int[] nodeToScrollIndexArray = 
					convertIdToIndexArray(nodeToScroll.getUniqueID());
			int[] stationaryNodeIndexArray = 
					convertIdToIndexArray(
							occlusionNodeList.get(x).getUniqueID());
			
			int sizeLimit = Math.min(
					nodeToScrollIndexArray.length, 
					stationaryNodeIndexArray.length);
			
			int firstUnmatchedIndex = -1;
			
			for (int i = 0; i < sizeLimit; i++) {
				if (nodeToScrollIndexArray[i] != stationaryNodeIndexArray[i]) {
					firstUnmatchedIndex = i;
					break;
				}
			}
			
			// If we matched all the way through, then return null.
			if (firstUnmatchedIndex == -1) {
				continue;
			}
			
			// Descend the hierarchy to the firstUnmatchedIndex, and then start
			// checking for scrollable nodes. Start at index 1 because we are
			// starting with the root node.
			UIViewTreeNode possibleScrollNode = getRootNode();
			
			for (int i = 1; i < nodeToScrollIndexArray.length; i++) {
				possibleScrollNode = possibleScrollNode.getChildWithIndex(
						nodeToScrollIndexArray[i]);
				
				if (possibleScrollNode == null) {
					break;
				}
				
				if (i >= firstUnmatchedIndex) {
					if (possibleScrollNode.getIsScrollable() && 
							nodeToScroll != possibleScrollNode) {
						scrollNodeCandidateList.add(possibleScrollNode);
						break;
					}
				}
			}
		}
		
		// Hopefully we only have one common scroll node. If we don't send back
		// the one that contains all the others.
		if (scrollNodeCandidateList.size() == 0) {
			return null;
		}
		
		UIViewTreeNode scrollNode = scrollNodeCandidateList.get(0);
		
		for (int i = 1; i < scrollNodeCandidateList.size(); i++) {
			if (scrollNode.getUniqueID().length() > 
				scrollNodeCandidateList.get(i).getUniqueID().length()) {
				scrollNode = scrollNodeCandidateList.get(i);
			}
		}
		
		return scrollNode;
	}
	
	/**
	 * Move the view such that it is no longer occluded by anything in the 
	 * active view region.
	 * @param node Node to unocclude.
	 * @return Same node but from the updated hierarchy, or null if failed.
	 */
	private UIViewTreeNode unOccludeView(UIViewTreeNode node) {
		
		UIViewTreeNode scrollingView = 
				getScrollableNodeBelowCommonParent(node);
		boolean dragVertical = true;
		
		if (scrollingView == null) {
			return null;
		}
		
		// If the first parent scroll view is a horizontal scroll, set the 
		// dragVertical flag to false. We base all of our drag actions on the
		// value of this flag.
		if (scrollingView.getClassReference().trim().equals(
				"android.widget.HorizontalScrollView")) {
			dragVertical = false;
		}
		
		boolean[] openLocationArray;
		
		if (dragVertical) {
			openLocationArray = new boolean[getRootNode().getHeight()];
		} else {
			openLocationArray = new boolean[getRootNode().getWidth()];
		}
		
		for (int i = 0; i < openLocationArray.length; i++) {
			openLocationArray[i] = true;
		}
		
		fillOpenLocationArray(node, getRootNode(), dragVertical, openLocationArray);
		
		// Find the largest open section in the openLocationArray.
		// Do this by walking the array and looking for the greatest open area.
		int savedStartIndex = 0;
		int savedEndIndex = 0;
		int tmpStartIndex = 0;
		int tmpEndIndex = 0;
		
		for (int i = 0; i < openLocationArray.length-1; i++) {
			
			if (openLocationArray[i]) {
				tmpEndIndex = i;
			} else {
				
				if ((tmpEndIndex - tmpStartIndex) > (savedEndIndex - savedStartIndex)) {	
					savedStartIndex = tmpStartIndex;
					savedEndIndex = tmpEndIndex;
				}
				tmpStartIndex=i+1;
				tmpEndIndex = i;
			}
		}
		
		// Drag distance will be from the center of the node to the center
		// of the largest open area. Then apply the drag from the center of the
		// first scrollable parent + the distance calculated between target node
		// and open area.
		int openAreaCenter = (savedEndIndex - savedStartIndex)/2+savedStartIndex;
		int dragDistance = 0;
		
		if (dragVertical) {
			dragDistance = openAreaCenter - node.getCenter().y;
		} else {
			dragDistance = openAreaCenter - node.getCenter().x;
		}

		Point scrollViewCenter = scrollingView.getCenter();
		
		if (dragVertical) {
			device.getIChimpDevice().drag(scrollViewCenter.x, scrollViewCenter.y, scrollViewCenter.x, scrollViewCenter.y+dragDistance, 20, DRAG_DURATION);
		} else {
			device.getIChimpDevice().drag(scrollViewCenter.x, scrollViewCenter.y, scrollViewCenter.x+dragDistance, scrollViewCenter.y, 20, DRAG_DURATION);
		}
		
		dumpUIHierarchy();
		waitForNewRootNode();
		
		int[] indexArray = convertIdToIndexArray(node.getUniqueID());
		UIViewTreeNode tmpNode = getRootNode();
		
		// Start at one, because, we are already at the root node.
		for (int i = 1; i < indexArray.length; i++) {
			
			if (tmpNode == null) {
				return null;
			}
			
			tmpNode = tmpNode.getChildWithIndex(indexArray[i]);
		}
		
		return tmpNode;
	}
	
	/**
	 * Fill the openLocationArray with locations of other occluding views. The 
	 * openLocationArray should be initialized to true. If there is a view 
	 * that could possibly occlude the targetNode, the related pixel indexes
	 * in the openLocationArray will be set to false.
	 * 
	 * This is used to get a pixel column or row of the UIAutomation view
	 * space through the center of the view we want to operate on. By filling
	 * the pixel values for possible occlusions we create a map of open areas
	 * to move the view to for selection.
	 * 
	 * @param targetNode Node we want to move to an unoccluded location.
	 * @param possibleOcclusionNode Node that may or may not occlude the 
	 * targetNode.
	 * @param verticalOrientation True if analysis should be done vertically,
	 * false for analysis to be done horizontally.
	 * @param openLocationArray Array with the same length as the longest 
	 * respective length of the possibleOcclusionNode initially passed in along
	 * the axis specified by verticalOrientation.
	 */
	private void fillOpenLocationArray(
			UIViewTreeNode targetNode, 
			UIViewTreeNode possibleOcclusionNode, 
			boolean verticalOrientation, 
			boolean[] openLocationArray) {
		
		// Cases where we will NOT consider there to be an occlusion...
		// 1) Direct parent line.
		// 2) Child of shared scrolling view (everything in that scroll view
		// will move with this node).
		// 3) Same node as targetNode (by reference address)
		UIViewTreeNode targetScrollParent = 
				getScrollableNodeBelowCommonParent(targetNode);
		
		if (!isChildDecendentOfParent(targetNode, possibleOcclusionNode) &&
				!isChildDecendentOfParent(possibleOcclusionNode, targetScrollParent) &&
				possibleOcclusionNode != targetNode) {

			Point occlusionTestTopLeft = 
					possibleOcclusionNode.getTopLeftBounds();
			Point occlusionTestBottomRight = 
					possibleOcclusionNode.getBottomRightBounds();
			
			if (verticalOrientation) {
				for (int x = occlusionTestTopLeft.y; x < occlusionTestBottomRight.y; x++) {
					if (x < openLocationArray.length && x >= 0) {
						openLocationArray[x] = false;
					}
				}
			} else {
				for (int x = occlusionTestTopLeft.x; x < occlusionTestBottomRight.x; x++) {
					if (x < openLocationArray.length && x >= 0) {
						openLocationArray[x] = false;
					}
				}
			}
		}
		
		for (int i = 0; i < possibleOcclusionNode.getNumberOfChildren(); i++) {
			fillOpenLocationArray(
					targetNode, 
					possibleOcclusionNode.getChildAtIndex(i), 
					verticalOrientation, 
					openLocationArray);
		}
	}
	
	/**
	 * See if the child is a descendant of the parent node.
	 * @param child Child to check.
	 * @param parent Parent to check.
	 * @return True if child is a descendant of the parent, false otherwise.
	 */
	private boolean isChildDecendentOfParent(UIViewTreeNode child, UIViewTreeNode parent) {
		
		if (child == parent) {
			return true;
		}
		
		while (child.getParent() != null) {
			if (child.getParent() == parent) {
				return true;
			}
			child = child.getParent();
		}
		
		return false;
	}
	
	/**
	 * Recursive operation to find the child nodes that contains the
	 * specified click location. Any node that contains the click location and
	 * doesn't have any children that contain the click location are included.
	 * Only nodes that are clickable will be returned.
	 * @param node Node to start search from.
	 * @param x X axis position.
	 * @param y Y axis position.
	 * @return Recursive state flag.
	 */
	private boolean getClickableViewsAtLocation(UIViewTreeNode node, int x, int y){
		
		boolean childMatch = false;
		
		for (int i = 0; i < node.getNumberOfChildren(); i++) {
			childMatch |= getClickableViewsAtLocation(node.getChildAtIndex(i), x, y);
		}
		
		if (childMatch) {
			return true;
		}
		
		Point topLeft = node.getTopLeftBounds();
		Point bottomRight = node.getBottomRightBounds();
		
		if (x >= topLeft.x && x <= bottomRight.x && 
				y >= topLeft.y && y <= bottomRight.y &&
				node.getIsClickable()) {
			pickedNodes.add(node);			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get all of the nodes that contain the click location.
	 * @param node Node to start search from.
	 * @param x X axis position
	 * @param y Y axis position
	 * @return Recursive state flag.
	 */
	private boolean getAllViewsAtLocation(UIViewTreeNode node, int x, int y){
		
		boolean childMatch = false;
		
		for (int i = 0; i < node.getNumberOfChildren(); i++) {
			childMatch |= getAllViewsAtLocation(node.getChildAtIndex(i), x, y);
		}
		
		if (childMatch) {
			return true;
		}
		
		Point topLeft = node.getTopLeftBounds();
		Point bottomRight = node.getBottomRightBounds();
		
		if (x >= topLeft.x && x <= bottomRight.x && 
				y >= topLeft.y && y <= bottomRight.y) {
			pickedNodes.add(node);			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Convert the ID string into the index array that can be followed to 
	 * quickly get to a specific node.
	 * @param id ID string to convert.
	 * @return Index array.
	 */
	private int[] convertIdToIndexArray(String id) {
		
		int[] indexArray = new int[id.length()];
		
		for (int i = 0; i < indexArray.length; i++) {
			indexArray[i] = Integer.valueOf(id.substring(0, 1));
			id = id.substring(1);
		}
		
		return indexArray;
	}
	
	/**
	 * Print out the UI hierarchy for quick examination in the console.
	 * @param node Node to begin traversing from.
	 */
	private void printUIHierarchy(UIViewTreeNode node) {
		System.out.println(
				"node : "+node.getUniqueID()+
				" index: "+node.getIndex()+
				" leftBounds: "+node.getTopLeftBounds().x+
				","+node.getTopLeftBounds().y+
				" rightBounds: "+node.getBottomRightBounds().x+
				","+node.getBottomRightBounds().y);;
		for (int i = 0; i < node.getNumberOfChildren(); i++) {
			printUIHierarchy(node.getChildAtIndex(i));
		}
	}
	
	/**
	 * Get the node at the specified ID. Be sure to request the root node again
	 * after calling this function as this function may perform scrolling
	 * operations in attempt to locate the view requested. In doing so it may
	 * make calls to dump the UI hierarchy.
	 * @param id ID requested. This is the unique ID of the node.
	 * @return UIViewTreeNode with that ID, or null if not found.
	 */
	private UIViewTreeNode getNodeAtID(String id) {

		// The index array contains expected index values. This does not mean
		// we can reference the child by this index. We actually need to compare
		// the child's index values to this index value to find a match.
		
		// We can do some interesting things with this information. For example,
		// if the index is less than the first child's index, we know that the
		// view needs to be scrolled down or to the right to bring it into
		// view. If the index is greater than the last child's index, we know
		// that the view needs to be scrolled down or to the left to bring it
		// into view.
		int[] indexArray = convertIdToIndexArray(id);
		
		UIViewTreeNode node = getRootNode();
		
		// Start at one, because, we are already at the root node.
		for (int i = 1; i < indexArray.length; i++) {
			
			if (node == null) {
				return null;
			}
			
			// If there aren't any children, attempt to scroll to make them.
			// visible. Because we have more indices, we expect that they
			// exist, they are just off screen.
			if (node.getNumberOfChildren() < 1) {

				// If there isn't a scrollable parent, bail.
				UIViewTreeNode scrollParent = getFirstScrollableParent(node);
				
				if (scrollParent == null) {
					return null;
				}
				
				// Scroll according to the class implementation.
				if (scrollParent.getClassReference().trim().equals("android.widget.HorizontalScrollView")) {
					if (node.getClickableCenter().x > getRootNode().getWidth()/2) {
						// Scroll to the left
						node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.LEFT);
						if (node == null) {
							return null;
						} else {
							continue;
						}
					} else {
						// Scroll to the right
						node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.RIGHT);
						if (node == null) {
							return null;
						} else {
							continue;
						}
					}
				} else {
					if (node.getClickableCenter().y > getRootNode().getWidth()/2) {
						// Scroll up
						node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.UP);
						if (node == null) {
							return null;
						} else {
							continue;
						}
					} else {
						// Scroll down
						node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.DOWN);
						if (node == null) {
							return null;
						} else {
							continue;
						}
					}
				}

			} else if (indexArray[i] < node.getChildAtIndex(0).getIndex()) {

				// If there isn't a scrollable parent, bail.
				UIViewTreeNode scrollParent = getFirstScrollableParent(node);
				
				if (scrollParent == null) {
					return null;
				}
				
				// Scroll according to the class implementation.
				if (scrollParent.getClassReference().trim().equals("android.widget.HorizontalScrollView")) {
					node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.RIGHT);
					if (node == null) {
						return null;
					} else {
						continue;
					}
				} else {
					node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.DOWN);
					if (node == null) {
						return null;
					} else {
						continue;
					}
				}
			} else if (indexArray[i] > node.getChildAtIndex(node.getNumberOfChildren()-1).getIndex()) {

				// If there isn't a scrollable parent, bail.
				UIViewTreeNode scrollParent = getFirstScrollableParent(node);
				
				if (scrollParent == null) {
					return null;
				}
				
				// Scroll according to the class implementation.
				if (scrollParent.getClassReference().trim().equals("android.widget.HorizontalScrollView")) {
					node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.LEFT);
					if (node == null) {
						return null;
					} else {
						continue;
					}
				} else {
					node = dragToIndex(node, node.getUniqueID()+indexArray[i], DRAG_DIRECTION.UP);
					if (node == null) {
						return null;
					} else {
						continue;
					}
				}
			} else {

				// Attempt to get the index matching child node. Validate the
				// match was found after the for loop.
				boolean matched = false;
				for (int j = 0; j < node.getNumberOfChildren(); j++) {
					if (node.getChildAtIndex(j).getIndex() == indexArray[i]) {
						node = node.getChildAtIndex(j);
						matched = true;
						break;
					}
				}
				
				if (!matched) {
					return null;
				}
			}
		}

		return node;
	}
	
	/**
	 * Drag the nearest scrollable UI element in order to uncover the view with
	 * the specified unique ID. Choose a drag direction to scroll in. If the
	 * view isn't found the loop will exit once the new hierarchy matches the
	 * previous side pocketed one.
	 * @param nodeWithExpectedChild The view node with the child we are trying
	 * to find by unique ID.
	 * @param uniqueIDToFind Unique ID of child to find.
	 * @param dragDirection Direction to drag in when looking.
	 * @return UIViewTreeNode matching the unique ID, or null if not found.
	 */
	private UIViewTreeNode dragToIndex(
			UIViewTreeNode nodeWithExpectedChild, 
			String uniqueIDToFind, 
			DRAG_DIRECTION dragDirection) {

		UIViewTreeNode sidepocketedRootNode = getRootNode();
		UIViewTreeNode scrollingView = 
				getFirstScrollableParent(nodeWithExpectedChild);
		
		if (scrollingView == null) {
			return null;
		}
		
		Point startPoint = new Point();
		Point endPoint = new Point();
		
		switch (dragDirection) {
		
			case UP:
				startPoint = scrollingView.getCenter();
				endPoint = scrollingView.getTopLeftBounds();
				endPoint.x = startPoint.x;
				break;
			case DOWN:
				startPoint = scrollingView.getCenter();
				endPoint = scrollingView.getBottomRightBounds();
				endPoint.x = startPoint.x;
				break;
			case LEFT:
				startPoint = scrollingView.getCenter();
				endPoint = scrollingView.getTopLeftBounds();
				endPoint.y = startPoint.y;
				break;
			case RIGHT:
				startPoint = scrollingView.getCenter();
				endPoint = scrollingView.getBottomRightBounds();
				endPoint.y = startPoint.y;
				break;
		}
		
		// There are two ways to exit this loop. 
		// 1) we have drug the view enough to the point that the hierarchies
		// are the same. Exit, and return null.
		// 2) we found the node we were after, return it.
		while (true) {

			device.getIChimpDevice().drag(startPoint.x, startPoint.y, endPoint.x, endPoint.y, 20, DRAG_DURATION);
			
			// Start the search for the view. First dump the hierarchy. Next
			// check if the drag effected any change. If it did, search for the
			// ID. Continue to loop until drag events effect no change in the
			// hierarchy.
			dumpUIHierarchy();
			waitForNewRootNode();
			
			// If there was no change to the hierarchy, then there was nothing left
			// to try and find. Return null.
			if (areSameHierarchy(sidepocketedRootNode, getRootNode())) {
				return null;
			} else {
				sidepocketedRootNode = getRootNode();
			}
			
			UIViewTreeNode tmpNode = getNodeAtID(uniqueIDToFind);
			
			if (tmpNode != null) {
				if (!isViewOccluded(tmpNode, getRootNode())) {
					return tmpNode;
				}
			}
		}
	}
	
	/**
	 * Find the first node in the parent hierarchy, exclusive of specified
	 * node, that has the scrollable flag set to true.
	 * @param node Node to begin search with.
	 * @return First node found that is scrollable, null otherwise.
	 */
	private UIViewTreeNode getFirstScrollableParent(UIViewTreeNode node) {
		
		while (node.getParent() != null) {
			node = node.getParent();
			if (node.getIsScrollable()) {
				return node;
			}
		}
		
		return null;
	}	
}
