package cs601.servlets;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author akin_
 *		List Reviews info
 */
@SuppressWarnings("serial")
public class ReviewsServlet extends BaseServlet{
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
		
	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then list all reviews info to the user for specific hotel
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponse("Reviews", resp);
		displayLogOut(resp);
		sortReviews(req, resp);
		dbhandler.listReviewsInfo(req, resp, "Get");
		endingResponse(resp);		
	}

	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then checks which button is pressed,
	 *  				if Submit is pressed, user's review is inserted to reviews
	 *  				if Delete is pressed, user's review is deleted
	 *  				if Modify is pressed, user's review is modified. 
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponse("Reviews", resp);
		displayLogOut(resp);
		sortReviews(req, resp);
		String clicked_button = req.getParameter("button").trim();
		if (clicked_button.equals("Submit")) {			
			dbhandler.insertReview(req, resp);
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("Delete")) {			
			dbhandler.deleteReview(req, resp);
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("Modify")) {			
			dbhandler.modifyReview(req, resp);			
		} else if (clicked_button.equals("Default")) {
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("By date (most recent ones on top)")) {
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("By rating (highly rated on top)")) {
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		}
		endingResponse(resp);		
	}

}
