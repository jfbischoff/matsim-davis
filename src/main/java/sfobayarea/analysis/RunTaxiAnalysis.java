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
  
package sfobayarea.analysis;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class RunTaxiAnalysis {

	public static void main(String[] args) {
		String folder = "D:\\matsim\\atcase_20it/";
		
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(folder+"/output_network.xml.gz");
		EventsManager events = EventsUtils.createEventsManager();
		TaxiFleetAnalysis tfa = new TaxiFleetAnalysis(network, false,";");
		TaxiWaitTimeWriter tww = new TaxiWaitTimeWriter(network,";");
		events.addHandler(tww);
		events.addHandler(tfa);
		new MatsimEventsReader(events).readFile(folder+"/output_events.xml.gz");
		tfa.writeStatistics(folder+"/tazTaxiFleetStatistics.csv");
		tww.writeWaittimes(folder+"/tazTaxiWaitStatistics.csv");
	}

}
