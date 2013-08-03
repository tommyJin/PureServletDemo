package health;


import com.codahale.metrics.health.HealthCheck;

import resources.DBConnection;

public class TemplateHealthCheck extends HealthCheck {


	@Override
	protected Result check() throws Exception 
	{
		if(!DBConnection.con.isValid(10))
		{
			return Result.unhealthy("Can't connect to database");
		}
		else	
			return Result.healthy();
	}
}	