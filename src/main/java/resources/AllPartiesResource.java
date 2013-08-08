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
import com.fasterxml.jackson.databind.ObjectMapper;

import contextListener.MyAdminServletContextListener;

@Path("/parties")
public class AllPartiesResource {
	/**
	 * contains resources
	 */
	@Context
	UriInfo url;
	
	@Context
	Request request;
	
	String name;
	
	public static final Timer allTime = MyAdminServletContextListener.registry.timer(MetricRegistry.name("Timer","all-parties"));

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllParties() throws Exception
	{
		final Timer.Context context=allTime.time(); //start the timer 
		String string; 
		DBConnection.readAllData();
		ObjectMapper jsonMapper = new ObjectMapper();
		string=jsonMapper.writeValueAsString(DBConnection.getPartyCollection());
		context.stop(); //stops timer 
		return string;
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
