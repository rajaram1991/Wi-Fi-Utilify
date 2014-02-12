package com.wifitether.chatmodule;

import android.content.Context;

public class InitiStaticCode {

	    public static void initStaticCode(Context ctx) {
	            // This has the be the application class loader,
	            // *not* the system class loader
	            ClassLoader appClassLoader = ctx.getClassLoader();

	            try {
	                    Class.forName(org.jivesoftware.smackx.ServiceDiscoveryManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smack.PrivacyListManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.XHTMLManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.muc.MultiUserChat.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.filetransfer.FileTransferManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.LastActivityManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smack.ReconnectionManager.class.getName(), true, appClassLoader);
	                    Class.forName(org.jivesoftware.smackx.commands.AdHocCommandManager.class.getName(), true, appClassLoader);
	            } catch (ClassNotFoundException e) {
	                    throw new IllegalStateException("Could not init static class blocks", e);
	            }
	    }
	}
