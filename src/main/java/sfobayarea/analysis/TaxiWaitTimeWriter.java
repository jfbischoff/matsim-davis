package sfobayarea.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.io.IOUtils;

public class TaxiWaitTimeWriter implements PersonDepartureEventHandler, PersonEntersVehicleEventHandler {

	private Map<Id<Person>,Double> departureTimes = new HashMap<>(); 
	private Map<Id<Person>,Id<Link>> departureLink = new HashMap<>(); 
	private List<String> waits = new ArrayList<>();
	private final Network network;
	private static final String DEL = ",";
	private static final String HEADER = "personId"+DEL+"departureTime"+DEL+"waitTime"+DEL+"linkId"+DEL+"coordX"+DEL+"coordY";
	
	@Inject
	public TaxiWaitTimeWriter(Network network) {
		this.network = network;
	}
	
	
	@Override
	public void reset(int iteration) {
		departureTimes.clear();
		departureLink.clear();
	}
	
	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		Id<Person> pid = event.getPersonId();
		if (departureTimes.containsKey(pid)) {
			Id<Link> linkId = departureLink.remove(pid);
			double departureTime = departureTimes.remove(pid);
			double waitTime = event.getTime() - departureTime;
			Coord linkCoord =  network.getLinks().get(linkId).getCoord();
			String s = pid+DEL+departureTime+DEL+waitTime+DEL+linkId+DEL+linkCoord.getX()+DEL+linkCoord.getY();
			this.waits.add(s);
		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals("taxi")) {
			this.departureTimes.put(event.getPersonId(), event.getTime());
			this.departureLink.put(event.getPersonId(), event.getLinkId());
			}
		
	}
	
	public List<String> getWaits() {
		return waits;
	}
	
	public void writeWaittimes(String filename) {
		final BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		
		try {
			bw.write(HEADER);
			for (String s : waits) {
			bw.write("\n"+s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
