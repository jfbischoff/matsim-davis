package bartaccess.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunCar2BartScenario {
	
	public static void main(String[] args) {

	Config config = ConfigUtils.loadConfig("C:/Users/anmol331/Desktop/Scenario_3/configCar2Bart.xml") ;
	
	
	Scenario scenario = ScenarioUtils.loadScenario(config) ;

	Controler controler = new Controler( scenario ) ;

	controler.run();

}
}
