/* Initial beliefs and rules */


/* Initial goals */

!start.

/* Plans */

+!start : true <- makeArtifact("containers","container_manager.ContainersManager",[],ID);
				  focus(ID);
				  !printRunningContainers;
				  !checkContainers.

+!checkContainers <- read;
					 .wait(2000);
					 !checkContainers.

+!printRunningContainers : running_containers(X) <- .println(X, " containers running...").

+!createContainer(Service, Image, HostPort, ContainerPort)  <- .println("Creating container for service ", Service);
																 create(Service, Image, HostPort, ContainerPort, Id);
																 .println("Container created for service ", Service," with Id ", Id);
																 +containers(Id, Service, Image).
																 
-!createContainer(Service, Image, HostPort, ContainerPort) <- .println("Error trying to create a container for service ", Service).

									 
+!removeContainers(Service) <- .println("Creating container for service ", Service);
								remove(Service);
								-containers(_, Service, _);
								.println("Containers for service ", Service," were removed.").

+serviceCrash(Service) <- .println("Restarting crashed service ", Service);
						  start(Service).

+noContainersForService(Service) <- .print("There is no containers running for service ", Service);
									.send(services_agent, askOne, services(Service,_,_,_,_));
									.wait(300);
									!hasService(Service).

+!hasService(Service) : services(Service, Image, Replicas, HostPort, ContainerPort) <- !createContainer(Service, Image, HostPort, ContainerPort).

-!hasService(Service) <- .println("Error trying to create a container for service ", Service).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }