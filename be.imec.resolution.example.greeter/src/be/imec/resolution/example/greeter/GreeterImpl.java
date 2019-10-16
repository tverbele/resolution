package be.imec.resolution.example.greeter;

import be.imec.resolution.example.util.Util;

public class GreeterImpl {

	public String greet(String name) {
		return Util.cat("Hello,",name,"!");
	}
	
}
