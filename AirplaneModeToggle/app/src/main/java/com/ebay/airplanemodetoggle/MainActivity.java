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

package com.ebay.airplanemodetoggle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Global variable request code
    private static final int WRITE_PERMISSION_REQUEST = 5000;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                // read the airplane mode setting
                boolean isEnabled = Settings.System.getInt(
                        getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON, 0) == 1;

                // toggle airplane mode
                Settings.System.putInt(
                        getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);

                // Post an intent to reload
                Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", !isEnabled);
                sendBroadcast(intent);
            } catch (ActivityNotFoundException e) {
                Log.e("AirplaneMode", e.getMessage());
            }
        } else {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                try {
                    Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, "Not able to set airplane mode", Toast.LENGTH_SHORT).show();
                }
            }
        }
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
