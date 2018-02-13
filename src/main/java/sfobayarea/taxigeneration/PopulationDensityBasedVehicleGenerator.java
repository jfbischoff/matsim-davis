package sfobayarea.taxigeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.VehicleImpl;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.contrib.util.random.WeightedRandomSelection;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import utils.DavisMatsimUtils;

public class PopulationDensityBasedVehicleGenerator {

	private Network network;
	private Map<String,List<Link>> linksPerTAZ = new HashMap<>();
	private Map<String,MutableInt> homesPerTAZ = new HashMap<>();
	
	public static void main(String[] args) {
		String networkFile = "C:\\Users\\anmol331\\Desktop\\scenario\\network_parkingCost.xml.gz";
		String inputPlansFile = "C:\\Users\\anmol331\\Desktop\\scenario\\plans_0.1.xml.gz";
		int fleetsize = 30000;
		String outputVehiclesFile = "C:\\Users\\anmol331\\Desktop\\scenario\\vehicles_"+fleetsize+".xml.gz";
		
		new PopulationDensityBasedVehicleGenerator().run(networkFile, inputPlansFile, outputVehiclesFile, fleetsize);
	}

	private void run(String networkFile, String inputPlansFile, String outputVehiclesFile, int fleetsize) {
		
		network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkFile);
		
		
		linksPerTAZ = DavisMatsimUtils.getLinksPerTAZ(network);
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile(inputPlansFile);
		for (Person p : scenario.getPopulation().getPersons().values()) {
			Plan plan = p.getSelectedPlan();
			Activity home = (Activity) plan.getPlanElements().get(0);
			Link homeLink = NetworkUtils.getNearestLink(network, home.getCoord());
			String taz = (String) homeLink.getAttributes().getAttribute("taz");
			if (homesPerTAZ.containsKey(taz)) {
				homesPerTAZ.get(taz).increment();
			}	else {
				homesPerTAZ.put(taz, new MutableInt(1));
			}	
		}
		WeightedRandomSelection<String> wrs = new WeightedRandomSelection<>();
		homesPerTAZ.entrySet().forEach(e->wrs.add(e.getKey(), e.getValue().doubleValue()));
		List<Vehicle> vehicles = new ArrayList<>();
		for (int i = 0; i<fleetsize; i++) {
			String taz = wrs.select();
			Collections.shuffle(linksPerTAZ.get(taz));
			Link startLink = linksPerTAZ.get(taz).get(0);
			Vehicle v = new VehicleImpl(Id.create(i,Vehicle.class), startLink, 1, 0, 30*3600); 
			vehicles.add(v);
		}
		new VehicleWriter(vehicles).write(outputVehiclesFile);
		

		
	}
}
