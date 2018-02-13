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
  
package sfobayarea.scoring;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;

public class AddParkingCharges implements PersonArrivalEventHandler, PersonDepartureEventHandler {

	private final Network network;
	private final EventsManager events;
	private Map<Id<Person>,Double> lastCarArrival = new HashMap<>();
	private Scenario scenario;
	@Inject
	public AddParkingCharges(Scenario scenario, EventsManager events) {
		events.addHandler(this);
		this.network=scenario.getNetwork();
		this.events = events;
		this.scenario = scenario;
	}
	
	@Override 
		public void reset(int iteration) {
		lastCarArrival.clear();
	}
	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (lastCarArrival.containsKey(event.getPersonId())) {
			double arrivalTime = lastCarArrival.remove(event.getPersonId());
			double parkTime = (event.getTime()-arrivalTime)/3600;
			Double cost = (double) network.getLinks().get(event.getLinkId()).getAttributes().getAttribute("parkCost") / 100.;
			double charge = cost*parkTime;
			if (charge>0) {	
			//TODO: add parking time cost (15 minutes * vot)	
			Double vot = (Double) scenario.getPopulation().getPersons().get(event.getPersonId()).getAttributes().getAttribute("vot");
			double personCost = 0.;
			if (vot!=null) {
				personCost = vot* .25; // 15 Minutes for parking search and so on
			}
			charge = charge + personCost;
			events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(),-charge));
			}
			}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getLegMode().equals(TransportMode.car)) {
			if (scenario.getPopulation().getPersons().containsKey(event.getPersonId())) {
				// avoid to charge taxi driver agents or bus drivers a parking charge
			this.lastCarArrival.put(event.getPersonId(),event.getTime());
			}
		}
	}

}
