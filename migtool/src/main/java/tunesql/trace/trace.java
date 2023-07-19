package tunesql.trace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class trace {

	private static Connection conn = null;
	
	private final static String url = "jdbc:oracle:thin:@10.90.10.24:1521:PROD2";
    private final static String user = "UGENS";
    private final static String password = "welcome123!";
    
    private static List<String> planHeaderList;
	
    private static Connection getConnection() throws SQLException {
    	
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
    
	/*+
	 * 현재 작업의 Update를 위해
	 */
	public static String getSqlID(Connection conn, String key_string) throws Exception {
		
		final String sql1 ="SELECT SUBSTR(SQL_TEXT, 1, 30) SQL_TEXT,  \n"
				+ "            SQL_ID, CHILD_NUMBER  \n"
				+ "     FROM V$SQL  \n"
				+ "     WHERE SQL_TEXT LIKE '%' || ? || '%'";
		String sql_id = "";

		ResultSet rs = null;
		 
		try {
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql1);

			pstmt.setString(1, key_string);
			
			rs = pstmt.executeQuery();
			if(rs.next()) {
				sql_id = rs.getString("SQL_ID");
			}
		    
		    return sql_id;
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Data Select error  ");
		} 	
	}
	private static int countDepth(String str) {
		int depth = 0;
		for(int i=0; i<str.length(); i++) {
			if(str.charAt(i) == ' ') {
				depth = i;
			} else {
				break;
			}
		}
		return depth;	
	}
	
	private static long  calcValue(String str) {
		long val = 0;
		if(! str.trim().equals("")) {
			
			int pos = str.indexOf("(");
			if(pos > 0) {
				str = str.substring(0, str.indexOf("(")-1);
			}
			
			char ch = str.trim().charAt(str.trim().length()-1);
			if(ch == 'M') {
				val = Long.parseLong(str.trim().replaceAll("M", "")) * 1000000;
			} else if(ch == 'K') {
				val = Long.parseLong(str.trim().replaceAll("K", "")) * 1000;
			} else {
				val = Long.parseLong(str.trim());
			}
		}
		return val;
	}
	private static int calcSecond(String str) {
		
		int sec = 0;
		String[] splitStr = str.trim().substring(0,8).split(":");

		for(int i=0; i< 3; i++) {
			sec = sec * 60 + Integer.parseInt(splitStr[i].trim()); 
		}
		return sec;
	}
	
	private static PlanData planParse(String plan_str) {
		List<String> list = Arrays.asList(plan_str.split("\\|"));

		PlanData planData = new PlanData();
		int no = 0;
		for(String str : list) {
			switch(planHeaderList.get(no).trim()) {
				case "Id" :
					if(str.indexOf("*") >= 0) {
						planData.setFilter_yn("Y");
						planData.setId(Integer.parseInt(str.replace("*", "").trim()));   
					} else {
						planData.setFilter_yn("N");
						planData.setId(Integer.parseInt(str.trim()));
					}
					break;
				case "Operation" :
					planData.setOperation(str);
					planData.setDepth(countDepth(str));
//					System.out.println( planData.getOperation());
//					System.out.println( planData.getDepth());
					break;
				case "Name" :
					planData.setName(str);
					break;
				case "Starts" :
					planData.setStarts(Integer.parseInt(str.trim()));
					break;
				case "E-Rows" :
					planData.setE_rows(calcValue(str));
					break;
				case "A-Rows" :
					planData.setA_rows(calcValue(str));
					break;
				case "A-Time" :
					planData.setA_time(str);
					planData.setA_exec_sec(calcSecond(str));
//					System.out.println( planData.getA_exec_sec() );
					break;
				case "Buffers" :
					planData.setBuffers(calcValue(str));
//					System.out.println( planData.getBuffers());
					break;
				case "Reads" :
					planData.setReads(calcValue(str));
					break;
				case "Writes" :
					planData.setWrites(calcValue(str));
					break;
				case "OMem" :
					planData.setMen0(calcValue(str));
					break;
				case "1Mem" :
					planData.setMem1(calcValue(str));
					break;
				case "Used-Mem" :
					planData.setUsed_mem(calcValue(str));
//					System.out.println( planData.getUsed_mem() );
					break;
				case "Used-Tmp" :
					planData.setUsed_temp(calcValue(str));
//					System.out.println( planData.getUsed_temp() );
					break;
			}
//			System.out.println(no + " : [" + str + "]");
			no++;
		}	
		return planData;
    }
	
	public static String getTraceList(Connection conn, String sql_id) throws Exception {
		
		List<PlanData> plan_list = new ArrayList<PlanData>(); 
		
		final String sql1 ="SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR( ?, 0, 'ALLSTATS LAST'))  ";

		ResultSet rs = null;
		String output = ""; 
		try {
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql1);

			pstmt.setString(1, sql_id);
			
			rs = pstmt.executeQuery();
			int status = 0;
			while(rs.next()) {
				output = rs.getString("PLAN_TABLE_OUTPUT");
				switch(status) {
					case 0:
						if(output.length() >= 15 && output.substring(0,15).equals("Plan hash value") ) {
							status = 1;
						}
						break;
					case 1:
						if(output.length() >= 21 && output.substring(0,21).equals("Predicate Information") ) {
							status = 2;
						} else if(output.charAt(0) == '|' &&  output.substring(0,4).equals("| Id") ) {
							planHeaderList = Arrays.asList(output.split("\\|"));
//							int i = 0;
//							for(String str : planHeaderList) {
//								System.out.println( i + " : " + str);
//								i++;
//							}
						} else if(output.charAt(0) == '|' &&  ! output.substring(0,4).equals("| Id") ) {
							//PlanData plandata = planParse(output);
							plan_list.add(planParse(output));
						}
						break;
					case 2:
						break;
					case 3:
						break;
					case 4:
						break;
				}
				System.out.println(output);
			}
			
		    return sql_id;
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Plan table output error ");
		} 	
	}
	
	public static void main(String[] args) {
		try {
			conn = getConnection();

			String sql_id = getSqlID(conn, "년월");
			getTraceList(conn, sql_id);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
