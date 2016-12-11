package cs601.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Provides base functionality to all servlets in this example. Original author:
 * Prof. Engle
 *
 * @see MainServer 
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {

	/**
	 * 
	 * @param title
	 * 			- Head of page
	 * @param response
	 * 			- HttpServletResponse
	 * 
	 * 			HTML header of page
	 */
	protected void prepareResponse(String title, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();

			writer.println("<!DOCTYPE html>");
			writer.println("<html>");
			writer.println("<head>");
			writer.println("\t<title>" + title + "</title>");
			writer.println("</head>");
			writer.println("<body>");
		} catch (IOException ex) {
			System.out.println("IOException while preparing the response: " + ex);
			return;
		}
	}
	
	/**
	 * 
	 * @param response
	 * 			- HttpServletResponse
	 * 
	 * 			HTML
	 */
	protected void prepareResponseHtml(HttpServletResponse resp) {		
		resp.setContentType("text/html");
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
	/**
	 * 
	 * @param title
	 * 			- Head of page
	 * @return 
	 */
	protected VelocityContext getContext(String title) {		
		VelocityContext context = new VelocityContext();
		context.put("title", title);
		return context;
	}
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param htmlFileName
	 * 			- HTML File Name
	 * @return - template
	 */
	protected Template getTemplate(HttpServletRequest req, String htmlFileName) {		
		VelocityEngine ve = (VelocityEngine)req.getServletContext().getAttribute("templateEngine");		
		Template template = ve.getTemplate("templates/" + htmlFileName);
		return template;
	}
	
	/**
	 * 
	 * @param resp
	 * 			- HttpServletResponse
	 * @param template
	 * 			- Template
	 * @param context
	 * 			- VelocityContext
	 * @throws IOException
	 */
	protected void mergeAndPrintResponse(HttpServletResponse resp, Template template, VelocityContext context) throws IOException {		
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		resp.getWriter().println(writer.toString());
	}
	
	
	

	/**
	 * 
	 * @param response
	 * 			- HttpServletResponse
	 * 
	 * 			HTML footer of page with last updated info
	 */
	protected void finishResponse(HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();

			writer.println();
			writer.println("<p style=\"font-size: 10pt; font-style: italic;\">");
			writer.println("Last updated at " + getDate());
			writer.println("</p>");

			writer.println("</body>");
			writer.println("</html>");

			writer.flush();

			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		} catch (IOException ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}
	
	/**
	 * 
	 * @param response
	 * 			- HttpServletResponse
	 * 
	 * 			HTML footer of page
	 */
	protected void endingResponse(HttpServletResponse resp) {
		try {
			PrintWriter printWriter = resp.getWriter();
			printWriter.println("</body>");
			printWriter.println("</html>");
			printWriter.flush();
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
	 * 
	 * @param resp
	 * 		 	- HttpServletResponse
	 * 
	 * 			Display View Hotels, View My Reviews,Logout buttons
	 */
	protected void displayLogOut(HttpServletResponse resp) {
		try {
			PrintWriter printWriter = resp.getWriter();
			printWriter.println("<form action=\"/logout\" method=\"get\">");
			printWriter.println("<p><input type=\"submit\" value=\"Logout\" style=\"float:right\" ></p>");
			printWriter.println("</form>");						
			printWriter.println("<form action=\"/myreviews\" method=\"get\">");
			printWriter.println("<p><input type=\"submit\" value=\"View My Reviews\" style=\"float:right; margin-right: 10px;\" ></p>");
			printWriter.println("</form>");
			printWriter.println("<form action=\"/myhotels\" method=\"get\">");
			printWriter.println("<p><input type=\"submit\" value=\"View My Saved Hotels\" style=\"float:right; margin-right: 10px;\" ></p>");
			printWriter.println("</form>");
			printWriter.println("<form action=\"/hotels\" method=\"get\">");
			printWriter.println("<p><input type=\"submit\" value=\"View Hotels\" style=\"float:right; margin-right: 10px;\" ></p>");
			printWriter.println("</form>");
			printWriter.flush();
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param context
	 * 			- VelocityContext
	 */
	protected void displayLastLogInTime(HttpServletRequest req, VelocityContext context) {
		HttpSession session = req.getSession();
		Timestamp lastLoginTime = (Timestamp) session.getAttribute("LastLoginTime");
		context.put("lastLoginTime", "");
		if (lastLoginTime != null){
			context.put("lastLoginTime", "Last log in time : " + lastLoginTime.toString());
		}				
	}
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws IOException
	 * 
	 * 			Checks user is already logged or not. Then redirects
	 */
	protected void checkUserSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {		
		HttpSession session = req.getSession();
		String username = (String) session.getAttribute("user");
		if(username == null || username.isEmpty()){
			resp.sendRedirect(resp.encodeRedirectURL("/login"));
		}
	}
	
	/**
	 * 
	 * @param resp
	 * 		 	- HttpServletResponse
	 * 
	 * 			Display Search Hotels,and button
	 */
	protected void searchHotels(HttpServletResponse resp) {
		try {
			PrintWriter printWriter = resp.getWriter();
			printWriter.println("<form action=\"/hotels\" method=\"post\">");
			printWriter.println("<p><input type=\"search\" name=\"searchHotel\" id=\"searchHotel\" placeholder=\"Search for hotels..\" "
					+ "style=\" float:left; width:500px; height:25px;\"> &nbsp;");
			printWriter.println("<input type=\"submit\" value=\"Search\" style=\"height:25px;\" ></p>");
			printWriter.println("</form>");
			printWriter.flush();
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 
	 * @param req 
	 * 			- HttpServletRequest
	 * @param resp
	 * 		 	- HttpServletResponse
	 * 
	 * 			Display Sort Reviews,and button
	 */
	protected void displaySortReviews(HttpServletRequest req, HttpServletResponse resp) {
		try {
			String hotelId = req.getParameter("hotelId");
			hotelId = StringEscapeUtils.escapeHtml4(hotelId);
			PrintWriter printWriter = resp.getWriter();
			printWriter.println("<form action=\"/reviews\" method=\"post\">");
			printWriter.println("<p> <b>Sort By:</b> &nbsp; &nbsp;");
			printWriter.println("<input type=\"submit\" name=\"button\" value=\"Default\" "
					+ "style=\"background-color: Transparent; cursor:pointer; overflow: hidden; color: blue;\">");
			printWriter.println("&nbsp; &nbsp; &nbsp;");
			printWriter.println("<input type=\"submit\" name=\"button\" value=\"By date (most recent ones on top)\" "
					+ "style=\"background-color: Transparent; cursor:pointer; overflow: hidden; color: blue;\">");
			printWriter.println("&nbsp; &nbsp; &nbsp;");
			printWriter.println("<input type=\"submit\" name=\"button\" value=\"By rating (highly rated on top)\" "
					+ "style=\"background-color: Transparent; cursor:pointer; overflow: hidden; color: blue;\">" + "</p>");
			printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
			printWriter.println("</form>");
			printWriter.println("<hr>");
			printWriter.flush();
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 
	 * @return
	 * 		- Calendar date
	 */
	protected String getDate() {
		String format = "hh:mm a 'on' EEE, MMM dd, yyyy";
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(Calendar.getInstance().getTime());
	}

	/**
	 * Return a cookie map from the cookies in the request
	 * 
	 * @param request
	 * @return
	 */
	protected Map<String, String> getCookieMap(HttpServletRequest request) {
		HashMap<String, String> map = new HashMap<String, String>();

		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				map.put(cookie.getName(), cookie.getValue());
			}
		}

		return map;
	}

	/**
	 * Clear cookies
	 * 
	 * @param request
	 * @param response
	 */
	protected void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return;
		}

		for (Cookie cookie : cookies) {
			cookie.setValue("");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	/**
	 * Clear cookie
	 * 
	 * @param cookieName
	 * @param response
	 */
	protected void clearCookie(String cookieName, HttpServletResponse response) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	/**
	 * Get Status message
	 * 
	 * @param errorName
	 * @return
	 */
	protected String getStatusMessage(String errorName) {
		Status status = null;

		try {
			status = Status.valueOf(errorName);
		} catch (Exception ex) {
			status = Status.ERROR;
		}

		return status.toString();
	}

	/**
	 * Get Status message
	 * 
	 * @param code
	 * @return
	 */
	protected String getStatusMessage(int code) {
		Status status = null;

		try {
			status = Status.values()[code];
		} catch (Exception ex) {
			status = Status.ERROR;
		}

		return status.toString();
	}
}