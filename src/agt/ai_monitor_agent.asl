// Agent ai_monitor_agent in project container_manager

/* Initial beliefs and rules */

//Parameters
reward(5).
cost(1).
penalty(6).
benefit(2).
learningRate(0.8).
discountFactor(0.65).
gradientStep(0.0001).
importanceMax(1).
importance(1).
importanceMin(0).

qMonitor(0).
qNotMonitor(0).

failProb(0).
notFailProb(0).

timeSlot(1).

/* Initial goals */

!start.

/* Plans */

+!start <- .wait(2000)
		   makeArtifact("monitor","container_manager.Monitor",[],M);
		   focus(M);
		   ?observeServices(S);
		   focus(S);
		   ?observeContainers(C);
		   focus(C);
		   println("AI monitor initialized");
		   !monitorServices.

+?observeServices(Id) <- lookupArtifact("services",Id).

-?observeServices(Id) <- .wait(10);
						 ?observeServices(Id).

+?observeContainers(Id) <- lookupArtifact("containers",Id).

-?observeContainers(Id) <- .wait(10);
						 ?observeContainers(Id).
						 
+!monitorServices <- for(services(Name, Image, Replicas, HostPort, ContainerPort)){
										selectAction;
										!executeAction(Name);
					  				};
					  				.wait(500)
					  				!monitorServices.

+!executeAction(Service) : action(0) & qMonitor(Q) 
				  & learningRate(L)  & discountFactor(D)
				  & reward(R) 		 & cost(C) 
				  & importanceMax(I) & qNotMonitor(QL) <- monitorAI(Service, "monitor", R, C, PAY);
				  										  -+qMonitor((1 - L) * Q + L * (PAY + D * I * QL));
				  										  !approximateMonitorFailureProbability;
				  										  !updateImportance;
				  										  !updateActionsProbabilities;
				  										  normalise;
				  										  !updateLearningRate.
				  										  
+!executeAction(Service) : action(1) & qNotMonitor(Q) 
				  & learningRate(L)  & discountFactor(D) 
				  & reward(R) 		 & cost(C) 
				  & importanceMax(I) & qMonitor(QL)    <- monitorAI(Service, "notMonitor", R, C, PAY);
				  										  -+qNotMonitor((1 - L) * Q + L * (PAY + D * I * QL));
				  										  !approximateNotMonitorFailureProbability;
				  										  !updateImportance;
				  										  !updateActionsProbabilities;
				  										  normalise;
				  										  !updateLearningRate.
				  										  
-!executeAction(Service) <- .println("Selected action not exists").


+!approximateMonitorFailureProbability : failProb(F) & qMonitor(Q) 
										& reward(R) & cost(C) & notFailProb(NF) <- -+failProb((Q / (R - C)) - (-1 * C) * NF);
																				   -+notFailProb((Q / (-1 * C)) - (R - C) * F).

+!approximateNotMonitorFailureProbability : failProb(F) & qNotMonitor(Q) 
											& reward(R) & cost(C) & notFailProb(NF) <- -+failProb(Q / (R * -1));
																					   -+notFailProb(R * F).

+!updateImportance <- -+importance(0.5).

+!updateActionsProbabilities : monitorProb(M) & notMonitorProb(NM) 
							  & importanceMax(IM) & importance(I) &	gradientStep(GS)
							  & qMonitor(QM)  & qNotMonitor(QNM)    <- -+monitorProb(I * (M + GS * QM));
							  										   -+notMonitorProb((IM - I) * (NM + GS * QNM)).

+!updateLearningRate : timeSlot(K) & learningRate(L) <- -+learningRate(K / (K + 1) * L);
														-+timeSlot(K + 1).
														
-!updateLearningRate <- .println("Error trying to update learning rate").

+serviceCrash(Service) <- .println("Down service found ", Service).

+noContainersForService(Service) <- .print("Service without container found ", Service).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }