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

package access2019.run;/*
 * created by jbischoff, 07.03.2019
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class TollEventHandler implements LinkEnterEventHandler {
    Map<Id<Link>,Double> tolls = new HashMap<>();

   private final EventsManager eventsManager;

    @Inject
    Population population;


    @Inject
    TollEventHandler(EventsManager eventsManager){
        this.eventsManager = eventsManager;
        this.eventsManager.addHandler(this);
        tolls.put(Id.createLinkId("12181069_0"), 6.0);
        tolls.put(Id.createLinkId("157618391_0"), 6.0);
        tolls.put(Id.createLinkId("537838948_0"), 6.0);
        tolls.put(Id.createLinkId("156047241_0"), 6.0);
        tolls.put(Id.createLinkId("26972072_0"), 6.0);
    }


    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        if(tolls.containsKey(linkEnterEvent.getLinkId())) {
            Id<Person> driverId = Id.create(linkEnterEvent.getVehicleId(), Person.class);
            if (population.getPersons().containsKey(driverId)) {
            eventsManager.processEvent(new PersonMoneyEvent(linkEnterEvent.getTime(),driverId,-tolls.get(linkEnterEvent.getLinkId())));
            }
        }
    }
}
