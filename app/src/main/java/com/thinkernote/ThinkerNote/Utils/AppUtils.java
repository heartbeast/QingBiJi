package com.thinkernote.ThinkerNote.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.thinkernote.ThinkerNote.General.TNSettings;

public class AppUtils {
	private static final String TAG = "AppUtils";
	
    /**
     * 閼惧嘲褰囬張顒佹簚閹靛婧�閸�?
     * @param act
     * @return 閺堫剚婧�閹靛婧�閸欓鐖滈敍瀹痷ll娑撻缚骞忛崣鏍︾瑝閸掔増澧滈張鍝勫娇閻�?
     */
	public static String getPhoneNumber(Activity act) {
		TelephonyManager tm = (TelephonyManager) act
				.getSystemService(Activity.TELEPHONY_SERVICE);
		String phoneNumber = tm.getLine1Number();
		if (phoneNumber == null || phoneNumber.equals(""))
			return null;
		phoneNumber = phoneNumber.replace("+86", "").trim();
		phoneNumber = phoneNumber.replace(" ", "");
		if (phoneNumber.length() != 11)
			return null;

		return phoneNumber;
	}

    /**
     * 閼惧嘲褰嘺pp閻ㄥ嫰绮拋顦奱ta鐠侯垰绶�
     * @return
     */
	public static String getAppRootPath() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().getPath()
					+ "/Android/data/"
					+ TNSettings.getInstance().appContext.getPackageName()
					+ "/";
		} else {
			return TNSettings.getInstance().appContext.getFilesDir()
					.getPath() + "/";
		}
	}

    /**
     * 閼惧嘲褰嘺pp閻ㄥ嫪澶嶉弮鍓佹窗瑜�?
     * @param fileName
     * @return a
     */
	public static String getTmpPath(String fileName) {
        return getAppRootPath() + "tmp/" + fileName;
	}

    /**
     * 閼惧嘲褰囬崶鍓у缂傛挸鐡ㄩ惄顔肩秿
     * @param fileName
     * @return a
     */
	public static String getImageCacheDir() {
        return getAppRootPath() + "cache/img/";
	}

    /**
     * 閸掓稑缂� .nomedia閺傚洣娆㈤敍宀�鏁ゆ禍搴＄潌閽勮棄鐛熸担鎾硅拫娴犺埖澹傞幓?
     */
	public static void createNomedia() throws IOException {
		if (FileUtils.hasExternalStorage()) {
			File f = new File(getAppRootPath() + ".nomedia");
			if (!f.exists()) {
				f.getParentFile().mkdirs();
			}
		}
	}

    /**
     * 閸栧綊鍘ゅ锝呭灟鐞涖劏鎻?
     * @param aRegex 濮濓絽鍨悰銊ㄦ彧瀵�?
     * @param aStr 濡�?閺屻儳娈戠�涙顑佹稉?
     * @return
     */
	public static boolean checkRegex(String aRegex, String aStr) {
		return Pattern.compile(aRegex).matcher(aStr).matches();
	}

    /**
     * 閸掋倖鏌囬弰顖氭儊閺堝缍夌紒?
     * @param context
     * @return true閿涙碍婀佺純鎴礉false閿涙碍妫ょ純?
     */
	public static boolean isNetWork(Context context) {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

    /**
     * 閸掋倖鏌噖ifi閺勵垰鎯佹潻鐐村复
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiInfo != null
                && wifiInfo.isConnected()){
            return true;
        }
        return false;
    }

    /**
     * 閼惧嘲褰噈eta data
     * @param context
     * @param name
     * @return
     */
	public static String getMetaData(Context context, String name) {
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			return appInfo.metaData.get(name).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "unknow";
		}
	}

    /**
     * 閼惧嘲褰囬幍瀣簚閻ㄥ嚍eviceId閿涘湜MEI閸欏嚖绱�
     * 鐠囥儲鏌熷▔鏇炴躬闂堢偞澧滈張楦款啎婢跺洣鑵戦弮鐘虫櫏
     * @param context
     * @return
     */
	public static String getDeviceId(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	/**
	 * 閼惧嘲褰嘺ndroidId
	 * 鐠囥儲鏌熷▔鏇炴躬闁劌鍨庨崢鍌氭櫌閻ㄥ嫯顔曟径鍥﹁厬鏉╂柨娲栭惃鍕Ц閻╃鎮撻惃鍑
	 * @param context
	 * @return
	 */
	public static String getAndroidId(Context context){
		String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		return androidId;
	}
	
	public static String getUUID(Context context){
		String id = getDeviceId(context);
		if(id == null){
			id = getAndroidId(context);
		}
		if(id == null){
			id = "unknown";
		}
		return id;
	}

    /**
     * dip鏉炵惥x
     * @param context
     * @param dipValue
     * @return
     */
	public static int dipToPx(Context context, float dipValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return dipToPx(scale, dipValue);
	}

    /**
     * dip鏉炵惥x
     * @param density
     * @param dipValue
     * @return
     */
	public static int dipToPx(float density, float dipValue) {
		return (int) (dipValue * density + 0.5f);
	}

    /**
     * px鏉炵惄ip
     * @param context
     * @param pxValue
     * @return
     */
	public static int pxToDip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return pxToDip(scale, pxValue);
	}

    /**
     * px 鏉炵惄ip
     * @param density
     * @param pxValue
     * @return
     */
	public static int pxToDip(float density, float pxValue) {
		return (int) (pxValue / density + 0.5f);
	}

	/**
	 * 閻㈢喐鍨氭稉?娑擃亪娈㈤張鐑樻殶
	 * 
	 * @param max
	 *            闂呭繑婧�閺佹壆娈戦張?婢堆�?纭风礄閸栧懎鎯堥敍?
	 * @param min
	 *            闂呭繑婧�閺佹壆娈戦張?鐏忓繐?纭风礄閸栧懎鎯堥敍?
	 * @return >=min <= max 閻ㄥ嫰娈㈤張鐑樻殶
	 */
	public static int getRandom(int max, int min) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	/**
	 * 娴犲穬ssets 閺傚洣娆㈡径閫涜厬閼惧嘲褰囬弬鍥︽楠炴儼顕伴幋鎬眛ring
	 * 
	 * @param fileName
	 * @return
	 */
	public static String readAssetsFile(Context context, String fileName) {
		String result = "";
		try {
			InputStream in = context.getResources().getAssets().open(fileName);
			// 閼惧嘲褰囬弬鍥︽閻ㄥ嫬鐡ч懞鍌涙殶
			int lenght = in.available();
			// 閸掓稑缂揵yte閺佹壆绮�
			byte[] buffer = new byte[lenght];
			// 鐏忓棙鏋冩禒鏈佃厬閻ㄥ嫭鏆熼幑顔款嚢閸掔櫚yte閺佹壆绮嶆稉?
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

    /**
     * 婢跺秴鍩楅弬鍥ㄦ拱閸掓澘澹�鐠愬瓨婢�
     * @param text
     * @param context
     */
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
    public static void copy(String text, Context context) {
        if (Build.VERSION.SDK_INT >= 11) {
            ClipboardManager cbm = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            cbm.setPrimaryClip(ClipData.newPlainText(null, text));
        } else {
            android.text.ClipboardManager cbm = (android.text.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            cbm.setText(text);
        }
    }

    /**
     * 缁鍒�
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static String paste(Context context) {
        if (Build.VERSION.SDK_INT >= 11) {
            ClipboardManager cbm = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (cbm.hasPrimaryClip()) {
                return cbm.getPrimaryClip().getItemAt(0).getText().toString();
            }
        } else {
            android.text.ClipboardManager cbm = (android.text.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (cbm.hasText()) {
                return cbm.getText().toString();
            }
        }
        return null;
    }
    
    /**
     * 閹稿缍呯純顔藉焻閸欐牕鐡х粭锔胯
     */
    public static String subString(String str, int index){
    	return str.trim().substring(index);
    }
    
    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        } 
        return statusBarHeight;
    }
    
    public static String getDrugHost(){
    	String drugHost = null;
		try {
			Class<?> kclass = Thread.currentThread().getContextClassLoader()
					.loadClass("com.citic21.user.utils.UserConf");
			drugHost = (String)kclass.getField("urlIp").get(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return drugHost;
    }
    /**
     * userAgent 1.0閺嶇厧绱￠敍?
     * company/version (OS Version; Device) Product/Version
     * Alijk/閺嶇厧绱￠悧鍫熸拱 (os缁鐎� 閻楀牊婀�; 鐠佹儳顦�) 娴溠冩惂閸�?/娴溠冩惂閻楀牊婀�
     * 婵″偊绱癆lijk/1.0 (Android 4.4.4; HM NOTE) AlijkClient/2.0.2
     * @return
     * @throws NameNotFoundException 
     */
    public static String getUserAgent(){
    	String versionName = "unknown";
    	try {
    		Context context = TNSettings.getInstance().appContext;
			PackageInfo packInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
    	String userAgent = String.format("Alijk/1.0 (android %s; %s) AlijkAndroidClient/%s", Build.VERSION.RELEASE, Build.MODEL, versionName);
//    	Log.d(TAG, "User-Agent:" + userAgent);
    	return userAgent;
    }
    
    /**
	 * 閸掋倖鏌� null閿�?"","null"
	 * 
	 * "null" 閺夈儴鍤渋os 楠炲啿褰�
	 * 
	 * 
	 * @param pStr
	 * @return
	 */
	public static boolean isEmptyStr(String pStr) {
		if (pStr == null) {
			return true;
		}
		if (pStr.length() == 0) {
			return true;
		}

		if ("null".equalsIgnoreCase(pStr)) {
			return true;
		}

		return false;

	}

}
