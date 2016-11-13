

import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import cs601.hotelapp.HotelDataBuilder;
import cs601.hotelapp.ThreadSafeHotelData;

/**
 * Demonstrates how to use Jetty, servlets and JDBC for user registration. This is a
 * simplified example, and **NOT** secure. 
 * Modified from the example by Prof. Engle.
 */
public class MainServer {
	private static int PORT = 8080;

	public static void main(String[] args) {
		// Before we start our server, we need to load all the hotel data 
		// (both general hotel info and reviews) into our data structures 
		// from the input files.
		ThreadSafeHotelData tsData = new ThreadSafeHotelData();
		HotelDataBuilder hotelDataBuilder = new HotelDataBuilder(tsData);
		hotelDataBuilder.loadHotelInfo("input/hotels200.json");
		hotelDataBuilder.loadReviews(Paths.get("input/reviews"));
		hotelDataBuilder.shutdown();
				
		Server server = new Server(PORT);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(RegistrationServlet.class, "/register");
		handler.addServletWithMapping(LogInServlet.class, "/login");
		// other servlets can be added as needed such as LoginServlet etc.
		
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