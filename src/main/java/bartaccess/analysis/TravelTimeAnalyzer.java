package bartaccess.analysis;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;

public class TravelTimeAnalyzer implements ActivityEndEventHandler, ActivityStartEventHandler {

	
	private Map<Id<Person>,Double> departureTimes = new HashMap<>();
	private Map<Id<Person>,Double> travelTimes = new HashMap<>();
	
	@Override
	public void handleEvent(ActivityStartEvent event) {
	if (event.getActType().equals("bart")) {
		double departureTime = departureTimes.remove(event.getPersonId());
		double travelTime = event.getTime() - departureTime;
		this.travelTimes.put(event.getPersonId(), travelTime);
		
	}
		
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (event.getActType().equals("home")) {
			departureTimes.put(event.getPersonId(), event.getTime());
		}
		
	}
 
	public Map<Id<Person>, Double> getTravelTimes() {
		return travelTimes;
	}
	
}
