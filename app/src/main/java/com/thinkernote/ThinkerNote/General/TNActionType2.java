package com.thinkernote.ThinkerNote.General;

public enum TNActionType2 {
//	Init,
//	Deinit,
			
	/* 输入：
	 * 0、username String
	 * 1、password String MD5
	 * 2、netLoginType integer //LOGIN_TYPE 1正常登录、2自动登录、3同步时的自动登录
	 * 3、operation //0 旧用户登录, 1 绑定旧用户, 2 新注册用户, 3, 登陆后进行其他三方账号绑定
	 * 返回：
	 * 0、userLocalId
	 */	
	Login,

	Logout,
	ClearCache,
	/* 输入：
	 * 0、username String
	 * 1、password String MD5
	 * 2、newPwd 
	 */
	ChangePassword,
	
	/* 输入：
	 * 0、username String
	 * 1、password String MD5
	 * 2、newEmail
	 * 3、"changeEmail","verifyEmail"
	 */
	ChangeEmail,
	
	/* 输入：
	 * 0、username String 
	 * 1、password String MD5
	 * 2、newUserName
	 */
	ChangeUserName,
		
	/* 输入：
	 * 0、noteId
	 * 1、fromEmail
	 * 2、toEmails
	 * 3、subject
	 * 4、addition
	 */
	ShareByEmail,
	
	/* 输入：
	 * 0、skinName
	 */
	ChangeSkin,
	
	ReadDevKey,
	
	GetAllLocalUser,
	ImportTrialUserData,
	
	/* 输入：
	 * 0、userLocalId
	 * 1、note
	 */
	NoteSave, 	// 笔记新建、编辑时使用
	
	/* 输入：
	 * 0、userLocalId
	 * 1、note
	 */
	NoteCollection, 	//收藏笔记
	
	/* 输入：
	 * 0、noteLocalId
	 * 1、catLocalId
	 */
	NoteMoveTo,
	
	/* 输入：
	 * 0、note
	 */
	NoteUpdate,	// 笔记属性修改时使用
	
	/* 输入：
	 * 0、userLocalId
	 * 1、listType 1 所有笔记 2 文件夹 3 回收站 4标签 5搜索 6一篇笔记 7个人公开 8他人公开
	 * 2、listDetail, String
	 * 3、order
	 */
	GetNoteList,
	
	/* 
	 * 
	 */
	ClearRecycle, //清空回收站
	
	/* 输入：
	 * 0、noteLocalId
	 */
	GetNoteBrief,
	GetShareNoteBrief,
	
	
	/* 输入:
	 * 0 note_id
	 */
	GetShareNoteThumbnail,
	
	/* 输入：
	 * 0、noteLocalId
	 */
	GetNote,
	NoteDelete,
	NoteRestore,
	NoteRealDelete,
	NoteDataClear,
	
	GetCommentList,
	CommentDelete,
		
	/* 输入：
	 * 0、userLocalId
	 */
	GetUserData,
	GetProjects,
	
	
	/* 输入：
	 * 0、projectId
	 */
	GetProject,
	
	/* 输入：
	 * 0、projectId
	 * 1、joinmsg
	 */
	JoinProject,
	

	/* 输入:
	 * 0、projectLocalId
	 */
	GetUpdateLogs,
	
	/* 输入:
	 * 0、projectId
	 */
	GetUnreadList,
	
	GetAllUnreadCount,
	
	/* 输入:
	 * 0、projectId
	 * 1、noteId  -1表示设置项目下所有笔记为已读;
	 */
	SetNoteReaded,
	
	/* 输入：
	 * 0、projectId
	 * 1、userId
	 * 2、status  批准:"active", 拒绝:"deny"
	 */
	ApproveUser,//审批加入群组申请
	
	
	/* 输入：
	 * 0、userLocalId
	 * 1、projectLocalId
	 */
	GetAllCats,

	/* 输入：
	 * 0、catLocalId
	 */
	GetCat,

	/* 输入：
	 * 0、userLocalId
	 */
	GetAllTags,
	
	/* 输入：
	 * 0、note
	 */
	NoteTagSave,

	/* 输入：
	 * 0、userLocalId
	 * 1、tagStr
	 */
	TagStrAdd,
	
	/* 输入：
	 * 0、userLocalId
	 * 1、tagName
	 */
	TagAdd,
	
	/* 输入：
	 * 0、tagLocalId
	 */
	TagDelete,
	
	/* 输入：
	 * 0、tag
	 */
	TagRename,
	
	/* 输入：
	 * 0、userLocalId
	 * 1、projectLocalId
	 * 2、catName
	 * 3、pCatLocalId
	 * 4、isLeaf
	 */
	CatAdd,
	
	/* 输入：
	 * 0、catLocalId
	 */	
	CatDelete,
	
	/* 输入：
	 * 0、catLocalId
	 * 1、catName
	 */	
	CatRename,
	
	/* 输入：
	 * 0、userLocalId
	 * 1、catLocalId
	 */	
	CatSetDefault,
	
	/* 输入：
	 * 0、catLocalId
	 * 1、pCatLocalId
	 */	
	CatMoveTo,
	
	NoteAttSave,
	
	ClearShareNotes,
	
	
	
	
	//NetAsyncTask,
	
	/* 0、username String
	 * 1、password String MD5
	 * 2、netLoginType integer //LOGIN_TYPE 1正常登录、2自动登录、3同步时的自动登录
	 */
	NetLogin,
	NetRegister,
	NetForgetPwd,
	NetCheckUsername,
	NetCheckEmail,
	SetInviteCode,
	
	SocketSend,
	SocketSendRaw,
	SocketAppReg,
	
	/* 0、msg String
	 */
	ReportError,
	
	Db_Execute,
	DbReportError,
	DBReset,	//重建数据库表，当发生数据库错误时使用 
	
	GetPushInfo,	//推送
	SetRemindTime, //设置推送已读
	
	/* 输入：
	 * 0、syncId(projectLocalId, catLocalId, noteLocalId)
	 * 1、projectLocalId
	 */
	Synchronize, // 全部同步
		SynchronizeUser,
		SynchronizeCats,
		SynchronizeTags,
		SynchronizeNotes,
		SynchronizeAtts,
		SynchronizeMore,
	SynchronizeAll, // 完全同步，将下载笔记内容和附件
		SynchronizeAllMore,
	SynchronizeCatAll, // 完全同步某文件夹
		SynchronizeCatAllMore,
	SynchronizeCatNote, //简单同步某个文件夹的笔记
		SynchronizeCatNoteMore,
	SynchronizeNote, // 同步一篇笔记
		SynchronizeNoteMore,
	SynchronizeNoteAll, // 完全同步一篇笔记
		SynchronizeNoteAllMore,

	SyncNoteContent, // 下载笔记内容
	SyncNoteAtt, // 下载附件
	
	SyncProject, // 下载项目信息
	SynchronizeProjectsAll, //下载所有项目中的笔记,只下载文件夹和笔记标题,不下载笔记内容和附件

	/* 输入：
	 * 0 noteId
	 * 1 content
	 * 2 noteLocalId
	 * 3 note.revision
	 * 4 userId
	 * 5 nikeName
	 * 6 email
	 */
	SyncAddComment,//添加评论
	
	SyncComment,//下载评论
	
	UpdateInfo, // 获取最新版软件信息
	UpdateSoftware, // 下载apk，并更新
	
	DownloadSkin, //下载皮肤
	
	/* 输入：
	 * 0、"GET", "POST"
	 * 1、url
	 * 2、json参数
	 */
	OpenUrl,
	
	/* 轻笔记openAPI调用
	 * 0、"GET"、"POST"
	 * 1、cmd
	 * 2、json参数
	 */
	TNOpenUrl,
	
	/* Http方式 下载/上传 附件
	 * 0、"DOWNLOAD", "UPLOAD"
	 * 1、URL
	 * 2、保存路径outPath
	 */
	OpenFileUrl,
	
	/* 
	 * 0、url
	 */
	PartnerLogin360,
	PartnerLoginBaidu,	
	PartnerLoginSina,
	PartnerLogin360QQ,
	PartnerLoginGoogle,
	PartnerLoginRenRen,
	PartnerLogin189,
	
	/* input:
	 * 0 type 1 baidu, 2 sina, 3 QQ, 4 google, 5 360, 6 renren;
	 * 1 projectId
	 * 2 refreshToken 
	 */
	PartnerLoginRefreshToken,
	
	/* 
	 * 发微博:
	 * 0 TNWeibo
	 */
	OAuth2SendWeibo,
	
	//binding
	/* 0 username
	 * 1 password
	 * 2 netLoginType
	 * 3 operation
	 * 4 url
	 * 5 userType
	 */
	NetBinding,
	/* 
	 * 0 userType
	 */
	NetUnBinding,
	
	/* 
	 * 0 uri
	 */
//	BindingBaidu,	
	GetBindingList,
	
//	/* 
//	 * 0、accessToken
//	 * 1、attLocalId
//	 */
//	PcsUploadAtt,
//	PcsDownloadAtt,
//	PcsCheckAtt,
//	PcsQuota,
	
	/*
	 * 0 netdiskUrl
	 */
	HttpDownloadAtt,
	
	/* http方式从轻笔记服务器下载附件
	 * 
	 */
	TNHttpDownloadAtt,
	
	/*
	 * 0、netdiskType  0,all   1,百度
	 * 1、projectId    -1,全部      0,个人 
	 */
	GetNetDisk,
	
	/* 0 pageIndex 页码(1-9999)
	 * 1 pageSize 分页大小(1-100)
	 * 2 orderBy 排序方式	星级:"star",  公开时间:"shareTime"
	 */
	NetGetShareNotes,
	
	/* 
	 * 0 noteId
	 */
	NetAddCopyCount,
	
	
	//OpenAPI Task
	
	/* input:
	 * 
	 */
	OAuthLogin,
	
	OAuthCreateGroup,
	
	/* 输入：
	 * 0 note_id
	 * 1 width
	 * 2 height
	 */
	OAuthGetNoteThumbnail,
	
	//test
	Test;
}
