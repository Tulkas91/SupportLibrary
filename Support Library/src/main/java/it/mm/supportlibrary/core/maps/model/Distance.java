package it.mm.supportlibrary.core.maps.model;

import com.google.gson.annotations.SerializedName;

public class Distance{

	@SerializedName("text")
	private String text;

	@SerializedName("value")
	private int value;

	public void setText(String text){
		this.text = text;
	}

	public String getText(){
		return text;
	}

	public void setValue(int value){
		this.value = value;
	}

	public int getValue(){
		return value;
	}
}