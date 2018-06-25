package com.thinkernote.ThinkerNote.http;


import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.CommonListBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;
import com.thinkernote.ThinkerNote.bean.main.AlipayBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.bean.main.WxpayBean;
import com.thinkernote.ThinkerNote.bean.settings.FeedBackBean;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by jingbin on 16/11/21.
 * Rxjava+retrofit的封装，用于调用详细接口信息
 * Rxjava的响应式流程+retrofit的注解机制
 * 具体的操作，需要看MVP下的M层操作
 * <p>
 * Observable引用：import rx.Observable
 * 接口说明：所有的接口都有固定参数 token(除 登录/注册两个接口);
 *
 * @PUT 与@POST差别不大，只是网络header需要单独设置
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
     * 非文件使用
     */
    class Builder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getDefaultServer(MyHttpService.class);
        }
    }

    /**
     * （2）put方式调用
     */
    class FileBuilder {
        public static MyHttpService getHttpServer() {
            return HttpUtils.getInstance().getFileServer(MyHttpService.class);
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
    Observable<LoginBean> loginNormal(
            @Field("username") String username
            , @Field("password") String password);


    /**
     * 02 第三方登录：qq登录/sina登录/wechat登录
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.LOGIN_THIRD)
    Observable<LoginBean> loginThird(
            @Field("btype") int btype
            , @Field("bid") String password
            , @Field("stamp") long stamp
            , @Field("sign") String sign
            , @Field("session_token") String session_token
    );

    /**
     * （3）登录同步更新
     *
     * @return
     */
    @GET(URLUtils.Log.PROFILE)
    Observable<CommonBean2<ProfileBean>> LogNormalProfile(@Query("session_token") String session_token);


    /**
     * 04 手机验证码(两个参数)
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
     * 05 绑定手机号
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
     * 06 图片验证
     *
     * @return
     */
    @GET(URLUtils.Log.VERIFY_PIC)
    Observable<VerifyPicBean> getVerifyPic(@Query("session_token") String session_token);


    /**
     * 07 手机验证码(四个参数)
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
     * 8 邮箱验证码
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.EMAIL_QUERFY_CODE)
    Observable<VerifyPicBean> emailVerifyCode(
            @Field("email") String btphoneype
            , @Field("t") String t
            , @Field("session_token") String session_token);


    /**
     * 9 忘记密码 提交
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
     * 10 注册 提交
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Log.SUBMIT_RESGIST)
    Observable<VerifyPicBean> registSubmit(
            @Field("phone") String phone
            , @Field("password") String password
            , @Field("vcode") String vcode
    );

    /**
     * 11 修改手机号
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
     * 12 获取用户信息
     *
     * @return
     */
    @GET(URLUtils.Log.USER_INFO)
    Observable<CommonBean> getUserInfo(@Query("session_token") String session_token);

    /**
     * 13 退出登录
     *
     * @return
     */
    @GET(URLUtils.Log.LOGOUT)
    Observable<CommonBean> logout(@Query("session_token") String session_token);


    //-------------------------------------------------main相关----------------------------------------------------d

    /**
     * 13 检查更新
     *
     * @return
     */
    @GET(URLUtils.Home.UPGRADE)
    Observable<CommonBean1<MainUpgradeBean>> upgrade(@Query("session_token") String session_token);

    /**
     * 下载安装包，动态路径形式
     *
     * @return
     */
    @GET
    Observable<CommonBean1<MainUpgradeBean>> download(@Url String url
            , @Query("session_token") String session_token);


    /**
     * 14 同步数据
     *
     * @return
     */
    @GET(URLUtils.Home.UPGRADE)
    Observable<CommonBean1<MainUpgradeBean>> synchronizeData(@Query("session_token") String session_token);


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
    Observable<CommonBean> upLoadPic(@Field("session_token") String session_token);

    //-------------------------------------------------同步相关----------------------------------------------------

    /**
     * 同步新建文件
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.FOLDER)
    Observable<CommonBean> syncFolderAdd(@Field("name") String name
            , @Field("session_token") String session_token);


    /**
     * 同步tag
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.TAG)
    Observable<CommonBean> syncTagAdd(@Field("name") String name
            , @Field("session_token") String session_token);


    /**
     * 同步 上传图片
     *
     * @return
     */
    @Multipart
    @POST(URLUtils.Note.UPLOAD_PIC)
    Observable<OldNotePicBean> syncOldNotePic(
            @Part MultipartBody.Part file
            , @Part("session_token") RequestBody session_token);

    /**
     * 同步 上传图片
     *
     * @return
     */
    @Multipart
    @POST(URLUtils.Note.UPLOAD_PIC)
    Observable<OldNotePicBean> syncNewNotePic(
            @Part MultipartBody.Part file
            , @Part("session_token") RequestBody session_token);

    /**
     * 同步 上传图片
     *
     * @return
     */
    @Multipart
    @POST(URLUtils.Note.UPLOAD_PIC)
    Observable<OldNotePicBean> syncEditNotePic(
            @Part MultipartBody.Part file
            , @Part("session_token") RequestBody session_token);

    /**
     * 同步 上传图片
     *
     * @return
     */
    @Multipart
    @POST(URLUtils.Note.UPLOAD_PIC)
    Observable<OldNotePicBean> syncRecoveryNotePic(
            @Part List<MultipartBody.Part> parts);


    /**
     * 同步oldNoteAdd
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.NOTE)
    Observable<OldNoteAddBean> syncOldNoteAdd(
            @Field("title") String title
            , @Field("content") String content
            , @Field("tags") String tags
            , @Field("folder_id") long folder_id
            , @Field("create_time") int create_time
            , @Field("update_time") int update_time
            , @Field("longitude") int longitude
            , @Field("latitude") int latitude
            , @Field("address") String address
            , @Field("radius") int radius
            , @Field("session_token") String session_token);


    /**
     * 同步NewNote
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.NOTE)
    Observable<OldNoteAddBean> syncNewNoteAdd(
            @Field("title") String title
            , @Field("content") String content
            , @Field("tags") String tags
            , @Field("folder_id") long folder_id
            , @Field("create_time") int create_time
            , @Field("update_time") int update_time
            , @Field("longitude") int longitude
            , @Field("latitude") int latitude
            , @Field("address") String address
            , @Field("radius") int radius
            , @Field("session_token") String session_token);


    /**
     * 同步syncRecoveryNoteAdd
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Note.NOTE)
    Observable<OldNoteAddBean> syncRecoveryNoteAdd(
            @Field("title") String title
            , @Field("content") String content
            , @Field("tags") String tags
            , @Field("folder_id") long folder_id
            , @Field("create_time") int create_time
            , @Field("update_time") int update_time
            , @Field("longitude") int longitude
            , @Field("latitude") int latitude
            , @Field("address") String address
            , @Field("radius") int radius
            , @Field("session_token") String session_token);

    /**
     * 同步syncRecoveryNote
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Note.RECOVERY_NOTE)
    Observable<CommonBean> syncRecoveryNote(@Field("note_id") long note_id
            , @Field("session_token") String session_token);


    /**
     * 同步获取folder get形式
     *
     * @return
     */
    @GET(URLUtils.Note.FOLDER)
    Observable<AllFolderBean> syncGetFolder(
            @Query("session_token") String session_token);

    /**
     * 同步获取GetFolderByFodlerId
     *
     * @return
     */
    @GET(URLUtils.Note.FOLDER)
    Observable<AllFolderBean> syncGetFolderByFodlerId(@Query("folder_id") long folder_id
            , @Query("session_token") String session_token);


    /**
     * 同步 syncTagList
     *
     * @return
     */
    @GET(URLUtils.Note.TAGLIST)
    Observable<TagListBean> syncTagList(@Query("session_token") String session_token);


    /**
     * 同步 DeleteNote
     * 使用 delete请求
     *
     * @return
     */
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @HTTP(method = "DELETE", path = URLUtils.Note.NOTE, hasBody = true)
    @FormUrlEncoded
    Observable<CommonBean> syncDeleteNote(
            @Field("note_id") long note_id
            , @Field("session_token") String session_token);


    /**
     * 2-9
     * 同步 DeleteRealNote1
     * 使用 delete请求
     *
     * @return
     */
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @HTTP(method = "DELETE", path = URLUtils.Note.NOTE, hasBody = true)
    @FormUrlEncoded
    Observable<CommonBean> syncDeleteRealNote1(
            @Field("note_id") long note_id
            , @Field("session_token") String session_token);


    /**
     * 同步 DeleteRealNote2
     * 使用 delete请求
     *
     * @return
     */
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @HTTP(method = "DELETE", path = URLUtils.Note.RECOVERY_NOTE, hasBody = true)
    @FormUrlEncoded
    Observable<CommonBean> syncDeleteRealNote2(
            @Field("note_id") long note_id
            , @Field("session_token") String session_token);

    /**
     * 同步 getAllNotsId
     *
     * @return
     */
    @GET(URLUtils.Note.ALLNOTESID)
    Observable<AllNotesIdsBean> syncAllNotsId(@Query("session_token") String session_token);


    /**
     * 同步 EditNote
     * TODO 需要测试put请求
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Note.NOTE)
    Observable<CommonBean> syncEditNote(
            @Field("note_id") long note_id
            , @Field("title") String title
            , @Field("content") String content
            , @Field("tags") String tags
            , @Field("folder_id") long folder_id
            , @Field("create_time") int create_time
            , @Field("update_time") int update_time
            , @Field("session_token") String session_token);

    /**
     * 同步 getAllNotsId
     *
     * @return
     */
    @GET(URLUtils.Note.NOTE)
    Observable<CommonBean3<GetNoteByNoteIdBean>> GetNoteByNoteId(
            @Query("note_id") long note_id
            , @Query("session_token") String session_token);


    /**
     * 同步 GetTrashNoteIds
     *
     * @return
     */
    @GET(URLUtils.Note.TRASH_NOTE)
    Observable<AllNotesIdsBean> GetTrashNoteIds(@Query("session_token") String session_token);


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
    Observable<TagListBean> getTagList(@Query("session_token") String session_token);


    /**
     * getNote
     *
     * @return
     */
    @GET(URLUtils.Note.NOTE)
    Observable<CommonBean> getNote(@Query("note_id") long note_id
            , @Query("session_token") String session_token);


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
    @GET(URLUtils.Note.NOTE_LIST_FOLDERID)
    Observable<NoteListBean> getNoteListByFolderId(
            @Query("folder_id") long folder_id
            , @Query("pagenum") int pagenum
            , @Query("pagesize") int pagesize
            , @Query("sortord") String sortord
            , @Query("session_token") String session_token);


    /**
     * NoteListByTagId
     *
     * @return
     */
    @GET(URLUtils.Note.NOTE_LIST_TAGID)
    Observable<NoteListBean> getNoteListByTagId(
            @Query("folder_id") long folder_id
            , @Query("pagenum") int pagenum
            , @Query("pagesize") int pagesize
            , @Query("sortord") String sortord
            , @Query("session_token") String session_token);

    //-------------------------------------------------设置相关----------------------------------------------------

    /**
     * 上传多张图片
     *
     * @param parts
     * @return
     */
    @Multipart
    @POST(URLUtils.Settings.UPLOAD_PIC)
    Observable<FeedBackBean> upLoadFeedBackPic(
            @Part List<MultipartBody.Part> parts);

    /**
     * feedBack
     *
     * @return
     */
    @FormUrlEncoded
    @POST(URLUtils.Settings.FEEDBACK)
    Observable<CommonBean> feedBack(
            @Field("content") String content
            , @Field("pic_id") long pic
            , @Field("email") String email
            , @Field("session_token") String session_token);

    /**
     * set
     *
     * @return
     */
    @FormUrlEncoded
    @PUT(URLUtils.Cat.DEFAULT_FOLDER)
    Observable<CommonBean> setDefaultFolder(
            @Field("folder_id") String folder_id
            , @Field("session_token") String session_token);

//*************************************************以下不使用***********************************************

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