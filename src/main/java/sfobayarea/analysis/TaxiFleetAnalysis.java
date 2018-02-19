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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.taxi.vrpagent.TaxiActionCreator;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;


public class TaxiFleetAnalysis implements LinkEnterEventHandler, ActivityEndEventHandler {

	//true : vehicle is occupied, false : vehicle is empty
	private Map<Id<Vehicle>,Boolean> vehicleOccupancyStatus = new HashMap<>();
	private Map<Id<Vehicle>,MutableDouble> currentPickupDistance = new HashMap<>();
	private Network network;
	private Map<String,Tuple<MutableDouble,MutableDouble>> tazEmptyAndOccupiedDistance = new HashMap<>();
	private Map<String,Tuple<MutableInt,MutableDouble>> tripsAndPickupDistancePerTaz = new HashMap<>();
	private String del = ",";
	private String vmt = "VKMT";
	private String HEADER;
	private double conv = 1000.0;
	
	public TaxiFleetAnalysis(Network network, boolean useMiles, String delimiter) {
		Locale.setDefault(new Locale("en", "US"));

		this.network = network;
		del = delimiter;
		if (useMiles) {
			vmt = "VMT";
			conv = 1609.34;
		}
		HEADER = "taz"+del+"tripsStarting"+del+"meanPickupTrip"+vmt+del+"totalEmptyTaxi"+vmt+del+"totalOccupied"+vmt+del+"emptyVehRatio";
	}
	
	
	@Override
	public void handleEvent(ActivityEndEvent event) {
		if ((event.getActType().equals(VrpAgentLogic.BEFORE_SCHEDULE_ACTIVITY_TYPE))||(event.getActType().equals(TaxiActionCreator.DROPOFF_ACTIVITY_TYPE))){
			this.vehicleOccupancyStatus.put(Id.createVehicleId(event.getPersonId()), false);
			this.currentPickupDistance.put(Id.createVehicleId(event.getPersonId()), new MutableDouble());
		}	
			else if (event.getActType().equals(TaxiActionCreator.PICKUP_ACTIVITY_TYPE)) {
			this.vehicleOccupancyStatus.put(Id.createVehicleId(event.getPersonId()), true);
			String taz = (String) network.getLinks().get(event.getLinkId()).getAttributes().getAttribute("taz");
			if (!tripsAndPickupDistancePerTaz.containsKey(taz)) {
				tripsAndPickupDistancePerTaz.put(taz, new Tuple<MutableInt,MutableDouble>(new MutableInt(), new MutableDouble()));
			}
			this.tripsAndPickupDistancePerTaz.get(taz).getFirst().increment();
			this.tripsAndPickupDistancePerTaz.get(taz).getSecond().add(currentPickupDistance.get(Id.createVehicleId(event.getPersonId())).doubleValue());;
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (vehicleOccupancyStatus.containsKey(event.getVehicleId())) {
			Link l = network.getLinks().get(event.getLinkId());
			String taz = (String) l.getAttributes().getAttribute("taz");
			
			if (!tazEmptyAndOccupiedDistance.containsKey(taz)) {
				tazEmptyAndOccupiedDistance.put(taz, new Tuple<MutableDouble,MutableDouble>(new MutableDouble(), new MutableDouble()));
			}
			
			if (vehicleOccupancyStatus.get(event.getVehicleId())) {
				//taxi is with passenger
				tazEmptyAndOccupiedDistance.get(taz).getSecond().add(l.getLength());
			} else {
				//taxi is empty
				tazEmptyAndOccupiedDistance.get(taz).getFirst().add(l.getLength());
				currentPickupDistance.get(event.getVehicleId()).add(l.getLength());
			}
		}

	}
	public void writeStatistics(String outputFile) {
		BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
	
		try {
			bw.write(HEADER);
			for (Entry<String, Tuple<MutableDouble, MutableDouble>> e : tazEmptyAndOccupiedDistance.entrySet()) {
				double tripsStarting = 0;
				double pickupDistance = 0; 
				double meanPickupDistance = 0;
				if (tripsAndPickupDistancePerTaz.containsKey(e.getKey())) {
				tripsStarting = tripsAndPickupDistancePerTaz.get(e.getKey()).getFirst().doubleValue();
				pickupDistance = tripsAndPickupDistancePerTaz.get(e.getKey()).getSecond().doubleValue();
				meanPickupDistance = pickupDistance/tripsStarting;
				}
				double emptyMileage = e.getValue().getFirst().doubleValue()/conv;
				double occMileage = e.getValue().getSecond().doubleValue()/conv;
				bw.newLine();
				bw.write(e.getKey()+del);
				bw.write(String.format("%.0f", tripsStarting)+del);
				bw.write(String.format("%.2f", meanPickupDistance/conv)+del);
				bw.write(String.format("%.2f", emptyMileage)+del);
				bw.write(String.format("%.2f", occMileage)+del);
				bw.write(String.format("%.2f", emptyMileage/(emptyMileage+occMileage)));
				
			}
			
			bw.flush();
			bw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
