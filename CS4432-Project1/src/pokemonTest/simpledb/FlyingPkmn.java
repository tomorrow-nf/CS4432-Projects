package pokemonTest.simpledb;

import java.sql.*;
import simpledb.remote.SimpleDriver;

public class FlyingPkmn {
    public static void main(String[] args) {
		Connection conn = null;
		try {
			// Step 1: connect to database server
			Driver d = new SimpleDriver();
			conn = d.connect("jdbc:simpledb://localhost", null);

			// Step 2: execute the query
			// CS4432-Project1
			// Basic query to find Flying type Pokemon in the database
			// (Only checks Type Two)
			Statement stmt = conn.createStatement();
			String qry = "select SpecName "
			           + "from SPECIES "
			           + "where TypeTwo = 'Flying' ";
			ResultSet rs = stmt.executeQuery(qry);

			// Step 3: loop through the result set
			System.out.println("Flying Type Pokemon");
			while (rs.next()) {
				String sname = rs.getString("SpecName");
				System.out.println(sname);
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			// Step 4: close the connection
			try {
				if (conn != null)
					conn.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
