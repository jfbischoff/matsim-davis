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

package sfobayarea.plan;/*
 * created by jbischoff, 24.05.2018
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.utils.CreatePseudoNetwork;
import org.matsim.pt.utils.CreateVehiclesForSchedule;
import org.matsim.vehicles.VehicleWriterV1;

public class BuildScheduleNetwork {
    public static void main(String[] args) {
        String networkFile = "D:\\matsim_davis\\Scenario_1\\matsim_input\\network_parkingCost_pt.xml.gz";
        String scheduleFile = "D:\\matsim_davis\\Scenario_1\\matsim_input\\transitSchedule.xml.gz";
        String transitVehiclesFile = "D:\\matsim_davis\\Scenario_1\\matsim_input\\transitVehicles.xml.gz";
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new TransitScheduleReader(scenario).readFile("D:\\matsim_davis\\Scenario_1\\transit\\mergedSchedule.xml");

        //if neccessary, parse in an existing network file here:

		new MatsimNetworkReader(scenario.getNetwork()).readFile("D:\\matsim_davis\\Scenario_1\\matsim_input\\network_parkingCost.xml.gz");

        //Create a network around the schedule
        new CreatePseudoNetwork(scenario.getTransitSchedule(),scenario.getNetwork(),"pt_").createNetwork();

        //Create simple transit vehicles
        new CreateVehiclesForSchedule(scenario.getTransitSchedule(), scenario.getTransitVehicles()).run();

        //Write out network, vehicles and schedule
        new NetworkWriter(scenario.getNetwork()).write(networkFile);
        new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(scheduleFile);
        new VehicleWriterV1(scenario.getTransitVehicles()).writeFile(transitVehiclesFile);
    }
}
