package cs601.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author akin_
 *		List Hotel info
 */
@SuppressWarnings("serial")
public class HotelServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then list hotel's info to the user
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponse("Hotel", resp);
		displayLogOut(resp);
		dbhandler.listHotelInfo(req, resp);
		endingResponse(resp);		
	}
}
