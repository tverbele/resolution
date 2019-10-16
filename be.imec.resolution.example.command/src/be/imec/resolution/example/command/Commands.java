package be.imec.resolution.example.command;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import be.imec.resolution.example.api.Greeter;

@Component
public class Commands {

	@Reference
	Greeter greeter;
	
	@Activate
	void activate() {
		this.greet("OSGi Community Event");
	}
	
	public void greet(String name) {
		System.out.println(greeter.greet(name));
	}
}
