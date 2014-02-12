package com.wifitether.wtether;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.wifitether.R;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;


public class SecondActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

	Button turn_on;
	EditText ssid_input,password_input;
	String name="",password="";
	CheckBox show_password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.secondpage);
		
		turn_on=(Button)this.findViewById(R.id.button_turnhotspot);
		ssid_input=(EditText)this.findViewById(R.id.editText_ssid);
		password_input=(EditText)this.findViewById(R.id.editText_password);
		show_password=(CheckBox)this.findViewById(R.id.checkBox_password);
		
		show_password.setOnCheckedChangeListener(this);
		turn_on.setOnClickListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.secondpage, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		
		name=ssid_input.getText().toString();
		password=password_input.getText().toString();
		
		password_input.setInputType(InputType.TYPE_CLASS_TEXT);
		
		if(isHotspotOn())
		{
			if(name.trim().length()>0 && password.trim().length()>0)
				switchOnHotspot(false);
			else
				Toast.makeText(getApplicationContext(), "Enter a valid SSID and Password", Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(name.trim().length()>0 && password.trim().length()>0)
				switchOnHotspot(true);
			else
				Toast.makeText(getApplicationContext(), "Enter a valid SSID and Password", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void switchOnHotspot(boolean key)
    {
	
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Method[] wmMethods = wifi.getClass().getDeclaredMethods();
        
        for(Method method: wmMethods){
            
            if(method.getName().equals("setWifiApEnabled")) {
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.SSID = name;
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.preSharedKey=password;
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                try {
                    
                    method.invoke(wifi, netConfig, key);
                   // if (netConfig.wepKeys!=null && netConfig.wepKeys.length>=1)
                    //mIsWifiEnabled = true;
                } catch (Exception e) {
                   
                }
            }
        }
     }
	
	public Boolean isHotspotOn()
	{
		boolean isWifiAPenabled = false;
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
		for(Method method: wmMethods){
		
			if(method.getName().equals("isWifiApEnabled")) {

				try {
					isWifiAPenabled= (Boolean) method.invoke(wifi);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
	
			}
		}
		return isWifiAPenabled;
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		
		if(arg0.isChecked())
		{
			password_input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		}
		else
		{
			password_input.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
		
		
	}
	
	

}
