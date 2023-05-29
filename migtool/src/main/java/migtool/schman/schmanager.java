package migtool.schman;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class schmanager {


    public static class Task implements Runnable {
	    String sql;

	    public Task(String Sql) {
	      this.sql = sql;
	    }

	    public void run() {
//	      log.info("called!");

	      try {
	        Thread.sleep(1000);
	      } catch (Exception e) {
	        e.printStackTrace();
	      }

//	      log.info("finished!");
	    }
	  }
	
	
	/*+
	 * 현재 작업의 Update를 위해
	 */
	private static void updateStatus(Connection conn, String procedureName) {
		final String sql ="";
		try {
			
		} catch(Exception e) {
			
		}
	}
	/*+
	 * 기존 Mig Tool의 상태를 Update하기
	 */
			
	private static void updateMigTool(Connection conn, String procedureName) {
		final String sql ="";
		try {
			
		} catch(Exception e) {
			
		}
		
		
	}
	private static List<MigJob> getMigjobList(Connection conn) {
		Map map;
		final String sql ="SELECT M.PROCESS_ID, M.DB_NAME, M.PROCEDURE_NAME, M.SQL_TYPE_CD \n"
				+ "  FROM MIG_PROCESS_LIST M \n"
				+ " WHERE M.MIG_STATUS_CD = 'R'   \n"
				+ "   AND FN_MIG_NOT_COMPLETE_CNT( M.PRECEDING_WORK_LIST ) = 0 \n"
				+ "   ORDER BY PROCESS_ID";
		List<MigJob> list = new ArrayList<>();
		try {
			
			
			
			
			//while(rs.next()) {
				
			
		} catch(Exception e) {
			
		}
		return list;
	}
	private static void makeExcelTaskList(Connection conn) {
		
	}
		
	public static void main(String[] args) {

		boolean endFlag = false;
		String sql = "";
		Connection conn;
		try {
			ExecutorService service = Executors.newFixedThreadPool(10);
			ConnectionPool pool = ConnectionPool.getInstance();
			conn = pool.getConnection();
			
			while(! endFlag) {
				
				// 1. 처리할 작업 List를 가져온다.
				//    ( 선행작업이 끝난 작업의 List을 가져온다.)
				List<MigJob> joblist = getMigjobList(conn);
				
				
				// 2. procedure 형식에 따라 Task를 
				for(MigJob job : joblist) {
					switch(job.getSqlType()) {
					case "SP" :
						sql = job.getProcedureName() + "(?)";
						service.submit(new migThread.Task(job.getDbName(),sql));
						break;
					case "SQL" :
						// sql = "";
						service.submit(new migThread.Task(job.getDbName(),sql));
						break;
					}
				}
				// 3. 작업이 모두 끝났는지 Check
				
			}
			
			// 엑셀로 이행결과 출력
			makeExcelTaskList(conn);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		

	}	
}
