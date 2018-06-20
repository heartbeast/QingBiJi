package com.thinkernote.ThinkerNote.OAuth2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.net.wifi.WifiManager;

import com.thinkernote.ThinkerNote.Utils.DeviceInfoUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;

/**
 * http执行类
 * <p>
 * TNOAuth2封装调用
 */
public class TNHttpHelper {
    private static final String TAG = "JavaQingBiJi";
    private Context mContex;

    private static final int CONNECT_TIMEOUT = 30 * 1000; // ms
    private static final int T_TIMEOUT = 200 * 1000; // ms200000

    private static final List<NameValuePair> EMPTY_PARAMS = new ArrayList<NameValuePair>();

    public TNHttpHelper(Context contex) {
        this.mContex = contex;
    }

    /**
     * @param baseURL 基本url 如http://xxx.com/path
     * @param params  [{'a':'b'}, {'c':'d'}]
     * @throws BadRequestException
     * @return: 用于传入doGet, doPost 的url, 如 http://xxx.com/path?a=b&c=d
     */
    public static String buildURL(String baseURL, List<NameValuePair> params) throws BadRequestException {
        if (params == null)
            params = EMPTY_PARAMS;

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "utf8");
            String url = baseURL + '?' + EntityUtils.toString(entity);
            return url;
        } catch (UnsupportedEncodingException e1) {
            throw new BadRequestException("error on buildURL" + baseURL + params);
        } catch (IOException e) {
            throw new BadRequestException("error on buildURL" + baseURL + params);
        }
    }

    public TNHttpEntity doGet(String url, Vector<Header> headers) throws RestHttpException, HttpException {
        MLog.i("doGet", "url=" + url);
        final HttpGet request = new HttpGet(url);
        if (headers != null && headers.size() > 0) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));
        return execute(request);
    }

    public void doGetToFile(String url, String localFilePath) throws RestHttpException, HttpException {
        MLog.i("doGetToFile", "url=" + url + " outPath=" + localFilePath);
        final HttpGet request = new HttpGet(url);
        final HttpResponse resp;
        try {
//			resp = newHttpClient().execute(request);
            resp = getNewHttpClient(mContex).execute(request);

            StatusLine status = resp.getStatusLine();
            int statusCode = status.getStatusCode();
            MLog.i(TAG, "response=" + resp);
            MLog.i(TAG, "statusCode=" + statusCode);
            MLog.i(TAG, "toString=" + status.toString());
            Header[] headers = resp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                MLog.i(TAG, "header: name=" + headers[i].getName() + " value=" + headers[i].getValue());
            }
            request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));

            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                File f = new File(localFilePath);
                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(f);
                resp.getEntity().writeTo(out);
                out.close();

                return;
            } else {
                final String response = EntityUtils.toString(resp.getEntity());
                throw new RestHttpException(resp.getStatusLine().getStatusCode(), response);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new HttpException("IOException " + e.toString());
        }
    }

    /**
     * post application/x-www-form-urlencoded
     *
     * @throws HttpException
     */
    public TNHttpEntity doPost(String url, List<NameValuePair> params) throws RestHttpException, HttpException {
        MLog.i("doPost", "url=" + url);
        if (params == null)
            params = EMPTY_PARAMS;

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "utf8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        final HttpPost request = new HttpPost(url);
        request.addHeader(entity.getContentType());
        request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));

        request.setEntity(entity);
        return execute(request);
    }

    public TNHttpEntity doPostMultipart(String url, List<NameValuePair> params, Object imageData, String key) throws RestHttpException, HttpException {
        MLog.i("doPostMultipart", "url=" + url);
        if (params == null)
            params = EMPTY_PARAMS;

        HttpPost request = new HttpPost(url);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (NameValuePair kv : params) {
            multipartAddKV(reqEntity, kv.getName(), kv.getValue());
        }

        if (String.class.isInstance(imageData)) {
            FileBody bin = new FileBody(new File((String) imageData), "image/jpeg");
            reqEntity.addPart(key, bin);
        } else if (Bitmap.class.isInstance(imageData)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Bitmap image = (Bitmap) imageData;
            // CompressFormat set up to JPG, you can change to PNG or whatever you want;
            image.compress(CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();

            reqEntity.addPart(key, new ByteArrayBody(data, "image.jpg"));
        }
        request.setEntity(reqEntity);
        request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));

        return execute(request);
    }

    public TNHttpEntity doPut(String url, List<NameValuePair> params) throws RestHttpException, HttpException {
        MLog.i("doPut", "url=" + url);
        if (params == null)
            params = EMPTY_PARAMS;

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "utf8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        final HttpPut request = new HttpPut(url);
        request.addHeader(entity.getContentType());
        request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));

        request.setEntity(entity);
        return execute(request);
    }

    /**
     * 删除
     * @param url
     * @param params
     * @return
     * @throws RestHttpException
     * @throws HttpException
     */
    public TNHttpEntity doDelete(String url, List<NameValuePair> params) throws RestHttpException, HttpException {
        MLog.i("doDelete", "url=" + url);
        if (params == null)
            params = EMPTY_PARAMS;

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "utf8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        final HttpDeleteWithBody request = new HttpDeleteWithBody(url);
        request.addHeader(entity.getContentType());
        request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));

        request.setEntity(entity);
        return execute(request);
    }

    public TNHttpEntity doUploadFile(String url, String filePath, List<NameValuePair> params) throws RestHttpException, HttpException {
        MLog.i("doUploadFile", "url=" + url);
        url = url.replace(" ", "%20");//文件名有空格
        final HttpPost request = new HttpPost(url);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (params != null) {
            for (NameValuePair kv : params) {
                multipartAddKV(reqEntity, kv.getName(), kv.getValue());
            }
        }
        if (filePath != null) {
            FileBody bin = new FileBody(new File(filePath));
            reqEntity.addPart("file", bin);
        }
        request.addHeader("user-agent", DeviceInfoUtils.getUserDeviceInfo(mContex));
        request.setEntity(reqEntity);

        return execute(request);
    }

    private TNHttpEntity execute(HttpRequestBase request) throws RestHttpException, HttpException {
        request.addHeader("User-Agent", TAG);
        final HttpResponse resp;
        try {
//			resp = newHttpClient().execute(request);
            resp = getNewHttpClient(mContex).execute(request);

            long thumbnailAttId = -1;
            Header[] headers = resp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                MLog.i(TAG, "header: name=" + headers[i].getName() + " value=" + headers[i].getValue());
            }
            Header head = resp.getFirstHeader("Thumb-Att-Id");
            if (head != null) {
                thumbnailAttId = Long.valueOf(head.getValue());
                MLog.e(TAG, "thumbnailAttId=" + thumbnailAttId);
            }
            StatusLine status = resp.getStatusLine();
            int statusCode = status.getStatusCode();
            MLog.i(TAG, "response=" + resp);
            MLog.i(TAG, "statusCode=" + statusCode);
            MLog.i(TAG, "toString=" + status.toString());

            HttpEntity entity = resp.getEntity();
            MLog.i(TAG, "entity=" + entity);

            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                TNHttpEntity tnEntity = new TNHttpEntity(entity, thumbnailAttId);
                return tnEntity;
            } else {
                String response = EntityUtils.toString(entity);
                MLog.e(TAG, "http errorcode: " + resp.getStatusLine().getStatusCode()
                        + " response: " + response);
                throw new RestHttpException(resp.getStatusLine().getStatusCode(), response);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new HttpException("IOException " + e.toString());
        }
    }

    private static void multipartAddKV(MultipartEntity reqEntity, String key, String value) {
        StringBody body = null;
        try {
            body = new StringBody(value, Charset.forName("utf-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        reqEntity.addPart(key, body);
    }

    public static class BadRequestException extends Exception {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public BadRequestException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class RestHttpException extends Exception {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        public int statusCode;
        public String responseBody;

        public RestHttpException(int statusCode, String responseBody) {
            super("" + statusCode + ":" + responseBody);
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }

    /**
     * 封装特殊的httpClient
     * @param context
     * @return
     */
    public static HttpClient getNewHttpClient(Context context) {
        try {

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, T_TIMEOUT);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", sf, 443));
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);

            HttpClient client = new DefaultHttpClient(ccm, params);

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                // 获取当前正在使用的APN接入点
                Uri uri = Uri.parse("content://telephony/carriers/preferapn");
                Cursor mCursor = context.getContentResolver().query(uri, null,
                        null, null, null);
                if (mCursor != null && mCursor.moveToFirst()) {
                    // 游标移至第一条记录，当然也只有一条
                    String proxyStr = mCursor.getString(mCursor
                            .getColumnIndex("proxy"));
                    if (proxyStr != null && proxyStr.trim().length() > 0) {
                        HttpHost proxy = new HttpHost(proxyStr, 80);
                        client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
                    }
                }
            }
            return client;
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

}
