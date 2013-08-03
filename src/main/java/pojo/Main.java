package pojo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.AdminServlet;

import contextListener.MyAdminServletContextListener;

/**
 * 
 * @author joayers
 * 
 * TO-DO:
 * --Set up admin server for metrics, Jetty working n
 * 
 * REFERENCE: What I'm using as a guide ---> https://github.com/jesperfj/jax-rs-heroku/tree/jetty
 *
 */

public class Main {

    public static void main(String[] args) throws Exception {
    	Server server = new Server(8112);
    	AdminServlet admin = new AdminServlet();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHolder h = new ServletHolder(new ServletContainer());
        ServletHolder metrics = new ServletHolder(admin);
        h.setInitParameter("com.sun.jersey.config.property.packages", "resources");
        h.setInitOrder(1);
        context.addServlet(h, "/rest/*");
        context.addServlet(metrics, "/metrics/*");
        context.setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry",contextListener.MyAdminServletContextListener.registry);
        context.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry", contextListener.MyAdminServletContextListener.Hregistry);

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