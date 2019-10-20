package be.imec.resolution.example.install;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.repository.Repository;
import org.osgi.service.repository.RepositoryContent;
import org.osgi.service.repository.RequirementBuilder;
import org.osgi.service.resolver.HostedCapability;
import org.osgi.service.resolver.ResolveContext;
import org.osgi.service.resolver.Resolver;

@Component(service=InstallAgent.class)
@GogoCommand(scope="example", function="install")
public class InstallAgent {

	@Activate
	BundleContext context;
	
	@Reference
	Resolver resolver;
	
	@Reference
	volatile List<Repository> repositories;
	
	public void install(String id, String version) throws Exception {
		
		RequirementBuilder builder = repositories.get(0).newRequirementBuilder("osgi.identity");
		Requirement requirement = builder.addDirective("filter", String.format("(&(osgi.identity=%s)%s)", id, buildVersionFilter(version))).build();

		CurrentResolveContext resolveContext = new CurrentResolveContext(requirement);
		Map<Resource, List<Wire>> resources = resolver.resolve(resolveContext);

		List<Bundle> toStart = new ArrayList<>();
		for(Resource r : resources.keySet()){
			if(r instanceof RepositoryContent){
				RepositoryContent content = (RepositoryContent) r;	
				String location = (String) r.getCapabilities("osgi.content").get(0).getAttributes().get("url");
		
					Bundle b = context.installBundle(location, content.getContent());
					toStart.add(b);
			} 
		}

		for(Bundle b : toStart){
			b.start();
		}
	}
	
	
	private class CurrentResolveContext extends ResolveContext {

		private Collection<Resource> mandatoryResources; 
		
		public CurrentResolveContext(Requirement r) {
			List<Capability> found = findProviders(r);
			if(!found.isEmpty()){
				Resource resource = found.iterator().next().getResource();
				this.mandatoryResources = Collections.singleton(resource);
			} else {
				throw new RuntimeException("No resources found for requirement "+r);
			}
		}
		
		public Collection<Resource> getMandatoryResources(){
			return Collections.unmodifiableCollection(mandatoryResources);
		}

		@Override
		public List<Capability> findProviders(Requirement requirement) {
			List<Capability> capabilities = new ArrayList<Capability>();
			// First add the current framework's matching capabilities
			for(Bundle b : context.getBundles()){
				BundleRevision rev = b.adapt(BundleRevision.class);
				try {
					String filterStr = requirement.getDirectives().get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
					Filter filter = filterStr != null ? FrameworkUtil.createFilter(filterStr) : null;

					for (Capability cap : rev.getCapabilities(null)) {
						boolean match;
						if (filter == null){
							match = true;
						} else {
							match = filter.matches(cap.getAttributes());
						}
						if (match){
							capabilities.add(cap);
						}
					}
				}catch (InvalidSyntaxException e) {
					// our filter string should be correct
					e.printStackTrace();
				}
			}
			
			// Then search capabilities in the available Repositories
			for (Repository repo : repositories) {
				Collection<Capability> found = repo.findProviders(
						Collections.singleton(requirement)).values().iterator().next();
				for (Capability c : found) {
					capabilities.add(c);
				}
			}
				
			return capabilities;
		}

		@Override
		public int insertHostedCapability(List<Capability> capabilities,
				HostedCapability hostedCapability) {
			return 0;
		}

		@Override
		public boolean isEffective(Requirement requirement) {
			String e = requirement.getDirectives().get( "effective" );
			return e==null || "resolve".equals( e );
		}

		@Override
		public Map<Resource, Wiring> getWirings() {
			Map<Resource, Wiring> currentWiring = new HashMap<Resource, Wiring>();
			for(Bundle b : context.getBundles()){
				currentWiring.put(b.adapt(BundleRevision.class), b.adapt(BundleWiring.class));
			}
			return currentWiring;
		}
	}
	
	private static String buildVersionFilter(String version){
		String s ="";
		if(version.startsWith("[")){
			s+="(&";
			Version v = new Version(version.substring(1, version.indexOf(",")));
			s+=String.format("(version>=%s)", v.toString());
		} else if(version.startsWith("(")){
			s+="(&";
			Version v = new Version(version.substring(1, version.indexOf(",")));
			s+=String.format("(&(version>=%s)(!(version=%s)))", v.toString(), v.toString());
		} else {
			Version v = new Version(version);
			s=String.format("(version=%s)", v.toString());
		}
		if(version.endsWith(")")){
			Version v = new Version(version.substring(version.indexOf(",")+1, version.length()-1));
			s+=String.format("(!(version>=%s))", v.toString());
			s+=")";
		} else if (version.endsWith("]")){
			Version v = new Version(version.substring(version.indexOf(",")+1, version.length()-1));
			s+=String.format("(|(!(version>=%s))(version=%s))", v.toString(), v.toString());
			s+=")";
		}
		return s;
	}
}
