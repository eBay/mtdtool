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
 * Class: ToggleAirplaneModeActivity
 * 
 * Description:
 * When executed, this app will toggle the state of airplane mode on a device.
 */

package com.ebay.toggleairplanemode;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class ToggleAirplaneModeActivity extends Activity {

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toggle_airplane_mode);
        
        // Check the current value for airplane mode.
        boolean isEnabled = 
        		Settings.System.getInt(
        				getContentResolver(), 
        				Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        
        // Toggle airplane mode.
        Settings.System.putInt(
        		getContentResolver(), 
        		Settings.System.AIRPLANE_MODE_ON, 
        		isEnabled ? 0:1);
        
        // Post an intent to reload
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", !isEnabled);
        sendBroadcast(intent);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_toggle_airplane_mode, menu);
        return true;
    }
}
