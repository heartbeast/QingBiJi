package com.thinkernote.ThinkerNote.http.fileprogress;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 文件上传下载 查看进度 --请求体
 */
public class FileProgressResponseBody extends ResponseBody {
    private ResponseBody responseBody;//请求体

    FileProgressListener listener;//下载进度回调

    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private BufferedSource bufferedSource;

    //
    public FileProgressResponseBody(ResponseBody responseBody, FileProgressListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    //Okio.Source 核心方法
    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                MLog.e("download", "read: " + (int) (totalBytesRead * 100 / responseBody.contentLength()));
                if (null != listener) {
                    if (bytesRead != -1) {
                        listener.onFileProgressing((int) (totalBytesRead * 100 / responseBody.contentLength()));
                    }

                }
                return bytesRead;
            }
        };

    }
}
