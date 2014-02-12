package com.wifitether.chatmodule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.jivesoftware.smack.packet.Message;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.model.QBFileObjectAccess;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileObjectAccessResult;
import com.quickblox.module.content.result.QBFileResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;
import com.wifitether.R;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * List of users QuickBlox applications available to chat.
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class MultiChatActivity extends ProfileMenuActivity implements QBCallback {

    private EditText messageText;
    private TextView meLabel;
    private TextView friendLabel;
    private ViewGroup messagesContainer;
    private ScrollView scrollContainer;
    public QBUser me;
    private MultiChatController multiChatController;
    private boolean isDownloadMeProfile = false;
    private boolean isDownloadDiffProfile = false;
    private boolean isFirstTime = false;
    private boolean isFileTransfer = false;
    private boolean isFileTransferDownload = false;
    

    private ProgressDialog progressDialog;
    
    public HashMap<Integer,Drawable> profilePicList = new HashMap<Integer,Drawable>();
    
    public QBUser getMe(){
		return me;
	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multichat);

        // Load QBUser objects from bundle (passed from previous activity).
        Bundle extras = getIntent().getExtras();
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

       me = new QBUser();
        me.setId(extras.getInt("myId"));
        me.setLogin(extras.getString("myLogin"));
        me.setPassword(extras.getString("myPassword"));
        //QBUsers.getUser(extras.getInt("myId"), this,"Test");
         QBUsers.getUserByLogin(extras.getString("myLogin"), this);
    }
    
    public void initiateFileTransfer(View view){
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
        intent.setType("*/*"); 
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    10);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getOnWithChat(){

        // UI stuff
        messagesContainer = (ViewGroup) findViewById(R.id.messagesContainer);
        scrollContainer = (ScrollView) findViewById(R.id.scrollContainer);

        Button sendMessageButton = (Button) findViewById(R.id.sendButton);
        sendMessageButton.setOnClickListener(onSendMessageClickListener);
        

        Button sendMessageReceiverButton = (Button) findViewById(R.id.sendButtonReceiver);
        sendMessageReceiverButton.setOnClickListener(onSendMessageReceiverClickListener);

        messageText = (EditText) findViewById(R.id.messageEdit);
        meLabel = (TextView) findViewById(R.id.meLabel);
        friendLabel = (TextView) findViewById(R.id.friendLabel);
        meLabel.setText(getMyName() + " (me)");
        
        // ================= QuickBlox ===== Step 5 =================
        // Get chat login based on QuickBlox user account.
        // Note, that to start chat you should use only short login,
        // that looks like '17744-1028' (<qb_user_id>-<qb_app_id>).
        String chatLogin = QBChat.getChatLoginShort(me);

        // Our current (me) user's password.
        String password = me.getPassword();

        if (me != null) {


            // ================= QuickBlox ===== Step 6 =================
            // All chat logic can be implemented by yourself using
            // ASMACK library (https://github.com/Flowdalic/asmack/downloads)
            // -- Android wrapper for Java XMPP library (http://www.igniterealtime.org/projects/smack/).
            multiChatController = new MultiChatController(chatLogin, password);
            multiChatController.setOnMessageReceivedListener(onMessageReceivedListener);

            // ================= QuickBlox ===== Step 7 =================
            // Get friend's login based on QuickBlox user account.
            // Note, that for your companion you should use full chat login,
            // that looks like '17792-1028@chat.quickblox.com' (<qb_user_id>-<qb_app_id>@chat.quickblox.com).
            // Don't use short login, it
           
            multiChatController.startChat(getApplicationContext(),me);
        }
    }
    
    
	private String getMyName(){
		
    	if(me==null)
    		return "";
    	else if(me.getFullName()==null || me.getFullName().trim().length()<=0)
    		return me.getLogin();
    	else 
    		return me.getFullName();
    }
    
	//message format : profile_pic_id###<user name>:message
    private void sendMessage() {
    	try{
        if (messageText != null && messageText.getText().toString().trim().length()>0) {
            String messageString = messageText.getText().toString();  
            int profileId = 0;
            if(me.getFileId()!=null)
            	profileId = me.getFileId().intValue();
            multiChatController.sendMessage(profileId+"###<"+getMyName()+">:"+messageString);
            messageText.setText("");
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(MultiChatActivity.this);
            if(sharedPrefs==null){
            	Log.i("AFRIN","shared prefs null");
            }
            else{
            	Log.i("AFRIN","shared prefs NOT null");
            	if(sharedPrefs.getBoolean("prefShowSelfChat",true))
            		showMessage(profileId+"###"+messageString, true);
            }
            
        }
    	}catch(Exception e){
    		e.printStackTrace();
    		cleanup();
    	}
    }
    
    private int sendMyMessage(String messageString) {
    	try{
            int profileId = 0;
            if(me.getFileId()!=null)
            	profileId = me.getFileId().intValue();
            multiChatController.sendMessage(profileId+"###<"+getMyName()+">:"+messageString);
            return 1;
            
    	}catch(Exception e){
    		return -1;
    	}
    }
    
    private void sendMessageReceiver() {
    	Log.i("AFRIN","Receiver : I am clicked");
    	EditText messageEditReceiver = (EditText)findViewById(R.id.messageEditReceiver);
       
        if (messageEditReceiver != null && messageEditReceiver.getText().toString().trim().length()>0) {
            String messageString = messageEditReceiver.getText().toString();  
            messageEditReceiver.setText("");
            showMessage(messageString, false);
        }
    }
    
    public String receiverMsgString;

    private MultiChatController.OnMessageReceivedListener onMessageReceivedListener = new MultiChatController.OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(final Message message) {
        	Log.i("AFRIN", "onMessageReceived"+message.getFrom()+" "+message.getPacketID());
            receiverMsgString = message.getBody();
            
            	 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                    	 try{
                    	 EditText messageEditReceiver = (EditText)findViewById(R.id.messageEditReceiver);
                         messageEditReceiver.setText(receiverMsgString);
                         receiverMsgString=null;
                         Log.i("AFRIN","edittextval:"+messageEditReceiver.getText().toString());
                         
                         Button sendButtonReceiver = (Button)findViewById(R.id.sendButtonReceiver);
                         Log.i("AFRIN","performing receiver click");
                         
                         if(sendButtonReceiver.performClick())
                         	Log.i("AFRIN","performing receiver click listener");
                         else
                         	Log.i("AFRIN","NO performing receiver click");
                         }catch(Exception e){
                         	Log.i("AFRIN","Exception onMessageReceivedListener");
                         	e.printStackTrace();
                         }
                     }
                     
                 });
            
            
            //showMessage(messageString, false);
        }
    };

    private void showMessage(String messageStr, boolean leftSide) {
    	
    	try{
    	Log.i("AFRIN","messageStr:"+messageStr);
    	String []messageBlocks = messageStr.split("###");
    	Log.i("AFRIN", messageBlocks[0]);
    	if(leftSide)
    		Log.i("AFRIN", "length:"+messageBlocks.length+"boolean:left");
    	else
    	    	Log.i("AFRIN", "length:"+messageBlocks.length+"boolean:right");
    	String message = messageBlocks[1];
    	Log.i("AFRIN","chat message:"+message);
    	if(message.contains("<File_ID>")){
    		String []ftBlocks = message.split(":::");
    		String fromUser = ftBlocks[0];
    		Log.i("AFRIN","Got filedownload request:"+ftBlocks[2]+" with name :"+ftBlocks[3]);
    		message = new String(fromUser+" has sent a file");
    		
    		isFileTransferDownload = true;
    		QBContent.downloadFileTask(Integer.parseInt(ftBlocks[2]), this);
    		
    	}
    	final LinearLayout chatLine = new LinearLayout(MultiChatActivity.this);
    	LinearLayout.LayoutParams paramsChatLine = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	chatLine.setOrientation(LinearLayout.HORIZONTAL);
    	if (!leftSide) {            
            paramsChatLine.gravity = Gravity.RIGHT;
        }
    	chatLine.setLayoutParams(paramsChatLine);
    	
    	
    	//for textview
        final TextView textView = new TextView(MultiChatActivity.this);
        textView.setTextColor(Color.BLACK);
        textView.setText(message);

        int bgRes = R.drawable.left_message_bg;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (!leftSide) {
            bgRes = R.drawable.right_message_bg;
            params.gravity = Gravity.RIGHT;
        }

        textView.setLayoutParams(params);
        textView.setBackgroundResource(bgRes);
        
        
        //for imageview
        final ImageView imageView = new ImageView(MultiChatActivity.this);
        
        	int imageId=Integer.parseInt(messageBlocks[0]);
        	if(imageId>0){
        		if(this.profilePicList.get(imageId)==null){
        			Log.i("AFRIN", "will initiate download of profile image. setting temp pic as prof image for now");
        			imageView.setImageResource(R.drawable.no_avatar);
        		}else{
        			Log.i("AFRIN", "diff has profile image");
	        	imageView.setImageDrawable(this.profilePicList.get(imageId));
        		}
	        }else{
	        	Log.i("AFRIN", "diff doesn't have profile image");
	        	imageView.setImageResource(R.drawable.no_avatar);
	        }
        
        
        
        LinearLayout.LayoutParams paramsImage = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        paramsImage.height = 100;
        paramsImage.width = 100;
        if (!leftSide) {
            
        	paramsImage.gravity = Gravity.RIGHT;
        }
        imageView.setLayoutParams(paramsImage);

        if (leftSide) {
        	chatLine.addView(imageView);
            chatLine.addView(textView);
        }else{
        	chatLine.addView(textView);
        	chatLine.addView(imageView);
        }
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	Log.i("AFRIN", "adding view");
            	messagesContainer.addView(chatLine);                

                // Scroll to bottom
                if (scrollContainer.getChildAt(0) != null) {
                    scrollContainer.scrollTo(scrollContainer.getScrollX(), scrollContainer.getChildAt(0).getHeight());
                }
                scrollContainer.fullScroll(View.FOCUS_DOWN);
            }
            
        });
        
        if(Integer.parseInt(messageBlocks[0])>0 && !this.profilePicList.containsKey(Integer.parseInt(messageBlocks[0]))&&!leftSide){
        	Log.i("AFRIN", "isDownloadDiffProfile:"+messageBlocks[0]);
        	isDownloadDiffProfile = true;
        	QBContent.getFile(Integer.parseInt(messageBlocks[0]), this);
        	//new QBDownloader().start();
        	
        	//QBContent.getFile(82703, this);
        	//QBUsers.getUsers(this);
        	//new DownloaderThread(this).start();
        	
        }
    	}catch(Exception e){
    		e.printStackTrace();
    		cleanup();
    	}
    }    
       
    private View.OnClickListener onSendMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendMessage();
        }
    };
    
    private View.OnClickListener onSendMessageReceiverClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	sendMessageReceiver();
        }
    };
    
	@Override
	public void setNewMe(QBUser newMe) {
		this.me = newMe;
		updateWithNewProfile();
	}

	@Override
	public void updateWithNewProfile() {
		
		meLabel.setText(getMyName() + " (me)");
		if(me!=null && me.getFileId()!=null){
			isDownloadMeProfile = true;
			
			QBContent.downloadFile(""+me.getFileId(), this);
		}
		
	}
	
	public Drawable myProfileImage;
	public Integer currentProfilePicDownloadId = 0;
	public int count=0;

	@Override
	public void onComplete(Result result) {
		Log.i("AFRIN", "just on complete");
		if(result.isSuccess()){
if(isDownloadMeProfile){
				
				isDownloadMeProfile = false;
				if(result instanceof QBFileDownloadResult){
					QBFileDownloadResult qBFileDownloadResult = (QBFileDownloadResult)result;
					myProfileImage = Drawable.createFromStream(qBFileDownloadResult.getContentStream(),"image");
				}else{
					Log.i("AFRIN", "unknown result of type :"+result.getClass().toString());
				}
				
				
			}else if(isDownloadDiffProfile){
					if(result instanceof QBFileDownloadResult && currentProfilePicDownloadId>0){
						isDownloadDiffProfile = false;
						Log.i("AFRIN", "diff user's "+currentProfilePicDownloadId+" profile pic downloaded");
						QBFileDownloadResult qBFileDownloadResult = (QBFileDownloadResult)result;
						Drawable diffProfPic = Drawable.createFromStream(qBFileDownloadResult.getContentStream(),"image");
						this.profilePicList.put(currentProfilePicDownloadId, diffProfPic);
						/*if(count++==0){
							isDownloadDiffProfile = true;
						QBContent.getFile(82657, this);
						}*/
					}else if(result instanceof QBFileResult){
						
						Log.i("AFRIN", "got fileresult");
						QBFileResult qBFileResult = (QBFileResult)result;
						QBFile qbFile = qBFileResult.getFile();
						currentProfilePicDownloadId = qbFile.getId();
						QBContent.downloadFile(qbFile, this);
						
					}else if(result instanceof QBFileObjectAccessResult){
						
						Log.i("AFRIN", "got QBFileObjectAccessResult");
						QBFileObjectAccessResult qBFileObjectAccessResult = (QBFileObjectAccessResult)result;
						QBFileObjectAccess qbFileObjectAccess = qBFileObjectAccessResult.getFileObjectAccess();
						Log.i("AFRIN","file params1:"+qbFileObjectAccess.getParams());
						Log.i("AFRIN","file params2:"+qbFileObjectAccess.getType());
						Log.i("AFRIN","file params3:"+qbFileObjectAccess.getId());
						
					}else{
						Log.i("AFRIN", "unknown result of type :"+result.getClass().toString());
					}
			}else if(isFileTransfer){
				if(result instanceof QBFileResult){
					isFileTransfer = false;
					Log.i("AFRIN", "got fileresult in isFileTransfer");
					QBFileResult qBFileResult = (QBFileResult)result;
					QBFile qbFile = qBFileResult.getFile();
					int fileTransferId = qbFile.getId();
					this.sendMyMessage("::<File_ID>:::"+fileTransferId+":::"+qbFile.getName());
					this.successFileTransfer();
					
				}else if(result instanceof QBFileUploadTaskResult){
						Log.i("AFRIN", "QBFileUploadTaskResult");
		                QBFileUploadTaskResult qbFileUploadTaskResultq = (QBFileUploadTaskResult) result;
		            	QBFile qbFile = qbFileUploadTaskResultq.getFile();
		            	isFileTransfer = false;
						Log.i("AFRIN", "got fileresult in isFileTransfer");
						int fileTransferId = qbFile.getId();
						this.sendMyMessage("::<File_ID>:::"+fileTransferId+":::"+qbFile.getName());
						this.successFileTransfer();		            	
					
				}else{
					Log.i("AFRIN", "unknown result of type :"+result.getClass().toString());
				}
		}else if(isFileTransferDownload){
			isFileTransferDownload = false;
			QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
			Log.e("Afrin","testing0");
            //byte[] content = qbFileDownloadResult.getContent();       // that's downloaded file content
            Log.e("Afrin","testing1");
            InputStream inputStream = qbFileDownloadResult.getContentStream(); // that's downloaded file content
            Log.e("Nibin","testing2");
            int randNum = (int) (1000+ Math.random()* (9999));
            String fileUrl = "/mnt/sdcard/download";
            File SDCardRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(SDCardRoot,randNum+".png");
            OutputStream outputStream = null;;
            try{
            	
            
	             outputStream=new FileOutputStream(file);
	            
	            byte buf[]=new byte[1024];
	            int len;
	            while((len=inputStream.read(buf))>0)
	            outputStream.write(buf,0,len);
	            
	            Log.i("AFRIN","about to toast");
	            Toast.makeText(this, "File Downloaded Successfully as "+ randNum+".png", Toast.LENGTH_LONG).show();
	            }
            catch(Exception e)  {
	            	e.printStackTrace();
	            }finally{
	            	if(outputStream!=null)
	            		try{
	            			outputStream.close();
	            		}catch(Exception e){
	            			e.printStackTrace();
	            		}
	            	if(inputStream!=null)
	            		try{
	            			inputStream.close();
	            		}catch(Exception e){
	            			e.printStackTrace();
	            		}
            }
            
            
            //Log.d("file downloaded successful", qbFileDownloadResult.getContent().toString());
        }
		else if(result instanceof QBUserResult){
				QBUserResult qbUserResult = (QBUserResult) result;
				me = qbUserResult.getUser();
				Bundle extras = getIntent().getExtras();
				me.setId(extras.getInt("myId"));
		        me.setLogin(extras.getString("myLogin"));
		        me.setPassword(extras.getString("myPassword"));
				getOnWithChat();
			}
			
		} //result.isSuccess();
		
	}

	@Override
	public void onComplete(Result result, Object obj) {
		
		Log.i("AFRIN", "on complete with obj");
		
	}
	
	protected void onResume(){
		super.onResume();
		/*Log.i("AFRIN","onResume");
		Intent resultIntent = new Intent();
		setResult(RESULT_OK,resultIntent);
		finish();*/
	}
    
	protected void onRestart(){
		super.onRestart();
		//cleanup();
	}
	


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	    	cleanup();
	        
	    }
	    return super.onKeyDown(keyCode, event);
	}

	private void cleanup() {
		
		
		if(multiChatController!=null){
			if(multiChatController.muc2!=null){
				try{
					if(multiChatController.muc2.isJoined()){
						multiChatController.muc2.leave();
					}
				}catch(Exception e){
					Log.i("AFRIN","chatroom leave exception in cleanup");
					e.printStackTrace();
				}
			}
			
			if(multiChatController.connection!=null){
				try{
					new Thread(new Runnable(){

						@Override
						public void run() {
							try{
							multiChatController.connection.disconnect();
							}catch(Exception e){
								Log.i("AFRIN","connection disconnect exception in cleanup");
								e.printStackTrace();
							}
							
						}}).start();
					
				}catch(Exception e){
					Log.i("AFRIN","connection disconnect exception in cleanup");
					e.printStackTrace();
				}
			}
		}
		Intent resultIntent = new Intent();
		setResult(RESULT_OK,resultIntent);
		finish();
		
	}
	
	public void failedFileTransfer(){
		if(progressDialog!=null)
			progressDialog.dismiss();
		Toast.makeText(MultiChatActivity.this, "File Transfer failed!", Toast.LENGTH_SHORT).show();
	}
	
	public void successFileTransfer(){
		if(progressDialog!=null)
			progressDialog.dismiss();
		Toast.makeText(MultiChatActivity.this, "File Transfer successful!", Toast.LENGTH_SHORT).show();
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==10){
			if (resultCode == RESULT_OK) {
	            // Get the Uri of the selected file 
	            Uri uri = data.getData();
	            Log.d("AFRIN", "File Uri: " + uri.toString());
	            // Get the path
	            String path = getMyFilePath(this, uri);
	            if(path==null){
	            	failedFileTransfer();
	            }else{
	            	File file = new File(path);
	            	this.isFileTransfer = true;
	            	this.progressDialog.setMessage("File Transfer in progress");
	            	this.progressDialog.show();
	    	        QBContent.uploadFileTask(file, true, this);
	            	
	            }
	            Log.d("AFRIN", "File Path: " + path);
	            // Get the file instance
	            // File file = new File(path);
	            // Initiate the upload
	        }
		}else
			super.onActivityResult(requestCode, resultCode, data);
	}
	
	public  String getMyFilePath(Context context, Uri uri) {
	    if ("content".equalsIgnoreCase(uri.getScheme())) {
	        String[] projection = { "_data" };
	        Cursor cursor = null;

	        try {
	            cursor = context.getContentResolver().query(uri, projection, null, null, null);
	            int column_index = cursor.getColumnIndexOrThrow("_data");
	            if (cursor.moveToFirst()) {
	                return cursor.getString(column_index);
	            }
	        } catch (Exception e) {
	           e.printStackTrace();
	           return null;
	        }
	    }
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	} 
}