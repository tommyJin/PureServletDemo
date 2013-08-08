package pojo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.codahale.metrics.servlets.AdminServlet;

/**
 * 
 * Embeds server
 * 
 * REFERENCE: What I'm using as a guide ---> https://github.com/jesperfj/jax-rs-heroku/tree/jetty
 *
 */

public class Main {

    public static void main(String[] args) throws Exception {
    	Server server = new Server(8112); //server use port 8112
    	AdminServlet admin = new AdminServlet(); //servlet with metrics
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/"); //context path set to / means you'll do host:port/(whatever)
        server.setHandler(context); //add it to server
        ServletHolder h = new ServletHolder(new ServletContainer());
        ServletHolder metrics = new ServletHolder(admin);
        h.setInitParameter("com.sun.jersey.config.property.packages", "resources"); //add your resources to handler
        h.setInitOrder(1);
        context.addServlet(h, "/rest/*"); //path to resources so host:port/rest/...
        context.addServlet(metrics, "/metrics/*"); //path to metrics host:port/metrics/...
        context.setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry",contextListener.MyAdminServletContextListener.registry); //register your metrics registry
        context.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry", contextListener.MyAdminServletContextListener.Hregistry); //register your Healthregistry

        try
        {
            server.start();
            server.join();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }
	}