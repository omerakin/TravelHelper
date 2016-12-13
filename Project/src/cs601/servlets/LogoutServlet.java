package cs601.servlets;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author akin_
 *		Log out button
 */
@SuppressWarnings("serial")
public class LogoutServlet extends BaseServlet {

	/**
	 * 		it removes user attribute from session and kill the session, 
	 * 	then redirects to the login page
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		req.getSession().removeAttribute("user");
		req.getSession().invalidate();
		resp.sendRedirect(resp.encodeRedirectURL("/login"));
	}
}
