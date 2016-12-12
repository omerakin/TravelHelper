package cs601.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

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
		prepareResponseHtml(resp);
		VelocityContext context = getContext("Hotel");
		Template template = getTemplate(req, "HotelInfo.html");		
		dbhandler.listHotelInfoTemplateEngine(req, context);
		mergeAndPrintResponse(resp, template, context);
	}

	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then clicked link is added to Expedia history
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		dbhandler.insertExpedia(req);
	}
	
}
