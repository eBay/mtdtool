package com.ebay.testdemultiplexer.uiautomator;

public interface ThreadedUIViewTreeParserListener {

	/**
	 * Hands back the root of the view tree once parsing is complete.
	 * @param rootNode Root of the view tree. May be null.
	 * @param deviceSerialNo Serial number of the device containing this view
	 * tree.
	 */
	public void doneParsingTreeView(
			UIViewTreeNode rootNode, 
			String deviceSerialNo);
}
