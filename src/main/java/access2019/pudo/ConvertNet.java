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

package access2019.pudo;/*
 * created by jbischoff, 06.03.2019
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class ConvertNet {

    public static void main(String[] args) {
        final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile("D:/matsim_davis/scenario_2019/matsim_input/detailednet.xml.gz");
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("EPSG:3857","EPSG:32610");
        scenario.getNetwork().getNodes().values().forEach(n->n.setCoord(ct.transform(n.getCoord())));
        new NetworkWriter(scenario.getNetwork()).write("D:/matsim_davis/scenario_2019/matsim_input/detailednet_epsg.xml.gz");
    }
}
