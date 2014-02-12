package com.example.onlinestreamer;



import com.wifitether.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		Button enterASiteButton = (Button) findViewById(R.id.enterAnAddressButton);
		Button demoButton = (Button) findViewById(R.id.showDemoButton);
		
		enterASiteButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent (MainMenu.this, PickAStream.class);
				startActivity(i);
				
			}
		});
		
		demoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent (MainMenu.this, OnlineStreamerActivity.class);
				startActivity(i);
			}

		});
		}

}
