// CArtAgO artifact code for project container_manager

package container_manager;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import cartago.*;
import entities.Service;

public class ServicesManager extends Artifact {
	
	private final String PATH = "/home/vpfeifer/container-manager/services";
	private List<Service> services;
	
	void init() {
		loadServices();
	}
	
	List<Service> getServices() {
		JSONParser parser = new JSONParser();
		List<Service> result = new ArrayList<Service>();
		
		try (Stream<Path> paths = Files.walk(Paths.get(PATH))) {
			paths.filter(f -> f.toString().endsWith(".service"))
			.forEach(f ->
			{
				try 
				{
					Object obj = parser.parse(new FileReader(f.toString()));
					JSONObject jsonObject = (JSONObject) obj;
					
					Service service = new Service();
					service.setName((String)jsonObject.get("name"));
					service.setImage((String)jsonObject.get("image"));
					service.setReplicas((long)jsonObject.get("replicas"));
					service.setHostPort((String)jsonObject.get("hostPort"));
					service.setContainerPort((String)jsonObject.get("containerPort"));
					
					result.add(service);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			});
			
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	void loadServices()
	{
		services = getServices();
		services.forEach(s -> {
			defineObsProperty("services", s.getName(), s.getImage(), s.getReplicas(), s.getHostPort(), s.getContainerPort());
		});
		defineObsProperty("configured_services", services.size());
	}
	
	@OPERATION
	void reloadServices()
	{
		List<Service> servicesFromDisk = getServices();
		servicesFromDisk.forEach(s ->
		{
			if (!hasObsPropertyByTemplate("services", s.getName(), s.getImage(), s.getReplicas(), s.getHostPort(), s.getContainerPort())) {
				defineObsProperty("services", s.getName(), s.getImage(), s.getReplicas(), s.getHostPort(), s.getContainerPort());
				signal("newService", s.getName(), s.getImage(), s.getHostPort(), s.getContainerPort());
			}
		});
		
		services.forEach(s ->
		{
			if (!servicesFromDisk.contains(s)) {
				removeObsPropertyByTemplate("services", s.getName(), s.getImage(), s.getReplicas(), s.getHostPort(), s.getContainerPort());
				signal("removedService", s.getName());
			}
		});
		
		updateObsProperty("configured_services", servicesFromDisk.size());
		services = servicesFromDisk;
	}
}