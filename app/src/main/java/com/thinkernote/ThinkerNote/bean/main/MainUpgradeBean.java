package com.thinkernote.ThinkerNote.bean.main;

import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;

/**
 *  更新 返回数据的bean
 */

public class MainUpgradeBean extends CommonBean implements Serializable {
    String version;
    String versionCode;
    int size;
    String content;
    String url;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MainUpgradeBean{" +
                "version='" + version + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", size=" + size +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
