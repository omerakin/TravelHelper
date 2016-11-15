import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class LogInServlet extends BaseServlet{
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		prepareResponse("Login", resp);
		
		PrintWriter printWriter = resp.getWriter(); 
		// error will not be null if we were forwarded her from the post method where something went wrong
		String error = req.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			printWriter.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}
		displayForm(printWriter);
		finishResponse(resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		prepareResponse("Login User", resp);

		// Get data from the textfields of the html form
		String user = req.getParameter("user");
		String pass = req.getParameter("pass");
		// sanitize user input to avoid XSS attacks:
		user = StringEscapeUtils.escapeHtml4(user);
		pass = StringEscapeUtils.escapeHtml4(pass);
		// check user's info in the database 
		Status status = dbhandler.loginUser(user, pass);
		if(status == Status.OK) { // registration was successful
			String url = "/hotels";
			url = resp.encodeRedirectURL(url);
			resp.sendRedirect(url);
		} else { // if something went wrong
			String url = "/login?error=" + status.name();
			url = resp.encodeRedirectURL(url);
			resp.sendRedirect(url); // send a get request  (redirect to the same path)
		}
	}
	
	/** Writes and HTML form that shows two textfields and a button to the PrintWriter */
	private void displayForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/login\" method=\"post\">"); // the form will be processed by POST
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
		out.println("<p><input type=\"submit\" value=\"Login\"></p>");
		out.println("</form>");
	}

}
