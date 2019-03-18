/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package access2019;/*
 * created by jbischoff, 06.03.2019
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import java.util.HashMap;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class AddPersonAttributes {

    private HashMap<String,Double> vots = new HashMap<>();

    public static void main(String[] args) {
        new AddPersonAttributes().run();
    }

    private void run() {
        readVOTs("D:/matsim_davis/Scenario_3/tmz_input/personData_6.csv");
        Config config = createConfig();
        Scenario scenario = createScenario(config);
        new PopulationReader(scenario).readFile("D:/matsim_davis/scenario_2019/matsim_input/initial_plans.xml");
        scenario.getPopulation().getPersons().values().forEach(p-> {
            String shortPID = p.getId().toString().split("_")[0];
            Double vot = vots.get(shortPID);
            if (vot!=null){
                p.getAttributes().putAttribute("vot",vot);
            } else {
                System.out.println("no vot found for agent "+p.getId());
            }
        });
        new PopulationWriter(scenario.getPopulation()).write("D:/matsim_davis/scenario_2019/matsim_input/initial_plans.xml");
    }

    private void readVOTs(String personDataFile) {
        TabularFileParserConfig tfc = new TabularFileParserConfig();
        tfc.setDelimiterTags(new String[] {","});
        tfc.setFileName(personDataFile);
        new TabularFileParser().parse(tfc, new TabularFileHandler() {
            boolean firstrow = true;
            @Override
            public void startRow(String[] row) {
                if (firstrow) {
                    firstrow = false;
                }else {
                    double vot = Double.parseDouble(row[6]);
                    vots.put(row[1],vot);
                }
            }
        });

    }
}
