import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.hotelapp.ThreadSafeHotelData;

@SuppressWarnings("serial")
public class ReviewsServlet extends BaseServlet{
	private static final String REVIEWS_SQL = "SELECT review_title, review_text, username, rating FROM reviews WHERE hotel_id = ?";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		prepareResponse("Reviews", resp);
		String hotelId = req.getParameter("hotelId");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		listReviewsInfo(resp, hotelId);
		endingResponse(resp);
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	
	private void listReviewsInfo(HttpServletResponse resp, String hotelId) throws FileNotFoundException, IOException {
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(REVIEWS_SQL);){
			statementReview.setString(1, hotelId);
			ResultSet reviewResultSet = statementReview.executeQuery();
			while(reviewResultSet.next()){
				printWriter.println("<p>" + "Review by " + reviewResultSet.getString("username") + ": " + "</p>"); 
				printWriter.println("<p>" + "Rating : " + reviewResultSet.getString("rating") + "</p>"); 
				printWriter.println("<p>" + "Title : " + reviewResultSet.getString("review_title") + "</p>"); 
				printWriter.println("<p>" + "Text : " + reviewResultSet.getString("review_text") + "</p>"); 
				printWriter.println("<p>" + "---------------------------------------------------------" + "</p>"); 
			}  
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
