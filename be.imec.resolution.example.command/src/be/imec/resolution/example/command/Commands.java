package be.imec.resolution.example.command;

import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import be.imec.resolution.example.api.Greeter;

@Component(service=Commands.class)
@GogoCommand(scope="example", function="greet")
@Requirement(namespace="be.imec.greeter.format", name="fancy")
// @RequireFancy
public class Commands {

	@Reference
	Greeter greeter;
	
	public void greet(String name) {
		System.out.println(greeter.greet(name));
	}
}
