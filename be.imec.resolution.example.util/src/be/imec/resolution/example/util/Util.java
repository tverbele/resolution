package be.imec.resolution.example.util;

public class Util {

	public static String cat(String... strings) {
		if(strings == null) {
			return "";
		} else if(strings.length == 1) {
			return strings[0];
		} else {
			String result = strings[0];
			for(int i = 1; i < strings.length; i++) {
				result = result + " " + strings[i];
			}
			return result;
		}
	}
}
