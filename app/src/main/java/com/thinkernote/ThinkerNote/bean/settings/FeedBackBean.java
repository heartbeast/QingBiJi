package com.thinkernote.ThinkerNote.bean.settings;

import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;

/**
 * 反馈，上传图片
 */

public class FeedBackBean extends CommonBean implements Serializable {
    String signed_str;

    public String getSigned_str() {
        return signed_str;
    }

    public void setSigned_str(String signed_str) {
        this.signed_str = signed_str;
    }

}
