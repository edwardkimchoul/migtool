package migtool.schman;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


//import oracle.jdbc.internal.OracleTypes;

public class migThread2 {
    public static class Task implements Runnable {
        String process_id;
        String procedure_name;
	    String dbname;
    	String sql_type;
	    String sql;
	    
	    public Task(String process_id, String procedure_name, String dbname, String Sql_type, String Sql) {
	      this.process_id = process_id;
	      this.procedure_name = procedure_name;
	      this.dbname = dbname;
	      this.sql_type = Sql_type;
	      this.sql = Sql;
	      
	      //System.out.println("INININI" + dbname);
	    }
	    
	    @Override
	    public void run() {
//	      log.info("called!");
	    	String ERR_MSG = ""; 
	    	String start_time = "";
	        String stop_time = "";	
	        String status_cd = "";
	    	Connection conn = null;
	    	StopWatch timer = new StopWatch();
//	    	System.out.println("*******************" + dbname);
	      try {
	    	  schmanager2.updateStatus(process_id, "P");
             // 1. SQL Connection 가겨오기 sychoronize 사용
	    	  switch(dbname) {
		    	  case "PROD" :
			    	  ConnectionPoolMig migpool = ConnectionPoolMig.getInstance();
				      conn = migpool.getConnection();

			    	  switch(sql_type) {
				    	  case "PRC" :
				    		  CallableStatement cstmt = null;
				    		  String rowcntStr = "0";
				    		  try {
				    			  start_time = schmanager2.getCurrentDateTime();
				    			  cstmt = conn.prepareCall(sql);
				    			// Out parameter의 Type설정
				    			  cstmt.registerOutParameter(1, java.sql.Types.VARCHAR );
//				    			  cstmt.registerOutParameter(1, OracleTypes.CURSOR);
					    	      cstmt.execute();
					    	      // Procedure  결과 받기
					    	      String result = cstmt.getString(1);
					    	      if(result.substring(0, 3).equals("SUC")) {
					    	    	  status_cd = "C";
					    	    	  rowcntStr = result.substring(4);
					    	      } else {
					    	    	  status_cd = "E";
					    	    	  ERR_MSG = result.substring(4);
					    	      }

					    	      stop_time = schmanager2.getCurrentDateTime();
				    		  } catch (Exception e) {
				    			   System.out.println("[" + process_id + "] ---- Procedure 수행시 오류발생 = "+e.getMessage());
				    			   ERR_MSG = e.getMessage();
				    		  } finally {
					    	      Map<String, String> map = new HashMap<String, String>();
				    	      
					 	    	  map.put("START_TIME", start_time);
					 	    	  map.put("STOP_TIME", stop_time);
					 	    	  map.put("EXECUTION_TIME", timer.getEllapsed()+"");
					 	    	  map.put("ROW_COUNT", rowcntStr);
					 	    	  map.put("STATUS", status_cd);
					 	    	  map.put("ERROR", ERR_MSG);
					 	    	  map.put("PROCEDURE_NAME", this.procedure_name);	
					 	    	  map.put("PROCESS_ID", process_id);
					 	    	  
					 	    	  schmanager2.updateStatus(process_id, status_cd);
					 	    	  schmanager2.insertProcessHistory(map);
					 	    	 
				    			   try {cstmt.close();} catch (Exception ignored){}
				    			   try {System.out.println("[" + process_id + "] [" + procedure_name + "] [" +  timer.getEllapsed()  +"]초 ---- 작업완료 ");
				    				   migpool.releaseConnection(conn);} catch (Exception ignored) {}
				    		  }
				    		  break;
				    	  case "SQL" :
				    	  case "PLSQL" :
				    		  PreparedStatement pstmt = null;
				    		  int rowcnt = 0;
				    		  try {
				    			   start_time = schmanager2.getCurrentDateTime();
					    		   pstmt = conn.prepareStatement(sql); 
					    		   rowcnt = pstmt.executeUpdate();	
					    		   stop_time = schmanager2.getCurrentDateTime();
					    		   status_cd = "C";
				    		  } catch (Exception e) {
				    			   System.out.println("[" + process_id + "] ---- SQL 문장처리에서 오류가 발생하였습니다.= "+e.getMessage());
				    			   ERR_MSG = e.getMessage();
				    			   status_cd = "E";
				    		  } finally {
					 	    	   Map<String, String> map = new HashMap<String, String>();
					 	    	   
					 	    	   map.put("START_TIME", start_time);
					 	    	   map.put("STOP_TIME", stop_time);
					 	    	   map.put("EXECUTION_TIME", timer.getEllapsed()+"");
					 	    	   map.put("ROW_COUNT", rowcnt+"");
					 	    	   map.put("STATUS", status_cd);
					 	    	   map.put("ERROR", ERR_MSG);
					 	    	   map.put("PROCEDURE_NAME", this.procedure_name);	
					 	    	   map.put("PROCESS_ID", process_id);

					 	    	   schmanager2.updateStatus(process_id, status_cd);	
					 	    	   schmanager2.insertProcessHistory(map);
				    			  
				    			   try {pstmt.close();} catch (Exception ignored){}
				    			   try {System.out.println("[" + process_id + "] [" + procedure_name + "] [" +  timer.getEllapsed()  +"]초 ---- 작업완료 ");
				    				    migpool.releaseConnection(conn);} catch (Exception ignored) {}
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
		    			   start_time = schmanager2.getCurrentDateTime();
			    		   pstmt = conn.prepareStatement(sql); 
			    		   rowcnt = pstmt.executeUpdate();
			    		   stop_time = schmanager2.getCurrentDateTime();
			    		   status_cd = "C";
		    		  } catch (Exception e) {
		    			   System.out.println("[" + process_id + "] ---- SQL 문장처리에서 오류가 발생하였습니다.= "+e.getMessage());
		    			   ERR_MSG = e.getMessage();
		    			   status_cd = "E";
		    		  } finally {
		    			  
			 	    	   Map<String, String> map = new HashMap<String, String>();
			 	    	   
			 	    	   map.put("START_TIME", dbname);
			 	    	   map.put("STOP_TIME", dbname);
			 	    	   map.put("EXECUTION_TIME", dbname);
			 	    	   map.put("ROW_COUNT", rowcnt+"");
			 	    	   map.put("STATUS", status_cd);
			 	    	   map.put("ERROR", ERR_MSG);
			 	    	   map.put("PROCEDURE_NAME", this.procedure_name);	
			 	    	   
//			 	    	   schmanager2.updateMigTool(map);
			 	    	   schmanager2.updateStatus(process_id, status_cd);
			 	    	   schmanager2.insertProcessHistory(map);
		    			  
		    			   try {pstmt.close();} catch (Exception ignored){}
		    			   try {System.out.println("[" + process_id + "] [" + procedure_name + "] [" +  timer.getEllapsed()  +"]초 ---- 작업완료 ");
		    			        stgpool.releaseConnection(conn);} catch (Exception ignored) {}
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
