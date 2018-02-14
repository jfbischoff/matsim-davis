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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import utils.DavisMatsimUtils;

public class ConvertAndEnrichPlansFile {
	
	Map<Id<Person>,Double> vots = new HashMap<>();
	Map<String,List<Link>> linksPerTaz;
	Scenario scenario;
	/**
	 * Reads a plan file, samples it down to a percentage and adds the vot parameter from a csv file. 
	 * Also replaces taxi mode by car mode. 
	 * 
	 */
	public static void main(String[] args) {
	
		String inputPlansFile = "C:/Users/anmol331/Desktop/scenario/plans_all_7.xml.gz";
		String inputCsvFile = "C:/Users/anmol331/Desktop/scenario/personData_7.csv";
		String inputNetworkFile = "C:/Users/anmol331/Desktop/scenario/network_parkingCost.xml.gz";
		double sampleSize = 0.05;
		String outputPlansFile = "C:/Users/anmol331/Desktop/scenario/plans_"+sampleSize+".xml.gz";
		new ConvertAndEnrichPlansFile().run(inputPlansFile, outputPlansFile, inputCsvFile, inputNetworkFile, sampleSize);
}
	public void run(String inputPlansFile, String outputPlansFile, String inputCSVFile, String inputNetworkFile, double sampleSize) {
		readVOTs(inputCSVFile);
		scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(inputNetworkFile);
		linksPerTaz = DavisMatsimUtils.getLinksPerTAZ(scenario.getNetwork());
		StreamingPopulationReader spr = new StreamingPopulationReader(scenario);
		StreamingPopulationWriter spw = new StreamingPopulationWriter(sampleSize);
		spw.writeStartPlans(outputPlansFile);
		spr.addAlgorithm(new PersonAlgorithm() {
			
			@Override
			public void run(Person person) {
				Double vot = vots.get(person.getId());
				if (vot!=null) {
					person.getAttributes().putAttribute("vot", vot);
				}
				else {
					Logger.getLogger(getClass()).error("VOT not found for person "+person.getId().toString());
				}
				Double lastActivityEndTime = null;
				for (PlanElement pe : person.getSelectedPlan().getPlanElements()) {
					if (pe instanceof Leg) {
						if (((Leg) pe).getMode().equals("taxi")) {
							((Leg) pe).setMode(TransportMode.car);
						}
					}
					else {
						Activity act = (Activity) pe;
						//TODO: shuffle coords
						act.setCoord(shuffleCoordsinTAZ(act.getCoord()));
						if (lastActivityEndTime!=null) {
							if (act.getEndTime()<lastActivityEndTime+1800) {
								act.setEndTime(lastActivityEndTime+1800);
							}
						
						}
						lastActivityEndTime = act.getEndTime();
					}
				}
			}

			
		});
		spr.addAlgorithm(spw);
		spr.readFile(inputPlansFile);
		spw.closeStreaming();
	}
	private void readVOTs(String inputCSVFile) {
		TabularFileParserConfig tfc = new TabularFileParserConfig();
		tfc.setFileName(inputCSVFile);
		tfc.setDelimiterTags(new String[] {","});
		new TabularFileParser().parse(tfc, new TabularFileHandler() {
			@Override
			public void startRow(String[] row) {
			try {
				Id<Person> personId = Id.createPersonId(row[1]);
				double vot = Double.parseDouble(row[6]);
				vots.put(personId, vot);
				
			}	catch (Exception e) {
				System.err.println("Obmitting row: "+row);
			}
			}
		});
	}
	private Coord shuffleCoordsinTAZ(Coord coord) {
		String taz = (String) NetworkUtils.getNearestLink(scenario.getNetwork(), coord).getAttributes().getAttribute("taz");
		Coord c = getRandomCoordInTaz(taz);
		if (c!=null) {
			return c;
		}
		else return coord;
	}
	
	private Coord getRandomCoordInTaz(String taz) {
		if (linksPerTaz.containsKey(taz)) {
		Collections.shuffle(linksPerTaz.get(taz));
		Link startLink = linksPerTaz.get(taz).get(0);
		return startLink.getCoord();}
		else return null;
	}
}
