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

package sfobayarea.config;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;

/**
 * @author  jbischoff
 *
 */
/**
 *
 */
public class BasecaseConfigGenerator {
	public static void main(String[] args) {
		String basefolder = "C:\\Users\\Joschka\\Desktop\\davis\\scenario/";
		new BasecaseConfigGenerator().run(basefolder,0.1,0.3);
	}
	
	public void run(String basefolder, double flowCap, double storageCap){		
		Config config = ConfigUtils.createConfig();
		
	
		//network
		config.network().setInputFile("network_cleaned.xml");
	
		ControlerConfigGroup ccg = config.controler();
		ccg.setOutputDirectory("output/basecase");
		ccg.setFirstIteration(0);
		int lastIteration = 100;
		ccg.setLastIteration(lastIteration);
		ccg.setMobsim("qsim");
		ccg.setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		ccg.setWriteEventsInterval(100);
		ccg.setWritePlansInterval(100);
		config.global().setNumberOfThreads(16);
		
		QSimConfigGroup qsc = config.qsim();
		qsc.setUsingFastCapacityUpdate(true);
		qsc.setTrafficDynamics(TrafficDynamics.withHoles);
		qsc.setNumberOfThreads(8);
		qsc.setStorageCapFactor(storageCap);
		qsc.setFlowCapFactor(flowCap);
		qsc.setEndTime(30*3600);
		
		config.parallelEventHandling().setNumberOfThreads(6);
		
		
		config.plans().setInputFile("plans_0.1.xml.gz");
		
		ActivityParams escort = new ActivityParams("escort");
		escort.setTypicalDuration(900);
		config.planCalcScore().addActivityParams(escort);

		ActivityParams eatout = new ActivityParams("eatout");
		eatout.setTypicalDuration(3600);
		config.planCalcScore().addActivityParams(eatout);
		
		ActivityParams othmaint = new ActivityParams("othmaint");
		othmaint.setTypicalDuration(1800);
		config.planCalcScore().addActivityParams(othmaint);
		
		ActivityParams othdiscr = new ActivityParams("othdiscr");
		othdiscr.setTypicalDuration(1800);
		config.planCalcScore().addActivityParams(othdiscr);
		
		ActivityParams atwork = new ActivityParams("atwork");
		atwork.setTypicalDuration(8*3600);
		config.planCalcScore().addActivityParams(atwork);
		
		ActivityParams work = new ActivityParams("work");
		work.setTypicalDuration(8*3600);
		config.planCalcScore().addActivityParams(work);
		
		ActivityParams school = new ActivityParams("school");
		school.setTypicalDuration(8*3600);
		config.planCalcScore().addActivityParams(school);
		
		ActivityParams university = new ActivityParams("university");
		university.setTypicalDuration(8*3600);
		config.planCalcScore().addActivityParams(university);
		
		ActivityParams social = new ActivityParams("social");
		social.setTypicalDuration(2*3600);
		config.planCalcScore().addActivityParams(social);
		
		ActivityParams shopping = new ActivityParams("shopping");
		shopping.setTypicalDuration(2*3600);
		config.planCalcScore().addActivityParams(shopping);
		
		ActivityParams home = new ActivityParams("home");
		home.setTypicalDuration(11*3600);
		config.planCalcScore().addActivityParams(home);
		
		
		//TODO: adjust rates
		ModeParams car = config.planCalcScore().getModes().get(TransportMode.car);
		car.setMonetaryDistanceRate(-0.0001236);
		car.setMarginalUtilityOfTraveling(-3);
		car.setConstant(0);
		
		ModeParams ride = config.planCalcScore().getModes().get(TransportMode.ride);
		ride.setMonetaryDistanceRate(-0.00011236);
		ride.setMarginalUtilityOfTraveling(-3);
		ride.setConstant(0);
		
		ModeParams pt = config.planCalcScore().getModes().get(TransportMode.pt);
		pt.setMarginalUtilityOfTraveling(-1.5);
		pt.setConstant(-1.5);
	
		ModeParams walk= config.planCalcScore().getModes().get(TransportMode.walk);
		walk.setMarginalUtilityOfTraveling(-1.5);
		
		ModeParams bike = config.planCalcScore().getModes().get(TransportMode.bike);
		bike.setMarginalUtilityOfTraveling(-3);
		bike.setConstant(-2);
		
		ModeRoutingParams bikeP = config.plansCalcRoute().getOrCreateModeRoutingParams(TransportMode.bike);
		bikeP.setBeelineDistanceFactor(1.3);
		bikeP.setTeleportedModeSpeed(3.333);
		
		ModeRoutingParams walkP = config.plansCalcRoute().getOrCreateModeRoutingParams(TransportMode.walk);
		walkP.setBeelineDistanceFactor(1.3);
		walkP.setTeleportedModeSpeed(1.0);
		
		ModeRoutingParams ptP = config.plansCalcRoute().getOrCreateModeRoutingParams(TransportMode.pt);
		ptP.setTeleportedModeFreespeedFactor(2.0);
		
		StrategySettings reroute = new StrategySettings();
		reroute.setStrategyName(DefaultStrategy.ReRoute.toString());
		reroute.setWeight(0.3);
		config.strategy().addStrategySettings(reroute);
		
		
		StrategySettings changeExpBeta = new StrategySettings();
		changeExpBeta.setStrategyName(DefaultSelector.ChangeExpBeta.toString());
		changeExpBeta.setWeight(0.7);
		config.strategy().addStrategySettings(changeExpBeta);
		
		
		config.strategy().setFractionOfIterationsToDisableInnovation(.8);
		
		config.strategy().setMaxAgentPlanMemorySize(5);
		
		
		new ConfigWriter(config).write(basefolder+"/config_"+flowCap+".xml");

		
		
	}
}
