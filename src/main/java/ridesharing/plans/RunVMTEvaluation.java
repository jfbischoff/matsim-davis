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
  
package ridesharing.plans;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

public class RunVMTEvaluation {

	public static void main(String[] args) {
		String matsimOutputFolder = "D:/matsim/rideshare/run01/";
		String vmtSavingsFile = "C:/Users/Joschka/Desktop/davis/Scenario_2/vmtsavings.csv";
		Map<String,Double> vmtPerPerson = new HashMap<>();
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile(matsimOutputFolder+"/output_plans.xml.gz");
		for (Person p : scenario.getPopulation().getPersons().values()) {
			Leg l1 = (Leg) p.getSelectedPlan().getPlanElements().get(1);
			double dist = l1.getRoute().getDistance();
			vmtPerPerson.put(p.getId().toString(),dist);
		}
		BufferedWriter bw = IOUtils.getBufferedWriter(vmtSavingsFile);
		try {
			bw.write("personId,VMTsaved");
			for (Entry<String, Double> e : vmtPerPerson.entrySet()) {
				bw.write("\n"+e.getKey()+","+e.getValue());
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
