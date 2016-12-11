package cs601.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

@SuppressWarnings("serial")
public class MyExpediasServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("My Expedia");
		Template template = getTemplate(req, "MyExpediaInfo.html");		
		dbhandler.listExpediaInfoTemplateEngine(req, resp, context);
		mergeAndPrintResponse(resp, template, context);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("My Expedia");
		Template template = getTemplate(req, "MyExpediaInfo.html");
		String clicked_button = req.getParameter("button").trim();
		if (clicked_button.equals("Delete")) {
			dbhandler.deleteExpedia(req);
		}
		dbhandler.listExpediaInfoTemplateEngine(req, resp, context);
		mergeAndPrintResponse(resp, template, context);
	}	

}
