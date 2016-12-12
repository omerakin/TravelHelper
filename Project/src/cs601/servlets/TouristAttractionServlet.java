package cs601.servlets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class TouristAttractionServlet extends BaseServlet {

	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
		
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		checkUserSession(req, resp);
		prepareResponseHtml(resp);
		VelocityContext context = getContext("Tourist Attraction");
		Template template = getTemplate(req, "TouristAttractionInfo.html");
		listAttractionsInfo(req, context);
		mergeAndPrintResponse(resp, template, context);		
	}
	
	/**
	 * 
	 * @param req 
	 * 			- HttpServletRequest
	 * @param context 
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			Get Tourist Attraction information for hotel such as Attraction name, 
	 * 			address, rate
	 */
	private void listAttractionsInfo(HttpServletRequest req, VelocityContext context) throws FileNotFoundException, IOException {
		Vector<TouristAttractionInfodb> touristAttraction = new Vector<>();
		SSLSocketFactory sslSocketFactory = null;
		SSLSocket sslSocket = null;
		BufferedReader bufferedReader = null;
		PrintWriter printWriter = null;		
		String host = "maps.googleapis.com";
		String request = "";
		String jsonObjectString = "";
		int radiusInMiles = 2;
		
		String hotelId = req.getParameter("hotelId");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);		
		String query = dbhandler.generateQueries(req);
		sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			// HTTPS uses port 443
			sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, 443);
			// output stream for the secure socket
			printWriter = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
			request = getRequest(host,query,radiusInMiles);
			
			//send a request to the server
			printWriter.println(request);
			printWriter.flush();
			
			// input stream for the secure socket.
			bufferedReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
			String str;
			StringBuffer stringBuffer = new StringBuffer();
			while((str = bufferedReader.readLine()) != null ) {
				stringBuffer.append(str);
			}
			jsonObjectString = stringBuffer.toString();
			
			//remove headers and get jsonObject
			Pattern p = Pattern.compile("Connection: close(.*)");
			Matcher matcher = p.matcher(jsonObjectString);
			if(matcher.find()){
				jsonObjectString = matcher.group(1);
				//System.out.println(jsonObjectString);	
			}
			
			JSONParser jsonParser = new JSONParser();
			try {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonObjectString);
				JSONArray jsonArray = (JSONArray) jsonObject.get("results");
				JSONObject jsonObjectAttraction;
				
				for(int i=0; i<jsonArray.size();i++) {
					jsonObjectAttraction = (JSONObject) jsonArray.get(i);
					
					String name = (String) jsonObjectAttraction.get("name");
					String address = (String) jsonObjectAttraction.get("formatted_address");
					double rating = 0;
					if(jsonObjectAttraction.get("rating") != null){
						rating = ((Number)jsonObjectAttraction.get("rating")).doubleValue();
					}
					TouristAttractionInfodb touristAttractionInfodb = new TouristAttractionInfodb(name, address, rating);
					touristAttraction.addElement(touristAttractionInfodb);
				}
				context.put("touristAttraction", touristAttraction);
			} catch (ParseException e) {
				e.printStackTrace();
			}			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				printWriter.close();
				bufferedReader.close();
				sslSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}	

	/**
	 * 
	 * @param host
	 * 			- "maps.googleapis.com"
	 * @param hotelLocationInfo
	 * 			- represent location infos in string example = "tourist%20attractions+in+Emeryville&location=37.837773,-122.298142"
	 * @param radiusInMiles
	 * 			- near distance amount 
	 * @return
	 * 			- returns string represents Get request 
	 */
	private String getRequest(String host, String query, int radiusInMiles) {
		String result;
		String key = "AIzaSyCvBVHwB8nRJDMKHI1WxkNR0kZMhnI9_oU";
		//String key = "AIzaSyDhsmtS2ZuTEg3scxv2ZsipglHNBgw3vB4";
		int radiusInMeters;
		
		radiusInMeters = radiusInMiles * 1609;		
		result = "GET /maps/api/place/textsearch/json?query=" + query 
					+ "&radius=" + radiusInMeters + "&key=" + key + " HTTP/1.1" + System.lineSeparator() // GET request
					+ "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
					+ "Connection: close" + System.lineSeparator() // make sure the server closes the connection after we fetch one page
					+ System.lineSeparator();
		return result;
	}
	
}
