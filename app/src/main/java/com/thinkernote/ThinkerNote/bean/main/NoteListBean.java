package com.thinkernote.ThinkerNote.bean.main;

import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;

/**
 * 登录返回数据的bean
 */

public class NoteListBean extends CommonBean implements Serializable {
    int pagenum;
    int count;

    public int getPagenum() {
        return pagenum;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
