package pojo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * 
 * @author joayers
 * 
 * TO-DO:
 * --Get embeddy Jetty working. I think I'm close but it's not quite there. 
 * 
 * REFERENCE: What I'm using as a guide ---> https://github.com/jesperfj/jax-rs-heroku/tree/jetty
 *
 */

public class Main {

    public static void main(String[] args) throws Exception {
    	Server server = new Server(8112);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHolder h = new ServletHolder(new ServletContainer());
 //       h.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        h.setInitParameter("com.sun.jersey.config.property.packages", "resources");
        h.setInitOrder(1);
        context.addServlet(h, "/rest/*");
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
