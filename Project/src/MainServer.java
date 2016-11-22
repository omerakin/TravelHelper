

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Demonstrates how to use Jetty, servlets and JDBC for user registration. This is a
 * simplified example, and **NOT** secure. 
 * Modified from the example by Prof. Engle.
 */
public class MainServer {
	private static int PORT = 8080;

	public static void main(String[] args) {
		// Server
		Server server = new Server(PORT);
		
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");
		servletContextHandler.addServlet(RegistrationServlet.class, "/register");
		servletContextHandler.addServlet(LogInServlet.class, "/login");
		servletContextHandler.addServlet(HotelsServlet.class, "/hotels");
		servletContextHandler.addServlet(ReviewsServlet.class, "/reviews");
		servletContextHandler.addServlet(MyReviewsServlet.class, "/myreviews");
		servletContextHandler.addServlet(LogoutServlet.class, "/logout");
		
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] {servletContextHandler});
		
		server.setHandler(handlers);		
		try {
			server.start();
			System.out.println("Server is ready!!!");
			server.join();

		} catch (Exception ex) {
			System.out.println("An exception occurred while running the server. ");
			System.exit(-1);
		}
	}
}