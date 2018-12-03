// CArtAgO artifact code for project container_manager

package container_manager;

import java.util.Random;

import cartago.*;

enum actions {
	monitor,
	notMonitor
}

public class Monitor extends Artifact {
	
	void init() {
		defineObsProperty("action", actions.notMonitor);
		defineObsProperty("monitorProb", 0.5);
		defineObsProperty("notMonitorProb", 0.5);
	}

	@OPERATION
	void selectAction() {
		float monitorProb = getObsProperty("monitorProb").floatValue();
		
		Random rand = new Random();
		float randomValue = rand.nextFloat();
		
		if (randomValue >= 0 && randomValue <= monitorProb) {
			updateObsProperty("action", 0);
		}
		else {
			updateObsProperty("action", 1);
		}
	}
	
	@OPERATION
	void normalise() {
		float d = 0;
		double c0 = 0.5;
		double L = 0.001;
		float monitorProbValue = getObsProperty("monitorProb").floatValue();
		float notMonitorProbValue = getObsProperty("notMonitorProb").floatValue();
		
		if (monitorProbValue < notMonitorProbValue) {
			d = monitorProbValue;
		}else {
			d = notMonitorProbValue;
		}
		
		if (d < L) {
			double p = c0 - L / c0 - d;
			updateObsProperty("monitorProb", c0 - p * (c0 - monitorProbValue));
			updateObsProperty("notMonitorProb", c0 - p * (c0 - notMonitorProbValue));
		}
		
		float a1 = getObsProperty("monitorProb").floatValue();
		float a2 = getObsProperty("notMonitorProb").floatValue();
		float r =  a1 + a2;
		updateObsProperty("monitorProb", a1 / r);
		updateObsProperty("notMonitorProb", a2 / r);
	}
}

