package com.drivehome.start.service;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.drivehome.start.model.FileDrive;

public interface IDriveHomeService {
	public abstract void uploadFiles(MultipartFile[] files, String path) throws Exception;
	public abstract Map<String,Object> getDirectoriesAndFiles(String path) throws Exception;
	public abstract void deleteElement(FileDrive[] elements) throws Exception;
	public abstract Boolean createDir(String path, String name) throws Exception;
	public abstract Boolean renameElement(String path, String oldName, String newName) throws Exception;
	public abstract Resource[] download(FileDrive[] files) throws Exception;
	public abstract Resource[] getResourcesFolder(String path, String folder) throws Exception;
}
