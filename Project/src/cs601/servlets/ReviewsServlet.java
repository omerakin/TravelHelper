package cs601.servlets;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

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
		/*
		checkUserSession(req, resp);
		prepareResponse("Reviews", resp);
		displayLogOut(resp);
		displaySortReviews(req, resp);
		dbhandler.listReviewsInfo(req, resp, "Get");
		endingResponse(resp);	
		*/
		
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("Reviews");
		Template template = getTemplate(req, "ReviewsInfo.html");
		dbhandler.listReviewsInfoTemplateEngine(req, context, "Get");
		mergeAndPrintResponse(resp, template, context);
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
		/*
		checkUserSession(req, resp);
		prepareResponse("Reviews", resp);
		displayLogOut(resp);
		displaySortReviews(req, resp);
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
		} else if (clicked_button.equals("Like")) {
			dbhandler.insertLikeReview(req, resp);
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("Unlike")) {
			dbhandler.deleteLikeReview(req, resp);
			dbhandler.listReviewsInfo(req, resp, clicked_button);
		}
		endingResponse(resp);
		*/
		
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("Reviews");
		Template template = getTemplate(req, "ReviewsInfo.html");
		String clicked_button = req.getParameter("button").trim();
		if (clicked_button.equals("Submit")) {			
			dbhandler.insertReview(req);
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		} else if (clicked_button.equals("Delete")) {			
			dbhandler.deleteReview(req);
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		} else if (clicked_button.equals("Modify")) {			
			dbhandler.modifyReview(req, context);			
		} else if (clicked_button.equals("By date (most oldest ones on top)")) {
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		} else if (clicked_button.equals("By date (most recent ones on top)")) {
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		} else if (clicked_button.equals("By rating (highly rated on top)")) {
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		} else if (clicked_button.equals("Like")) {
			dbhandler.insertLikeReview(req);
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		} else if (clicked_button.equals("Unlike")) {
			dbhandler.deleteLikeReview(req);
			dbhandler.listReviewsInfoTemplateEngine(req, context, clicked_button);
		}
		mergeAndPrintResponse(resp, template, context);
	}

}
