package cs601.servlets;

public class TouristAttractionInfodb {
	private String name;
	private String address;
	private double rating;
	
	public TouristAttractionInfodb(String name, String address, double rating2) {
		this.name = name;
		this.address = address;
		this.rating = rating2;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public double getRating() {
		return rating;
	}

}