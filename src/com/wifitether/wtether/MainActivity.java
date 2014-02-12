package com.wifitether.wtether;


import com.wifitether.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	    ImageView hotspot = (ImageView) findViewById(R.id.imageTether);   
    hotspot.setOnClickListener((OnClickListener) this);

    ImageView music = (ImageView) findViewById(R.id.imageMusic);   
    music.setOnClickListener((OnClickListener) this);
   
    ImageView chat = (ImageView) findViewById(R.id.imageChat);   
   chat.setOnClickListener((OnClickListener) this);
    
  /* ImageView file = (ImageView) findViewById(R.id.file);   
    file.setOnClickListener((OnClickListener) this);*/
    
    
	}

public void onClick(View view) {
		
	    switch (view.getId()) {
	    case R.id.imageTether:
	    	Intent intent = new Intent(MainActivity.this,SecondActivity.class);
	        startActivity(intent);
	    break;
	    case R.id.imageMusic:
	    	Intent intent1 = new Intent(MainActivity.this,com.example.onlinestreamer.MainMenu.class);
	    	startActivity(intent1);
            break;
	    case R.id.imageChat:
    	   	Intent intent2 = new Intent(MainActivity.this,com.wifitether.chatmodule.MainActivity.class);
			  startActivity(intent2);
			  break;
		    
	  }
	}
    ;
 }
 