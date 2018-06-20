package com.thinkernote.ThinkerNote.http;


import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.CommonListBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;
import com.thinkernote.ThinkerNote.bean.main.AlipayBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.bean.main.WxpayBean;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by jingbin on 16/11/21.
 * Rxjava+retrofit的封装，用于调用详细接口信息
 * Rxjava的响应式流程+retrofit的注解机制
 * 具体的操作，需要看MVP下的M层操作
 * <p>
 * Observable引用：import rx.Observable
 */

public interface MyHttpService {

    /**
     * 说明本app接口需求：
     * 1所有get有header
     * 2除登录 注册，所有接口有token
     * 3所有put方式有特殊header
     ********************************************--构建不同的网络框架，满足不同的接口需求--********************************************************
     */

    /**
     * （1）retrofit+okhttp+Rxjava默认构建样式（header+token+无缓存）
     */
    class Builder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getDefaultServer(MyHttpService.class);
        }
    }

    /**
     * （2）retrofit+okhttp+Rxjava样式+接口要求带token验证
     */
    class TokenBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getTokenServer(MyHttpService.class);
        }
    }

    /**
     * （3）put方式调用
     */
    class PUTBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getPUTServer(MyHttpService.class);
        }
    }

    /**
     * （3）get方式调用：header+token+无缓存
     * 登录和注册不可调用
     */
    class GETBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getGETServer(MyHttpService.class);
        }
    }

    /**
     * （3）get方式调用：header+token
     * 登录和注册不可调用
     */
    class POSTBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getGETServer(MyHttpService.class);
        }
    }

    /**
     * （3）get方式调用：header
     * 登录和注册专用
     */
    class PostNoTokenBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getGETServer(MyHttpService.class);
        }
    }

    /**
     * （4）retrofit+okhttp+Rxjava样式+特殊缓存接口
     */
    class NoCacheBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getNoCacheServer(MyHttpService.class);
        }
    }


    /**
     ********************************************--接口相关--**************************************************
     */

//-------------------------------------------------登录相关----------------------------------------------------

    /**
     * 01 密码登录
     *
     * @param username
     * @param password
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINNORMAL)
    Observable<LoginBean> postLoginNormal(
            @Field("username") String username
            , @Field("password") String password);


    /**
     * 02 qq登录
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINQQ)
    Observable<CommonBean> postLoginQQ(
            @Field("btype") int btype
            , @Field("bid") String password
            , @Field("stamp") long stamp
            , @Field("sign") String sign
            , @Field("session_token") String session_token
    );

    /**
     * 登录同步更新
     *
     * @return
     */
    @GET(URLUtils.Log.PROFILE)
    Observable<CommonBean2<ProfileBean>> LogNormalProfile(@Field("session_token") String session_token);

    /**
     * 03 sina登录
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINQQ)
    Observable<CommonBean> postLoginSina(
            @Field("btype") int btype
            , @Field("bid") String password
            , @Field("stamp") long stamp
            , @Field("sign") String sign
            , @Field("session_token") String session_token
    );

    /**
     * 04 wechat登录
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINQQ)
    Observable<CommonBean> postLoginWechat(
            @Field("btype") int btype
            , @Field("bid") String password
            , @Field("stamp") long stamp
            , @Field("sign") String sign
            , @Field("session_token") String session_token
    );


    // 05--07绑定手机号

    /**
     * 05 手机验证码(两个参数)
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.PHONE_QUERFY_CODE)
    Observable<CommonBean> postVerifyCode2(
            @Field("phone") String phone
            , @Field("t") String t
            , @Field("session_token") String session_token
    );

    /**
     * 06 绑定手机号
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGIN_BIND)
    Observable<CommonBean> postLoginBindPhone(
            @Field("btype") int btype
            , @Field("bid") String bid
            , @Field("name") String name
            , @Field("access_token") String access_token
            , @Field("refresh_token") String refresh_token
            , @Field("stamp") long currentTime
            , @Field("phone") String phone
            , @Field("vcode") String vcode
            , @Field("sign") String sign
            , @Field("session_token") String session_token
    );

    /**
     * 07 绑定手机号后，自动登录，(同qq登录接口)
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINQQ)
    Observable<LoginBean> postLoginAfterBind(
            @Field("btype") int btype
            , @Field("bid") String password
            , @Field("stamp") long stamp
            , @Field("sign") String sign
            , @Field("session_token") String session_token
    );

    // 08--11忘记密码

    /**
     * 08 图片验证
     *
     * @return
     */
    @GET(URLUtils.Log.VERIFY_PIC)
    Observable<VerifyPicBean> getVerifyPic(@Field("session_token") String session_token);


    /**
     * 09 手机验证码(四个参数)
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.PHONE_QUERFY_CODE)
    Observable<VerifyPicBean> postVerifyCode4(
            @Field("phone") String btphoneype
            , @Field("t") String t
            , @Field("answer") String answer
            , @Field("nonce") String nonce
            , @Field("hashkey") String hashkey
            , @Field("session_token") String session_token
    );


    /**
     * 10 邮箱验证码
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.EMAIL_QUERFY_CODE)
    Observable<VerifyPicBean> emailVerifyCode(
            @Field("email") String btphoneype
            , @Field("t") String t
            , @Field("session_token") String session_token);

    // 11--12注册

    /**
     * 11 忘记密码 提交
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.SUBMIT_FINDPS)
    Observable<VerifyPicBean> findPsSubmit(
            @Field("phone") String phone
            , @Field("password") String password
            , @Field("vcode") String vcode
            , @Field("session_token") String session_token);


    /**
     * 12 注册 提交
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.SUBMIT_RESGIST)
    Observable<VerifyPicBean> registSubmit(
            @Field("phone") String phone
            , @Field("password") String password
            , @Field("vcode") String vcode
            , @Field("session_token") String session_token);

    /**
     * 13 修改手机号
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Log.CHANGE_PHONE)
    Observable<CommonBean> changePhone(
            @Field("phone") String phone
            , @Field("password") String password
            , @Field("vcode") String vcode
            , @Field("session_token") String session_token);

    /**
     * 14 获取用户信息
     *
     * @return
     */
    @GET(URLUtils.Log.USER_INFO)
    Observable<CommonBean> getUserInfo(@Field("session_token") String session_token);

    /**
     * 15 退出登录
     *
     * @return
     */
    @GET(URLUtils.Log.LOGOUT)
    Observable<CommonBean> logout(@Field("session_token") String session_token);


    //-------------------------------------------------main相关----------------------------------------------------

    /**
     * 13 检查更新
     *
     * @return
     */
    @GET(URLUtils.Home.UPGRADE)
    Observable<CommonBean1<MainUpgradeBean>> upgrade(@Field("session_token") String session_token);


    /**
     * 14 同步数据
     *
     * @return
     */
    @GET(URLUtils.Home.UPGRADE)
    Observable<CommonBean1<MainUpgradeBean>> synchronizeData(@Field("session_token") String session_token);

    /**
     * feedBack
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Home.FEEDBACK)
    Observable<CommonBean> feedBack(
            @Field("content") String content
            , @Field("pic_id") long pic
            , @Field("email") String email
            , @Field("session_token") String session_token);

    /**
     * 支付宝支付
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Home.PAY_TIP)
    Observable<CommonBean1<AlipayBean>> alipay(
            @Field("amount") String amount
            , @Field("channel") String channel
            , @Field("session_token") String session_token);


    /**
     * 微信支付
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Home.PAY_TIP)
    Observable<WxpayBean> wxpay(
            @Field("amount") String amount
            , @Field("channel") String channel
            , @Field("session_token") String session_token);


    /**
     * 图片上传 TODO
     *
     * @return
     */
    @POST(URLUtils.Home.UPLOAD_PIC)
    Observable<CommonBean> upLoadPic( @Field("session_token") String session_token);


    //-------------------------------------------------写笔记相关----------------------------------------------------

    /**
     * 新建 标签
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.TAG)
    Observable<CommonBean> tagAdd(@Field("name") String phone
            , @Field("session_token") String session_token);

    /**
     * 新建 文件
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.FOLDER)
    Observable<CommonBean> folderAdd(@Field("name") String phone
            , @Field("session_token") String session_token);


    /**
     * 新建 文件
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.FOLDER)
    Observable<CommonBean> folderAdd(
            @Field("name") String phone
            , @Field("pid") long pid
            , @Field("session_token") String session_token);


    /**
     * 文件rename
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Note.FOLDER)
    Observable<CommonBean> folderRename(
            @Field("name") String phone
            , @Field("folder_id") long pid
            , @Field("session_token") String session_token);

    /**
     * 标签rename
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Note.TAG)
    Observable<CommonBean> tagRename(
            @Field("name") String phone
            , @Field("tag_id") long pid
            , @Field("session_token") String session_token);

    /**
     * TagList
     *
     * @return
     */
    @GET(URLUtils.Note.TAGLIST)
    Observable<CommonBean> getTagList(@Field("session_token") String session_token);


    /**
     * getNote
     *
     * @return
     */
    @FormUrlEncoded
    @GET(URLUtils.Note.NOTE)
    Observable<CommonBean> getNote(@Field("note_id") long note_id
            , @Field("session_token") String session_token);


    /**
     * deleteTag
     *
     * @return
     */
    @DELETE(URLUtils.Note.TAG)
    Observable<CommonBean> deleteTag(@Field("tag_id") long tag_id
            , @Field("session_token") String session_token);


    /**
     * 设置默认文件路径
     *
     * @return
     */
    @PUT(URLUtils.Note.DEFAULT_FOLDER)
    Observable<CommonBean> setDefaultFolder(@Field("folder_id") long pid
            , @Field("session_token") String session_token);


    /**
     * verifyEmail
     *
     * @return
     */
    @POST(URLUtils.Note.VERIFY_EMAIL)
    Observable<CommonBean> verifyEmail(@Field("session_token") String session_token);


    /**
     * NoteListByFolderId
     *
     * @return
     */
    @FormUrlEncoded
    @GET(URLUtils.Note.NOTE_LIST_FOLDERID)
    Observable<NoteListBean> getNoteListByFolderId(
            @Field("folder_id") long folder_id
            , @Field("pagenum") int pagenum
            , @Field("pagesize") int pagesize
            , @Field("sortord") String sortord
            , @Field("session_token") String session_token);


    /**
     * NoteListByTagId
     *
     * @return
     */
    @FormUrlEncoded
    @GET(URLUtils.Note.NOTE_LIST_TAGID)
    Observable<NoteListBean> getNoteListByTagId(
            @Field("folder_id") long folder_id
            , @Field("pagenum") int pagenum
            , @Field("pagesize") int pagesize
            , @Field("sortord") String sortord
            , @Field("session_token") String session_token);















    /**
     * 01轮播图 表单的使用方式，post
     *
     * @param city_name 默认全国
     * @return 200 404
     */

    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINNORMAL)
    Observable<CommonListBean<String>> getBanner(
            @Field("city_name") String city_name
            , @Field("device") int device);

    /**
     * 02 头像上传/修改头像
     * 图片/文件 上传
     *
     * @return
     */
    @Multipart
    @POST(URLUtils.Log.LOGINNORMAL)
    Observable<CommonBean> changePhotoPost(
            @Part List<MultipartBody.Part> list);

    /**
     * 03获取省市区数据
     */
    @GET(URLUtils.Log.LOGINNORMAL)
    Observable<String> getInstitutionAreaData();


    /**
     * 02 机构评论 发表评论 (有图上传)
     *
     * @return
     */
    @Multipart
    @POST(URLUtils.Log.LOGINNORMAL)
    Observable<CommonListBean<String>> postCommentWithPic(
            @Part("id") int id,
            @Part("order_id") int order_id,
            @Part("token") String token,
            @Part("agency_score") int agency_score,
            @Part("service_score") int servoce_score,
            @Part("content") String content,
            @Part("type") int type,
            @Part MultipartBody.Part file
    );


    /**
     * 微信授权
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGINNORMAL)
    Observable<CommonBean> postWeichatMessage(
            @Field("nickname") String nickname
            , @Field("openid") String openid
            , @Field("headimgurl") String headimgurl
            , @Field("unionid") String unionid
            , @Field("device") int device);

    //    /**
    //     * 添加访客
    //     * <p>
    //     * 文本和图片上传
    //     * post
    //     *
    //     * @return
    //     */
    //    @Multipart
    //    @POST(URLUtils.ADD_VISITOR)
    //    Observable<BaseBean> addVisitor(
    //            @Part("obj") String obj
    //            , @Part MultipartBody.Part file);
}