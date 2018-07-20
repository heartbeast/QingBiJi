package com.thinkernote.ThinkerNote.General;

/**
 * 枚举
 */
public enum TNActionType {

	Db_Execute,
	DBReset,

	/*
	 * TODO 以下 删
	 */
	GetAllData,
	
	/*
	 * 获取单篇笔记的所有内容
	 */
	GetAllDataByNoteId,

	
	/*
	 * 获取标签列表
	 */
	GetTagList,

	
	/*
	 * 获取全部笔记
	 */
	GetNoteList,

	
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
	 * 获取第一级文件夹列表
	 */
	GetParentFolders,
	
	/*
	 * 获取父文件夹下的子文件夹列表
	 */
	GetFoldersByFolderId,

	
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

	NoteLocalRecovery,
	
	NoteLocalRealDelete,

	//*******************************足够华丽的同步分割线********************************
	Synchronize,
	
	//同步本地编辑的内容
	SynchronizeEdit,
	
	//完全同步文件夾
	SynchronizeCat,

	
	
}
