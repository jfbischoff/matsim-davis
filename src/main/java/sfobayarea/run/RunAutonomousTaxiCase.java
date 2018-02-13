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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.av.flow.AvIncreasedCapacityModule;
import org.matsim.contrib.av.robotaxi.scoring.TaxiFareConfigGroup;
import org.matsim.contrib.av.robotaxi.scoring.TaxiFareHandler;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.taxi.run.TaxiConfigConsistencyChecker;
import org.matsim.contrib.taxi.run.TaxiConfigGroup;
import org.matsim.contrib.taxi.run.TaxiModule;
import org.matsim.contrib.taxi.run.TaxiOutputModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import sfobayarea.scoring.AddParkingCharges;
import sfobayarea.scoring.AgentSpecificVOTScoring;
import utils.ChangeLeg.ChangeAllLegModeWithPredefinedFromModesModule;

/**
 * @author  jbischoff
 *
 */

public class RunAutonomousTaxiCase {
public static void main(String[] args) {
	//read in the config file:
	Config config = ConfigUtils.loadConfig("C:/Users/anmol331\\Desktop\\scenario/config_0.1_at_case.xml",new DvrpConfigGroup(), new TaxiConfigGroup(),
			 new TaxiFareConfigGroup());
	TaxiConfigGroup.get(config).setChangeStartLinkToLastLinkInSchedule(true);
	config.addConfigConsistencyChecker(new TaxiConfigConsistencyChecker());
	config.checkConsistency();
	//from the config, read in all other files (such as network, population...). this is called a "scenario"
	Scenario scenario = ScenarioUtils.loadScenario(config);
	
	//based on the scenario, initiate a controler, which later runs the simulation
	Controler controler = new Controler(scenario);
	controler.addOverridingModule(new ChangeAllLegModeWithPredefinedFromModesModule() );
	controler.addOverridingModule(new AvIncreasedCapacityModule(1.5));
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
			
			addEventHandlerBinding().to(TaxiFareHandler.class).asEagerSingleton();

	}});
	

	controler.addOverridingModule(new TaxiOutputModule());
	controler.addOverridingModule(new TaxiModule());
		
	controler.run();
	
	
}
}
