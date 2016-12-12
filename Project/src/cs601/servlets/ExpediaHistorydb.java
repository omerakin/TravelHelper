package cs601.servlets;

public class ExpediaHistorydb {
	private String expedia;
	private String hotel_id;
	
	/**
	 * 
	 * @param expedia
	 * @param hotel_id
	 * 
	 * 		To store ExpediaHistory context, this class is created
	 */
	public ExpediaHistorydb(String expedia, String hotel_id) {
		this.expedia = expedia;
		this.hotel_id = hotel_id;
	}

	public String getExpedia() {
		return expedia;
	}

	public String getHotel_id() {
		return hotel_id;
	}

}
