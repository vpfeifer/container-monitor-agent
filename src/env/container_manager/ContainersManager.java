// CArtAgO artifact code for project container_manager

package container_manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import cartago.*;
import entities.ServiceCrash;

public class ContainersManager extends Artifact {
	
	final DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock");
	
	HashMap<String, List<ServiceCrash>> crashes = new HashMap<String, List<ServiceCrash>>();
	HashMap<String, Integer> monitoredTimesMap = new HashMap<String, Integer>();
	
	void init() {
		defineObsProperty("running_containers", getContainers().size());
		defineObsProperty("step", 0);
	}
	
	@OPERATION
	void read() {
		List<Container> containersFromHost = getContainers();
		containersFromHost.forEach(c ->
		{
			if (!hasObsPropertyByTemplate("containers", c.id(), c.names().get(0).substring(1), c.image())) {
				defineObsProperty("containers", c.id(), c.names().get(0).substring(1), c.image());
			}
		});
		
		updateObsProperty("running_containers", containersFromHost.size());
	}
	
	@OPERATION
	void create(String serviceName, String image, String hostPort, String containerPort, OpFeedbackParam<String> id) {
		// Bind container port to host port.
		final Map<String, List<PortBinding>> portBindings = new HashMap<>();
		List<PortBinding> hostPorts = new ArrayList<>();
	    hostPorts.add(PortBinding.of("0.0.0.0", hostPort));
		portBindings.put(containerPort, hostPorts);
		
		final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
		
		ContainerConfig config = ContainerConfig.builder()
										.hostConfig(hostConfig)
										.image(image)
										.exposedPorts(containerPort)
										.build();
		
		try {
			ContainerCreation containerCreation = docker.createContainer(config, serviceName);
			docker.startContainer(containerCreation.id());
			
			updateRunningContainers(getContainers().size());
			
			id.set(containerCreation.id());
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@OPERATION
	void remove(String serviceName) {
		List<Container> runningContainers = getContainers();
		runningContainers.forEach(container -> {
			if (container.names().contains("/"+serviceName)) {
				String id = container.id();
				try {
					docker.killContainer(id);
					docker.removeContainer(id);
				} catch (DockerException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		updateRunningContainers(runningContainers.size());
	}
	
	@OPERATION
	void start(String serviceName) {
		try {
			final List<Container> exitedContainers = docker.listContainers(ListContainersParam.withStatusExited());
			for(Container container : exitedContainers)
			{
				if (container.names().contains("/"+serviceName)) {
					docker.startContainer(container.id());
					return;
				}
			}
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@OPERATION
	void monitor(String serviceName)
	{
		incrementMonitoredTimes(serviceName);
		
		List<Container> runningContainers = getContainers();
		for(Container container : runningContainers)
		{
			if (container.names().contains("/"+serviceName)) {
				return;
			}
		}
		
		try {
			final List<Container> exitedContainers = docker.listContainers(ListContainersParam.withStatusExited());
			for(Container container : exitedContainers)
			{
				if (container.names().contains("/"+serviceName)) {
					ContainerInfo info = docker.inspectContainer(container.id());
					LocalDateTime exitedDate = info.state().finishedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					LocalDateTime now = LocalDateTime.now();
					Duration diff = Duration.between(exitedDate, now);
					
					Integer monitoredTimes = monitoredTimesMap.get(serviceName);
					
					List<ServiceCrash> serviceCrashes = crashes.get(serviceName);
					ServiceCrash crash = new ServiceCrash(serviceName, monitoredTimes, exitedDate, now, diff.toMillis());
					
					if (serviceCrashes == null || !serviceCrashes.contains(crash)) {
						addCrash(crash);
						monitoredTimesMap.replace(serviceName, 0);
						signal("serviceCrash", serviceName);
					}
					
					return;
				}
			}
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		signal("noContainersForService", serviceName);
	}
	
	@OPERATION
	void monitorAI(String serviceName, String action, int reward, int cost, OpFeedbackParam<Integer> pay)
	{
		if ("monitor".equals(action)) {
			incrementMonitoredTimes(serviceName);
		}
		
		List<Container> runningContainers = getContainers();
		for(Container container : runningContainers)
		{
			if (container.names().contains("/"+serviceName)) {
				// monitor and not fail
				if ("monitor".equals(action)) {
					pay.set(cost * -1);
				}
				// not monitor and not fail
				if ("notMonitor".equals(action)) {
					pay.set(0);
				}
				return;
			}
		}
		
		if("monitor".equals(action))
		{
			try {
				final List<Container> exitedContainers = docker.listContainers(ListContainersParam.withStatusExited());
				for(Container container : exitedContainers)
				{
					if (container.names().contains("/"+serviceName)) {
						ContainerInfo info = docker.inspectContainer(container.id());
						LocalDateTime exitedDate = info.state().finishedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
						LocalDateTime now = LocalDateTime.now();
						Duration diff = Duration.between(exitedDate, now);
						
						Integer monitoredTimes = monitoredTimesMap.get(serviceName);
						
						List<ServiceCrash> serviceCrashes = crashes.get(serviceName);
						ServiceCrash crash = new ServiceCrash(serviceName, monitoredTimes, exitedDate, now, diff.toMillis());
						
						if (serviceCrashes == null || !serviceCrashes.contains(crash)) {
							addCrash(crash);
							monitoredTimesMap.replace(serviceName, 0);
							signal("serviceCrash", serviceName);
						}
						
						return;
					}
				}
			} catch (DockerException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			signal("noContainersForService", serviceName);
			
			// monitor and fail
			pay.set(reward - cost);
		}
		
		// not monitor and fail
		pay.set(reward * -1);
	}
	
	@OPERATION
	void stopRandom() {
		try {
			final List<Container> runningContainers = getContainers();
			if (runningContainers.size() > 0) {
				Random rand = new Random();
				int index = rand.nextInt(runningContainers.size());
				Container container = runningContainers.get(index);
				docker.stopContainer(container.id(), 1);
			}
			
			ObsProperty prop = getObsProperty("step");
			prop.updateValue(prop.intValue() + 1);
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@OPERATION
	void printResults() {
		System.out.println("Monitored Services :");
		System.out.println(monitoredTimesMap.toString());
		System.out.println("Crashes :");
		System.out.println(crashes.toString());
	}
	
	private void addCrash(ServiceCrash crash) {
		String serviceName = crash.getServiceName();
		if (crashes.containsKey(serviceName)) {
			List<ServiceCrash> value = crashes.get(serviceName);
			value.add(crash);
			crashes.replace(serviceName, value);
			return;
		}
		
		List<ServiceCrash> newCrashes = new ArrayList<ServiceCrash>();
		newCrashes.add(crash);
		crashes.put(serviceName, newCrashes);
	}

	private void incrementMonitoredTimes(String serviceName) {
		if (monitoredTimesMap.containsKey(serviceName)) {
			Integer value = monitoredTimesMap.get(serviceName);
			monitoredTimesMap.replace(serviceName, value + 1);
			return;
		}
		
		monitoredTimesMap.put(serviceName, 1);
	}

	void updateRunningContainers(int size) {
		ObsProperty runningContainersProp = getObsProperty("running_containers");
		runningContainersProp.updateValue(size);
	}
	
	List<Container> getContainers()
	{
		try {
			return docker.listContainers();
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}