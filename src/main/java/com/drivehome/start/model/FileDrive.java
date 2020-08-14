package com.drivehome.start.model;

import java.io.Serializable;

public class FileDrive implements Serializable{
	private String name;
	private String path;
	
	public FileDrive () {}
	
	public FileDrive(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getPath() {
		return path;
	}



	public void setPath(String path) {
		this.path = path;
	}



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
