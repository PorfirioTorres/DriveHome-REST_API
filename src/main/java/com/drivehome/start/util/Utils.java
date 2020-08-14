package com.drivehome.start.util;

public class Utils {
	 public static String generateName() {
	        StringBuilder name = new StringBuilder("");
	        String characteres = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	        for (int i = 1; i <= 10; i++) {
	            int character = (int)(Math.random() * characteres.length());
	            name.append(characteres.charAt(character));
	        }
	        return name.toString();
	    }
}
