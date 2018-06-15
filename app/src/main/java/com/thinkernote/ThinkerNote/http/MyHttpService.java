package com.thinkernote.ThinkerNote.http;


import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.CommonListBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;
import com.thinkernote.ThinkerNote.bean.main.AlipayBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
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
     ********************************************--构建不同的网络框架，满足不同的接口需求--********************************************************
     */

    /**
     * （1）retrofit+okhttp+Rxjava默认构建样式（没有复杂的样式要求）
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
     * （3）retrofit+okhttp+Rxjava样式+缓存接口
     */
    class CacheBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getCacheServer(MyHttpService.class);
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
     * （5）retrofit+okhttp+Rxjava样式+特殊header接口
     */
    class HeaderBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getHeaderServer(MyHttpService.class);
        }
    }
    //...需要的样式 灵活添加


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
    );

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
    );

    // 08--11忘记密码

    /**
     * 08 图片验证
     *
     * @return
     */
    @GET(URLUtils.Log.VERIFY_PIC)
    Observable<VerifyPicBean> getVerifyPic();


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
            , @Field("hashkey") String hashkey);

    /**
     * 10 邮箱验证码
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.EMAIL_QUERFY_CODE)
    Observable<VerifyPicBean> emailVerifyCode(
            @Field("email") String btphoneype
            , @Field("t") String t);

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
            , @Field("vcode") String vcode);


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
            , @Field("vcode") String vcode);

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
            , @Field("vcode") String vcode);

    /**
     * 14 获取用户信息
     *
     * @return
     */
    @GET(URLUtils.Log.USER_INFO)
    Observable<CommonBean> getUserInfo();

    /**
     * 15 退出登录
     *
     * @return
     */
    @GET(URLUtils.Log.LOGOUT)
    Observable<CommonBean> logout();


    //-------------------------------------------------main相关----------------------------------------------------

    /**
     * 13 检查更新
     *
     * @return
     */
    @GET(URLUtils.Home.UPGRADE)
    Observable<CommonBean1<MainUpgradeBean>> upgrade();


    /**
     * 14 同步数据
     *
     * @return
     */
    @GET(URLUtils.Home.UPGRADE)
    Observable<CommonBean1<MainUpgradeBean>> synchronizeData();

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
            , @Field("email") String email);

    /**
     *支付宝支付
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Home.PAY_TIP)
    Observable<CommonBean1<AlipayBean>> alipay(
            @Field("amount") String amount
            , @Field("channel") String channel);


    /**
     *微信支付
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Home.PAY_TIP)
    Observable<WxpayBean> wxpay(
            @Field("amount") String amount
            , @Field("channel") String channel);


    /**
     * 图片上传 TODO
     *
     * @return
     */
    @POST(URLUtils.Home.UPLOAD_PIC)
    Observable<CommonBean> upLoadPic();


    //-------------------------------------------------写笔记相关----------------------------------------------------

    /**
     * 新建 标签
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.TAG)
    Observable<CommonBean> tagAdd(@Field("name") String phone);

    /**
     * 新建 文件
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.FOLDER)
    Observable<CommonBean> folderAdd(@Field("name") String phone);


    /**
     * 新建 文件
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.FOLDER)
    Observable<CommonBean> folderAdd(
            @Field("name") String phone
            , @Field("pid") long pid);


    /**
     * 文件rename
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Note.FOLDER)
    Observable<CommonBean> folderRename(
            @Field("name") String phone
            , @Field("folder_id") long pid);

    /**
     * 标签rename
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Note.TAG)
    Observable<CommonBean> tagRename(
            @Field("name") String phone
            , @Field("tag_id") long pid);

    /**
     * TagList
     *
     * @return
     */
    @GET(URLUtils.Note.TAGLIST)
    Observable<CommonBean> getTagList();


    /**
     * deleteTag
     *
     * @return
     */
    @DELETE(URLUtils.Note.TAG)
    Observable<CommonBean> deleteTag(@Field("tag_id") long tag_id);


    /**
     * 设置默认文件路径
     *
     * @return
     */
    @PUT(URLUtils.Note.DEFAULT_FOLDER)
    Observable<CommonBean> setDefaultFolder(@Field("folder_id") long pid);


    /**
     * verifyEmail
     *
     * @return
     */
    @POST(URLUtils.Note.VERIFY_EMAIL)
    Observable<CommonBean> verifyEmail();


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