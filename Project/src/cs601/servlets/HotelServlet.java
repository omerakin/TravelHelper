package cs601.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

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
		
		/*
		checkUserSession(req, resp);
		prepareResponse("Hotel", resp);
		displayLogOut(resp);
		dbhandler.listHotelInfo(req, resp);
		endingResponse(resp);
		*/
		
		
		checkUserSession(req, resp);
		
		resp.setContentType("text/html");
		resp.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = resp.getWriter();
		
		VelocityEngine ve = (VelocityEngine)req.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/hotelInfo.html");
		context.put("title", "Hotel");
		
		dbhandler.listHotelInfoTemplateEngine(req, resp, context);	
		
		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		out.println(writer.toString());		
	}
}
