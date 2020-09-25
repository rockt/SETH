package de.hu.berlin.wbi.objects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Convenience class for database communication
 * @author Philippe Thomas
 *
 */
public class DatabaseConnection {

	private final String user;
	private final String password;
	private final String database;
	private final String host;
	private ResultSet rs;

	protected Connection conn;
    protected Statement stmt;

    /**
     * Set up a database connection by providing all relevant connection information
     * @param user Username
     * @param password  Userpassword
     * @param database database name
     * @param host hostname
     */
    public DatabaseConnection(String user, String password, String database, String host) {
        this.user = user;
        this.password = password;
        this.database = database;
        this.host = host;
    }

    /**
	 * Set up a database connection, by using a java-property object
	 * @param property -- contains database properties
	 */
	public DatabaseConnection(Properties property) {
		super();
		this.user = property.getProperty("database.user");
		this.password = property.getProperty("database.password");
		this.database = property.getProperty("database.name");
		this.host = property.getProperty("database.host");
	}

    /**
	 * Wrapper for querying the database
	 * @param query      Query String
	 */
	public void query(String query){
		stmt = null;
		rs= null;

		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
            throw new IllegalArgumentException("Cannot create statement for query: '" + query + "'", e);
		}
		try {
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
            throw new IllegalArgumentException("Cannot execute query: '" + query + "'", e);
		}
	}

	/**
	 * Close resultset
	 */
	public void closeQuery(){
		try {
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			stmt.close();
		} catch (SQLException e) {
            throw new IllegalStateException(e);
		}
	}

	/**
	 * Connects with database
	 */
	public void connect() {

		// register JDBC driver, optional, since java 1.6
		/**

		try {
			Class.forName(driver).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException("Cannot instantiate instance of driver: '" + this.driver +"'", e);
        }
		 */

        String connectionString = host +database;
        //if(driver.contains("mysql"))
        //    connectionString+="?connectTimeout=30000&amp;useUnicode=true&amp;characterEncoding=UTF-8&amp;autoReconnect=true&amp;failOverReadOnly=false&amp;maxReconnects=10";
        //connectionString+="?characterEncoding=UTF8";

        try {
            conn = DriverManager.getConnection(connectionString, user, password);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

	/**
	 * Disconnects from database
	 */
	public void disconnect() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

	public ResultSet getRs() {
		return rs;
	}

	public Connection getConn() {
		return conn;
	}

    /**
     * Returns a string representation of the object
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "DatabaseConnection{" +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
