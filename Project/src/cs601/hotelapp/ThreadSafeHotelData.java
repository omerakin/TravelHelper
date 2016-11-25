package cs601.hotelapp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cs601.concurrent.ReentrantReadWriteLock;

/**
 * Class HotelData - a data structure that stores information about hotels and
 * hotel reviews. Allows to quickly lookup a hotel given the hotel id. 
 * Allows to easily find hotel reviews for a given hotel, given the hotelID. 
 * Reviews for a given hotel id are sorted by the date and user nickname.
 *
 */
public class ThreadSafeHotelData {

	// FILL IN CODE - declare data structures to store hotel data
	private final Map<String, Hotel> hotelsGivenByHotelId;
	private final Map<String, TreeSet<Review>> reviewsGivenByHotelId;
	private Hotel hotel;
	private Address address;
	private Review reviews;
	private Boolean isSuccessful;
	
	//Created ReentrantReadWriteLock lock object
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	

	/**
	 * Default constructor.
	 */
	public ThreadSafeHotelData() {
		// FILL IN CODE
		// Initialise all data structures
		hotelsGivenByHotelId = new HashMap<String,Hotel>();
		reviewsGivenByHotelId = new HashMap<String,TreeSet<Review>>();

	}

	/**
	 * Create a Hotel given the parameters, and add it to the appropriate data
	 * structure(s).
	 * 
	 * @param hotelId
	 *            - the id of the hotel
	 * @param hotelName
	 *            - the name of the hotel
	 * @param city
	 *            - the city where the hotel is located
	 * @param state
	 *            - the state where the hotel is located.
	 * @param streetAddress
	 *            - the building number and the street
	 * @param latitude
	 * @param longitude
	 */
	public void addHotel(String hotelId, String hotelName, String city, String state, String country ,String streetAddress, double lat,
			double lon) {
		// FILL IN CODE
		/*
		 * Here, I locked address and hotel variable, is that okey ?
		 */
		
		//With the lockWrite it is added to our HashMap safely
		lock.lockWrite();
		try {
			//Set the values to the address and hotel object.
			address = new Address(streetAddress, city, state, country, lat, lon);
			hotel = new Hotel(hotelId, hotelName, address);		
			//Add to the hotelsGivenByHotelId TreeMap.
			hotelsGivenByHotelId.put(hotelId, hotel);
		} finally {
			lock.unlockWrite();
		}
		
	}

	/**
	 * Add a new review.
	 * 
	 * @param hotelId
	 *            - the id of the hotel reviewed
	 * @param reviewId
	 *            - the id of the review
	 * @param rating
	 *            - integer rating 1-5.
	 * @param reviewTitle
	 *            - the title of the review
	 * @param review
	 *            - text of the review
	 * @param isRecommended
	 *            - whether the user recommends it or not
	 * @param date
	 *            - date of the review in the format yyyy-MM-dd, e.g.
	 *            2016-08-29.
	 * @param username
	 *            - the nickname of the user writing the review.
	 * @return true if successful, false if unsuccessful because of invalid date
	 *         or rating. Needs to catch and handle ParseException if the date is invalid.
	 *         Needs to check whether the rating is in the correct range
	 */
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
			boolean isRecom, String date, String username) {
		
		/*
		 * Here, I have try inside of try, so is it okey ?
		 */
		
		//With the lockWrite it is added to our HashMap safely
		lock.lockWrite();
		try {
			// FILL IN CODE
			//Initialise it to default value.
			isSuccessful = false;
			//Check the rating is in the correct range or not.
			if(1> rating || 5 < rating) {
				// set the false.
				isSuccessful = false;
			} else {
				date = date.replace("T", " ");
				date = date.replace("Z", "");
				//Check the date is correct format or not.
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
				try {
					Date date1 = sdf.parse(date);
					// set the true.
					isSuccessful = true;
				} catch (ParseException e) {
					//e.printStackTrace();
					System.out.println("Date is invalid!");
				}
			}
			// If successful is true add it.
			//if(isSuccessful && hotelsGivenByHotelId.containsKey(hotelId)) {
			if(isSuccessful) {
				System.out.println(date);
				//Set the values to the reviews object.
				reviews = new Review(reviewId, hotelId, reviewTitle, review, username, isRecom, date, rating);			
				//Check that if hotel id already exist or not
				if(!(reviewsGivenByHotelId.containsKey(hotelId))) {
					TreeSet<Review> newReviewSet = new TreeSet<Review>();
					newReviewSet.add(reviews);
					//Add to the reviewsGivenByHotelId TreeMap.
					reviewsGivenByHotelId.put(hotelId, newReviewSet);
				} else {
					TreeSet<Review> existingReviewSet;
					existingReviewSet = reviewsGivenByHotelId.get(hotelId);			
					existingReviewSet.add(reviews);	
					//Add to the reviewsGivenByHotelId TreeMap.
					reviewsGivenByHotelId.put(hotelId, existingReviewSet);
				}
			}
			return isSuccessful; // don't forget to change it
			
		} finally {
			lock.unlockWrite();
		}
		
	}

	/**
	 * Return an alphabetized list of the ids of all hotels
	 * 
	 * @return
	 */
	public List<String> getHotels() {
		// FILL IN CODE
		
		//With the lockRead it is read from HashMap and written the local variable hotelIdList ArrayList
		lock.lockRead();
		try {
			//Initialise an ArrayList to store hotelIds
			List<String> hotelIdList = new ArrayList<>();
			//Add hotelId to ArrayList
			for (String hotelId: hotelsGivenByHotelId.keySet()){
				hotelIdList.add(hotelId);
			}
			//Sort hotelIds
			Collections.sort(hotelIdList);
			//return it.
			return hotelIdList; // don't forget to change it
		} finally {
			lock.unlockRead();
		}
		
	}

	/**
	 * Returns a string representing information about the hotel with the given
	 * id, including all the reviews for this hotel separated by
	 * -------------------- Format of the string: HoteName: hotelId
	 * streetAddress city, state -------------------- Review by username: rating
	 * ReviewTitle ReviewText -------------------- Review by username: rating
	 * ReviewTitle ReviewText ...
	 * 
	 * @param hotel
	 *            id
	 * @return - output string.
	 */
	public String toString(String hotelId) {
		// FILL IN CODE
		
		//With the lockRead it is written to the local variable result
		lock.lockRead();
		try {
			StringBuffer stringBuffer = new StringBuffer();
			for (String hotel_id_hotels: hotelsGivenByHotelId.keySet()){
				if(hotel_id_hotels.equals(hotelId)){
					stringBuffer.append(hotelsGivenByHotelId.get(hotel_id_hotels).getHotel_name() + ": "
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getHotel_id() + "\n"
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getAddress().getStreet_address() + "\n"
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getAddress().getCity() + ", "
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getAddress().getState() + "\n");		
				}
			}
			for (String hotel_id_review: reviewsGivenByHotelId.keySet()){
				if(hotel_id_review.equals(hotelId)){
					for(Review hotelIdReview : reviewsGivenByHotelId.get(hotel_id_review)){
						stringBuffer.append("--------------------\n"
								+ "Review by " + hotelIdReview.getUsername() + ": "
								+ hotelIdReview.getRating() + "\n"
								+ hotelIdReview.getReview_title() + "\n"
								+ hotelIdReview.getReview_text() + "\n");
					}
				}
			}
			return stringBuffer.toString(); // don't forget to change to the correct string
		} finally {
			lock.unlockRead();
		}
		
	}

	/**
	 * Save the string representation of the hotel data to the file specified by
	 * filename in the following format: 
	 * an empty line 
	 * A line of 20 asterisks ******************** on the next line 
	 * information for each hotel, printed in the format described in the toString method of this class.
	 * 
	 * @param filename
	 *            - Path specifying where to save the output.
	 */
	public void printToFile(Path filename) {
		// FILL IN CODE

		lock.lockRead();
		try {
			try {
				PrintWriter printWriter = new PrintWriter(new FileWriter(filename.toString()));
				for(String hotelid_info: getHotels()){
					printWriter.println("\n********************");
					printWriter.print(toString(hotelid_info));
				}
				printWriter.flush();
				printWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			lock.unlockRead();
		}
	}

	/**
	 * 
	 * @param localtshData
	 * 			- Second ThreadSafeHotelData will be merged with main ThreadSafeHotelData
	 */
	public void merge(ThreadSafeHotelData localtshData) {
		// I locked with lockWrite. Otherwise, deadlock occurs.
		lock.lockWrite();
		try {
			for (String hotel_id_review: localtshData.reviewsGivenByHotelId.keySet()){
				if(hotelsGivenByHotelId.containsKey(hotel_id_review)){
					reviewsGivenByHotelId.put(hotel_id_review, localtshData.reviewsGivenByHotelId.get(hotel_id_review));
				}
			}
		} finally {
			lock.unlockWrite();
		}
	}

	/**
	 * 
	 * @param string - Hotel Id
	 * @return
	 * 		- If HotelId exist, it returns Hotel. if not, null.
	 */
	public Hotel containsHotelKeyForHttpServer(String string) {
		lock.lockRead();
		try {
			return hotelsGivenByHotelId.get(string);
		} finally {
			lock.unlockRead();
		}		
	}

	/**
	 * 
	 * @param string - Hotel Id
	 * @return
	 * 		- the number of reviews belong to HotelId.
	 */
	public int maxNumberOfReviewsForHttpServer(String string) {
		lock.lockRead();
		try {
			return reviewsGivenByHotelId.size();
		} finally {
			lock.unlockRead();
		}
	}

	/**
	 * 
	 * @param string - Hotel Id
	 * @param num - number of Reviews that will be returned.
	 * @return
	 * 		- JsonObject that contains reviews inside it.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getJSONReviewsForHttpServer(String string, int num) {
		lock.lockRead();
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("success", true);
			jsonObject.put("hotelId", string);										
			JSONArray jsonArray = new JSONArray();
			int i=0;
			if (num == 0) { return jsonObject; }
			for(Review review: reviewsGivenByHotelId.get(string)){				
				JSONObject jsonObjectInArray = new JSONObject();
				jsonObjectInArray.put("reviewId", review.getReview_id());
				jsonObjectInArray.put("title", review.getReview_title());
				jsonObjectInArray.put("user", review.getUsername());
				jsonObjectInArray.put("reviewText",  review.getReview_text());
				jsonObjectInArray.put("date", review.getDate());
				jsonArray.add(jsonObjectInArray);
				i++;
				if(i>=num){
					jsonObject.put("reviews", jsonArray);
					return jsonObject;
				}
			}
			return null;
		} finally {
			lock.unlockRead();
		}
	}

	public void listReviewsInfo(PrintWriter printWriter, Connection connection) {
		lock.lockRead();
		try {
			int count = 0;
			
			for (String hotel_id_review: reviewsGivenByHotelId.keySet()){
				for(Review hotelIdReview : reviewsGivenByHotelId.get(hotel_id_review)){
					
					printWriter.println("<p>" + "hotel_id : " + hotel_id_review + "</p>");
					printWriter.println("<p>" + "review_id : " + hotelIdReview.getReview_id() + "</p>"); 
					printWriter.println("<p>" + "Review_title : " + hotelIdReview.getReview_title() + "</p>");
					printWriter.println("<p>" + "Review_text : " + hotelIdReview.getReview_text() + "</p>");
					printWriter.println("<p>" + "Username : " + hotelIdReview.getUsername() + "</p>"); 
					printWriter.println("<p>" + "IsRecom : " + hotelIdReview.getIsRecom() + "</p>"); 
					printWriter.println("<p>" + "Rating : " + hotelIdReview.getRating() + "</p>"); 
					printWriter.println("<p>" + "Date : " + hotelIdReview.getDate() + "</p>");
					printWriter.println("<p>" + "--------------------" + "</p>");
					/*
					try (PreparedStatement statement = connection.prepareStatement("INSERT INTO reviews(hotel_id, review_id, review_title, review_text, username, isRecom, rating, date)"
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?);");) {
						statement.setString(1, hotel_id_review);
						statement.setString(2, hotelIdReview.getReview_id());
						statement.setString(3, hotelIdReview.getReview_title());
						statement.setString(4, hotelIdReview.getReview_text());
						statement.setString(5, hotelIdReview.getUsername());
						statement.setBoolean(6, hotelIdReview.getIsRecom());
						statement.setDouble(7, hotelIdReview.getRating());
						String stringDate = hotelIdReview.getDate();
						SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
						Date date1 = null;
						try {
							date1 = sdf.parse(stringDate);
							// set the true.
							isSuccessful = true;
						} catch (ParseException e) {
							//e.printStackTrace();
							System.out.println("Date is invalid!");
						}
						statement.setTimestamp(8, new java.sql.Timestamp(date1.getTime()));
						System.out.println(new java.sql.Timestamp(date1.getTime()).toString());
						statement.executeUpdate();
						count++;
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
				}
			}
			System.out.println("inserted : " + count);		
			
		} finally {
			lock.unlockRead();
		}	
		
	}
}
