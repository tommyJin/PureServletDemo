package resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sun.jersey.api.NotFoundException;

import contextListener.MyAdminServletContextListener;



import pojo.Party;
 
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) //if you're using the Jackson code delete MediaType.APPLICATION_XML
	public Party getParty() throws Exception
	{
		// ----> code for jackson
//		Party party = DBConnection.readOneParty(name);
//		String string;
//		ObjectMapper jsonMapper = new ObjectMapper();
//		if(party==null)
//		{
//			throw new NotFoundException("No such party exists");
//		}
//		string=jsonMapper.writeValueAsString(party.getPartyInfo());
//		return string;
		Timer.Context context = time.time(); //start timer
		Party party = DBConnection.readOneParty(name);
		if(party==null)
		{
			throw new NotFoundException("No such party exists");
		}
		context.stop(); //stop
		return party;
	}
}
