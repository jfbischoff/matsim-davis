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

package bartaccess.run;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.matsim.contrib.drt.analysis.zonal.DrtZonalModule;
import org.matsim.contrib.drt.analysis.zonal.DrtZonalSystem;
import org.matsim.contrib.drt.optimizer.rebalancing.DemandBasedRebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.drt.run.DrtConfigConsistencyChecker;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.*;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

/**
 * @author jbischoff
 */
public class RunDRTScenario {
	
	
	public static void run(Config config, boolean otfvis) {
		config.addConfigConsistencyChecker(new DrtConfigConsistencyChecker());
		config.checkConsistency();
		config.controler().setLastIteration(5);

		
		
		Controler controler = DrtControlerCreator.createControler(config, otfvis);
		DrtZonalSystem zones = new DrtZonalSystem(controler.getScenario().getNetwork(), 3000);
		
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				bind(RebalancingStrategy.class).to(DemandBasedRebalancingStrategy.class).asEagerSingleton();
				bind(DrtZonalSystem.class).toInstance(zones);
			}
		});

		controler.addOverridingModule(new DrtZonalModule());
		controler.run();
	}

	

	public static void main(String[] args) {
		
		Config config = ConfigUtils.loadConfig("C:/Users/anmol331/Desktop/Scenario_3/configDRT.xml", new DrtConfigGroup(), new DvrpConfigGroup(), new OTFVisConfigGroup());
		run(config,false);

	}
}