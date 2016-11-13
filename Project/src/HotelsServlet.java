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
		resp.setContentType("application/json");
		
		
		//prepareResponse("Login", resp);
		
		PrintWriter printWriter = resp.getWriter(); 
		System.out.println("buraaaa 11111");
		ThreadSafeHotelData tsData = (ThreadSafeHotelData) getServletContext().getAttribute("tsData");
		System.out.println("buraaaa 2222222");
		Hotel hotel = tsData.containsHotelKeyForHttpServer("10323");
		System.out.println("buraaaa 33333");
		if(hotel != null) {
			System.out.println("buraaaa 444444");
			// Json File
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("success", true);
			jsonObject.put("hotelId", "10323");
			jsonObject.put("name", hotel.getHotel_name());
			jsonObject.put("addr", hotel.getAddress().getStreet_address());
			jsonObject.put("city", hotel.getAddress().getCity());
			jsonObject.put("state", hotel.getAddress().getState());
			jsonObject.put("lat", hotel.getAddress().getLongitude());
			jsonObject.put("lng", hotel.getAddress().getLatitude());
			jsonObject.put("country", "USA");
			printWriter.print(jsonObject);
		} else {
			System.out.println("buraaaa 44444");
			JSONObject jsonObjectNotExist = new JSONObject();
			jsonObjectNotExist.put("success", false);
			jsonObjectNotExist.put("hotelId", "invalid");
			jsonObjectNotExist.writeJSONString(printWriter);					
		}
		System.out.println("buraaaa 55555");
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	
}
