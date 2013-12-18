package com.luyun.msg;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.*;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MsgPlugin extends CordovaPlugin implements MsgListener {

	private static final String TAG = "MsgPlugin";
//	private static final String EVENT_JS = "EVENT_JS";
	int majorRelease;
	int minorRelease;
	boolean isConnected;
	private ZmqThread zmqThread = null;
	MsgListener mListener;
	public String uid;
	public String key;
	private ConnectivityChangeReceiver mConnectivityChangeReceiver;
	
	// JNI
	static {
		Log.d(TAG, "loadLibrary");
		System.loadLibrary("msg");
	}
	
	private native void makeMsgHandler();
	private native void setUidAndKey(String uid, String key);
	private native void connect();
	private native void checkIn(Checkin checkin);
	
	CallbackContext cbContext;
	

	/* (non-Javadoc)
	 * @see org.apache.cordova.CordovaPlugin#execute(java.lang.String, org.json.JSONArray, org.apache.cordova.CallbackContext)
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(TAG, "execute");
        if ("init".equals(action)) {
//        	cbContext = callbackContext;
            init();
            return true;
        } else if ("trans".equals(action)) {
        	String strMsg = args.getString(0);
        	trans(strMsg);
        	return true;
        }
		return false;
	}
	
//	@Override
//    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
//		Log.d(TAG, "initialize");
//		makeMsgHandler();
//		
//		mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
//		Log.d(TAG, "cordova: " + cordova);
//		Log.d(TAG, "cordova getActivity: " + cordova.getActivity().toString());
//		cordova.getActivity().registerReceiver(mConnectivityChangeReceiver, new IntentFilter(
//				ConnectivityManager.CONNECTIVITY_ACTION));
//
//		TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//		String imei = tm.getDeviceId();	//取得IMEI
//		String mobile = tm.getLine1Number();	//取得手机号
//		String android_id = Secure.getString(cordova.getActivity().getContentResolver(), Secure.ANDROID_ID);
//		String simSerialNumber = tm.getSimSerialNumber();
//		String subscriberId = tm.getSubscriberId();
//		
//		Log.d(TAG, "imei: " + imei
//				+ "\nmobile: " + mobile
//				+ "\nandroid_id: " + android_id
//				+ "\nsimSerialNumber: " + simSerialNumber
//				+ "\nsubscriberId: " + subscriberId);
//
//		uid = imei;
//		key = uid;
//		Log.i(TAG, "uid = " + uid);
//		setUidAndKey(uid, key);
//		
//		zmqThread = new ZmqThread();
//		zmqThread.start();
//		zmqThread.setOnMsgListener(MsgPlugin.this);
//        
//    }
	
	public void init()
	{
		Log.d(TAG, "init");
		makeMsgHandler();
		
		mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
		Log.d(TAG, "cordova: " + cordova);
		Log.d(TAG, "cordova getActivity: " + cordova.getActivity().toString());
		cordova.getActivity().registerReceiver(mConnectivityChangeReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();	//取得IMEI
		String mobile = tm.getLine1Number();	//取得手机号
		String android_id = Secure.getString(cordova.getActivity().getContentResolver(), Secure.ANDROID_ID);
		String simSerialNumber = tm.getSimSerialNumber();
		String subscriberId = tm.getSubscriberId();
		
		Log.d(TAG, "imei: " + imei
				+ "\nmobile: " + mobile
				+ "\nandroid_id: " + android_id
				+ "\nsimSerialNumber: " + simSerialNumber
				+ "\nsubscriberId: " + subscriberId);

		uid = imei;
		key = uid;
		Log.i(TAG, "uid = " + uid);
		setUidAndKey(uid, key);
		
		zmqThread = new ZmqThread();
		zmqThread.start();
		zmqThread.setOnMsgListener(MsgPlugin.this);
	}
	
	public boolean isConnectedToInternet() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) cordova.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		// mobile 3G Data Network
		State mobile = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		// wifi
		State wifi = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();

		if (mobile == State.CONNECTED || mobile == State.CONNECTING)
			return true;
		if (wifi == State.CONNECTED || wifi == State.CONNECTING)
			return true;

		return false;
	}
	
	public class ConnectivityChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isConnectedToInternet()) {
				Log.d(TAG, "internet availabe, connect");
				connect();
				checkIn();
			}
		}
	}	

	void checkIn() {
		// 获取版本信息
		try {
			PackageInfo info = cordova.getActivity().getPackageManager().getPackageInfo(
					cordova.getActivity().getPackageName(), 0);
			String versionName = info.versionName;
			Pattern p = Pattern.compile("(\\d)(\\.)(\\d)");
			Matcher m = p.matcher(versionName);
			if (m.find()) {
				majorRelease = Integer.parseInt(m.group(1));
				minorRelease = Integer.parseInt(m.group(3));
				Log.d(TAG, String.format("major=%d, minor=%d", majorRelease, minorRelease));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Checkin ci = new Checkin();
		ci.deviceModel = android.os.Build.MODEL;
		// ci.osType = OsType.ANDROID;
		ci.osVersion = android.os.Build.VERSION.SDK;
		ci.majorRelease = majorRelease;
		ci.minorRelease = minorRelease;

		checkIn(ci);
	}
	
	// ---- new thread ----

	public class ZmqThread extends Thread {
		// long msgDispatcherPtr_;
		private native void makeMsgDispatcher();
		private native void setListener(MsgListener listener);
		private native void dispatch();

		public ZmqThread() {
			super();
			makeMsgDispatcher();
		}

		void setOnMsgListener(MsgListener listener) {
			setListener(listener);
		}

		@Override
		public void run() {
			Log.d(TAG, "run");
			dispatch();
		}
	}

	public native void trans(String trans);

	// ---- MsgListener interface ----
	
	@Override
	public void onCheckin(Checkin checkin) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCheckin, majorRelease: " + checkin.majorRelease
				+ " minorRelease: " + checkin.minorRelease
				+ " download_url: " + checkin.downloadUrl
				+ " desc: " + checkin.desc);
		
		if (checkin.majorRelease > majorRelease) {
			onSoftwareUpgrade(checkin.majorRelease, checkin.minorRelease, 
					checkin.downloadUrl, checkin.desc, true);
		} else if (checkin.minorRelease > minorRelease) {
			onSoftwareUpgrade(checkin.majorRelease, checkin.minorRelease,
					checkin.downloadUrl, checkin.desc, false);
		}
		
	}
	
//    String javaScriptEventTemplate =
//            "var e = new CustomEvent(''EVENT_TRANS'', '{' trans: ''{0}'' '}');\n" +
//            "document.dispatchEvent(e);";
    
    String javaScriptEventTemplate =
            "var e = document.createEvent(''Events'');\n" +
            "e.initEvent(''EVENT_TRANS'');\n" +
            "e.trans = ''{0}'';\n" +
            "document.dispatchEvent(e);";
    
	@Override
	public void onTrans(String trans) {
		Log.d(TAG, "onTrans: " + trans);
		String tmpMsg = "[ { \"os_type\": \"android\", \"os_version\": \"5.1\", \"version\": 1, \"release\": 0 }, { \"os_type\": \"ios\", \"os_version\": \"5.1\", \"version\": 1, \"release\": 0 } ]";
        String command = MessageFormat.format(javaScriptEventTemplate, trans);
        String tmpCmd = MessageFormat.format(javaScriptEventTemplate, tmpMsg);
        Log.v(TAG, command);
        Log.v(TAG, tmpCmd);
        webView.sendJavascript(command);
	}
	
	public void onSoftwareUpgrade(int major, int minor, String url, String desc, boolean force) {
		final String upgradeUrl = url;
		String msg = String.format("您的当前版本: %d.%d, 最新版本: %d.%d\n%s\n", 
				majorRelease, minorRelease, major, minor, desc);

		final AlertDialog.Builder alert = new AlertDialog.Builder(cordova.getActivity())
		.setTitle("升级提示")
		.setMessage(msg)
		.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				/* User clicked OK so do some stuff */
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(upgradeUrl));
				Log.d(TAG, "download url: " + upgradeUrl);
				cordova.getActivity().startActivity(intent);
//				System.exit(0);
			}
		});
		
		if (!force) {
			alert.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked Cancel */
				}
			});
		}
		
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				alert.show();
			}
		});
	}
}
