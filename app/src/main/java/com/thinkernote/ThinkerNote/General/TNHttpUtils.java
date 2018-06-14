package com.thinkernote.ThinkerNote.General;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.thinkernote.ThinkerNote.OAuth2.TNHttpEntity;
import com.thinkernote.ThinkerNote.OAuth2.TNHttpHelper.RestHttpException;

public class TNHttpUtils {
	private static String TAG = "TNHttpUtils";

	public static List<NameValuePair> convertToNameValuePair(JSONObject params) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();

		Iterator<?> it = params.keys();
		while (it.hasNext()) {
			try {
				String key = it.next().toString();
				String value = params.get(key).toString();
				NameValuePair pair = new BasicNameValuePair(key, value);
				list.add(pair);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public static String getHttpsEntityContent(TNHttpEntity tnEntity) {
		Log.d(TAG, "getHttpsEntityContent");
		String result = "";
		HttpEntity entity = tnEntity.getEntity();
		Header header = entity.getContentType();
		if (header != null)
			Log.e(TAG, "contentType:" + header.getValue());

		InputStream inputStream;
		try {
			inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			// Return result from buffered stream
			result = new String(content.toByteArray());
			inputStream.close();
			content.close();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Object getEntityContent(TNHttpEntity tnEntity) {
		Log.d(TAG, "getEntityContent");
		HttpEntity entity = tnEntity.getEntity();
		Header header = entity.getContentType();
		if (header != null)
			Log.e(TAG, "contentType:" + header.getValue());
		try {
			if (header != null && header.getValue().startsWith("image")) {
				InputStream is = entity.getContent();
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				is.close();
				return bitmap;
			}

			return EntityUtils.toString(entity, "UTF-8");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEntityToFile(TNHttpEntity tnEntity, String outPath)
			throws RestHttpException, Exception {
		Log.d(TAG, "getEntityToFile");
		HttpEntity entity = tnEntity.getEntity();
		Header header = entity.getContentType();
		String value = null;
		if(header != null){
			value = header.getValue();
		}
		if (header == null 
				|| value.indexOf("application/json") >= 0) {
			String result = EntityUtils.toString(entity, "UTF-8");
			Log.e(TAG, result);
			throw new RestHttpException(200, result);
		}
		Log.e(TAG, "contentType:" + value);

		File f = new File(outPath);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(f);
		entity.writeTo(out);
		out.close();

		return outPath;
	}

	public static String makeUrl(String host, JSONObject params) {
		return String.format("%s?%s", host, makeUrlParams(params));
	}

	private static String makeUrlParams(JSONObject params) {
		StringBuilder sb = new StringBuilder();

		Iterator<?> it = params.keys();
		while (it.hasNext()) {
			try {
				String key = it.next().toString();
				String value = params.get(key).toString();
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(key);
				sb.append("=");
				sb.append(value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Log.i("makeUrlParams", sb.toString());
		return sb.toString();
	}
}
