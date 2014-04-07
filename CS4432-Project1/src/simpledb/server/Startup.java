package simpledb.server;

import simpledb.remote.*;
import java.rmi.registry.*;

public class Startup {
   public static void main(String args[]) throws Exception {
      // configure and initialize the database

	  // CS4432-Project1: Also allows for a second integer argument 
	  // that dictates which replacement policy to use
	  // 1 - Default (or any other number that isnt 2 or 3)
	  // 2 - LRU
	  // 3 - Clock
	  if (args.length == 2) {
		  SimpleDB.init(args[0], Integer.parseInt(args[1]));
	  }
	  else {
		  SimpleDB.init(args[0]);
	  }
      
      // create a registry specific for the server on the default port
      Registry reg = LocateRegistry.createRegistry(1099);
      
      // and post the server entry in it
      RemoteDriver d = new RemoteDriverImpl();
      reg.rebind("simpledb", d);
      
      System.out.println("database server ready");
   }
}
