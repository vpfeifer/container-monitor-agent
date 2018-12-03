package entities;

public class Service {
	private String name;
	private String image;
	private long replicas;
	private String hostPort;
	private String containerPort;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	public long getReplicas() {
		return replicas;
	}

	public void setReplicas(long replicas) {
		this.replicas = replicas;
	}
	
	public String getContainerPort() {
		return containerPort;
	}

	public void setContainerPort(String containerPort) {
		this.containerPort = containerPort;
	}
	
	@Override
	public String toString() {
		return this.name + " - " + this.image;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Service)) {
			return false;
		}
		final Service other = (Service)obj;
		return this.getName().equals(other.getName());
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}
}