package sfobayarea.plan;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

public class ReadAndCheckNetworkFile {
	public static void main(String[] args) {
		String networkFile = "C:/Users/Joschka/Desktop/davis/scenario/network.xml";
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		new NetworkCleaner().run(scenario.getNetwork());
		new NetworkWriter(scenario.getNetwork()).write("C:/Users/Joschka/Desktop/davis/scenario/network_cleaned.xml");
	}
}
