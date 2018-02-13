package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class DavisMatsimUtils {

	
	public static Map<String,List<Link>> getLinksPerTAZ(Network network){
		
		Map<String,List<Link>> linksPerTAZ = new HashMap<>();
		for (Link l : network.getLinks().values()) {
			String taz = (String) l.getAttributes().getAttribute("taz");
			if (linksPerTAZ.containsKey(taz)) {
				linksPerTAZ.get(taz).add(l);
			} else {
				List<Link> links = new ArrayList<>();
				links.add(l);
				linksPerTAZ.put(taz, links);
				}
		}
		return linksPerTAZ;
	}
	
}
