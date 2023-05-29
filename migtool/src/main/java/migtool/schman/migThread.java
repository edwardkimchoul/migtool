package migtool.schman;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class migThread {
    public static class Task implements Runnable {
        int process_id;
        String procedure_name;
    	String sql;
	    String dbname;

	    public Task(int process_id, String procedure_name, String dbname, String Sql) {
	      this.process_id = process_id;
	      this.procedure_name = procedure_name;
	      this.dbname = dbname;
	      this.sql = sql;
	    }
	    
	    @Override
	    public void run() {
//	      log.info("called!");
	    	Connection conn;
	      try {
             // 1. SQL Connection 가겨오기 sychoronize 사용
//	    	  switch(dbname) {
//	    	  case "MIG3" :
//		    	  Synchonize {
//			    	  ConnectionMig3Pool mig3pool = ConnectionMig3Pool.getInstance();
//			    	  conn = pool.getConnection();
//		    	  }
//	    		  break;
//	    	  case "ORASTG" :
//	    		  ConnectionStgPool stgpool
//	    		  conn = ;
//	    		  break;
//	    	  }
	    	 // 2. Sql Type에 따라 수행작업 달리하기
	    	  
	    	 // 3. 수행결과 받기
	    	  
	    	 // 4. 수행결과 Update

	    	  Map<String, String> map = new HashMap<String, String>();
	    	  schmanager.updateStatus(conn, process_id, "C");
	    	  schmanager.updateMigTool(conn, map);
	    	  schmanager.insertMigJobHistory(conn, map);
	    	  
	        Thread.sleep(1000);
	      } catch (Exception e) {
	        e.printStackTrace();
	      }

//	      log.info("finished!");
	    }
	  }
}
