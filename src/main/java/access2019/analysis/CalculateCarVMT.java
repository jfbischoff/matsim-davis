package access2019.analysis;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

public class CalculateCarVMT {

    public static void main(String[] args) {

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("C:\\Users\\u229187\\Desktop\\output\\cb04\\output_plans.xml.gz");
        double vehicleMeters = scenario.getPopulation().getPersons().values().stream()
                .map(person -> person.getSelectedPlan())
                .flatMap(plan -> plan.getPlanElements().stream())
                .filter(Leg.class::isInstance)
                .filter(l -> ((Leg) l).getMode().equals(TransportMode.car))
                .mapToDouble(l -> {
                    Leg leg = (Leg) l;
                    double dist = leg.getRoute() != null ? leg.getRoute().getDistance() : 0;
                    if (Double.isNaN(dist)) {
                        dist = 0.0;
                    }
                    return dist;
                })
                .sum();

        System.out.println("Vehicle meters traveled: " + vehicleMeters);
        System.out.println("Vehicle miles traveled: " + vehicleMeters / 1609.34);


    }


}
