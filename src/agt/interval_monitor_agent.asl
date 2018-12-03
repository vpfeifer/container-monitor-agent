// Agent interval_monitor_agent in project container_manager

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start <- .wait(2000)
		   ?observeServices(S);
		   focus(S);
		   ?observeContainers(C);
		   focus(C);
		   println("Interval monitor initialized");
		   !monitorServices.

+?observeServices(Id) <- lookupArtifact("services",Id).

-?observeServices(Id) <- .wait(10);
						 ?observeServices(Id).

+?observeContainers(Id) <- lookupArtifact("containers",Id).

-?observeContainers(Id) <- .wait(10);
						 ?observeContainers(Id).
						 
+!monitorServices <- for(services(Name, Image, Replicas, HostPort, ContainerPort)){
						monitor(Name);
					  };
					  .wait(500)
					  !monitorServices.
					  
+serviceCrash(Service) <- .println("Down service found ", Service).

+noContainersForService(Service) <- .print("Service without container found ", Service).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }