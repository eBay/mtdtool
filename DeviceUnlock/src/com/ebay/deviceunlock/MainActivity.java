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
 * Class: MainActivity
 * 
 * Description:
 * When executed, this app will wake up a device and ready for unlocking. This
 * is the first part of the wake/unlock operation. The rest is handled by
 * key inputs to the device and is completed elsewhere.
 */

package com.ebay.deviceunlock;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onAttachedToWindow()
	 */
	@Override
	public void onAttachedToWindow() {

        Window window = getWindow();
        window.addFlags(
        		WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        		+ WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        		+ WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        		+ WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}
