package cs601.servlets;

public class Myreviewsinfodb {
	private String hotelName;
	private String rating;
	private String review_title;
	private String review_text;
	private String username;
	private String hotel_id;

	/**
	 * 
	 * @param hotelName
	 * @param rating
	 * @param review_title
	 * @param review_text
	 * @param username
	 * @param hotel_id
	 * 
	 * 		To store Myreviewsinfo context, this class is created
	 */
	public Myreviewsinfodb(String hotelName, String rating, String review_title, 
			String review_text, String username, String hotel_id) {
		this.hotelName = hotelName;
		this.rating = rating;
		this.review_title = review_title;
		this.review_text = review_text;
		this.username = username;
		this.hotel_id = hotel_id;
	}

	public String getHotelName() {
		return hotelName;
	}

	public String getRating() {
		return rating;
	}

	public String getReview_title() {
		return review_title;
	}

	public String getReview_text() {
		return review_text;
	}
	
	public String getUsername() {
		return username;
	}

	public String getHotel_id() {
		return hotel_id;
	}	

}
