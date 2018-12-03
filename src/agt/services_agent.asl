/* Initial beliefs and rules */


/* Initial goals */

!start.

/* Plans */

+!start <- makeArtifact("services","container_manager.ServicesManager",[],ID);
		   focus(ID);
		   !checkConfiguredServices.

+!checkConfiguredServices : configured_services(X) <- .findall(Name, services(Name,_,_,_,_), ServiceNameList);
													  .println(X, " configured services found : ", ServiceNameList);
													  .wait(1000)
													  !checkServices.
													  
+!checkServices <- reloadServices;
				   .wait(2000);
				   !checkServices.

+newService(Name, Image, HostPort, ContainerPort) <- .println("New service was created ", Name).		 

+removedService(Name) <- .println("Service ", Name," was removed...");
						 .send(containers_agent, achieve, removeContainers(Name)).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }