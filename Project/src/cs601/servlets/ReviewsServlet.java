package cs601.servlets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 
 * @author akin_
 *		List Reviews info
 */
@SuppressWarnings("serial")
public class ReviewsServlet extends BaseServlet{
	private static final String REVIEWS_SQL = "SELECT review_title, review_text, username, rating FROM reviews WHERE hotel_id = ?";
	private static final String REVIEWS_SQL_v2 = "SELECT review_title, review_text, username, rating FROM reviews WHERE hotel_id = ? AND username =?";
	private static final String INSERT_REVIEWS_SQL = "INSERT INTO reviews (hotel_id, review_id, review_title, review_text, username, isRecom, rating) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?);";
	private static final String DELETE_REVIEWS_SQL = "DELETE FROM reviews WHERE hotel_id = ? AND username = ?";
	
	/**
	 *  Firstly checks user already logged in or not,
	 *  if logged in, then list all reviews info to the user for specific hotel
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		checkUserSession(req, resp);
		prepareResponse("Reviews", resp);
		displayLogOut(resp);
		listReviewsInfo(req, resp, "Get");
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
		String clicked_button = req.getParameter("button").trim();
		if (clicked_button.equals("Submit")) {			
			insertReview(req, resp);
			listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("Delete")) {			
			deleteReview(req, resp);
			listReviewsInfo(req, resp, clicked_button);
		} else if (clicked_button.equals("Modify")) {			
			modifyReview(req, resp);			
		}		
		endingResponse(resp);
		
	}

	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 				
	 * 			user's review information is uploaded to the reviews table
	 */
	private void insertReview(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		
		String hotelId = req.getParameter("hotelId");
		String username = (String) session.getAttribute("user");
		String user_review_title = req.getParameter("user_review_title");
		String user_review_text = req.getParameter("user_review_text");
		String star = req.getParameter("user_star");
		
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		user_review_title = StringEscapeUtils.escapeHtml4(user_review_title);
		user_review_text = StringEscapeUtils.escapeHtml4(user_review_text);
		double user_star = Double.parseDouble(star);
		String user_review_id = UUID.randomUUID().toString(); // generate review_id for new review
		
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(INSERT_REVIEWS_SQL);){
			statementReview.setString(1, hotelId);
			statementReview.setString(2, user_review_id);
			statementReview.setString(3, user_review_title);
			statementReview.setString(4, user_review_text);
			statementReview.setString(5, username);
			statementReview.setBoolean(6, true);
			statementReview.setDouble(7, user_star);
			statementReview.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			user's review information is deleted from the reviews table
	 */
	private void deleteReview(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		
		String hotelId = req.getParameter("hotelId");
		String username = (String) session.getAttribute("user");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(DELETE_REVIEWS_SQL);){
			statementReview.setString(1, hotelId);
			statementReview.setString(2, username);
			statementReview.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			user's review information is modified and again uploaded to the reviews table
	 */
	private void modifyReview(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		PrintWriter printWriter = resp.getWriter();
		
		String review_title = null;
		String review_text = null;
		String rating = null;
		String hotelId = req.getParameter("hotelId");
		String username = (String) session.getAttribute("user");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		
		//select the review
		try(Connection connection = db.getConnection(); PreparedStatement selectReview = connection.prepareStatement(REVIEWS_SQL_v2);){
			selectReview.setString(1, hotelId);
			selectReview.setString(2, username);
			ResultSet reviewResultSet = selectReview.executeQuery();			
			if(reviewResultSet.next()){
				review_title = reviewResultSet.getString("review_title");
				review_text = reviewResultSet.getString("review_text");
				rating = reviewResultSet.getString("rating");
			}
			//delete the review
			try(PreparedStatement deleteReview = connection.prepareStatement(DELETE_REVIEWS_SQL);){
				deleteReview.setString(1, hotelId);
				deleteReview.setString(2, username);
				deleteReview.executeUpdate();
			}
			//list Reviews without users review
			listReviewsInfo(req, resp, "Modify");
			//prepare the new review
			prepareReview(printWriter, hotelId, review_title, review_text, rating);			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @param clicked_button
	 * 			- which button is clicked checks
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			Connect database and get all review information for specific hotel such as username, 
	 * 		rating, review_title, review_text
	 * 			Compare username with user's username, if they are equal, 
	 * 		it allows user to edit his/her own review such as modify or delete 
	 */
	private void listReviewsInfo(HttpServletRequest req, HttpServletResponse resp, String clicked_button) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		String hotelId = req.getParameter("hotelId");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		String username = (String) session.getAttribute("user");
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(REVIEWS_SQL);){
			statementReview.setString(1, hotelId);
			ResultSet reviewResultSet = statementReview.executeQuery();			
			int count = 0;
			while(reviewResultSet.next()){
				printWriter.println("<p>" + "Review by " + reviewResultSet.getString("username") + ": " + "</p>"); 
				printWriter.println("<p>" + "Rating : " + reviewResultSet.getString("rating") + "</p>"); 
				printWriter.println("<p>" + "Title : " + reviewResultSet.getString("review_title") + "</p>"); 
				printWriter.println("<p>" + "Text : " + reviewResultSet.getString("review_text") + "</p>"); 
				if(username.equals(reviewResultSet.getString("username"))){	
					modifyOrDeleteReview(printWriter, hotelId);
					count++; 
				}
				printWriter.println("<hr>"); 
			}
			if(count == 0 && (!clicked_button.equals("Modify"))) {
				displayReview(printWriter, hotelId);
			}			
			reviewResultSet.close();
			statementReview.close();			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param printWriter
	 * 			- PrintWriter
	 * @param hotelId
	 * 			- Hotel Id 
	 * 
	 * 			Modify and Delete buttons are formed. And if one of them is clicked, 
	 * 		with the hotel_id info, it is sent to the post method of reviewsservlet.
	 */
	private void modifyOrDeleteReview(PrintWriter printWriter, String hotelId) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Modify\"> &nbsp; &nbsp; &nbsp; &nbsp; <input type=\"submit\" name=\"button\" value=\"Delete\"> </p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");	
	}

	/**
	 * 
	 * @param printWriter
	 * 			- PrintWriter
	 * @param hotelId
	 * 			- Hotel Id
	 * 
	 * 			Display review information areas such as Review title, Review text, Rating
	 * 		and if it is clicked, information are sent to the post method of reviewsservlet. 
	 */
	private void displayReview(PrintWriter printWriter, String hotelId) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("Review title:  <input type=\"text\" name=\"user_review_title\" size=\"97\">  </br>");
		printWriter.println("Review text:   </br> <textarea name='user_review_text' id='comment' rows=\"5\" cols=\"100\"></textarea><br />");
		printWriter.println("Rating: <input type=\"radio\" name=\"user_star\" value=\"0\" />0");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"1\" />1");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"2\" />2");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"3\" />3");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"4\" />4");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"5\" />5 <br />");
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Submit\"></p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");
	}
	
	/**
	 * 
	 * @param printWriter
	 * 			- PrintWriter
	 * @param hotelId
	 * 			- Hotel Id
	 * @param review_title
	 * 			- Review Title
	 * @param review_text
	 * 			- Review Text
	 * @param rating
	 * 			- Rating
	 * 
	 * 			For the Modify, user's review information is filled.
	 */
	private void prepareReview(PrintWriter printWriter, String hotelId, String review_title, String review_text, String rating) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("Review title:  <input type=\"text\" name=\"user_review_title\" size=\"97\" value=\"" + review_title + "\">  </br>");
		printWriter.println("Review text:   </br> <textarea name='user_review_text' id='comment' rows=\"5\" cols=\"100\" >" + review_text + "</textarea><br />");
		if(rating.trim().equals("0")){
			printWriter.println("Rating: <input type=\"radio\" name=\"user_star\" value=\"0\" checked=\"checked\" />0");
		} else {
			printWriter.println("Rating: <input type=\"radio\" name=\"user_star\" value=\"0\" />0");
		}
		if(rating.trim().equals("1")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"1\" checked=\"checked\" />1");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"1\" />1");
		}
		if(rating.trim().equals("2")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"2\" checked=\"checked\" />2");		
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"2\" />2");		
		}
		if(rating.trim().equals("3")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"3\" checked=\"checked\" />3");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"3\" />3");
		}
		if(rating.trim().equals("4")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"4\" checked=\"checked\" />4");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"4\" />4");
		}
		if(rating.trim().equals("5")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"5\" checked=\"checked\" />5 <br />");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"5\" />5 <br />");
		}
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Submit\"></p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");
	}
}
