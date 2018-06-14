package com.thinkernote.ThinkerNote.Data;

import java.io.Serializable;

public class TNComment implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int commentLocalId;
	public long commentId;
	public long userId;
	public String content;
	public long noteLocalId;
	public long postTime;
	public String nickName;
	public String email;
	public int status; //0 active, 1 deleted;
}
