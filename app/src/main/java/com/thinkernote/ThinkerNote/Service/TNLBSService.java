package com.thinkernote.ThinkerNote.Service;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 百度定位
 */
public class TNLBSService {
	private static final String TAG = "TNBaiduLocationService";
	private static final int LOCATION_SCANSPAN = 1000*60*1;
	private static final int TIME_DIFF = 1000*60*10;
	
	private static TNLBSService singleton = null;
	
	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = new BaiduLocationListener();
	private BDLocation mBDLocation = null;
	
	private TNLBSService(){
		MLog.d(TAG, "TNBaiduLocationService()");
		initBDLocationClient();	
	}
	
	private void initBDLocationClient(){
		MLog.i(TAG, "init LocationClient");
		mLocationClient = new LocationClient(TNSettings.getInstance().appContext); // 声明LocationClient类
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(false); // 是否打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型为bd09ll
		option.setPriority(LocationClientOption.NetWorkFirst); // 设置网络优先
		option.setProdName("Qingbiji"); // 设置产品线名称
		option.setScanSpan(LOCATION_SCANSPAN); // 定时定位，每隔60秒钟定位一次。
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(myListener); // 注册监听函数	
	}
	
	public static TNLBSService getInstance(){
		if(singleton == null){
			synchronized (TNLBSService.class) {
				if(singleton == null){
					singleton = new TNLBSService();
				}
			}
		}
			
		return singleton;
	}
	
	public void startLocation(){
		if(mLocationClient == null){
			initBDLocationClient();
		}

		if (mLocationClient != null && !mLocationClient.isStarted()) {
			MLog.d(TAG, "startLocation");
			mLocationClient.start();
		}
	}
	
	public void requestLocation(){
		startLocation();
		if (mLocationClient != null && mLocationClient.isStarted()) {
			MLog.d(TAG, "requestLocation");
			try {
				mLocationClient.requestLocation();
			} catch (Exception e) {
				MLog.i(TAG, "requestLocation happened exception");
			}
		}
	}
	
	public void stopLocation(){
		if (mLocationClient != null && mLocationClient.isStarted()) {
			MLog.d(TAG, "stopLocation");
			mLocationClient.stop();
			mLocationClient = null;
		}
	}
	
	public BDLocation getLocation(){
		if(mBDLocation == null || !isShortTimeInterval(mBDLocation.getTime())){
			mBDLocation = new BDLocation();
			mBDLocation.setAddrStr("");
			mBDLocation.setLatitude(0);
			mBDLocation.setLongitude(0);
			mBDLocation.setRadius(0);
			mBDLocation.setTime("");
		}
		
		if(mBDLocation.getAddrStr() == null)
			mBDLocation.setAddrStr("");
		
		return mBDLocation;
	}
	
	private boolean isShortTimeInterval(String lastTime){
		if(lastTime.length() < 10){
			return false;
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d = df.parse(lastTime);			
			if(System.currentTimeMillis() - d.getTime() <= TIME_DIFF){
				return true;
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private class BaiduLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {
			MLog.i(TAG, "onReceiveLocation");
			if (location == null)
				return;
			
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {//GPS定位
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				mBDLocation = location;
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {//网络定位
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				mBDLocation = location;
			} else if(location.getLocType() == 65){
				requestLocation();
			}
			MLog.i(TAG, sb.toString());
		}
	}
}
