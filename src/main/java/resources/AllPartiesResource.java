package resources;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;


import java.util.ArrayList;
import java.util.List;

import pojo.Party;

/**
 * 
 * Class points to different urls, if you want to use the jackson code (currently commented out)
 * on the @Produces line make sure to delete the 
 * MediaType.XML field
 *
 */

@Path("/parties")
public class AllPartiesResource {
	
	@Context
	UriInfo url;
	
	@Context
	Request request;
	
	String name;
	
	public static final Timer allTime = DBConnection.registry.timer(MetricRegistry.name("Timer","all-parties"));

	@GET
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public List<Party> getAllParties() throws Exception
	{
		final Timer.Context context=allTime.time(); //start the timer 
		List<Party> list = new ArrayList<Party>();
		DBConnection.readAllData();
		list.addAll(DBConnection.getPartyCollection().values());
		context.stop(); //stops timer 
		return list;
		
//		---> code for Jackson
//		String string; 
//		DBConnection.readAllData();
//		ObjectMapper jsonMapper = new ObjectMapper();
//		string=jsonMapper.writeValueAsString(DBConnection.getPartyCollection());
//		return string;
	}
	
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPartyCount() throws Exception
	{
		DBConnection.readAllData();
		return String.valueOf(DBConnection.getPartyCollection().size());
	}
	
	@Path("{party}") //points to OnePartyResource.class
	public OnePartyResource getParty(@PathParam("party")String party)
	{
		name = party;
		return new OnePartyResource(url,request,party);
	}
}
