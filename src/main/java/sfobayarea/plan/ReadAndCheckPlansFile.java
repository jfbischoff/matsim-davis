package sfobayarea.plan;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

public class ReadAndCheckPlansFile {
	public static void main(String[] args) {
		String plansFile = "C:/Users/anmol331/Desktop/ConversionScripts/plans_all.xml.gz";
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		new PopulationReader(scenario).readFile(plansFile);
	}
}
