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
 *		List Hotels info
 */
@SuppressWarnings("serial")
public class HotelsServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
		
	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then list all hotels info to the user
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("Hotels");
		Template template = getTemplate(req, "HotelsInfo.html");
		displayLastLogInTime(req, context);
		dbhandler.listGeneralHotelsInfoTemplateEngine(context);
		mergeAndPrintResponse(resp, template, context);		
	}

	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then list all searched hotels info to the user
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("Hotels");
		Template template = getTemplate(req, "HotelsInfo.html");
		context.put("lastLoginTime", "");
		dbhandler.listSearchedHotelsInfoTemplateEngine(req, context);
		mergeAndPrintResponse(resp, template, context);		
	}	

}
