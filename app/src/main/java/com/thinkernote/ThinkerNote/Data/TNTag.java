package com.thinkernote.ThinkerNote.Data;

import java.io.Serializable;

public class TNTag implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long tagLocalId;
	public long tagId;
	public String tagName;
	public String strIndex;
	public int trash;
	public long userId;
	
	public int noteCounts;
}
