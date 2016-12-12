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
 *			List MyReviews info
 */
@SuppressWarnings("serial")
public class MyReviewsServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
		
	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then list all reviews info that user submitted.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("My Reviews");
		Template template = getTemplate(req, "MyReviewsInfo.html");
		dbhandler.listMyReviewsInfoTemplateEngine(req, context);
		mergeAndPrintResponse(resp, template, context);		
	}

}
