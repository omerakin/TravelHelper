import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.hotelapp.ThreadSafeHotelData;

@SuppressWarnings("serial")
public class ReviewsServlet extends BaseServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		prepareResponse("Reviews", resp);
		
		PrintWriter printWriter = resp.getWriter();
		String hotelId = req.getParameter("hotelId");
		ThreadSafeHotelData tsData = (ThreadSafeHotelData) getServletContext().getAttribute("tsData");
		tsData.listReviewsInfo(printWriter, hotelId);		
		printWriter.println("</body>");
		printWriter.println("</html>");
		printWriter.flush();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	
	

}
