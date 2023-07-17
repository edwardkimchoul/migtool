package migtool.schman;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/* ******************************************************************** 
 * PROGRAM : 제증명 이행을 위한 이행 스케쥴러 
 * -------  F로 시작하는 Process_ID 수행
 * 
 * ********************************************************************/
public class schmanager3 {

	public static String mig_task_name;
	private static Connection conn1 = null;
	private static Connection conn2 = null;
	private static Connection conn3 = null;
	private static HashMap<String, String> sqlHash;

    private static Connection getNewConnection() throws SQLException {
        Connection con = null;
        try {
            con = DriverManager.getConnection(ConnectionPoolMig.url, ConnectionPoolMig.user, ConnectionPoolMig.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
//	private static Connection conn3 = null;
	/*+
	 * 현재 작업의 Update를 위해
	 */
	public synchronized static void updateStatus(String process_id, String status_cd) throws Exception {
		final String sql1 ="UPDATE  CNV_PROCESS_LIST2  \n"
           				+ "   SET  MIG_STATUS_CD = ?  \n"
			        	+ " WHERE PROCESS_ID = ?  "
                       	+ "   AND MIG_STATUS_CD != 'E' ";
		
		try {
			// connection 복구코드를 넣음.
			if(conn1 == null || conn1.isClosed()) {
				conn1 = getNewConnection();
			}
			PreparedStatement s1 = (PreparedStatement) conn1.prepareStatement(sql1);

			s1.setString(1, status_cd);
			s1.setString(2, process_id);
			
		    s1.executeUpdate();
		    
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Status Update Error ");
		} 	
	}
	
	
	
	
	public synchronized static void insertProcessHistory(Map<String, String> map) throws Exception {

		
		final String sql2 ="INSERT INTO CNV_PROCESS_HIST(ID, MIG_HIST_NAME, PROCESS_ID,	START_TIME,	STOP_TIME,	EXECUTION_TIME,	ROW_COUNT, ERROR) \n"
				+ "    VALUES (CNV_PLSQL_STRUCT_SEQ.nextval, ?, ?, ?, ?, ?, ?, ? )";
		
		try {
			// connection 복구코드를 넣음.
			if(conn3 == null || conn3.isClosed()) {
				conn3 = getNewConnection();
			}
			
			PreparedStatement s1 = (PreparedStatement) conn3.prepareStatement(sql2);
            /*******************************************************************
		    // Mig History를 저장
		     *******************************************************************/

			s1.setString(1, schmanager3.mig_task_name);
			s1.setString(2, map.get("PROCESS_ID"));
			s1.setString(3, map.get("START_TIME"));
			s1.setString(4, map.get("STOP_TIME"));
			s1.setString(5, map.get("EXECUTION_TIME"));
			s1.setString(6, map.get("ROW_COUNT"));
			s1.setString(7, map.get("ERROR"));

		    s1.executeUpdate();		
		    
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Status Update Error ");
		} 	
	}
	/*+
	 * 기존 Mig Tool의 상태를 Update하기
	 */
//	public synchronized static void updateMigTool(Map<String, String> map) throws Exception {
//		final String sql ="UPDATE MIG_JOB_STATUS \n"
//				+ "   SET START_TIME = ?      \n"
//				+ "     , STOP_TIME = ?       \n"
//				+ "     , EXECUTION_TIME = ?  \n"
//				+ "     , ROW_COUNT = ?       \n"
//				+ "     , ERROR = ?           \n"
//				+ "     , STATUS = ?           \n"
//				+ " WHERE SUBSTR(JOB_TIMESTAMP,1,8) IN ( TO_CHAR(SYSDATE-7,'YYYYMMDD'), TO_CHAR(SYSDATE+1,'YYYYMMDD') )  \n"
//				+ "   AND PROCEDURE_NAME = ? ";
//		try {
//			String status_cd = "";
//			if(map.get("START_TIME").equals("C")) {
//				status_cd = "O";
//			} else {
//				status_cd = "E";
//			}
//			
//			PreparedStatement s1 = (PreparedStatement) conn2.prepareStatement(sql);
//			
//			s1.setString(1, map.get("START_TIME"));
//			s1.setString(2, map.get("STOP_TIME"));
//			s1.setString(3, map.get("EXECUTION_TIME"));
//			s1.setString(4, map.get("ROW_COUNT"));
//			s1.setString(5, map.get("ERROR"));
//			s1.setString(6, status_cd);
//			
//			s1.setString(7, map.get("PROCEDURE_NAME"));
//			
//		    s1.executeUpdate();		
//			
//			
//		} catch(Exception ex) {
//			ex.printStackTrace();
//			throw new Exception(" Mig Tool Update Error ");
//		}
//	}
	
	private static List<MigJob> getMigjobList(Connection conn) throws Exception {
		final String sql ="SELECT M.PROCESS_ID, M.MIG_DB_NAME, M.PROCEDURE_NAME, M.SQL_TYPE_CD \n"
				+ "  FROM CNV_PROCESS_LIST2 M \n"
				+ " WHERE M.MIG_STATUS_CD = 'R'   \n"
				+ "   AND M.PROCESS_ID NOT LIKE 'P%'   \n"
				+ "   AND M.PROCESS_ID LIKE 'F%'   \n"
				+ "   AND FN_MIG_NOT_COMPLETE_CNT( M.PRECEDING_WORK_LIST ) = 0 \n"
				+ "   ORDER BY PRIORITY_NO, \n"
				+ "     DECODE(SUBSTR(M.PROCESS_ID,1,3),'164', 1, '172',2, '184', 3, '261', 4, '182', 5, '019', 6, '161', 7, 9), \n"
				+ "     PROCESS_ID ";
		List<MigJob> tasklist = new ArrayList<>();

		try (PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);
			     ResultSet rs = s1.executeQuery()) {
				    while(rs.next()) {
				    	MigJob migjob =new MigJob();
				    	
				    	migjob.setProcesId(rs.getString("PROCESS_ID"));
				    	migjob.setDbName(rs.getString("MIG_DB_NAME"));
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
				+ "  FROM CNV_PROCESS_LIST2 M \n"
				+ " WHERE M.MIG_STATUS_CD IN ( 'R', 'Q', 'P' ) \n"
		        + "   AND M.PROCESS_ID LIKE 'F%'   \n";
		try (PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);
			     ResultSet rs = s1.executeQuery()) {
					
					if(rs.next()) {
						if(rs.getInt(1) == 0) {
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
	
	public static String getSqlmigtool(Connection conn, String procedurename) {

		final String sql ="SELECT MIG_BEFORE_QUERY          \n"
				        + "  FROM MIG_PLSQL                         \n"
				        + " WHERE PROCEDURE_NAME = ?  ";
		
		String text = "";
		try {
			 
			 PreparedStatement s1 = (PreparedStatement) conn.prepareStatement(sql);
			 s1.setString(1, procedurename);
		     ResultSet rs = s1.executeQuery(); 
			 while(rs.next()) {
				 text = "" +rs.getString("MIG_BEFORE_QUERY");
			 }
			 
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
		}
		return text;
	}
	
//	private static void getSqlXML() throws Exception{
//		
//		try {
//			sqlHash = new HashMap<String, String>();
//			String sql_file = "Sql.xml";
//			ClassLoader classLoader = schmanager.class.getClassLoader();
//			
//			
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			
//			Document doc = doc = builder.parse(classLoader.getResource(sql_file).getFile());
//			
//			NodeList selectList, insertList;
//			String sql_id, sqlStmt;
//			
//			selectList = doc.getElementsByTagName("sql");
//			System.out.println("selectList.length() -----> [" + selectList.getLength() + "] " );
//			for(int i=0 ; i<selectList.getLength() ; i++) {
//		        Node node = selectList.item(i);
//		        Element element = (Element)node;
//		        
//		        sql_id = element.getAttribute("id");
//		        sqlStmt = node.getTextContent();
//		        
//		        System.out.println("[Select STMT] -----> [" + sql_id + "]  SQL [" + sqlStmt + "]" );
//		        sqlHash.put(sql_id, sqlStmt);
//	        }
//			
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			// TODO Auto-generated catch block
//			System.out.println("sql.xml Parsing하는데 에러가 발생하였습니다.");
//			e.printStackTrace();
////			throw new Exception("sql.xml Parsing하는데 에러가 발생하였습니다. ");
//		}
//	} 	
	
	public static String getCurrentDateTime() {
		Date today = new Date();
		Locale currentLocale = new Locale("KOREAN", "KOREA");
		String pattern = "yyyyMMddHHmmss"; //hhmmss로 시간,분,초만 뽑기도 가능
		SimpleDateFormat formatter = new SimpleDateFormat(pattern,
				currentLocale);
		return formatter.format(today);
	}	
//	public synchronized static void insertMigJobHistory(Map<String, String> map) throws Exception {
//		final String sql ="INSERT INTO CNV_PROCESS_HIST(MIG_HIST_NAME, PROCESS_ID,	MIG_START_TM,	MIG_END_TM,	ELAPSED_TIME,	ROW_CNT, ERROR_MESSAGE)\r\n"
//				+ "    VALUE (?, ?, ?, ?, ?, ?, ? )";
//		try {
//			PreparedStatement s1 = (PreparedStatement) conn3.prepareStatement(sql);
//
//			s1.setString(1, mig_task_name);
//			s1.setInt(2, Integer.parseInt(map.get("PROCESS_ID")));
//			s1.setString(3, map.get("START_TIME"));
//			s1.setString(4, map.get("STOP_TIME"));
//			s1.setString(5, map.get("EXECUTION_TIME"));
//			s1.setString(6, map.get("ROW_COUNT"));
//			s1.setString(7, map.get("ERROR"));
//
//		    int cnt = s1.executeUpdate();		
//			
//		} catch(Exception ex) {
//			ex.printStackTrace();
//			throw new Exception(" Mig Tool Update Error ");
//		}
//	}
	
//	private static void makeExcelTaskList(Connection conn) {
//		
//	}
//		
	public static void main(String[] args) {

		boolean endFlag = false;
		String sql = "";
		Connection migconn;
		
		if(args.length != 1) {
			System.out.println("schmanager3 [taskname] ");
			System.out.println("=========================================================================");
			System.out.println("======[ Mig Scheduler(제증명 이행) Ver 1.1  ]");
			System.out.println("=========================================================================");
			System.exit(0);
		} else {
			mig_task_name = args[0];     // Task이름은 mig날짜로 할것  20230601 오류시 20230601-1   
		}

//		try {
//			getSqlXML();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		System.exit(0);
		try {
			ExecutorService service = Executors.newFixedThreadPool(10);
			ConnectionPoolMig migpool = ConnectionPoolMig.getInstance();
//			ConnectionPoolStg stgpool = ConnectionPoolStg.getInstance();
			migconn = migpool.getConnection();
			conn1 = migpool.getConnection();       // 상태 Update용 Connection
			conn2 = migpool.getConnection();
			conn3 = migpool.getConnection();
			
			
			while(! endFlag) {
				
				// 0. connection 복구코드를 넣음.
				if(migconn == null || migconn.isClosed()) {
					migconn = getNewConnection();
				}				
				// 1. 처리할 작업 List를 가져온다.
				//    ( 선행작업이 끝난 작업의 List을 가져온다.)
				List<MigJob> joblist = getMigjobList(migconn);
//				for(MigJob job : joblist) {
//					System.out.println(job.getProcedureName());
//				}
				
				if(migpool.getFreeCount() > 0)
				
				// 2. procedure 형식에 따라 Task를 
				for(MigJob job : joblist) {
					System.out.println(job.getProcedureName() + " : 작업 Queue에 들어갑니다.");
					switch(job.getSqlType()) {
					case "PRC" :
						sql = "{ call " + job.getProcedureName() + "(?) }";
						service.submit(new migThread2.Task(job.getProcesId(), job.getProcedureName(), job.getDbName(), job.getSqlType(), sql));
						updateStatus(job.getProcesId(), "Q");
						break;
//					case "SQ" :
//						// sql = "";
//						service.submit(new migThread.Task(job.getProcesId(), job.getProcedureName(), job.getDbName(), job.getSqlType(), sql));
//						break;
					case "SQL" :
						// sql = "";
						sql = getSqlmigtool(migconn, job.getProcedureName());
						service.submit(new migThread2.Task(job.getProcesId(), job.getProcedureName(), job.getDbName(), job.getSqlType(), sql));
						updateStatus(job.getProcesId(), "Q");
						break;
					}
				}
				// 3. 작업이 모두 끝났는지 Check
				if(isMigComplete(migconn)) {
					endFlag = true;
				}
				
				// 10초후 작업을 재개 한다. (전체 작업이 끝날때까지 계속한다.)
				Thread.sleep(10000);
			}
			
			// 엑셀로 이행결과 출력
			//makeExcelTaskList(migconn);
			
			// 서비스 Thread를 Down한다.
			service.shutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
