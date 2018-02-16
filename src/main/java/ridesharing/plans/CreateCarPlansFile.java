package ridesharing.plans;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import utils.DavisMatsimUtils;

public class CreateCarPlansFile {

	
	private Map<String,List<Link>> linksPerTAZ;
	private Scenario scenario;
	public static void main(String[] args) {
		
		new CreateCarPlansFile().run("C:/Users/Joschka/Desktop/davis/Scenario_2/network_parkingCost.xml.gz", "C:/Users/Joschka/Desktop/davis/Scenario_2/SharedTripDetails.csv","C:/Users/Joschka/Desktop/davis/Scenario_2/SharedTrip_plans.xml");
	}
	
	public void run (String networkFile, String inputTriptable, String outputPlansFile) {

		scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		
		linksPerTAZ = DavisMatsimUtils.getLinksPerTAZ(scenario.getNetwork());
		readCSVToPopulation(inputTriptable);
		new PopulationWriter(scenario.getPopulation()).write(outputPlansFile);

	}


	private void readCSVToPopulation(String inputTriptable) {
		TabularFileParserConfig tbc = new TabularFileParserConfig();
		tbc.setDelimiterTags(new String[] {","});
		tbc.setFileName(inputTriptable);
		PopulationFactory fac = scenario.getPopulation().getFactory(); 
		
		new TabularFileParser().parse(tbc, new TabularFileHandler() {
			boolean firstRow =  true;
			@Override
			public void startRow(String[] rw) {
				if (firstRow) {
					firstRow = false;
				}else {
				String homeTaz = rw[9];
				String workTaz = rw[11];
	
				double departureTime = Double.parseDouble(rw[14])*3600;
				Id<Person> personId = Id.createPersonId(rw[1]+"_"+rw[3]+"_"+rw[4]+"_"+rw[5]);
				Coord homeCoord =  getRandomCoordInTaz(homeTaz);
				Coord destinationCoord = getRandomCoordInTaz(workTaz);
				if ((homeCoord == null)||(destinationCoord == null)) {
					Logger.getLogger(getClass()).warn("no link in TAZ "+homeTaz);
				} else {
					
				Person p = fac.createPerson(personId);
				p.getAttributes().putAttribute("homeTAZ", homeTaz);
				p.getAttributes().putAttribute("workTAZ", workTaz);
						
				scenario.getPopulation().addPerson(p);
				Plan plan = fac.createPlan();
				p.addPlan(plan);
				Activity home = fac.createActivityFromCoord("dummy", homeCoord);
				home.setEndTime(departureTime);
				plan.addActivity(home);
				Leg leg = fac.createLeg("car");
				plan.addLeg(leg);
				plan.addActivity(fac.createActivityFromCoord("dummy", destinationCoord));
				}
				}
			}
			
		});
	}
	
	private Coord getRandomCoordInTaz(String taz) {
		if (linksPerTAZ.containsKey(taz)) {
		Collections.shuffle(linksPerTAZ.get(taz));
		Link startLink = linksPerTAZ.get(taz).get(0);
		return startLink.getCoord();}
		else return null;
	}
	
	
}
