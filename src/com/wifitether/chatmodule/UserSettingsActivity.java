package com.wifitether.chatmodule;

import com.wifitether.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;

public class UserSettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_settings, menu);
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	    	finish();
	        
	    }
	    return super.onKeyDown(keyCode, event);
	}

}
