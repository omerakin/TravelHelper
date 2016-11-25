package cs601.servlets;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Handles all database-related actions. Uses singleton design pattern. Modified
 * by Prof. Karpenko from the original example of Prof. Engle.
 * 
 * @see MainServer
 */
public class DatabaseHandler {

	/** Makes sure only one database handler is instantiated. */
	private static DatabaseHandler singleton = new DatabaseHandler();

	/** Used to determine if login_users table exists. */
	private static final String TABLES_SQL = "SHOW TABLES LIKE 'login_users';";
	
	/** Used to determine if hotels table exists. */
	private static final String HOTELS_SQL = "SHOW TABLES LIKE 'hotels';";
	
	/** Used to determine if reviews table exists. */
	private static final String REVIEWS_SQL = "SHOW TABLES LIKE 'reviews';";

	/** Used to create login_users table for this example. */
	private static final String CREATE_SQL = "CREATE TABLE login_users ("
			+ "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " + "username VARCHAR(32) NOT NULL UNIQUE, "
			+ "password CHAR(64) NOT NULL, " + "usersalt CHAR(32) NOT NULL);";
	
	/** Used to create hotels table for this example. */
	private static final String CREATE_HOTELS_SQL = "CREATE TABLE hotels ("
			+ "hotel_id char(20) NOT NULL , hotel_name char(250) NULL , "
			+ "hotel_street_address char(100) NULL , hotel_city char(50) NULL , "
			+ "hotel_state char(50) NULL , hotel_country char(50) NULL , "
			+ "hotel_longitude double NULL , hotel_latitude double NULL , "
			+ "hotel_Rating double NULL );";
	
	/** Used to create reviews table for this example. */
	private static final String CREATE_REVIEWS_SQL = "CREATE TABLE reviews ("
			+ "hotel_id char(20) NOT NULL , review_id char(250) NOT NULL , "
			+ "review_title char(250) NULL , review_text TEXT NULL , " 
			+ "username char(50) NULL , isRecom BOOL NULL , "
			+ "rating double NULL , date TIMESTAMP NULL );";
	

	/** Used to insert a new user's info into the login_users table */
	private static final String REGISTER_SQL = "INSERT INTO login_users (username, password, usersalt) "
			+ "VALUES (?, ?, ?);";

	/** Used to determine if a username already exists. */
	private static final String USER_SQL = "SELECT username FROM login_users WHERE username = ?";
	
	/** Used to determine if a username and password exist. */
	private static final String LOGIN_SQL = "SELECT username, password, usersalt FROM login_users WHERE username = ?";
	

	// ------------------ constants below will be useful for the login operation
	// once you implement it
	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL = "SELECT usersalt FROM login_users WHERE username = ?";

	/** Used to authenticate a user. */
	private static final String AUTH_SQL = "SELECT username FROM login_users " + "WHERE username = ? AND password = ?";

	/** Used to remove a user from the database. */
	private static final String DELETE_SQL = "DELETE FROM login_users WHERE username = ?";
	
	/** Used to get information about hotels. */
	private static final String HOTELS_HOTELS_SQL = "SELECT hotel_id, hotel_name, hotel_street_address, hotel_city, hotel_state FROM hotels";
	
	/** Used to get rating from specific hotel_id. */
	private static final String HOTELS_REVIEWS_SQL = "SELECT rating FROM reviews WHERE hotel_id = ?";
	
	/** Used to get information about hotels. */
	private static final String HOTEL_HOTEL_SQL = "SELECT hotel_id, hotel_name, hotel_street_address, hotel_city, hotel_state FROM hotels WHERE hotel_id = ?";
	
	/** Used to get information about hotel for tourist attractions. */
	private static final String TOURIST_HOTEL_SQL = "SELECT hotel_id, hotel_city, hotel_longitude, hotel_latitude FROM hotels WHERE hotel_id = ?";
	
	/** Used to insert information to reviews. */
	private static final String INSERT_REVIEWS_SQL = "INSERT INTO reviews (hotel_id, review_id, review_title, review_text, username, isRecom, rating, date) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

	/** Used to delete information from reviews. */
	private static final String DELETE_REVIEWS_SQL = "DELETE FROM reviews WHERE hotel_id = ? AND username = ?";
	
	/** Used to get information about reviews. */
	private static final String REVIEWS_SQL_v1 = "SELECT review_title, review_text, username, rating, date FROM reviews WHERE hotel_id = ?";
	
	/** Used to get information about reviews. */
	private static final String REVIEWS_SQL_v2 = "SELECT review_title, review_text, username, rating FROM reviews WHERE hotel_id = ? AND username =?";
	
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * This class is a singleton, so the constructor is private. Other classes
	 * need to call getInstance()
	 */
	private DatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try {
			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		} catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		} catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
			System.out.println("Error while obtaining a connection to the database: " + status);
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static DatabaseHandler getInstance() {
		return singleton;
	}

	/**
	 * Checks to see if a String is null or empty.
	 * 
	 * @param text
	 *            - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to create
	 * it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupTables() {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(TABLES_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				} else {
					status = Status.OK;
				}
			} else {
				status = Status.OK;
			}
			if(!statement.executeQuery(HOTELS_SQL).next()){
				// Table missing, must create
				statement.executeUpdate(CREATE_HOTELS_SQL);
				// Check if create was successful
				if (!statement.executeQuery(HOTELS_SQL).next()) {
					status = Status.CREATE_FAILED;
				} else {
					status = Status.OK;
				}
			} else {
				status = Status.OK;
			}
			//statement.executeUpdate("DROP TABLE reviews;");
			if(!statement.executeQuery(REVIEWS_SQL).next()){
				// Table missing, must create
				statement.executeUpdate(CREATE_REVIEWS_SQL);
				// Check if create was successful
				if (!statement.executeQuery(REVIEWS_SQL).next()) {
					status = Status.CREATE_FAILED;
					System.out.println("buraaaa FAIL");
				} else {
					status = Status.OK;
					System.out.println("buraaaa OKAY");
				}
			} else {
				status = Status.OK;
			}
			
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(USER_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		} catch (SQLException e) {
			status = Status.SQL_EXCEPTION;
			System.out.println("Exception occured while processing SQL statement:" + e);
		}

		return status;
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes
	 *            - byte array to encode
	 * @param length
	 *            - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password
	 *            - password to hash
	 * @param salt
	 *            - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		} catch (Exception ex) {
			System.out.println("Unable to properly hash password." + ex);
		}

		return hashed;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		System.out.println("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			System.out.println("Invalid regiser info");
			return status;
		}
		
		// make sure password satisfies a set of reasonable requirements
		Pattern pattern = Pattern.compile("((?=.*[a-zA-Z0-9])(?=.*[@#$%!^:\\?\\+\\']).{6,25})");
		Matcher matcher = pattern.matcher(newpass);
		if(!matcher.matches()){
			status = Status.INVALID_PASSWORD;
			System.out.println("Invalid password");
			return status;
		}

		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if (status == Status.OK) {
				// generate salt
				byte[] saltBytes = new byte[16];
				random.nextBytes(saltBytes);

				String usersalt = encodeHex(saltBytes, 32); // hash salt
				String passhash = getHash(newpass, usersalt); // combine
																// password and
																// salt and hash
																// again

				// add user info to the database table
				try (PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);) {
					statement.setString(1, newuser);
					statement.setString(2, passhash);
					statement.setString(3, usersalt);
					statement.executeUpdate();

					status = Status.OK;
				}
			}
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.out.println("Error while connecting to the database: " + ex);
		}

		return status;
	}
	
	/**
	 * Login a user, checking the username, password exist in
	 * the database.
	 *
	 * @param user
	 *            - username of user
	 * @param pass
	 *            - password of user
	 * @return {@link Status.OK} if login successful
	 */
	public Status loginUser(String user, String pass) {
		Status status = Status.ERROR;
		System.out.println("Login " + user + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(user) || isBlank(pass)) {
			status = Status.INVALID_LOGIN;
			System.out.println("Invalid login info");
			return status;
		}
		
		// try to connect to database and check for username and password
		try (Connection connection = db.getConnection();) {
			ResultSet results = null;
			try (PreparedStatement statement = connection.prepareStatement(LOGIN_SQL);) {
				statement.setString(1, user);
				results = statement.executeQuery();
				// if okay so far, check username and password
				if (results.next()) {
					String databaseUserSalt = results.getString("usersalt"); // hash salt
					String passhash = getHash(pass, databaseUserSalt); // combine password(user entered) and salt(database) and hash again
					String databasepassword = results.getString("password");
					//check the hashed results are equal
					if(passhash.equals(databasepassword)){
						status = Status.OK;
					} else {
						status = Status.WRONG_PASSWORD;
						System.out.println("Wrong password is entered!");
					}
				} else {
					status = Status.INVALID_USER;
				}
			} catch (SQLException e) {
				status = Status.SQL_EXCEPTION;
				System.out.println("Exception occured while processing SQL statement:" + e);
			}			
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.out.println("Error while connecting to the database: " + ex);
		}

		return status;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		String salt = null;

		try (PreparedStatement statement = connection.prepareStatement(SALT_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}

		return salt;
	}
	

	/**
	 * 
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			Connect database and get all information about hotels such as hotel_name, 
	 * 			hotel_street_address, hotel_city, hotel_state
	 */
	public void listGeneralHotelsInfo(HttpServletResponse resp) throws FileNotFoundException, IOException {
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(HOTELS_HOTELS_SQL);) {
			ResultSet results = statement.executeQuery();				
			while (results.next()) {
				double averageRating = 0;
				int count = 0;
				try(PreparedStatement statementReview = connection.prepareStatement(HOTELS_REVIEWS_SQL);){
					statementReview.setString(1, results.getString("hotel_id"));
					ResultSet reviewResultSet = statementReview.executeQuery();
					while(reviewResultSet.next()){
						String rating = reviewResultSet.getString("rating");
						averageRating = averageRating + Double.parseDouble(rating);
						count++;
					}
					if (count != 0) { averageRating = (averageRating / count); }  
				}
				printWriter.println("<form action=\"/hotel\" method=\"get\">");
				printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + results.getString("hotel_id") + "\" />");
				printWriter.println("<p>" +  "Hotel Name : " +"<input type=\"submit\" value=\""+ results.getString("hotel_name")+ "\" "
						+ "style=\"background-color: Transparent; cursor:pointer; overflow: hidden; color: blue; border: none; text-decoration: underline;\">" + "</p>");
				printWriter.println("</form>");
				printWriter.println("<p>" + "Total Reviews : " + count + "</p>");
				printWriter.println("<p>" + "Rating : " + averageRating + "</p>");
				printWriter.println("<hr>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}			
	}
	
	/**
	 * 
	 * @param req 
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			Connect database and get searched hotel information such as hotel_name, 
	 * 			hotel_street_address, hotel_city, hotel_state
	 */
	public void listSearchedHotelsInfo(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(HOTELS_HOTELS_SQL);) {
			ResultSet results = statement.executeQuery();				
			while (results.next()) {
				String searchedHotel = req.getParameter("searchHotel").toLowerCase();
				if(results.getString("hotel_name").toLowerCase().contains(searchedHotel) || 
						searchedHotel.trim().equals("")){
					double averageRating = 0;
					int count = 0;
					try(PreparedStatement statementReview = connection.prepareStatement(HOTELS_REVIEWS_SQL);){
						statementReview.setString(1, results.getString("hotel_id"));
						ResultSet reviewResultSet = statementReview.executeQuery();
						while(reviewResultSet.next()){
							String rating = reviewResultSet.getString("rating");
							averageRating = averageRating + Double.parseDouble(rating);
							count++;
						}
						if (count != 0) { averageRating = (averageRating / count); }  
					}
					printWriter.println("<form action=\"/hotel\" method=\"get\">");
					printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + results.getString("hotel_id") + "\" />");
					printWriter.println("<p>" +  "Hotel Name : " +"<input type=\"submit\" value=\""+ results.getString("hotel_name")+ "\" "
							+ "style=\"background-color: Transparent; cursor:pointer; overflow: hidden; color: blue; border: none; text-decoration: underline;\">" + "</p>");
					printWriter.println("</form>");
					printWriter.println("<p>" + "Total Reviews : " + count + "</p>");
					printWriter.println("<p>" + "Rating : " + averageRating + "</p>");
					printWriter.println("<hr>");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * @param req 
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			Connect database and get specific information about hotel such as hotel_name, 
	 * 			hotel_street_address, hotel_city, hotel_state
	 */
	public void listHotelInfo(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		String hotelId = req.getParameter("hotelId");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(HOTEL_HOTEL_SQL);) {
			statement.setString(1, hotelId);
			ResultSet results = statement.executeQuery();				
			if (results.next()) {
				double averageRating = 0;
				int count = 0;
				try(PreparedStatement statementReview = connection.prepareStatement(HOTELS_REVIEWS_SQL);){
					statementReview.setString(1, results.getString("hotel_id"));
					ResultSet reviewResultSet = statementReview.executeQuery();
					while(reviewResultSet.next()){
						String rating = reviewResultSet.getString("rating");
						averageRating = averageRating + Double.parseDouble(rating);
						count++;
					}
					if (count != 0) { averageRating = (averageRating / count); }  
				}
				printWriter.println("<img src=\"WebContent/images/hotel_image.gif" + "\" />");
				printWriter.println("<p>" +  "Hotel Name : " + results.getString("hotel_name")+ "</p>");
				printWriter.println("<p>" + "Address : " + results.getString("hotel_street_address") + "</p>");
				printWriter.println("<p>" + "City : " + results.getString("hotel_city") + ", " + results.getString("hotel_state") + "</p>");
				printWriter.println("<p>" + "Rating : " + averageRating + "</p>");
				printWriter.println("<p> the link to expedia's page of this hotel </p>");				
				printWriter.println("<form action=\"/reviews\" method=\"get\">");
				printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + results.getString("hotel_id") + "\" />");
				printWriter.println("<p><input type=\"submit\" value=\"Reviews\" style=\"float:left;\"></p>");
				printWriter.println("</form>");
				printWriter.println("<form action=\"/touristattraction\" method=\"get\">");
				printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + results.getString("hotel_id") + "\" />");
				printWriter.println("<p><input type=\"submit\" value=\"Tourist Attraction\" style=\"float:left; margin-left: 25px;\"></p>");
				printWriter.println("</form>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}				
	}	


	/**
	 * 
	 * @return
	 * 		- Connect database and returns String, which is a partial query containing location information for this hotel
	 */
	public String generateQueries(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		DatabaseConnector db = new DatabaseConnector("database.properties");
		String hotelId = req.getParameter("hotelId");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		String query = "";
		try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(TOURIST_HOTEL_SQL);) {
			statement.setString(1, hotelId);
			ResultSet results = statement.executeQuery();				
			if (results.next()) {
				query = "tourist%20attractions+in+"
						+ results.getString("hotel_city").replaceAll(" ", "%20")
						+ "&location="
						+ results.getString("hotel_latitude") + ","
						+ results.getString("hotel_longitude");
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return query;
	}
	


	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @param clicked_button
	 * 			- which button is clicked checks
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			Connect database and get all review information for specific hotel such as username, 
	 * 		rating, review_title, review_text
	 * 			Compare username with user's username, if they are equal, 
	 * 		it allows user to edit his/her own review such as modify or delete 
	 */
	public void listReviewsInfo(HttpServletRequest req, HttpServletResponse resp, String clicked_button) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		PrintWriter printWriter = resp.getWriter();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		String hotelId = req.getParameter("hotelId");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		String username = (String) session.getAttribute("user");
		String sortBy = "";
		
		if(clicked_button.equals("By date (most recent ones on top)")) {
			sortBy = " ORDER BY date DESC";
		} else if(clicked_button.equals("By rating (highly rated on top)")) {
			sortBy = " ORDER BY rating DESC";
		}		
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(REVIEWS_SQL_v1 + sortBy);){
			statementReview.setString(1, hotelId);
			ResultSet reviewResultSet = statementReview.executeQuery();			
			int count = 0;
			while(reviewResultSet.next()){
				printWriter.println("<p>" + "Review by " + reviewResultSet.getString("username") + ": " + "</p>"); 
				printWriter.println("<p>" + "Rating : " + reviewResultSet.getString("rating") + "</p>"); 
				printWriter.println("<p>" + "Title : " + reviewResultSet.getString("review_title") + "</p>"); 
				printWriter.println("<p>" + "Text : " + reviewResultSet.getString("review_text") + "</p>"); 
				printWriter.println("<p>" + "Date : " + reviewResultSet.getString("date") + "</p>");
				if(username.equals(reviewResultSet.getString("username"))){	
					modifyOrDeleteReview(printWriter, hotelId);
					count++; 
				}
				printWriter.println("<hr>"); 
			}
			if(count == 0 && (!clicked_button.equals("Modify"))) {
				displayReview(printWriter, hotelId);
			}			
			reviewResultSet.close();
			statementReview.close();			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 				
	 * 			user's review information is uploaded to the reviews table
	 */
	public void insertReview(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		
		String hotelId = req.getParameter("hotelId");
		String username = (String) session.getAttribute("user");
		String user_review_title = req.getParameter("user_review_title");
		String user_review_text = req.getParameter("user_review_text");
		String star = req.getParameter("user_star");
		
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		user_review_title = StringEscapeUtils.escapeHtml4(user_review_title);
		user_review_text = StringEscapeUtils.escapeHtml4(user_review_text);
		double user_star = Double.parseDouble(star);
		String user_review_id = UUID.randomUUID().toString(); // generate review_id for new review
		
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(INSERT_REVIEWS_SQL);){
			statementReview.setString(1, hotelId);
			statementReview.setString(2, user_review_id);
			statementReview.setString(3, user_review_title);
			statementReview.setString(4, user_review_text);
			statementReview.setString(5, username);
			statementReview.setBoolean(6, true);
			statementReview.setDouble(7, user_star);
			statementReview.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
			statementReview.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			user's review information is deleted from the reviews table
	 */
	public void deleteReview(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		
		String hotelId = req.getParameter("hotelId");
		String username = (String) session.getAttribute("user");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		
		try(Connection connection = db.getConnection(); PreparedStatement statementReview = connection.prepareStatement(DELETE_REVIEWS_SQL);){
			statementReview.setString(1, hotelId);
			statementReview.setString(2, username);
			statementReview.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 
	 * @param req
	 * 			- HttpServletRequest
	 * @param resp
	 * 			- HttpServletResponse
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * 			user's review information is modified and again uploaded to the reviews table
	 */
	public void modifyReview(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		HttpSession session = req.getSession();
		DatabaseConnector db = new DatabaseConnector("database.properties");
		PrintWriter printWriter = resp.getWriter();
		
		String review_title = null;
		String review_text = null;
		String rating = null;
		String hotelId = req.getParameter("hotelId");
		String username = (String) session.getAttribute("user");
		hotelId = StringEscapeUtils.escapeHtml4(hotelId);
		
		//select the review
		try(Connection connection = db.getConnection(); PreparedStatement selectReview = connection.prepareStatement(REVIEWS_SQL_v2);){
			selectReview.setString(1, hotelId);
			selectReview.setString(2, username);
			ResultSet reviewResultSet = selectReview.executeQuery();			
			if(reviewResultSet.next()){
				review_title = reviewResultSet.getString("review_title");
				review_text = reviewResultSet.getString("review_text");
				rating = reviewResultSet.getString("rating");
			}
			//delete the review
			try(PreparedStatement deleteReview = connection.prepareStatement(DELETE_REVIEWS_SQL);){
				deleteReview.setString(1, hotelId);
				deleteReview.setString(2, username);
				deleteReview.executeUpdate();
			}
			//list Reviews without users review
			listReviewsInfo(req, resp, "Modify");
			//prepare the new review
			prepareReview(printWriter, hotelId, review_title, review_text, rating);			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param printWriter
	 * 			- PrintWriter
	 * @param hotelId
	 * 			- Hotel Id 
	 * 
	 * 			Modify and Delete buttons are formed. And if one of them is clicked, 
	 * 		with the hotel_id info, it is sent to the post method of reviewsservlet.
	 */
	private void modifyOrDeleteReview(PrintWriter printWriter, String hotelId) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Modify\"> &nbsp; &nbsp; &nbsp; &nbsp; <input type=\"submit\" name=\"button\" value=\"Delete\"> </p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");	
	}
	

	/**
	 * 
	 * @param printWriter
	 * 			- PrintWriter
	 * @param hotelId
	 * 			- Hotel Id
	 * 
	 * 			Display review information areas such as Review title, Review text, Rating
	 * 		and if it is clicked, information are sent to the post method of reviewsservlet. 
	 */
	private void displayReview(PrintWriter printWriter, String hotelId) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("Review title:  <input type=\"text\" name=\"user_review_title\" size=\"97\">  </br>");
		printWriter.println("Review text:   </br> <textarea name='user_review_text' id='comment' rows=\"5\" cols=\"100\"></textarea><br />");
		printWriter.println("Rating: <input type=\"radio\" name=\"user_star\" value=\"0\" />0");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"1\" />1");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"2\" />2");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"3\" />3");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"4\" />4");
		printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"5\" />5 <br />");
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Submit\"></p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");
	}	
	
	
	/**
	 * 
	 * @param printWriter
	 * 			- PrintWriter
	 * @param hotelId
	 * 			- Hotel Id
	 * @param review_title
	 * 			- Review Title
	 * @param review_text
	 * 			- Review Text
	 * @param rating
	 * 			- Rating
	 * 
	 * 			For the Modify, user's review information is filled.
	 */
	private void prepareReview(PrintWriter printWriter, String hotelId, String review_title, String review_text, String rating) {
		printWriter.println("<form action=\"/reviews\" method=\"post\">");
		printWriter.println("Review title:  <input type=\"text\" name=\"user_review_title\" size=\"97\" value=\"" + review_title + "\">  </br>");
		printWriter.println("Review text:   </br> <textarea name='user_review_text' id='comment' rows=\"5\" cols=\"100\" >" + review_text + "</textarea><br />");
		if(rating.trim().equals("0")){
			printWriter.println("Rating: <input type=\"radio\" name=\"user_star\" value=\"0\" checked=\"checked\" />0");
		} else {
			printWriter.println("Rating: <input type=\"radio\" name=\"user_star\" value=\"0\" />0");
		}
		if(rating.trim().equals("1")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"1\" checked=\"checked\" />1");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"1\" />1");
		}
		if(rating.trim().equals("2")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"2\" checked=\"checked\" />2");		
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"2\" />2");		
		}
		if(rating.trim().equals("3")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"3\" checked=\"checked\" />3");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"3\" />3");
		}
		if(rating.trim().equals("4")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"4\" checked=\"checked\" />4");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"4\" />4");
		}
		if(rating.trim().equals("5")){
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"5\" checked=\"checked\" />5 <br />");
		} else {
			printWriter.println("<input type=\"radio\" name=\"user_star\" value=\"5\" />5 <br />");
		}
		printWriter.println("<p><input type=\"submit\" name=\"button\" value=\"Submit\"></p>");
		printWriter.println("<input type=\"hidden\" name=\"hotelId\" value=\"" + hotelId + "\" />");
		printWriter.println("</form>");
	}
	
}
