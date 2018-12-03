package entities;

import java.time.LocalDateTime;

public class ServiceCrash {
	private String serviceName;
	private int monitoredTimes;
	private LocalDateTime crashedAt;
	private LocalDateTime foundAt;
	private long elapsedTime;
	
	public ServiceCrash(String serviceName, int monitoredTimes, LocalDateTime crashedAt, LocalDateTime foundAt, long elapsedTime) {
		super();
		this.serviceName = serviceName;
		this.monitoredTimes = monitoredTimes;
		this.crashedAt = crashedAt;
		this.foundAt = foundAt;
		this.elapsedTime = elapsedTime;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public int getMonitoredTimes() {
		return monitoredTimes;
	}
	public void setMonitoredTimes(int monitoredTimes) {
		this.monitoredTimes = monitoredTimes;
	}
	public LocalDateTime getCrashedAt() {
		return crashedAt;
	}
	public void setCrashedAt(LocalDateTime crashedAt) {
		this.crashedAt = crashedAt;
	}
	public LocalDateTime getFoundAt() {
		return foundAt;
	}
	public void setFoundAt(LocalDateTime foundAt) {
		this.foundAt = foundAt;
	}
	public long getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((foundAt == null) ? 0 : foundAt.hashCode());
		result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceCrash other = (ServiceCrash) obj;
		return this.getServiceName().equals(other.getServiceName())
				&& this.getCrashedAt().compareTo(other.getCrashedAt()) == 0;
	}

	@Override
	public String toString() {
		return "ServiceCrash [serviceName=" + serviceName + ", monitoredTimes=" + monitoredTimes + ", crashedAt="
				+ crashedAt + ", foundAt=" + foundAt + ", elapsedTime=" + elapsedTime + "]";
	}
}
