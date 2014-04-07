package pokemonTest.simpledb;

import java.sql.*;
import simpledb.remote.SimpleDriver;

public class NonAtkingMoves {
    public static void main(String[] args) {
		Connection conn = null;
		try {
			// Step 1: connect to database server
			Driver d = new SimpleDriver();
			conn = d.connect("jdbc:simpledb://localhost", null);

			// Step 2: execute the query
			// CS4432-Project1
			// Basic query to find non-attacking moves
			Statement stmt = conn.createStatement();
			String qry = "select MoveName "
			           + "from MOVES "
			           + "where Category = 2";
			ResultSet rs = stmt.executeQuery(qry);

			// Step 3: loop through the result set
			System.out.println("Non-Attacking Moves");
			while (rs.next()) {
				String mname = rs.getString("MoveName");
				System.out.println(mname);
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
