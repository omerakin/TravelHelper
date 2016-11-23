import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author akin_
 *		/ other url directions
 */
@SuppressWarnings("serial")
public class OtherServlet extends HttpServlet{

	/**
	 * 		if user enter this url, check it is logged in or not
	 * 									if logged in, shows hotel page
	 * 									if not, shows log in page.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession();
		String username = (String) session.getAttribute("user");
		if(username == null || username.isEmpty()){
			resp.sendRedirect(resp.encodeRedirectURL("/login"));
		} else {
			resp.sendRedirect(resp.encodeRedirectURL("/hotels"));
		}
	}

}
