package cs601.servlets;

public class Reviewsinfodb {
	private String username;
	private String rating;
	private String review_title;
	private String review_text;
	private String date;
	private String review_id;
	private int countLike;
	private int usersLike;

	public Reviewsinfodb(String username, String rating, String review_title, String review_text, 
			String date, String review_id, int countLike, int usersLike) {
		this.username = username;
		this.rating = rating;
		this.review_title = review_title;
		this.review_text = review_text;
		this.date = date;
		this.review_id = review_id;
		this.countLike = countLike;
	}

	public String getUsername() {
		return username;
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
	
	public String getDate() {
		return date;
	}
	
	public String getReview_id() {
		return review_id;
	}

	public int getCountLike() {
		return countLike;
	}

	public void setCountLike(int countLike) {
		this.countLike = countLike;
	}
	
	public int getUsersLike() {
		return usersLike;
	}

	public void setUsersLike(int usersLike) {
		this.usersLike = usersLike;
	}

}
