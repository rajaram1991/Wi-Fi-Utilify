package com.wifitether.chatmodule;

import com.wifitether.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class ChangeChatBGActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_chat_bg);
		Intent in = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, 1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.change_chat_bg, menu);
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		   super.onActivityResult(requestCode, resultCode, data);     
		    if (requestCode == 1 && resultCode == RESULT_OK && null != data){

		         Uri selectedImage = data.getData();
		            String[] filePathColumn = { MediaStore.Images.Media.DATA };
		            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
		            cursor.moveToFirst();
		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            String picturePath = cursor.getString(columnIndex);
		             cursor.close();
		             
		             Intent returnI = new Intent();
		             returnI.putExtra("picturePath", picturePath);
		             setResult(RESULT_OK, returnI);
		             finish();
		            /*Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
		            ImageView imageView = (ImageView) findViewById(R.id.imageProfilePic);
		            imageView.setImageBitmap(thumbnail);*/
		            
		     }else{
		    	 Intent returnI = new Intent();
	             setResult(RESULT_CANCELED, returnI);
	             finish();
		     }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
