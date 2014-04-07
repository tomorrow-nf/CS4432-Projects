package pokemonTest.simpledb;

import java.sql.*;
import simpledb.remote.SimpleDriver;

// CS4432-Project1
// Two basic SQL tables to test simpleDB

public class CreatePkmnDB {
    public static void main(String[] args) {
		Connection conn = null;
		try {
			Driver d = new SimpleDriver();
			conn = d.connect("jdbc:simpledb://localhost", null);
			Statement stmt = conn.createStatement();

			String s = "create table MOVES(MoveName varchar(20), Category int, Damage int, Accuracy int, MoveType varchar(10))";
			stmt.executeUpdate(s);
			System.out.println("Table MOVES created.");

			s = "insert into MOVES(MoveName, Category, Damage, Accuracy, MoveType) values ";
			String[] typevals = {"('Recover', 2, 0, 0, 'Normal')",
								 "('Fly', 0, 90, 95, 'Flying')",
								 "('Flamethrower', 1, 95, 100, 'Fire')",
								 "('Surf', 1, 95, 100, 'Water')",
								 "('Ancient Power', 1, 60, 100, 'Rock')",
								 "('Swords Dance', 2, 0, 0, 'Normal')",
								 "('Razor Leaf', 0, 55, 95, 'Grass')"
								 };
			for (int i=0; i<typevals.length; i++)
				stmt.executeUpdate(s + typevals[i]);
			System.out.println("MOVE records inserted.");

			s = "create table SPECIES(SpecName varchar(10), DexNum int, TypeOne varchar(10), TypeTwo varchar(10))";
			stmt.executeUpdate(s);
			System.out.println("Table SPECIES created.");

			s = "insert into SPECIES(SpecName, DexNum, TypeOne, TypeTwo) values ";
			String[] specvals = {"('Charizard', 6, 'Fire', 'Flying')",
								 "('Pidgeot', 18, 'Normal', 'Flying')",
								 "('Omastar', 139, 'Water', 'Rock')",
								 "('Magcargo', 219, 'Fire', 'Rock')",
								 "('Barbaracle', 689, 'Rock', 'Water')",
								 "('Archeops', 567, 'Rock', 'Flying')",
								 "('Tropius', 357, 'Grass', 'Flying')",
								 };
			for (int i=0; i<specvals.length; i++)
				stmt.executeUpdate(s + specvals[i]);
			System.out.println("SPECIES records inserted.");

		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
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
