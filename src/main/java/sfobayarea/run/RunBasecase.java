/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package sfobayarea.run;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import sfobayarea.scoring.AddParkingCharges;
import sfobayarea.scoring.AgentSpecificVOTScoring;

/**
 * @author  jbischoff
 *
 */

public class RunBasecase {
public static void main(String[] args) {
	String configFile;
	if (args.length>0) {
		configFile = args[0];
	} else {
		configFile = "D:/matsim_davis/Scenario_1/matsim_input/config_0.05.xml";
	}
	//read in the config file:
	Config config = ConfigUtils.loadConfig(configFile);
	config.controler().setWritePlansUntilIteration(0);
	//from the config, read in all other files (such as network, population...). this is called a "scenario"
	Scenario scenario = ScenarioUtils.loadScenario(config);
	
	//based on the scenario, initiate a controler, which later runs the simulation
	Controler controler = new Controler(scenario);

	// add some custom extensions to the Controler
	controler.addOverridingModule(new AbstractModule(){
		@Override public void install() {
			//adding a person specific VOT for the utility function
			bindScoringFunctionFactory().to(AgentSpecificVOTScoring.class);
			
			//adding travel times for ride mode based on actual congestion
			addTravelTimeBinding(TransportMode.ride).to(networkTravelTime());
			addTravelDisutilityFactoryBinding(TransportMode.ride).to(carTravelDisutilityFactoryKey());
			
			//adding an event handler that takes care of calculating parking charges
			bind(AddParkingCharges.class).asEagerSingleton();
			install(new SwissRailRaptorModule());

		}});
		
	controler.run();
	
	
}
}
