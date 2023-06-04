package migtool.schman;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import oracle.jdbc.internal.OracleTypes;

public class migThread {
    public static class Task implements Runnable {
        int process_id;
        String procedure_name;
	    String dbname;
    	String sql_type;
	    String sql;
	    
	    public Task(int process_id, String procedure_name, String dbname, String Sql_type, String Sql) {
	      this.process_id = process_id;
	      this.procedure_name = procedure_name;
	      this.dbname = dbname;
	      this.sql_type = Sql_type;
	      this.sql = Sql;
	    }
	    
	    @Override
	    public void run() {
//	      log.info("called!");
	    	String ERR_MSG = ""; 
	    	Connection conn = null;
	    	
	      try {
             // 1. SQL Connection 가겨오기 sychoronize 사용
	    	  switch(dbname) {
		    	  case "MIG3" :
			    	  ConnectionPoolMig migpool = ConnectionPoolMig.getInstance();
				      conn = migpool.getConnection();

			    	  switch(sql_type) {
				    	  case "PROCEDURE" :
				    		  CallableStatement cstmt = null;
				    		  int rowcnt = 0;
				    		  try {
				    			  cstmt = conn.prepareCall(sql);
				    			  cstmt.registerOutParameter(1, OracleTypes.CURSOR);
					    	      cstmt.execute();
					    	      // Procedure  결과 받기
					    	      ResultSet rset = (ResultSet)cstmt.getObject(1);
					    	      while (rset.next ()) {
					    	      
					    	      }	

				    		  } catch (Exception e) {
				    			   System.out.println("[" + process_id + "] ---- Procedure 수행시 오류발생 = "+e.getMessage());
				    			   ERR_MSG = e.getMessage();
				    		  } finally {
					    	      Map<String, String> map = new HashMap<String, String>();
				    	      
					 	    	  map.put("START_TIME", dbname);
					 	    	  map.put("STOP_TIME", dbname);
					 	    	  map.put("EXECUTION_TIME", dbname);
					 	    	  map.put("ROW_COUNT", rowcnt+"");
					 	    	  map.put("ERROR", dbname);
					 	    	  map.put("PROCEDURE_NAME", this.procedure_name);	
					 	    	   
					 	    	  schmanager.updateMigTool(map);
					 	    	  schmanager.updateStatus(map);				    			  
				    			  
				    			   try {cstmt.close();} catch (Exception ignored){}
				    			   try {migpool.releaseConnection(conn);} catch (Exception ignored) {}
				    		  }
				    		  break;
				    	  case "SQL" :
				    		  PreparedStatement pstmt = null;
				    		  rowcnt = 0;
				    		  try {
					    		   pstmt = conn.prepareStatement(sql); 
					    		   rowcnt = pstmt.executeUpdate();	
					    		   
				    		  } catch (Exception e) {
				    			   System.out.println("[" + process_id + "] ---- SQL 문장처리에서 오류가 발생하였습니다.= "+e.getMessage());
				    			   ERR_MSG = e.getMessage();
				    		  } finally {
				    			  
					 	    	   Map<String, String> map = new HashMap<String, String>();
					 	    	   
					 	    	   map.put("START_TIME", dbname);
					 	    	   map.put("STOP_TIME", dbname);
					 	    	   map.put("EXECUTION_TIME", dbname);
					 	    	   map.put("ROW_COUNT", rowcnt+"");
					 	    	   map.put("ERROR", ERR_MSG);
					 	    	   map.put("PROCEDURE_NAME", this.procedure_name);	
					 	    	   
					 	    	   schmanager.updateMigTool(map);
					 	    	   schmanager.updateStatus(map);				    			  
				    			  
				    			   try {pstmt.close();} catch (Exception ignored){}
				    			   try {migpool.releaseConnection(conn);} catch (Exception ignored) {}
				    		  } 
				    		  break;
			    	  }
		    		  break;
		    	  case "ORASTG" :
		    		  ConnectionPoolStg stgpool = ConnectionPoolStg.getInstance();
		    		  conn = stgpool.getConnection();
		    		  
		    		  PreparedStatement pstmt = null;
		    		  int rowcnt = 0;
		    		  try {
			    		   pstmt = conn.prepareStatement(sql); 
			    		   rowcnt = pstmt.executeUpdate();
			    		   
			 	    	   Map<String, String> map = new HashMap<String, String>();
			    		   
		    		  } catch (Exception e) {
		    			   System.out.println("[" + process_id + "] ---- SQL 문장처리에서 오류가 발생하였습니다.= "+e.getMessage());
		    			   ERR_MSG = e.getMessage();
		    		  } finally {
		    			  
			 	    	   Map<String, String> map = new HashMap<String, String>();
			 	    	   
			 	    	   map.put("START_TIME", dbname);
			 	    	   map.put("STOP_TIME", dbname);
			 	    	   map.put("EXECUTION_TIME", dbname);
			 	    	   map.put("ROW_COUNT", rowcnt+"");
			 	    	   map.put("ERROR", ERR_MSG);
			 	    	   map.put("PROCEDURE_NAME", this.procedure_name);	
			 	    	   
			 	    	   schmanager.updateMigTool(map);
			 	    	   schmanager.updateStatus(map);
		    			  
		    			   try {pstmt.close();} catch (Exception ignored){}
		    			   try {stgpool.releaseConnection(conn);} catch (Exception ignored) {}
		    		  } 
		    		  break;		    		  
	    	  }
	    	 // 4. 수행결과 Update

	        Thread.sleep(1000);
	      } catch (Exception e) {
	        e.printStackTrace();
	      }

//	      log.info("finished!");
	    }
	  }
}
