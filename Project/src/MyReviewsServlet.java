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
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class MyReviewsServlet extends BaseServlet {
	private static final String MYREVIEWS_SQL = "SELECT hotel_id, review_title, review_text, username, rating FROM reviews WHERE username = ?";
	private static final String HOTELS_SQL = "SELECT hotel_name FROM hotels WHERE hotel_id = ?";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponse("My Reviews", resp);
		displayLogOut(resp);
		listMyReviewsInfo(req, resp, "Get");
		endingResponse(resp);
	}

	private void listMyReviewsInfo(HttpServletRequest req, HttpServletResponse resp, String clicked_button) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		String username = (String) session.getAttribute("user");
		String hotelName = "";
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(MYREVIEWS_SQL);){
			statementReview.setString(1, username);
			ResultSet reviewResultSet = statementReview.executeQuery();			
			int count = 0;
			while(reviewResultSet.next()){
				try(PreparedStatement statementHotel = connection.prepareStatement(HOTELS_SQL);){
					statementHotel.setString(1, reviewResultSet.getString("hotel_id"));
					ResultSet hotelResultSet = statementHotel.executeQuery();
					if(hotelResultSet.next()){
						hotelName = hotelResultSet.getString("hotel_name");
					}
				}
				printWriter.println("<p>" + "Hotel Name : " + hotelName + "</p>"); 
				printWriter.println("<p>" + "Rating : " + reviewResultSet.getString("rating") + "</p>"); 
				printWriter.println("<p>" + "Title : " + reviewResultSet.getString("review_title") + "</p>"); 
				printWriter.println("<p>" + "Text : " + reviewResultSet.getString("review_text") + "</p>"); 
				if(username.equals(reviewResultSet.getString("username"))){	
					modifyOrDeleteReview(printWriter, reviewResultSet.getString("hotel_id"));
					count++; 
				}
				printWriter.println("<p>" + "---------------------------------------------------------" + "</p>"); 
			}
			/*
			if(count == 0 && (!clicked_button.equals("Modify"))) {
				displayReview(printWriter, reviewResultSet.getString("hotel_id"));
			}
			*/			
			reviewResultSet.close();
			statementReview.close();			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	private void modifyOrDeleteReview(PrintWriter printWriter, String hotelId) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Modify\"> &nbsp; &nbsp; &nbsp; &nbsp; <input type=\"submit\" name=\"button\" value=\"Delete\"> </p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");	
	}
	

}
