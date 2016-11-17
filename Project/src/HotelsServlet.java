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

@SuppressWarnings("serial")
public class HotelsServlet extends BaseServlet {
	private static final String HOTELS_SQL = "SELECT hotel_id, hotel_name, hotel_street_address, hotel_city, hotel_state FROM hotels";
	private static final String REVIEWS_SQL = "SELECT rating FROM reviews WHERE hotel_id = ?";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
				
		prepareResponse("Hotels", resp);
		listGeneralHotelsInfo(resp);
		endingResponse(resp);
		
	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}

	private void listGeneralHotelsInfo(HttpServletResponse resp) throws FileNotFoundException, IOException {
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(HOTELS_SQL);) {
			ResultSet results = statement.executeQuery();				
			while (results.next()) {
				double averageRating = 0;
				int count = 0;
				try(PreparedStatement statementReview = connection.prepareStatement(REVIEWS_SQL);){
					statementReview.setString(1, results.getString("hotel_id"));
					ResultSet reviewResultSet = statementReview.executeQuery();
					while(reviewResultSet.next()){
						String rating = reviewResultSet.getString("rating");
						averageRating = averageRating + Double.parseDouble(rating);
						count++;
					}
					if (count != 0) { averageRating = (averageRating / count); }  
				}
				printWriter.println("<p>" +  "Hotel Name : " + results.getString("hotel_name") + "</p>");
				printWriter.println("<p>" + "Address : " + results.getString("hotel_street_address") + "</p>");
				printWriter.println("<p>" + "City : " + results.getString("hotel_city") + ", "
									+ results.getString("hotel_state") + "</p>");
				printWriter.println("<p>" + "Rating : " + averageRating + "</p>");
				printWriter.println("<form action=\"/reviews\" method=\"get\">");
				printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + results.getString("hotel_id") + "\" />");
				printWriter.println("<p><input type=\"submit\" value=\"Reviews\"></p>");
				printWriter.println("</form>");
				printWriter.println("<p>" + "----------------------------------------------------" + "</p>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}			
	}
}
