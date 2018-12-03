// Agent test_agent in project container_manager

/* Initial beliefs and rules */


/* Initial goals */

!start.

/* Plans */

+!start <- .wait(5000)
		   ?observeContainers(C);
		   focus(C);
		   !test.

+?observeContainers(Id) <- lookupArtifact("containers",Id).

-?observeContainers(Id) <- .wait(10);
						 ?observeContainers(Id).

+!test : step(X) <- .println("Running step ", X);
					stopRandom;
					.wait(5000);
					!checkStopCondition.

+!checkStopCondition : step(5) <- .println("Stop");
								    printResults.

-!checkStopCondition <- !test.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }