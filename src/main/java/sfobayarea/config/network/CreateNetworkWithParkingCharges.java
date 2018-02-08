/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
  
package sfobayarea.config.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class CreateNetworkWithParkingCharges {
	
	private Collection<SimpleFeature> features;
	private Map<String,Double> tazParkCost = new HashMap<>();
	CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("EPSG:32610", TransformationFactory.WGS84);
public static void main(String[] args) {
	String networkFile = "C:/Users/Joschka/Desktop/davis/scenario/network_cleaned.xml";
	String tazFile = "C:/Users/Joschka/Desktop/davis/taz/Communities_of_Concern_TAZ.shp";
	String outputNetworkFile = "C:/Users/Joschka/Desktop/davis/scenario/network_parkingCost.xml.gz";
	String parkingChargeFile = "C:/Users/Joschka/Desktop/davis/Original Data/tazData.csv";
	new CreateNetworkWithParkingCharges().run(networkFile, tazFile, outputNetworkFile, parkingChargeFile);
}

public void run (String networkFile , String tazFile, String outputNetworkFile ,String parkingChargeFile ) {
		
		ShapeFileReader sfr = new ShapeFileReader();
		
		features = sfr.readFileAndInitialize(tazFile);

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		
		TabularFileParserConfig tfc = new TabularFileParserConfig();
		tfc.setDelimiterTags(new String[] {","});
		tfc.setFileName(parkingChargeFile);
		new TabularFileParser().parse(tfc, new TabularFileHandler() {
			@Override
			public void startRow(String[] row) {
				try {
					String taz = row[0];
					Double parkCost = Double.parseDouble(row[30]);
					if (parkCost>0)Logger.getLogger(getClass()).info(taz + " : "+parkCost);
					tazParkCost.put(taz, parkCost);
				}
				catch (Exception e) {
					System.err.println(row[0] + " : "+ row[30]);
					e.printStackTrace();
				}
			}
		});
		scenario.getNetwork().getLinks().values().forEach(l->setLinkTAZAndParkCost(l));
		new NetworkWriter(scenario.getNetwork()).write(outputNetworkFile);
		
}

private void setLinkTAZAndParkCost(Link l) {
	Point p = MGC.coord2Point(ct.transform(l.getCoord()));
	String taz = "unknown";
	for (SimpleFeature f : this.features) {
		Geometry g = (Geometry) f.getDefaultGeometry();
		if (g.contains(p)) {
			taz = Long.toString((long) f.getAttribute("taz_key"));
			break;
		}
	}
	Double parkCost = tazParkCost.get(taz);
	l.getAttributes().putAttribute("taz", taz);
	if (parkCost!=null) {
		l.getAttributes().putAttribute("parkCost", parkCost);
	} else {
		l.getAttributes().putAttribute("parkCost", 0.0);
	}
}
}
