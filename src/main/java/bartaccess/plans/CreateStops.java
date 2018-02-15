package bartaccess.plans;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class CreateStops {

	public static void main(String[] args) {

		final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile("C:/Users/anmol331/Desktop/Scenario_3/network_parkingCost.xml.gz");
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("PROJCS[\"NAD_1983_StatePlane_California_VI_FIPS_0406_Feet\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"North_American_Datum_1983\",SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Lambert_Conformal_Conic_2SP\"],PARAMETER[\"False_Easting\",6561666.666666666],PARAMETER[\"False_Northing\",1640416.666666667],PARAMETER[\"Central_Meridian\",-116.25],PARAMETER[\"Standard_Parallel_1\",32.78333333333333],PARAMETER[\"Standard_Parallel_2\",33.88333333333333],PARAMETER[\"Latitude_Of_Origin\",32.16666666666666],UNIT[\"Foot_US\",0.30480060960121924],AUTHORITY[\"EPSG\",\"102646\"]]", "EPSG:32610");
		TabularFileParserConfig tbc = new TabularFileParserConfig();
		tbc.setDelimiterTags(new String[] {","});
		tbc.setFileName("C:/Users/anmol331/Desktop/Scenario_3/Mp_Real_1.csv");
		new TabularFileParser().parse(tbc, new TabularFileHandler() {
			
			@Override
			public void startRow(String[] row) {
				Id<TransitStopFacility> stopId = Id.create(row[0], TransitStopFacility.class);
				Coord coord = ct.transform(new Coord(Double.parseDouble(row[1]),Double.parseDouble(row[2])));
				TransitStopFacility f = scenario.getTransitSchedule().getFactory().createTransitStopFacility(stopId, coord, false);
				f.setLinkId(NetworkUtils.getNearestLink(scenario.getNetwork(),coord).getId());
				if (!scenario.getTransitSchedule().getFacilities().containsKey(stopId)) {
					scenario.getTransitSchedule().addStopFacility(f);}
			}
		});
		
		TabularFileParserConfig tbc2 = new TabularFileParserConfig();
		tbc2.setDelimiterTags(new String[] {"\t"});
		tbc2.setFileName("C:/Users/anmol331/Desktop/Scenario_3/stations.txt");
		new TabularFileParser().parse(tbc2, new TabularFileHandler() {
			
			@Override
			public void startRow(String[] row) {
				Id<TransitStopFacility> stopId = Id.create("bart_"+row[0], TransitStopFacility.class);
				Coord coord = ct.transform(new Coord(Double.parseDouble(row[1]),Double.parseDouble(row[2])));
				TransitStopFacility f = scenario.getTransitSchedule().getFactory().createTransitStopFacility(stopId, coord, false);
				f.setLinkId(NetworkUtils.getNearestLink(scenario.getNetwork(),coord).getId());
				if (!scenario.getTransitSchedule().getFacilities().containsKey(stopId)) {
					scenario.getTransitSchedule().addStopFacility(f);}
			}
		});
		
		
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile("C:/Users/anmol331/Desktop/Scenario_3/drt_stops.xml");
	}

}
