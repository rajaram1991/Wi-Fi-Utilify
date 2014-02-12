package com.wifitether.chatmodule;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.wifitether.R;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * Activity provides interface for user auth.
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class MainActivity extends Activity implements QBCallback, View.OnClickListener {

    private static final String DEFAULT_LOGIN = "afrin1234";
    private static final String DEFAULT_PASSWORD = "password";

    /*private Button loginButton;
    private Button registerButton;
    */private Button multiChatButton;
    private EditText loginEdit;
    private EditText passwordEdit;
    private ProgressDialog progressDialog;

    private String login;
    private String password;
    private QBUser user;
    
    public static boolean isMultiChatOn = false;
    public static boolean isFirstAttempt = true;
    public static boolean isSessionInitiation = false;
    public static boolean isSessionTermination = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // UI stuff
        loginEdit = (EditText) findViewById(R.id.loginEdit1);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        loginEdit.setText(DEFAULT_LOGIN);
        passwordEdit.setText(DEFAULT_PASSWORD);
        passwordEdit.setVisibility(View.GONE);
        /*loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        */
        multiChatButton = (Button) findViewById(R.id.multiChatButton);
        /*loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        registerButton.setVisibility(View.GONE);
        */multiChatButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        
        
    }

    @Override
    public void onClick(View view) {
    	isMultiChatOn = false;
    	login = loginEdit.getText().toString();
        password = passwordEdit.getText().toString();

        user = new QBUser(login, password);

        progressDialog.show();
        switch (view.getId()) {
            
                
            case R.id.multiChatButton:
            	isSessionInitiation = true;
            	QBSettings.getInstance().fastConfigInit("5128", "zMpP9QckPDn-cgK", "GMZfhU2EuCZvLfn");

                // ================= QuickBlox ===== Step 2 =================
                // Authorize application.
                QBAuth.createSession(this);
                

            	
        }
    }

    @Override
    public void onComplete(Result result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if(result.isSuccess()&& isSessionTermination){
        	isSessionTermination = false;
        	Toast.makeText(this, "You have been successfully logged out of chat", Toast.LENGTH_SHORT).show();
        }
        else if(result.isSuccess() && isSessionInitiation){
        	isMultiChatOn = true;
        	isFirstAttempt = true;
        	isSessionInitiation = false;
        	QBUsers.signIn(user, MainActivity.this);
        }
        else if (result.isSuccess()&& isMultiChatOn) {
        	Intent intent = new Intent(this, MultiChatActivity.class);
            intent.putExtra("myId", user.getId());
            intent.putExtra("myLogin", user.getLogin());
            intent.putExtra("myPassword", user.getPassword());

            startActivityForResult(intent,1);
           
        }
        
        else if (result.isSuccess()) {
        	
            Intent intent = new Intent(this, UsersListActivity.class);
            intent.putExtra("myId", user.getId());
            intent.putExtra("myLogin", user.getLogin());
            intent.putExtra("myPassword", user.getPassword());

            startActivityForResult(intent,1);
            Toast.makeText(this, "You've been successfully logged in application",
                    Toast.LENGTH_SHORT).show();
        } else if (isFirstAttempt){
        		isFirstAttempt = false;
        		QBUsers.signUpSignInTask(user, MainActivity.this);
        }
    	else{
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                "please. Errors: " + result.getErrors()).create().show();
    	}
        
        
        
    }
    
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
    	isSessionTermination = true;
    	QBAuth.deleteSession(this);
    }

    @Override
    public void onComplete(Result result, Object context) { }
}