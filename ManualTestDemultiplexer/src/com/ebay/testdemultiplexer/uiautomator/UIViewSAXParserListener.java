package com.ebay.testdemultiplexer.uiautomator;

public interface UIViewSAXParserListener {

	/**
	 * Notify the listener that parsing of the XML file is complete. Pass back
	 * the root node.
	 * @param rootNode Root node of the parsed XML.
	 */
	public void doneParsingXML(UIViewTreeNode rootNode);
}
