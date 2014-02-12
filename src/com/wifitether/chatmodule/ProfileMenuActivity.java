package com.wifitether.chatmodule;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import com.quickblox.core.model.QBEntity;
import com.quickblox.module.users.model.QBUser;
import com.wifitether.R;

public abstract class ProfileMenuActivity extends Activity {
	
	//must be set by child classes
	public QBUser me;

	public abstract QBUser getMe();
	
	public abstract void setNewMe(QBUser newMe);
	
	public abstract void updateWithNewProfile();

	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.edit_profile:  
            	callEditProfile();
                return true;
            case R.id.menu_settings:
            	callSettings();
            	return true;
            case R.id.menu_change_background:
            	callChangeChatBG();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void callEditProfile() {
    	Intent i = new Intent(ProfileMenuActivity.this,EditProfileActivity.class);    	
    	i.putExtra("me", (QBEntity)getMe());
    	startActivityForResult(i,1);
		
	}
    
    private void callSettings() {
    	Intent i = new Intent(ProfileMenuActivity.this,UserSettingsActivity.class);    	
    	startActivityForResult(i,2);
		
	}
    
    private void callChangeChatBG() {
    	Intent i = new Intent(ProfileMenuActivity.this,ChangeChatBGActivity.class);    	
    	startActivityForResult(i,3);
		
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i("AFRIN", "on acitivty result profile menu acitivity, me is "+me==null?"null":"not null");
    	  if (requestCode == 1) {
    		  
    	     if(resultCode == RESULT_OK){  
    	    	 setNewMe((QBUser) data.getSerializableExtra("me"));
    	         Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();         
    	     }
    	     if (resultCode == RESULT_CANCELED) {    
    	    	 Toast.makeText(getApplicationContext(), "Profile not updated", Toast.LENGTH_SHORT).show();         
    	    	    
    	     }
    	  }else if (requestCode == 2) {

     	       
     	    	 Toast.makeText(getApplicationContext(), "Settings updated", Toast.LENGTH_SHORT).show();         
     	    	    
     	     
     	  }else if(requestCode == 3){
     		  String picturePath = data.getStringExtra("picturePath");
     		  if(picturePath!=null){
     			 Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
     			 ScrollView sv = (ScrollView)findViewById(R.id.scrollContainer);
     			 sv.setBackground(new BitmapDrawable(getResources(),thumbnail));
     			Toast.makeText(getApplicationContext(), "Chat Background Changed!", Toast.LENGTH_SHORT).show();
     		  }else{
     			 Toast.makeText(getApplicationContext(), "Chat Background Could Not Be Changed! Please try again", Toast.LENGTH_SHORT).show();
     		  }
     		 
	            
     	  }else{
    		  Toast.makeText(getApplicationContext(), "Update failed. Try again", Toast.LENGTH_SHORT).show();         
      	    
    	  }
    	}


}
