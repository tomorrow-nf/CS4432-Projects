package simpledb.parse;

/**
 * The parser for the <i>create index</i> statement.
 * @author Edward Sciore
 */
public class CreateIndexData {
   private String indextype, idxname, tblname, fldname;
   
   /**
    * Saves the table and field names of the specified index.
    */
   public CreateIndexData(String indextype, String idxname, String tblname, String fldname) {
	  this.indextype = indextype;
      this.idxname = idxname;
      this.tblname = tblname;
      this.fldname = fldname;
   }
   
   /**
    * Returns the name of the index.
    * @return the name of the index
    */
   public String indexName() {
      return idxname;
   }
   
   /**
    * Returns the name of the indexed table.
    * @return the name of the indexed table
    */
   public String tableName() {
      return tblname;
   }
   
   /**
    * Returns the name of the indexed field.
    * @return the name of the indexed field
    */
   public String fieldName() {
      return fldname;
   }
   
   //TODO: Comments
   public String indextype() {
	   return indextype;
   }
}

