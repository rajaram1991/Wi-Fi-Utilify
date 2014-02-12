package com.wifitether.chatmodule;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;
import com.wifitether.R;

public class EditProfileActivity extends Activity implements QBCallback {

	
	public QBUser me = null;
	private String selectedImagePath;
	private String currentProfilePicturePath;
	private boolean profilePicChanged = false;
	private boolean isContentUpload = false;
	private boolean isProfilePicDownload = false;
	private boolean isInitialDownload = false;

    private ProgressDialog progressDialog;
	
	//MUST IMPLEMENT THIS METHOD
	public QBUser getMe(){
		return me;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
       
		Intent i = getIntent();
		
		me = (QBUser) i.getSerializableExtra("me");
		if(me==null){
			Log.i("AFRIN","me null");
			finishProfileEditCancel();
			return;
		}
		isInitialDownload = true;
		QBUsers.getUserByLogin(me.getLogin(),this);
		//setupProfileEdit();
		
	}

	private void setupProfileEdit() {
		isInitialDownload = false;
		progressDialog.setMessage("Loading");
		progressDialog.show();
		EditText fullName = (EditText) findViewById(R.id.editFullName);
		if(me.getFullName()!=null && me.getFullName().trim().length()>0)
			fullName.setText(me.getFullName());
		
		if(me.getFileId()!=null&&me.getFileId().intValue()>0){
			
			this.isProfilePicDownload = true;
			QBContent.getFile(me.getFileId(), this);
		}
		else{
			if(progressDialog!=null)
				progressDialog.dismiss();
		}
		Log.i("AFRIN","profile edit setup complete");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_profile, menu);
		return true;
	}
	
	public void updateProfile(View view){
		
		progressDialog.setMessage("Saving");
		progressDialog.show();
		EditText fullName = (EditText) findViewById(R.id.editFullName);
		String oldName = me.getFullName();
		String newName = fullName.getText().toString();
		Log.i("AFRIN", "oldname:"+oldName+"   newname:"+newName);
		if(newName!=null && newName.trim().length()>0){
			me.setFullName(newName);
			me.setOldPassword("password");
			
		}
		if(profilePicChanged){			 
			isContentUpload = true;
			final File img = new File(currentProfilePicturePath);
	        QBContent.uploadFileTask(img, true, this);
		}else{
			
			QBUsers.updateUser(me, this);
		}
		
	}
	
	public void cancelUpdateProfile(View view){
		finishProfileEditCancel();
	}

	@Override
	public void onComplete(Result result) {
		
		if(isContentUpload){
			if (result.isSuccess()) {
				if(result instanceof QBFileUploadTaskResult){
					Log.i("AFRIN", "QBFileUploadTaskResult");
	                QBFileUploadTaskResult qbFileUploadTaskResultq = (QBFileUploadTaskResult) result;
	            	QBFile qbFile = qbFileUploadTaskResultq.getFile();
	            	getMe().setFileId(qbFile.getId());
	            	me.setFileId(qbFile.getId());
	            	Log.i("AFRIN","updated profile id:"+getMe().getFileId());
	            	isContentUpload = false;
	            	QBUsers.updateUser(me, this);
				}else if(result instanceof QBUserResult){
					Log.i("AFRIN", "QBUserResult");
					QBUserResult qbUserResult = (QBUserResult)result;
					
				}
            } else {
            	finishProfileEditCancel();
            }	 
		}else if(isProfilePicDownload){
			if(result instanceof QBFileDownloadResult){
				isProfilePicDownload = false;
				QBFileDownloadResult qBFileDownloadResult = (QBFileDownloadResult)result;
				Drawable diffProfPic = Drawable.createFromStream(qBFileDownloadResult.getContentStream(),"image");
				Log.i("AFRIN", " me profile pic downloaded");
				ImageView imageView = (ImageView) findViewById(R.id.imageProfilePic);
				imageView.setImageDrawable(diffProfPic);
				if(progressDialog!=null)
				progressDialog.dismiss();
				
			}else if(result instanceof QBFileResult){
				
				Log.i("AFRIN", "got fileresult for me profile pic");
				QBFileResult qBFileResult = (QBFileResult)result;
				QBFile qbFile = qBFileResult.getFile();
				QBContent.downloadFile(qbFile, this);
				
			}
		}else if(isInitialDownload){
			isInitialDownload = false;
			if(result.isSuccess()){
				QBUserResult qbUserResult = (QBUserResult) result;
				QBUser user = (QBUser)qbUserResult.getUser();
				me = user;
				this.setupProfileEdit();
			}
		}
		else{
			
			
			if(result.isSuccess()){
				
				finishProfileEdit();
			}else{
				finishProfileEditCancel();
			}
		}
		
	}

	private void finishProfileEditCancel() {
		if(progressDialog!=null)
			progressDialog.dismiss();
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("me", me);
		setResult(RESULT_CANCELED, returnIntent);        
		finish();
	}

	private void finishProfileEdit() {
		if(progressDialog!=null)
			progressDialog.dismiss();
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("me", me);
		setResult(RESULT_OK, returnIntent);        
		finish();
	}

	@Override
	public void onComplete(Result arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void selectProfileImage(View view){
		Intent in = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, 1);

	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		   super.onActivityResult(requestCode, resultCode, data);     
		   
		     if (requestCode == 1 && resultCode == RESULT_OK && null != data){
		    	 profilePicChanged = true;
		         Uri selectedImage = data.getData();
		            String[] filePathColumn = { MediaStore.Images.Media.DATA };
		            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
		            cursor.moveToFirst();
		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            String picturePath = cursor.getString(columnIndex);
		            this.currentProfilePicturePath = picturePath;
		            cursor.close();
		            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
		            if(thumbnail.getHeight()>500 || thumbnail.getWidth()>500){
		            	Toast.makeText(this,"Please select a lower resolution image", Toast.LENGTH_SHORT).show();
		            	return;
		            }
		            ImageView imageView = (ImageView) findViewById(R.id.imageProfilePic);
		            imageView.setImageBitmap(thumbnail);
		            
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
