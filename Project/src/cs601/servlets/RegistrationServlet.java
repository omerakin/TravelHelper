package cs601.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

/** 
 * A servlet that handles user registration. doGet() method displays an HTML form with a button and
 * two textfields: one for the username, one for the password.
 * doPost() processes the form: if the username is not taken, it adds user info to the database.
 *
 */
@SuppressWarnings("serial")
public class RegistrationServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();


	/**
	 * Register form is formed. And if any error occurred, it shows the errorMessage
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Register New User", response);
		PrintWriter out = response.getWriter(); 
		// error will not be null if we were forwarded her from the post method where something went wrong
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		displayForm(out); 
		finishResponse(response);
	}

	/**
	 * Gets username and password from user and checks they are in correct form, 
	 * then redirect the user to page.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Register New User", response);

		// Get data from the textfields of the html form
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		// sanitize user input to avoid XSS attacks:
		newuser = StringEscapeUtils.escapeHtml4(newuser);
		newpass = StringEscapeUtils.escapeHtml4(newpass);
		
		// add user's info to the database 
		Status status = dbhandler.registerUser(newuser, newpass);

		if(status == Status.OK) { // registration was successful
			response.getWriter().println("Registered! Database updated.");
			response.sendRedirect(response.encodeRedirectURL("/login"));
		}
		else { // if something went wrong
			String url = "/register?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url); // send a get request  (redirect to the same path)
		}
	}

	/** Writes and HTML form that shows two textfields and a button to the PrintWriter */
	private void displayForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/register\" method=\"post\">"); // the form will be processed by POST
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Register\"></p>");
		out.println("</form>");
	}
}