/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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
  
package sfobayarea.scoring;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.CharyparNagelMoneyScoring;
import org.matsim.core.scoring.functions.ScoringParameters;

public class AgentSpecificVOTScoring implements ScoringFunctionFactory {

	private final Scenario scenario;
	@Singleton
	@Inject
	public AgentSpecificVOTScoring(Scenario scenario) {
		this.scenario = scenario;
	}
	
	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		SumScoringFunction sumScoringFunction = new SumScoringFunction();
		
		// Score activities, legs, payments and being stuck
		// with the default MATSim scoring based on utility parameters in the config file.
		ScoringParameters.Builder builder = new ScoringParameters.Builder(scenario, person.getId());

		Double vot = (Double) person.getAttributes().getAttribute("vot");
		if (vot!=null) {
		builder.setMarginalUtilityOfPerforming_s(vot/3600.);
		}else {
		Logger.getLogger(getClass()).warn("No custom VOT value found for person "+person.getId().toString()+". Assuming default value.");
		}
		ScoringParameters params = builder.build();
		sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(params));
		sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(params, scenario.getNetwork()));
		sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring(params));
		sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(params));
		return sumScoringFunction;
	}

}
