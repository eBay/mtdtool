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
 * Class: UIViewSAXParserListener
 * 
 * Description: 
 * Listener interface for receiving notifications when parsing of the 
 * UIAutomation dump file is complete.
 */

package com.ebay.testdemultiplexer.uiautomator;

public interface UIViewSAXParserListener {

	/**
	 * Notify the listener that parsing of the XML file is complete. Pass back
	 * the root node.
	 * @param rootNode Root node of the parsed XML.
	 */
	public void doneParsingXML(UIViewTreeNode rootNode);
}
