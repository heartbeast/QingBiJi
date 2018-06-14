package com.thinkernote.ThinkerNote.General;

public enum TNActionType {
	//	Init,
	//	Deinit,
	
	TNOpenUrl,
	
	/* 输入：
	 * 0、phone 手机号
	 * 1、password 密码
	 * 2、vcode 
	 */	
	Register,

	/*
	获取图形验证码
	 */
	Captcha,
	
	/* 输入：
	 * 0、phone String
	 * 1、t  //register
	 */	
	VerifyCode,
	
	/*
	 * 重置密码
	 */
	ForgotPassword,
	
	/*
	 * 通过邮箱找回密码
	 */
	FindPswByEmail,
			
	/* 输入：
	 * 0、username String
	 * 1、password String
	 */	
	Login,
	
	/*
	 * 第三方登录
	 */
	LoginThird,
	
	/*
	 * 第三方登录绑定
	 */
	LoginBind,
	
	/*
	 * 修改密码
	 */
	ChangePassword,
	
	/*
	 * 修改用户名手机号邮箱
	 */
	ChangeUserNameOrEmail,
	
	/*
	 * 修改手机号
	 */
	ChangePhone,
	
	/*
	 * 获取个人信息
	 * 输入：
	 * 0、token
	 */
	Profile,
	
	/*
	 * 验证邮箱
	 */
	VerifyEmail,
	
	/*
	 * 清除缓存
	 */
	ClearCache,
	
	/*
	 * 意见反馈
	 */
	FeedBack,
	
	/*
	 * 操作数据库
	 */
	Db_Execute,
	
	DBReset,
	
	DbReportError,

	Logout,
	
	/*
	 * 获取所有的内容
	 */
	GetAllData,
	
	/*
	 * 获取单篇笔记的所有内容
	 */
	GetAllDataByNoteId,
	
	/*
	 * 新增标签
	 * 0、name
	 */
	TagAdd,
	
	/*
	 * 删除标签
	 * 0、tag_id
	 */
	TagDelete,
	
	/*
	 * 标签重命名
	 * 0、tag_id
	 * 1、name
	 */
	TagRename,
	
	/*
	 * 获取标签列表
	 */
	GetTagList,
	
	/*
	 * 新增笔记
	 */
	NoteAdd,
	
	/*
	 * 编辑笔记
	 */
	NoteEdit,
	
	/*
	 * 删除笔记
	 */
	NoteDelete,
	
	/*
	 * 回收站删除笔记
	 */
	NoteRealDelete,
	
	/*
	 * 清空回收站
	 */
	ClearRecycle,
	
	/*
	 * 移动笔记到文件夹
	 */
	NoteMoveTo,
	
	/*
	 * 修改笔记创建时间
	 */
	NoteChangeCreateTime,
	
	/*
	 * 修改标签
	 */
	NoteChangeTag,
	
	/*
	 * 回收站恢复笔记
	 */
	NoteRecovery,
	
	/*
	 * 获取回收站的笔记
	 */
	GetNoteListByTrash,
	
	/*
	 * 获取全部笔记
	 */
	GetNoteList,
	
	/*
	 * 清空回收站的笔记
	 */
	ClearNotesByTrash,
	
	/*
	 * 获取文件夹下的笔记
	 */
	GetNoteListByFolderId,
	
	/*
	 * 获取标签下的笔记
	 */
	GetNoteListByTagId,
	
	/*
	 * 搜索笔记
	 */
	GetNoteListBySearch,
	
	/*
	 * 获取一篇笔记
	 */
	GetNoteByNoteId,

	/*
	 * 获取回收站的笔记
	 */
	GetTrashNoteByNoteId,
	
	/*
	 * 获取第一级文件夹列表
	 */
	GetParentFolders,
	
	/*
	 * 获取父文件夹下的子文件夹列表
	 */
	GetFoldersByFolderId,
	
	/*
	 * 获取所有文件夹
	 */
	GetAllFolders,
	
	/*
	 * 创建文件夹
	 */
	FolderAdd,
	
	/*
	 * 更新文件夹
	 */
	FolderEdit,
	
	/*
	 * 删除文件夹
	 */
	FolderDelete,
	
	/*
	 * 设置默认文件夹
	 */
	SetDefaultFolderId,
	
	/*
	 * 移动文件夹
	 */
	FolderMoveTo,
	
	/*
	 * 下载文件
	 */
	TNHttpDownloadAtt,
	
	/*
	 * 下载文件
	 */
	OpenFileUrl,
	
	/*
	 * 下载笔记属性
	 */
	SyncNoteAtt,
	
	/*
	 * 获取所有笔记的id
	 */
	GetAllNoteIds,

	/*
	 * 获取所有回收站笔记的id
	 */
	GetAllTrashNoteIds,
	
	/*
	 * 获取文件夹下所有笔记的id
	 */
	GetFolderNoteIds,
	
	/*
	 * 上传文件
	 */
	Upload,
	
	/*
	 * 版本更新
	 */
	Upgrade,
	
	/*
	 * 下载软件
	 */
	UpdateSoftware,
	
	/*
	 * 打赏
	 */
	Pay,
	
	
	//*******************************华丽的本地分割线*******************************
	NoteSave,
	
	NoteLocalAdd,
	
	NoteLocalEdit,
	
	NoteLocalDelete,
	
	NoteLocalRecovery,
	
	NoteLocalRealDelete,
	
	NoteLocalMoveTo,
	
	NoteLocalChangeTag,
	
	NoteLocalChangeCreateTime,
	
	ClearLocalRecycle,
	
	AttLocalSave,
	
	AttLocalDelete,
	
	//*******************************足够华丽的同步分割线********************************
	Synchronize,
	
	//同步本地编辑的内容
	SynchronizeEdit,
	
	//完全同步文件夾
	SynchronizeCat,

	//同步回收站的笔记
	SynchronizeTrash
	
	
}
