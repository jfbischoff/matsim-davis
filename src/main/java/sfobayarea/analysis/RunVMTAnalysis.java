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

import java.io.BufferedWriter;
import java.io.IOException;

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.io.IOUtils;

public class RunVMTAnalysis {
public static void main(String[] args) throws IOException {
	
	String folder = "D:/matsim_davis/Scenario_1/matsim_output/atcase_pt_36cpm/";
	
	Network network = NetworkUtils.createNetwork();
	new MatsimNetworkReader(network).readFile(folder+"output_network.xml.gz");
	
	EventsManager events = EventsUtils.createEventsManager();
	
	VmtAccumulator vmtAccumulator = new VmtAccumulator(network);
	events.addHandler(vmtAccumulator);
	new MatsimEventsReader(events).readFile(folder+"output_events.xml.gz");
	BufferedWriter bw = IOUtils.getBufferedWriter(folder+"output_vmt.txt");
	bw.write("Stats for Scenario in Folder +"+folder);
	bw.newLine();
	bw.write("CAR distances:\t"+vmtAccumulator.getCarDistance()+" m \t"+vmtAccumulator.getCarDistance()/1609.34 + " miles ");
	bw.newLine();
	bw.write("TAXI distances:\t"+vmtAccumulator.getTaxiDistance()+" m \t"+vmtAccumulator.getTaxiDistance()/1609.34 + " miles ");
	
	bw.flush();
	bw.close();
	
}



}

class VmtAccumulator implements LinkEnterEventHandler{

	private Network network;
	
	double taxiDistance = 0;
	double carDistance = 0;
	
	public VmtAccumulator(Network network) {
		this.network = network;
	}


	@Override
	public void handleEvent(LinkEnterEvent event) {
		double distance = network.getLinks().get(event.getLinkId()).getLength();
		if (event.getVehicleId().toString().startsWith("taxi")) {
			taxiDistance += distance;
		} else {
			carDistance += distance;
		}
		
	}
	public double getCarDistance() {
		return carDistance;
	}
	
	public double getTaxiDistance() {
		return taxiDistance;
	}
	}
