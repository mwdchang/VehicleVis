package datastore;

// Class to hold some of the global variables...
// Mostly just try to make it easier to point to different variations
public class Const {
   
         
   ////////////////////////////////////////////////////////////////////////////
   // Path for parser
   ////////////////////////////////////////////////////////////////////////////
   public static final String PART_FILE =    "C:\\Users\\Daniel\\DropBox\\Resources\\component_04";
   public static final String GRAMMAR_FILE = "C:\\Users\\Daniel\\DropBox\\Resources\\stanford-parser\\englishPCFG.ser.gz";   
   public static final String DATA_FILE =    "C:\\Users\\Daniel\\StaticResources\\FLAT_CMPL.txt";
   
   public static final int CMPL_IDX = 0;
   public static final int MFR_IDX = 2;
   public static final int MAKE_IDX = 3;
   public static final int MODEL_IDX = 4;
   public static final int DESC_IDX = 19;
   public static final int DATEA_IDX = 15;
   public static final int YEAR_IDX = 4;
   public static final int PROD_TYPE_IDX = 45;
   
   public static final String SWN_FILE = "C:\\Users\\Daniel\\DropBox\\Resources\\SentiWordNet\\SentiWordNet_3.txt";
   
   
   ////////////////////////////////////////////////////////////////////////////
   // Path for modeller
   ////////////////////////////////////////////////////////////////////////////
   public static final String MODEL_PATH = "C:\\Users\\Daniel\\DropBox\\Resources\\";
   public static final String TMP_DIR = "C:\\Users\\daniel\\temporary\\";
   
   
   ////////////////////////////////////////////////////////////////////////////
   // Database parameters (MySQL)
   ////////////////////////////////////////////////////////////////////////////
   public static final String DB_URL  = "jdbc:mysql://localhost/projectv3";
   public static final String DB_USER = "root";
   public static final String DB_PASS = "root";
   public static final String DB_DRVR = "com.mysql.jdbc.Driver";
   
   
   
      
   public Const() {}
}
