package com.thinkernote.ThinkerNote.http.fileprogress;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 查看文件上传下载进度 自定义拦截器
 * <p>
 * 封装进度回调，请求体
 */
public class FileProgressInterceptor implements Interceptor {
    FileProgressListener listener;

    public FileProgressInterceptor(FileProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(
                new FileProgressResponseBody(response.body(), listener)).build();
    }
}
