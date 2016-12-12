package cs601.servlets;

public class Hotelsinfodb{	
	private String hotel_id;
	private String hotel_name;
	private int total_count;
	private double average_rating;
	
	/**
	 * 
	 * @param hotel_id
	 * @param hotel_name
	 * @param total_count
	 * @param average_rating
	 * 
	 * 		To store Hotelsinfo context, this class is created
	 */
	public Hotelsinfodb(String hotel_id, String hotel_name, int total_count, double average_rating){
		this.hotel_id = hotel_id;
		this.hotel_name = hotel_name;
		this.total_count = total_count;
		this.average_rating = average_rating;		
	}


	public String getHotel_id() {
		return hotel_id;
	}


	public String getHotel_name() {
		return hotel_name;
	}


	public int getTotal_count() {
		return total_count;
	}


	public double getAverage_rating() {
		return average_rating;
	}	

}
