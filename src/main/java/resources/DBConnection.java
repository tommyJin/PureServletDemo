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
import com.codahale.metrics.Counter;
import com.codahale.metrics.RatioGauge;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import contextListener.MyAdminServletContextListener;

import pojo.PartyPojo;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
	/**
	 * The big class again. Not worth commenting it all again. Just use dropwizard as reference. I will comment on the
	 * parts that are different
	 * @author joayers
	 *
	 */
	public class DBConnection {
	
		public static Connection con;
		private static ResultSet resultSet;
		private static PreparedStatement ps;
		private static ResultSetMetaData meta;
		private static HashMap<String,PartyPojo> map;
		private	static DBConnection connection;
		private static PartyPojo party;
		private static final InetSocketAddress isa= new InetSocketAddress("atom1.cisco.com",11211);
		private static MemcachedClient memcache;
		private static Meter hits = MyAdminServletContextListener.registry.meter(MetricRegistry.name("Meter",
				"memcache-hits")); 
		private static Meter calls = MyAdminServletContextListener.registry.meter(MetricRegistry.name("Meter",
				"memcache-calls"));
		private static Counter evictions = MyAdminServletContextListener.registry.counter(MetricRegistry.name("Counter",
				"Memcache-Evictions"));
		private static int getHits,getCalls;
		private final static int MAX_MEMCACHED_EXPIRATION=2505600;

		private DBConnection()
		{
			try 
			{
				map = new HashMap<String,PartyPojo>();
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(
						"jdbc:mysql://atom3.cisco.com:3306/reddb", "redteam",
						"redteam");		
				memcache   = new MemcachedClient(isa); 
				//this is just a integer variable I was using to make sure my Meters were measuring correctly
				getHits = Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
						get("get_hits"));
				getCalls = Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
						get("cmd_get"));
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
					map.put(partyName, new PartyPojo()); //this is the map that keeps track of all parties
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
		public static String readOneParty(String partyName) //reads one party
		{
			String string=null;
			ObjectMapper jsonMapper = new ObjectMapper();
			PartyPojo localParty = new PartyPojo();
			if(connection==null)
			{
				connection = new DBConnection();
			}
			if(Integer.valueOf(memcache.getStats().get(DBConnection.isa).get("evictions"))!=evictions.getCount())
			{
				long numEvictions = evictions.getCount();
				evictions.dec(numEvictions);
				evictions.inc(Integer.valueOf(memcache.getStats().get(DBConnection.isa).get("evictions")));
			}
			if(memcache.get(partyName)!=null)
			{
				if(Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
						get("get_hits"))!=getHits)
				{
					getHits = Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
							get("get_hits"));
					hits.mark(getHits);
				}
				if(getCalls!=Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
						get("cmd_get")))
				{
					getCalls=Integer.valueOf(DBConnection.memcache.getStats().get(DBConnection.isa).
							get("cmd_get"));
					calls.mark(getCalls);
				}
				return (String)memcache.get(partyName);
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
				try {
					string=jsonMapper.writeValueAsString(localParty.getPartyInfo());
				
				} 
				catch (JsonProcessingException e) 
				{
					e.printStackTrace();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				partyName = "Pure"+partyName; //this makes sure that the memcache keys for PartyAPIMaven don't interfere with the keys from Dropwizard
				memcache.add(partyName, MAX_MEMCACHED_EXPIRATION, string); //add update key and string to memcache
				return string;
			}
			
		}
		public static HashMap<String,PartyPojo> getPartyCollection()
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
			MyAdminServletContextListener.registry.register("CacheHitPercentage", new CacheHitMissRatio(hits,calls));
		}
		public class CacheHitMissRatio extends RatioGauge
		{
			Meter hits, calls;
			public CacheHitMissRatio(Meter hits, Meter calls)
			{
				this.hits=hits;
				this.calls=calls;
				
			}
			@Override
			protected Ratio getRatio() {
				return Ratio.of(hits.getCount(), calls.getCount());
			}
			
		}
	}
