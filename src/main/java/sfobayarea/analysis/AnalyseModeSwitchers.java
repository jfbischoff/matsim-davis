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

package sfobayarea.analysis;/*
 * created by jbischoff, 11.06.2018
 */

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.router.TransitActsRemover;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class AnalyseModeSwitchers {

    String baseCasePlansFile = "D:/matsim_davis/Scenario_1/matsim_output/basecase_osm_pt/output_plans.xml.gz";
    String networkFile = "D:/matsim_davis/Scenario_1/matsim_output/atcase_pt_36cpm/output_network.xml.gz";
    String policyCasePlansFile = "D:/matsim_davis/Scenario_1/matsim_output/at_case/output_plans.xml.gz";
    String outputTable = "D:/matsim_davis/Scenario_1/matsim_output/at_case/modeUse.csv";
    String outputTaxiShare = "D:/matsim_davis/Scenario_1/matsim_output/at_case/outputTaxishare.csv";
    private Network network;

    public static void main(String[] args) {

        new AnalyseModeSwitchers().run();

    }

    private void run() {
            Config config = createConfig();
            Scenario scenario = createScenario(config);
            network = scenario.getNetwork();
            new MatsimNetworkReader(network).readFile(networkFile);
            Map<Id<Person>,ModeUse> modeUsers = new HashMap<>();
            Map<String,TazDepartures> tazDeparturesMap = new HashMap<>();
            StreamingPopulationReader streamingPopulationReader = new StreamingPopulationReader(scenario);
            streamingPopulationReader.addAlgorithm(new PersonAlgorithm() {
                @Override
                public void run(Person person) {
                    new TransitActsRemover().run(person.getSelectedPlan(),true);
                    ModeUse modeUse = new ModeUse();
                    modeUse.personId = person.getId();
                    Leg leg1 = (Leg) person.getSelectedPlan().getPlanElements().get(1);

                    Activity act0 = (Activity) person.getSelectedPlan().getPlanElements().get(0);
                    modeUse.originalMode=leg1.getMode();
                    modeUse.homeCoord = act0.getCoord();
                    modeUse.homeTAZ = (String) network.getLinks().get(act0.getLinkId()).getAttributes().getAttribute("taz");
                    modeUsers.put(person.getId(),modeUse);
                    modeUse.vot = (double) person.getAttributes().getAttribute("vot");
                    TazDepartures tazDepartures = tazDeparturesMap.getOrDefault(modeUse.homeTAZ,new TazDepartures(modeUse.homeTAZ));
                    if (leg1.getMode().equals(TransportMode.taxi)){
                        tazDepartures.taxiTrips_basecase++;
                    } else {
                        tazDepartures.otherTrips_basecase++;
                    }
                    tazDeparturesMap.put(tazDepartures.taz,tazDepartures);

                }
            });
            streamingPopulationReader.readFile(baseCasePlansFile);

            StreamingPopulationReader streamingPopulationReader1 = new StreamingPopulationReader(scenario);
            streamingPopulationReader1.addAlgorithm(new PersonAlgorithm() {
                @Override
                public void run(Person person) {
                    ModeUse modeUse = modeUsers.get(person.getId());
                    Leg leg1 = (Leg) person.getSelectedPlan().getPlanElements().get(1);
                    modeUse.policyMode = leg1.getMode();
                    TazDepartures tazDepartures = tazDeparturesMap.getOrDefault(modeUse.homeTAZ,new TazDepartures(modeUse.homeTAZ));
                    if (leg1.getMode().equals(TransportMode.taxi)){
                        tazDepartures.taxiTrips_pc++;
                    } else {
                        tazDepartures.otherTrips_pc++;
                    }
                    tazDeparturesMap.put(tazDepartures.taz,tazDepartures);

                }
            });
            streamingPopulationReader1.readFile(policyCasePlansFile);

        BufferedWriter bw = IOUtils.getBufferedWriter(outputTable);
        try {
            bw.write("personId;originalMode;policyMode;homeCoordX;homeCordY;homeTAZ;vot");
            for (ModeUse modeUse : modeUsers.values()){
                bw.newLine();
                bw.write(modeUse.toString());
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter bw2 = IOUtils.getBufferedWriter(outputTaxiShare);
        try {
            bw2.write("taz;otherTripsBC;taxiTripsBC;otherTripShareBC;taxiTripsShareBC;otherTripsPC;taxiTripsPC;otherTripSharePC;otherTripSharePC");
            for (TazDepartures tazDepartures: tazDeparturesMap.values()){
                double taxiShareBc = tazDepartures.taxiTrips_basecase/(tazDepartures.taxiTrips_basecase+tazDepartures.otherTrips_basecase);
                double otherShareBc = 1-taxiShareBc;
                double taxiSharePc = tazDepartures.taxiTrips_pc/(tazDepartures.taxiTrips_pc+tazDepartures.otherTrips_pc);
                double otherSharePc = 1-taxiSharePc;
                bw2.newLine();
                bw2.write(tazDepartures.taz+";"+tazDepartures.taxiTrips_basecase+";"+tazDepartures.otherTrips_basecase+";"+taxiShareBc+";"+otherShareBc+";"+
                tazDepartures.taxiTrips_pc+";"+tazDepartures.otherTrips_pc+";"+taxiSharePc+";"+otherSharePc);
            }
            bw2.flush();
            bw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ModeUse{

        Id<Person> personId;
        String originalMode;
        String policyMode;
        Coord homeCoord;
        double vot;
        String homeTAZ;

        @Override
        public String toString() {
            return        personId +
                    ";" + originalMode +
                    ";" + policyMode +
                    ";" + homeCoord.getX() +
                    ";" + homeCoord.getY()+
                    ";" + homeTAZ+
                    ";" + vot;
        }
    }

    class TazDepartures{
        String taz;
        double taxiTrips_basecase =0 ;
        double otherTrips_basecase = 0;
        double taxiTrips_pc = 0;
        double otherTrips_pc = 0;
        TazDepartures(String taz){
            this.taz = taz;
        }
    }
}
