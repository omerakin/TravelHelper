import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import cs601.hotelapp.Hotel;
import cs601.hotelapp.ThreadSafeHotelData;

@SuppressWarnings("serial")
public class HotelsServlet extends BaseServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//resp.setContentType("text/plain");
		prepareResponse("Hotels", resp);
		 
		PrintWriter printWriter = resp.getWriter();
		ThreadSafeHotelData tsData = (ThreadSafeHotelData) getServletContext().getAttribute("tsData");
		tsData.listGeneralHotelsInfo(printWriter);
		printWriter.println("</body>");
		printWriter.println("</html>");
		printWriter.flush();
		
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	
}
