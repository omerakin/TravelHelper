

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Demonstrates how to use Jetty, servlets and JDBC for user registration. This is a
 * simplified example, and **NOT** secure. 
 * Modified from the example by Prof. Engle.
 */
public class RegisterServer {
	private static int PORT = 8080;

	public static void main(String[] args) {
		Server server = new Server(PORT);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(RegistrationServlet.class, "/register");
		handler.addServletWithMapping(LogInServlet.class, "/login");
		// other servlets can be added as needed such as LoginServlet etc.
		
		try {
			server.start();
			server.join();

		} catch (Exception ex) {
			System.out.println("An exception occurred while running the server. ");
			System.exit(-1);
		}
	}
}