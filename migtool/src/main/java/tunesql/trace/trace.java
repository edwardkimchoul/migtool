package tunesql.trace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class trace {

	private static Connection conn = null;
	
    public final static String url = "jdbc:oracle:thin:@10.90.10.24:1521:PROD2";
    public final static String user = "UGENS";
    public final static String password = "welcome123!";
	
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

	private static PlanData planParse(String plan_str) {
		
		
		return new PlanData();
	}
	public static String getTraceList(Connection conn, String sql_id) throws Exception {
		
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
						if(output.substring(0,15).equals("Plan hash value") ) {
							status = 1;
						}
						break;
					case 1:
						if(output.substring(0,21).equals("Predicate Information") ) {
							status = 2;
						} else if(output.charAt(0) == '|' &&  ! output.substring(0,4).equals("| Id") ) {
							PlanData plandata = planParse(output);
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
