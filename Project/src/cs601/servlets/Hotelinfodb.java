package cs601.servlets;

public class Hotelinfodb {	
	private String hotel_name;
	private String hotel_street_address;
	private String hotel_city;
	private String hotel_state;
	private double averageRating;
	private String expedia;
	private String hotel_id;	
	
	public Hotelinfodb(String hotel_name, String hotel_street_address, String hotel_city, String hotel_state,
			double averageRating, String expedia, String hotel_id) {		
		this.hotel_name = hotel_name;
		this.hotel_street_address = hotel_street_address;
		this.hotel_city = hotel_city;
		this.hotel_state = hotel_state;
		this.averageRating = averageRating;
		this.expedia = expedia;
		this.hotel_id = hotel_id;		
	}

	public String getHotel_name() {
		return hotel_name;
	}

	public String getHotel_street_address() {
		return hotel_street_address;
	}

	public String getHotel_city() {
		return hotel_city;
	}
	
	public String getHotel_state() {
		return hotel_state;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public String getExpedia() {
		return expedia;
	}

	public String getHotel_id() {
		return hotel_id;
	}

}
