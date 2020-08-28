package com.drivehome.start.util;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {
	
	public static boolean validateName(String name) {
		String regex = "^[A-Za-z0-9_][A-Za-z0-9_\\.\\(\\)\\-]*$";
		
		Pattern p = Pattern.compile(regex);
		
		Matcher m = p.matcher(name);
		
		return m.find();
	}
	
	public static boolean validatePath(String path) {
		path = path.trim();
		
		String regex = "^(.+)\\/([^/]+)$";
		
		Pattern p = Pattern.compile(regex);
		
		Matcher m = p.matcher(path);
		
		if (m.find()) {
			try {
				Paths.get(path);
				return true;
			} catch(InvalidPathException e) {
				return false;
			}
		} else {
			return false;
		}
	}

}
