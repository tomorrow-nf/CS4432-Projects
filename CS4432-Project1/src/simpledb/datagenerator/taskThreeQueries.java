package simpledb.datagenerator;

import java.sql.*;
import java.util.Random;

import simpledb.remote.SimpleDriver;

public class taskThreeQueries {
	public static void main(String[] args) {
		Connection conn = null;
		try {
			// Step 1: connect to database server
			Driver d = new SimpleDriver();
			conn = d.connect("jdbc:simpledb://localhost", null);

			// Step 2: execute the queries
			for(int i=1;i<6;i++){
				long startTime = System.currentTimeMillis();

				Statement stmt = conn.createStatement();
				String qry = "select a1, a2 "
				           + "from test" + i
				           + " where a1 = 275 ";
				ResultSet rs = stmt.executeQuery(qry);

				// Step 3: loop through the result set
				System.out.println("*****\nTEST" + i + "\n-----\na1\ta2\n-----");
				while (rs.next()) {
					int a1 = rs.getInt("a1");
					int a2 = rs.getInt("a2");
					System.out.println(a1 + "\t" + a2);
				}
				rs.close();
				long endTime = System.currentTimeMillis();
				long elapsedTime = endTime - startTime;
				System.out.println("QUERY TIME: " + elapsedTime + "ms");
			}
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
