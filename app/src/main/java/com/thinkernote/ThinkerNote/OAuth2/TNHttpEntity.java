package com.thinkernote.ThinkerNote.OAuth2;

import org.apache.http.HttpEntity;

public class TNHttpEntity {
	private HttpEntity entity;
	private long headValue;
	
	public TNHttpEntity(HttpEntity entity, long thumbnailAttId){
		this.entity = entity;
		this.headValue = thumbnailAttId;
	}
	
	public HttpEntity getEntity() {
		return entity;
	}
	
	public long getHeadValue() {
		return headValue;
	}
	
}
