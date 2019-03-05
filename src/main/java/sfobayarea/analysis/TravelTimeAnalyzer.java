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
 * created by jbischoff, 20.07.2018
 */

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class TravelTimeAnalyzer {

    public static void main(String[] args) {
        String folder = "D:/matsim_davis/Scenario_1/matsim_output/at_case_2xparking/";
        Map<String,Integer> modeDepartures = new HashMap<>();
        Map<String,Double> modeTravelTime = new HashMap<>();
        EventsManager events = EventsUtils.createEventsManager();

        events.addHandler(new PersonDepartureEventHandler() {
            @Override
            public void handleEvent(PersonDepartureEvent personDepartureEvent) {
                if (personDepartureEvent.getPersonId().toString().startsWith("pt")) return;
                if (personDepartureEvent.getPersonId().toString().startsWith("taxi")) return;

                int dep = modeDepartures.getOrDefault(personDepartureEvent.getLegMode(),0);
                dep++;
                modeDepartures.put(personDepartureEvent.getLegMode(),dep);
                double tt = modeTravelTime.getOrDefault(personDepartureEvent.getLegMode(),0.);
                tt-= personDepartureEvent.getTime();
                modeTravelTime.put(personDepartureEvent.getLegMode(),tt);
            }
        });
        events.addHandler(new PersonArrivalEventHandler() {
            @Override
            public void handleEvent(PersonArrivalEvent personArrivalEvent) {
                //if (personArrivalEvent.getTime() >= 23*3600) return;

                if (personArrivalEvent.getPersonId().toString().startsWith("pt")) return;
                if (personArrivalEvent.getPersonId().toString().startsWith("taxi")) return;
                double tt = modeTravelTime.getOrDefault(personArrivalEvent.getLegMode(),0.);
                tt+= personArrivalEvent.getTime();
                modeTravelTime.put(personArrivalEvent.getLegMode(),tt);

            }
        });

        new MatsimEventsReader(events).readFile(folder+"output_events.xml.gz");
        modeDepartures.forEach((k,v)->System.out.println(k+"\t"+v));
        modeTravelTime.forEach((k,v)->System.out.println(k+"\t"+v));
    }


}
