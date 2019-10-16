package be.imec.resolution.example.fancygreeter;

import org.osgi.annotation.bundle.Capability;
import org.osgi.service.component.annotations.Component;

import be.imec.resolution.example.api.Greeter;
import be.imec.resolution.example.util.Util;

@Component
@Capability(namespace="be.imec.greeter.format", name="fancy")
public class FancyGreeterImpl implements Greeter {
	
	@Override
	public String greet(String name) {
		return Util.cat("°º¤ø,¸¸,ø¤º°`°º¤ø,¸","Hello,",name,"¸,ø¤°º¤ø,¸¸,ø¤º°");
	}
	
}
