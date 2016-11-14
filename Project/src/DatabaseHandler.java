

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			+ "rating double NULL );";
	

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
					System.out.println("buraaaa 222222");
				} else {
					status = Status.OK;
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
		Pattern pattern = Pattern.compile("((?=.*[a-zA-Z0-9])(?=.*[@#$%!^:\\?\\+\\']).{6,20})");
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
}
