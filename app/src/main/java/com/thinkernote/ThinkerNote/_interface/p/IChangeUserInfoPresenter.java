package com.thinkernote.ThinkerNote._interface.p;

/**
 * p层interface
 */
public interface IChangeUserInfoPresenter {
    void pChangePs(String oldPs, String newPs);

    void pChangeNameOrEmail(String nameOrEmail, String type, String userPs);

    void pProfile();

}
