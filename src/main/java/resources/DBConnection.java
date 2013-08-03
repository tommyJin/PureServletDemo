package resources;
	
import health.TemplateHealthCheck;

import java.net.InetSocketAddress;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import contextListener.MyAdminServletContextListener;

import pojo.Party;

import net.spy.memcached.MemcachedClient;

/**
 * Note that the Metrics will be functional...they just may not be doing what it says they're doing. I have it figured
 * out in the DropWizard code but I was flying through this and I think some old code that doesn't logically make sense
 * got added in (a lot of copy+paste going on), so the measurements they're reading (specifically 
 * on the memcache hit-miss ratio meter/gauge) may be off 
 * 
 * TO-DO:
 * --Get memcache update working, see line 113. Essentially when I call something in the cache, I want to refresh 
 * its expiration but it says I need to use Binary Protocol. Other than that everything should work fine
 * 
 * @author joayers
 *
 */
	
	public class DBConnection {
	
		public static Connection con;
		private static ResultSet resultSet;
		private static PreparedStatement ps;
		private static ResultSetMetaData meta;
		private static HashMap<String,Party> map;
		public 	static DBConnection connection;
		public static Party party;
		public static final InetSocketAddress isa= new InetSocketAddress("atom1.cisco.com",11211);
		public static MemcachedClient memcache;
		public static int getMisses;
		public static final Meter misses = MyAdminServletContextListener.registry.meter(MetricRegistry.name("Meter", "get-misses")); 
		public static final Meter calls = MyAdminServletContextListener.registry.meter(MetricRegistry.name("Meter", "get-calls"));
		
		private DBConnection()
		{
			try 
			{
				map = new HashMap<String,Party>();
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(
						"jdbc:mysql://atom3.cisco.com:3306/reddb", "redteam",
						"redteam");		
				//initialize memcache
				memcache   = new MemcachedClient(isa); 
				//this is just a integer variable I was using to make sure my Meters were measuring correctly
				getMisses = Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
						get("get_misses"));
				metricsRegistry();		
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public static void readAllData() //reads all parties in database
		{
			if(connection == null)
			{
				connection = new DBConnection();
			}
			try
			{
				map.clear();
				String query = "(SELECT * FROM xRM)";
				ps = con.prepareStatement(query);
				resultSet = ps.executeQuery();
				meta = resultSet.getMetaData();
				String columnName, value, partyName;
				while(resultSet.next())
				{
					partyName = resultSet.getString("PARTY_NAME");
					map.put(partyName, new Party()); //this is the map that keeps track of all parties
					party = map.get(partyName);
					for(int j=1;j<=meta.getColumnCount();j++) //necessary to start at j=1 because of MySQL index starting at 1
					{
						columnName = meta.getColumnLabel(j);
						value = resultSet.getString(columnName);
						party.getPartyInfo().put(columnName, value); //this is the hashmap within the party that keeps 
						//track of the individual values. The column Name = label, value is the value
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public static Party readOneParty(String partyName) //reads one party
		{
			Party localParty = new Party();
			if(connection==null)
			{
				connection = new DBConnection();
			}
			if(memcache.get(partyName)!=null)
			{
//				memcache.touch(partyName, 300); //will update to keep object relevant, needs binary protocol
				calls.mark();
				if(Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
						get("get_misses"))!=getMisses)
				{
					getMisses = Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
							get("get_misses"));
					misses.mark(getMisses);
				}
				return (Party)memcache.get(partyName);
			}
			else{
				try
				{
					String query = "SELECT * FROM xRM WHERE PARTY_NAME=?";
					ps = con.prepareStatement(query);
					ps.setString(1, partyName);
					resultSet = ps.executeQuery();
					meta = resultSet.getMetaData();
					String columnName, value;
					resultSet.next();
					for(int j=1;j<=meta.getColumnCount();j++) //necessary to start at j=1 because of MySQL index starting at 1
					{
						columnName = meta.getColumnLabel(j);
						value = resultSet.getString(columnName);
						localParty.getPartyInfo().put(columnName, value); //this is the hashmap within the party that keeps 
						//track of the individual values. The column Name = label, value is the value
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				memcache.add(partyName, 300, localParty);
				return localParty;
			}
			
		}
		public static HashMap<String,Party> getPartyCollection()
		{
			return map;
		}
		/* This metricsRegistry() is slightly different than the dropwizard one. This is because this project
		 * is running on Metrics 3.0.0 , Dropwizard is on 2.2.0. That is why you don't see @Timed or other annotations
		 * here. I decided to separate all my metrics declarations into one method to keep cluttering it more than it 
		 * already is
		 * 
		 * The biggest difference is now there's a registry you have to declare, (which I do up top), it keeps track of all 
		 * your metrics. I haven't added the alternate way of showing metrics (console, excel files, etc.) to this demo
		 * but that is now available in the DropWizard demo.
		 */
		private void metricsRegistry()
		{
			MyAdminServletContextListener.Hregistry.register("DB Check", new TemplateHealthCheck());
			MyAdminServletContextListener.registry.register(MetricRegistry.name("Gauge", "curr_items"), new Gauge<Integer>()
					{
						public Integer getValue()
						{
							return Integer.valueOf(memcache.getStats().get(DBConnection.isa).
									get("curr_items"));
						}
					});
			MyAdminServletContextListener.registry.register(MetricRegistry.name("Gauge","cache_hit_rate-gauge"), new Gauge<Double>()
					{
						public Double getValue()
						{
								double get_hits= Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
										get("get_hits"));
								double get_misses = Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
										get("get_misses"));
								return get_hits/(get_misses+get_hits);
						}
					});
		}
	}