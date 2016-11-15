import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cs601.hotelapp.ThreadSafeHotelData;

@SuppressWarnings("serial")
public class HotelsServlet extends BaseServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		prepareResponse("Hotels", resp);
		 
		PrintWriter printWriter = resp.getWriter();
		ThreadSafeHotelData tsData = (ThreadSafeHotelData) getServletContext().getAttribute("tsData");
		DatabaseConnector db = new DatabaseConnector("database.properties");
		try (Connection connection = db.getConnection();) {
			tsData.listGeneralHotelsInfo(printWriter, connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		printWriter.println("</body>");
		printWriter.println("</html>");
		printWriter.flush();
		
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	
}
