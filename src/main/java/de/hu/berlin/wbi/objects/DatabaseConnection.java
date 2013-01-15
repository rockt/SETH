package de.hu.berlin.wbi.objects;
/**
	Copyright 2010, 2011 Philippe Thomas
	This file is part of snp-normalizer.

snp-normalizer is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

snp-normalizer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with snp-normalizer.  If not, see <http://www.gnu.org/licenses/>.
 */

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

	private String user;
	private String password;
	private String database;
	private String host;
	private String driver;
	private ResultSet rs;

	private Connection conn;
	Statement stmt;

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
		this.driver = property.getProperty("database.driver");
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
			e.printStackTrace();
			System.exit(1);
		}
		try {
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {			
			e.printStackTrace();
			System.exit(1);
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
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Connects with database
	 */
	public void connect() {
		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			System.err.println("Problem during Instantiation of mySQL");
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			System.err.println("Illegal Access");
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");
			e.printStackTrace();
			System.exit(1);
		}
		try {
			String connectionString = host +database;

			if(driver.contains("mysql"))
				connectionString+="?characterEncoding=UTF8";

			conn =DriverManager.getConnection(connectionString, user, password);
		} catch (SQLException e) {
			System.err.println("SQL-Exception");
			e.printStackTrace();
			System.exit(1);
		}		
	}

	/**
	 * Disconnects from database
	 */
	public void disconnect() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public ResultSet getRs() {
		return rs;
	}

	public Connection getConn() {
		return conn;
	}
}
