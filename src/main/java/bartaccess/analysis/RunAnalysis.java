package bartaccess.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.core.utils.misc.Time;

public class RunAnalysis {

	private final Map<Id<Person>,List<String>>  resultsString = new TreeMap<>();
	private Scenario scenario ;
	private Map<String,Double> vots = new HashMap<>();
	
	private static String DEL = ";";
	private static String HEADER = "personId"+DEL+"personId_TMC"+DEL+"homeX"+DEL+"homeY"+DEL+"VOT"
			+DEL+"travelDistance_car_m"+DEL+"travelTime_car" +DEL+ "arrivalTime_car"
			+DEL+"travelTime_d2d_inclWait"+DEL+"waitTime_d2d"+DEL+ "arrivalTime_d2d"
			+DEL+"travelTime_stop_inclWait"+DEL+"waitTime_stop"+DEL+ "arrivalTime_stop"
			+DEL+"travelTime_unpooled"+DEL+"waitTime_unpooled"+DEL+ "arrivalTime_unpooled";
	
	public static void main(String[] args) {
		new RunAnalysis().run();
		
		
	}

	private void run() {
		
		Locale.setDefault(new Locale("en", "US"));

		String in = "D:/matsim_davis/Scenario_3/";
		scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		readVOTs(in+"tmz_input/personData_6.csv");
		readCarPopulation(in+"matsim_output/car2bart/output_plans.xml.gz");
		
		EventsManager carevents = EventsUtils.createEventsManager();
		TravelTimeAnalyzer carTT = new TravelTimeAnalyzer();
		carevents.addHandler(carTT);
		new MatsimEventsReader(carevents).readFile(in+"matsim_output/car2bart/output_events.xml.gz");
		
		EventsManager door2doorevents = EventsUtils.createEventsManager();
		TravelTimeAnalyzer door2doorTT = new TravelTimeAnalyzer();
		door2doorevents.addHandler(door2doorTT);
		new MatsimEventsReader(door2doorevents).readFile(in+"matsim_output/all_drt/output_events.xml.gz");
		
		EventsManager stopevents = EventsUtils.createEventsManager();
		TravelTimeAnalyzer stopTT = new TravelTimeAnalyzer();
		stopevents.addHandler(stopTT);
		new MatsimEventsReader(stopevents).readFile(in+"matsim_output/all_drt_withStops/output_events.xml.gz");
		
		EventsManager nopool = EventsUtils.createEventsManager();
		TravelTimeAnalyzer nopoolTT = new TravelTimeAnalyzer();
		nopool.addHandler(nopoolTT);
		new MatsimEventsReader(nopool).readFile(in+"matsim_output/all_drt_1pax/output_events.xml.gz");
		
		
		
		addTimesToOutput(carTT.getTravelTimes());
		addTimesToOutput(carTT.getArrivalTimes());

		addTimesToOutput(door2doorTT.getTravelTimes());
		readWaitTimes(in+"matsim_output/all_drt/ITERS/it.2/2.drt_trips.csv");
		addTimesToOutput(door2doorTT.getArrivalTimes());

		addTimesToOutput(stopTT.getTravelTimes());
		readWaitTimes(in+"matsim_output/all_drt_withStops/ITERS/it.2/2.drt_trips.csv");
		addTimesToOutput(stopTT.getArrivalTimes());

		addTimesToOutput(nopoolTT.getTravelTimes());
		readWaitTimes(in+"matsim_output/all_drt_1pax/ITERS/it.2/2.drt_trips.csv");
		addTimesToOutput(nopoolTT.getArrivalTimes());

		
		writeOutput(in+"matsim_output/scenarioAnalysisWithArrivalTimes.csv");
	}

	private void writeOutput(String outputFile) {
		BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
		
		try {
			bw.write(HEADER);
			for (List<String> r : resultsString.values()) {
				if (r.size() == 17) {
					bw.newLine();
				for (String s : r) {
					bw.write(s+DEL);
				}
				}
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void readWaitTimes(String drtStatsfile) {
		Map<Id<Person>,Double> waitTimes = new HashMap<>();
		TabularFileParserConfig tfc = new TabularFileParserConfig();
		tfc.setDelimiterTags(new String[] {";",","});
		tfc.setFileName(drtStatsfile);
		new TabularFileParser().parse(tfc, new TabularFileHandler() {
			boolean firstrow = true;
			@Override
			public void startRow(String[] row) {
				if (firstrow) {
					firstrow = false;
				}else {
				Id<Person> pid = Id.createPersonId(row[1]);	
				double waitTime = Double.parseDouble(row[9]);
				waitTimes.put(pid, waitTime);
				}
			}
		});
		for (Entry<Id<Person>, List<String>> e : resultsString.entrySet()) {
			if (waitTimes.containsKey(e.getKey())) {
				e.getValue().add(Time.writeTime(waitTimes.get(e.getKey())));
			} else {
				e.getValue().add(Time.writeTime(0));
				
			}
		}
	}

	private void addTimesToOutput(Map<Id<Person>, Double> travelTimes) {
		travelTimes.forEach((k,v)->resultsString.get(k).add(Time.writeTime(v)));
		}



	private void readVOTs(String personDataFile) {
		TabularFileParserConfig tfc = new TabularFileParserConfig();
		tfc.setDelimiterTags(new String[] {","});
		tfc.setFileName(personDataFile);
		new TabularFileParser().parse(tfc, new TabularFileHandler() {
			boolean firstrow = true;
			@Override
			public void startRow(String[] row) {
				if (firstrow) {
					firstrow = false;
				}else {
				double vot = Double.parseDouble(row[6]);
				vots.put(row[1],vot);
				}
			}
		});
		
	}

	private void readCarPopulation(String carOutputPlans) {
		new PopulationReader(scenario).readFile(carOutputPlans);
		for (Person p : scenario.getPopulation().getPersons().values()) {
			resultsString.put(p.getId(), new ArrayList<>());
			resultsString.get(p.getId()).add(p.getId().toString());
			String tmcid = getTMCPersonId(p.getId());
			resultsString.get(p.getId()).add(tmcid);

			Activity act0 =  (Activity) p.getSelectedPlan().getPlanElements().get(0);
			resultsString.get(p.getId()).add(String.format("%.5f", act0.getCoord().getX()));
			resultsString.get(p.getId()).add(String.format("%.5f", act0.getCoord().getY()));
			
			Leg carLeg = (Leg) p.getSelectedPlan().getPlanElements().get(1);
			Double vot = vots.get(tmcid);
			resultsString.get(p.getId()).add(String.format("%.3f", vot));
			double distance = carLeg.getRoute().getDistance();
			resultsString.get(p.getId()).add(String.format("%.3f", distance));
			
			
		}
		vots.clear();
	}
	
	private String getTMCPersonId(Id<Person> matsimPersonId) {
		return matsimPersonId.toString().split("_")[0];
	}
}
