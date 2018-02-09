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
  
package sfobayarea.plan;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

public class ExtractActTypesLegModes {
/**\
 * Reads a plan file and prints all activity types and modes occuring in it.
 * @param args
 */
	public static void main(String[] args) {
		final Set<String> activityTypes = new HashSet<>();
		final Set<String> modes = new HashSet<>();
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		StreamingPopulationReader spr = new StreamingPopulationReader(scenario);
		spr.addAlgorithm(new PersonAlgorithm() {
			
			@Override
			public void run(Person person) {
				for (PlanElement pe : person.getSelectedPlan().getPlanElements()) {
					if (pe instanceof Activity) {
						activityTypes.add(((Activity) pe).getType());
					} else {
						Leg l = (Leg) pe;
						modes.add(l.getMode());
					}
				}
			}
		});
		spr.readFile("C:/Users/Joschka/Desktop/davis/scenario/plans_all.xml.gz");
		System.out.println("activities found:");
		activityTypes.forEach(s ->System.out.println(s)); 
		System.out.println("----------------\nModes found:");
		modes.forEach(s ->System.out.println(s)); 
	}

}
