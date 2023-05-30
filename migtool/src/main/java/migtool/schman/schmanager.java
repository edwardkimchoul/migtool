package migtool.schman;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class schmanager {

	public static String mig_task_name;
	/*+
	 * 현재 작업의 Update를 위해
	 */
	public static void updateStatus(Connection conn, int process_id, String status_cd) throws Exception {
		final String sql ="UPDATE  MIG_PROCESS_LIST \n"
           				+ "   SET  MIG_STATUS_CD = ?  \n"
			        	+ " WHREE PROCESS_ID = ?  ";
		try {
			PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);

			s1.setString(1, status_cd);
			s1.setInt(2, process_id);
			
		    int cnt = s1.executeUpdate();

		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Status Update Error ");
		} 	
	}
	/*+
	 * 기존 Mig Tool의 상태를 Update하기
	 */
	public static void updateMigTool(Connection conn, Map<String, String> map) throws Exception {
		final String sql ="UPDATE MIG_JOB_STATUS \n"
				+ "   SET START_TIME = ?      \n"
				+ "     , STOP_TIME = ?       \n"
				+ "     , EXECUTION_TIME = ?  \n"
				+ "     , ROW_COUNT = ?       \n"
				+ "     , ERROR = ?           \n"
				+ " WHERE SUBSTR(JOBTIMESTAMP,1,8) IN ( TO_CHAR(SYSDATE-1,'YYYYMMDD'), TO_CHAR(SYSDATE,'YYYYMMDD') )  \n"
				+ "   AND PROCEDURE_NAME = ? ";
		try {
			PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);

			s1.setString(1, map.get("START_TIME"));
			s1.setString(2, map.get("STOP_TIME"));
			s1.setString(3, map.get("EXECUTION_TIME"));
			s1.setString(4, map.get("ROW_COUNT"));
			s1.setString(5, map.get("ERROR"));
			s1.setString(6, map.get("PROCEDURE_NAME"));
			
		    int cnt = s1.executeUpdate();		
			
			
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Mig Tool Update Error ");
		}
	}
	
	private static List<MigJob> getMigjobList(Connection conn) throws Exception {
		final String sql ="SELECT M.PROCESS_ID, M.DB_NAME, M.PROCEDURE_NAME, M.SQL_TYPE_CD \n"
				+ "  FROM MIG_PROCESS_LIST M \n"
				+ " WHERE M.MIG_STATUS_CD = 'R'   \n"
				+ "   AND FN_MIG_NOT_COMPLETE_CNT( M.PRECEDING_WORK_LIST ) = 0 \n"
				+ "   ORDER BY PROCESS_ID";
		List<MigJob> tasklist = new ArrayList<>();

		try (PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);
			     ResultSet rs = s1.executeQuery()) {
				    while(rs.next()) {
				    	MigJob migjob =new MigJob();
				    	
				    	migjob.setProcesId(rs.getInt("PROCESS_ID"));
				    	migjob.setDbName(rs.getString("DB_NAME"));
				    	migjob.setProcedureName(rs.getString("PROCEDURE_NAME"));
				    	migjob.setSqlType(rs.getString("SQL_TYPE_CD"));
				    	
				    	tasklist.add(migjob);
			        }
			} catch(Exception ex) {
				ex.printStackTrace();
				throw new Exception(" Mig Target list Error ");
			} 	
		return tasklist;
	}

	private static boolean isMigComplete(Connection conn) throws Exception {
		boolean isComplete = false;
		final String sql ="SELECT COUNT(1) CNT \n"
				+ "  FROM MIG_PROCESS_LIST M \n"
				+ " WHERE M.MIG_STATUS_CD IN ( 'R', 'Q', 'P', 'E' ) \n";

		try (PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);
			     ResultSet rs = s1.executeQuery()) {
					
					if(rs.next()) {
						if(rs.getInt(0) == 0) {
							isComplete = true;
						} else {
							isComplete = false;
						}
					}
			} catch(Exception ex) {
				ex.printStackTrace();
				throw new Exception(" select isMigComplete error ");
			} 	
		return isComplete;
	}
	
	public static void insertMigJobHistory(Connection conn, Map<String, String> map) throws Exception {
		final String sql ="INSERT INTO MIG_PROCESS_HIST(MIG_HIST_NAME, PROCESS_ID,	MIG_START_TM,	MIG_END_TM,	ELAPSED_TIME,	ROW_CNT, ERROR_MESSAGE)\r\n"
				+ "    VALUE (?, ?, ?, ?, ?, ?, ? )";
		try {
			PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);

			s1.setString(1, mig_task_name);
			s1.setInt(2, Integer.parseInt(map.get("PROCESS_ID")));
			s1.setString(3, map.get("START_TIME"));
			s1.setString(4, map.get("STOP_TIME"));
			s1.setString(5, map.get("EXECUTION_TIME"));
			s1.setString(6, map.get("ROW_COUNT"));
			s1.setString(7, map.get("ERROR"));

		    int cnt = s1.executeUpdate();		
			
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Mig Tool Update Error ");
		}
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
						service.submit(new migThread.Task(job.getProcesId(), job.getProcedureName(), job.getDbName(),sql));
						break;
					case "SQL" :
						// sql = "";
						service.submit(new migThread.Task(job.getProcesId(), job.getProcedureName(), job.getDbName(),sql));
						break;
					}
				}
				// 3. 작업이 모두 끝났는지 Check
				if(isMigComplete(conn)) {
					endFlag = true;
				}
				
				// 10초후 작업을 재개 한다. (전체 작업이 끝날때까지 계속한다.)
				Thread.sleep(10000);
			}
			
			// 엑셀로 이행결과 출력
			makeExcelTaskList(conn);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
