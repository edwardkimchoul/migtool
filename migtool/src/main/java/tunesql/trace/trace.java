package tunesql.trace;

import java.io.BufferedReader;
import java.io.FileReader;
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
    private static ArrayStack stack = new ArrayStack(100);
    private static List<PlanData> plan_list;
    private static List<PredicateData> predicate_list;
	
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
				val = Long.parseLong(str.trim().replaceAll("M", "")) * 100000;
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
		sec = sec*100 + Integer.parseInt(str.trim().substring(10));
		return sec;
	}
	
	private static void maketree (int id, int depth) {
		stack.put(id, depth);
		
        if(depth > 0) {		
    		int upper_id = stack.get(depth-1);
			for(PlanData plandata : plan_list) {
				if(plandata.getId() == upper_id) {
					plandata.addLeaf_list(id);
				}
			}
        }
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
					maketree(planData.getId(), planData.getDepth());
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

	private static void predicateParse(String predicate_str) {
		
		String str="", opr="", cond_str="";
	
		List<String> list = Arrays.asList(predicate_str.split("-"));
		if(list.size() >= 2) {
			PredicateData predicateData = new PredicateData();
			for(int i=0; i<2; i++) {
				switch(i) {
					case 0 :
						predicateData.setId( Integer.parseInt(list.get(i).trim()) );
						break;
					case 1 :
						str = list.get(i);
						opr = str.substring(0, str.indexOf("(")).trim();
						cond_str = str.substring(str.indexOf("(")+1).trim();
						predicateData.setOperation(opr);
						predicateData.setCondition_str(cond_str);
						predicate_list.add(predicateData);
						break;
				}
			}
		} else {
			str = predicate_str;
			PredicateData predicateData = predicate_list.get(predicate_list.size()-1);
			
			if(str.length() > 7 && str.indexOf("(") > 0) {
				opr = str.substring(0, str.indexOf("(")).trim();
				if(opr.equals("access") || opr.equals("filter")) {
					PredicateData predicateData1 = new PredicateData();
					opr = str.substring(0, str.indexOf("(")).trim();
					cond_str = str.substring(str.indexOf("(")+1).trim();
					
					predicateData1.setId(predicateData.getId());
					predicateData1.setOperation(opr);
					predicateData1.setCondition_str(cond_str);
					
					predicate_list.add(predicateData1);
					
				} else {
					cond_str = predicateData.getCondition_str();
					cond_str = cond_str.trim() + str.trim();
					predicateData.setCondition_str(cond_str);
					predicate_list.set(predicate_list.size()-1, predicateData);
					
				}
			} else {
				cond_str = predicateData.getCondition_str();
				cond_str = cond_str.trim() + str.trim();
				predicateData.setCondition_str(cond_str);
				predicate_list.set(predicate_list.size()-1, predicateData);
			}
		}
	}
	
	public static TraceData getTraceList(Connection conn, String sql_id) throws Exception {
		
		TraceData tracedata = new TraceData(); 
		plan_list = new ArrayList<PlanData>(); 
		predicate_list = new ArrayList<PredicateData>(); 
		
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
						if(output.trim().length() >= 2 && ! output.substring(0, 2).equals("--") ) {
							//List<String> list = Arrays.asList(output.split("-"));
							if(output.indexOf("-") > 0)
								predicateParse(output);
						}
						break;
				}				
				System.out.println(output);
			}
			
			tracedata.setPlanlist(plan_list);
			tracedata.setPredicatelist(predicate_list);
			
			for(PlanData plandata : plan_list) {
				System.out.println(plandata);
			}
			
			for(PredicateData predicatedata : predicate_list) {
				System.out.println(predicatedata);
			}

		    return tracedata;
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Plan table output error ");
		} 	
	}
	
	private static PlanData findPlanData(int id) {
		//PlanData plandata;
		for(PlanData plandata : plan_list ) {
			if(plandata.getId() == id) {
				return plandata;
			}
		}
		return null;
	}
	
	private static void execSql(Connection conn, String sql) throws Exception {
		ResultSet rs = null;
		String output = ""; 
		try {
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception(" Query Execution Error ... ");
		} 
	}
	
	public static void main(String[] args) {
		try {
			String key = "xview_11972";
		    String inputLine, sql = "/* " + key + " */ ";
			conn = getConnection();
			
			String filename = "c:/work/s_org.sql";
			BufferedReader dataReader = new BufferedReader(new FileReader(filename));
			while ((inputLine = dataReader.readLine()) != null) {
				sql = sql + inputLine + " \n";
			}
			
			execSql(conn, sql);
			String sql_id = getSqlID(conn, key);
			TraceData tracedata = getTraceList(conn, sql_id);
			
			List<Integer> leaf_list;
			int max_node_id=0, node_id;
			int max_time=1, leaf_time, a_time;
			long max_rows=1, leaf_rows, a_rows;
			PlanData plandataLeaf; 
            // Weak point 검색 
			for(PlanData plandata : plan_list) {
				// 1. 하위 node와 차이 비교
				node_id = plandata.getId();
				leaf_list =  plandata.getLeaf_list();
				
				max_node_id=0;
				max_time=1;
				max_rows=1;
				
				for(int leaf_node_id : leaf_list) {
					if(node_id == 189) {
						System.out.println("leaf_node_id in node_list : " + leaf_node_id);
					}
					plandataLeaf = findPlanData(leaf_node_id);
					if(node_id == 189) {
						System.out.println("  plandataLeaf.getId : " + plandataLeaf.getId());
					}
					
					leaf_time = plandataLeaf.getA_exec_sec();
					
					if(leaf_time > max_time) {
						max_time = leaf_time;
						max_node_id = plandataLeaf.getId();
						max_rows = plandataLeaf.getA_rows();
					}
				}

				a_time = plandata.getA_exec_sec();
				a_rows = plandata.getA_rows();
				
				if(node_id == 189) {
					System.out.println(node_id  + "::: a_time : [" + a_time +"] leaf_time : [" + max_time +"]  leaf_node_id : [" + max_node_id +"]" );
					if(a_time - max_time > 1000 ) {
						System.out.println("----10초 차이가 남-----");
					}
					System.out.println("a_time/max_time ---->[" + a_time/max_time + "]");
					if((a_time/max_time) > 10)  {
						System.out.println("----10배 차이가 남-----");
					}
				}
				
				if(a_time - max_time > 1000 && (a_time/max_time) > 10) {
					System.out.println(node_id  + ":::" + plandata);
				}  
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}