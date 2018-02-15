package bartaccess.plans;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import utils.DavisMatsimUtils;

public class CreateCarPlansFile {

	
	private Map<String,List<Link>> linksPerTAZ;
	private Map<String,Coord> stations;
	private Scenario scenario;
	private CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("PROJCS[\"NAD_1983_StatePlane_California_VI_FIPS_0406_Feet\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"North_American_Datum_1983\",SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Lambert_Conformal_Conic_2SP\"],PARAMETER[\"False_Easting\",6561666.666666666],PARAMETER[\"False_Northing\",1640416.666666667],PARAMETER[\"Central_Meridian\",-116.25],PARAMETER[\"Standard_Parallel_1\",32.78333333333333],PARAMETER[\"Standard_Parallel_2\",33.88333333333333],PARAMETER[\"Latitude_Of_Origin\",32.16666666666666],UNIT[\"Foot_US\",0.30480060960121924],AUTHORITY[\"EPSG\",\"102646\"]]", "EPSG:32610");
	public static void main(String[] args) {
		
		new CreateCarPlansFile().run("C:/Users/anmol331/Desktop/scenario/network_parkingCost.xml.gz", "C:/Users/anmol331/Desktop/Scenario_3/Trips.csv","C:/Users/anmol331/Desktop/Scenario_3/car2bart_plans.xml", "C:/Users/anmol331/Desktop/Scenario_3/StationTAZ.csv");
	}
	
	public void run (String networkFile, String inputTriptable, String outputPlansFile, String bartLocationFile) {

		scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		readStationFile(bartLocationFile);
		linksPerTAZ = DavisMatsimUtils.getLinksPerTAZ(scenario.getNetwork());
		readCSVToPopulation(inputTriptable);
		new PopulationWriter(scenario.getPopulation()).write(outputPlansFile);

	}

	private void readStationFile(String bartLocationFile) {
		this.stations = new HashMap<>();
		TabularFileParserConfig tbc = new TabularFileParserConfig();
		tbc.setDelimiterTags(new String[] {","});
		tbc.setFileName(bartLocationFile);
		new TabularFileParser().parse(tbc, new TabularFileHandler() {
			boolean firstRow =  true;

			@Override
			public void startRow(String[] row) {
				if (firstRow) {
					firstRow = false;
				}else {
					String id = row[0];
					Coord coord = ct.transform(new Coord(Double.parseDouble(row[1]),Double.parseDouble(row[2])) );
					stations.put(id, coord);
				}
				
			}
		});
	}

	private void readCSVToPopulation(String inputTriptable) {
		TabularFileParserConfig tbc = new TabularFileParserConfig();
		tbc.setDelimiterTags(new String[] {","});
		tbc.setFileName(inputTriptable);
		PopulationFactory fac = scenario.getPopulation().getFactory(); 
		
		new TabularFileParser().parse(tbc, new TabularFileHandler() {
			boolean firstRow =  true;
			Random r = MatsimRandom.getRandom();
			@Override
			public void startRow(String[] rw) {
				if (firstRow) {
					firstRow = false;
				}else {
				String homeTaz = rw[9];
				String workTaz = rw[11];
	
				double departureTime = Double.parseDouble(rw[14])*3600+r.nextInt(3600);
				Id<Person> personId = Id.createPersonId(rw[1]+"_"+(int)departureTime);
				Coord homeCoord =  getRandomCoordInTaz(homeTaz);
				if (homeCoord == null) {
					Logger.getLogger(getClass()).warn("no link in TAZ "+homeTaz);
				} else {
					Coord destinationCoord = findClosestStop(homeCoord);
					
				Person p = fac.createPerson(personId);
				p.getAttributes().putAttribute("homeTAZ", homeTaz);
				p.getAttributes().putAttribute("workTAZ", workTaz);
						
				scenario.getPopulation().addPerson(p);
				Plan plan = fac.createPlan();
				p.addPlan(plan);
				Activity home = fac.createActivityFromCoord("home", homeCoord);
				home.setEndTime(departureTime);
				plan.addActivity(home);
				Leg leg = fac.createLeg("car");
				plan.addLeg(leg);
				plan.addActivity(fac.createActivityFromCoord("bart", destinationCoord));
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
	private Coord findClosestStop(Coord homeCoord) {
		double bestDistance = Double.MAX_VALUE;
		Coord bestCoord = null;
		for (Coord c : stations.values()) {
			double distance = CoordUtils.calcEuclideanDistance(homeCoord, c);
			if (distance<bestDistance) {
				bestDistance = distance;
				bestCoord = c;		
			}
		}
		return bestCoord;
	}
	
}
