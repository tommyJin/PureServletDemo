package resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.NotFoundException;

import contextListener.MyAdminServletContextListener;



import pojo.PartyPojo;
 
public class OnePartyResource {
	
	@Context
	UriInfo url;
	
	@Context
	Request request;
	
	String name;
	

	public static final Timer time = MyAdminServletContextListener.registry.timer(MetricRegistry.name("Timer","one-party"));	
	
	public OnePartyResource(){}
	
	public OnePartyResource(UriInfo url, Request request, String name)
	{
		this.url=url;
		this.request=request;
		this.name=name;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON) 
	public String getParty() throws Exception
	{
		Timer.Context context = time.time(); 
		String party = DBConnection.readOneParty(name);
		if(party==null)
		{
			throw new NotFoundException("No such party exists");
		}
		context.stop();
		return party;

	}
}
