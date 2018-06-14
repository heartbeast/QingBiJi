package com.thinkernote.ThinkerNote._interface.v;

public interface OnMainListener {
    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onSynchronizeSuccess(Object obj);

    void onSynchronizeFailed(String msg, Exception e);
}
