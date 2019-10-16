package be.imec.resolution.example.greeter;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import be.imec.resolution.example.api.Greeter;
import be.imec.resolution.example.util.Util;

@Component
public class GreeterImpl implements Greeter {

	@Activate
	void activate() {
		System.out.println("Greeter implementation activated!");
	}
	
	@Override
	public String greet(String name) {
		return Util.cat("Hello,",name,"!");
	}
	
}
