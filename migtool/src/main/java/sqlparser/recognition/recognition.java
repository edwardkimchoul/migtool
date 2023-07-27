package sqlparser.recognition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.gana.data.Item;
import com.gana.data.MetaData;
import com.gana.data.Token;
import com.gana.tool.parser.Lexer;

public class recognition {
	private static List<gitem> resultset;
	private static String axisliststr = "WITH|SELECT|FROM|WHERE|ORDER|GROUP";
	private static HashMap<String, String> axisHash;
	
	
	private static gitem iteration(int idx, int depth) {
		HashMap<Object, Integer> wordmap = new HashMap<Object, Integer>(); 
		gitem item = new gitem();
		
		for(int i=0; i<Lexer.tokens.size(); i++) {
			Token token = Lexer.tokens.get(i);
			String word = token.getStmt(); 
			
			if(word.equals("(")) {
				item = iteration(i, 1);
			} else if(word.equals(")")) {
				return item;
			}
			
			if(wordmap.containsKey(word)) {
				int cnt = wordmap.get(word);
				wordmap.put(word, cnt+1);
			} else {
				wordmap.put(word, 1);
			}
			 
		}
//		for(Item item : itemList) {
//			List<Token> tokenlist = item.getTokenList();
//			if( item.getName().equals("char") ) {     // ( , ,
//				for(Token token : tokenlist) {
//					if(wordmap.containsKey(token)) {
//						int cnt = wordmap.get(token);
//						wordmap.put(token.getStmt(), cnt+1);
//					} else {
//						wordmap.put(token.getStmt(), 1);
//					}
//				}
//			}  
//		}
		// 2. 최다 빈도수 구하기(Sort)
		List<Map.Entry<Object, Integer>> entries = new ArrayList<>(wordmap.entrySet());
		entries.sort((v1, v2) -> v1.getValue().compareTo(v2.getValue()));
		
		return new gitem();
	}

	private static void make_axis_hash() {
		String[] axisarr = axisliststr.split("|");
		
		axisHash = new HashMap<String, String>();
		for(String axis : axisarr) {
			axisHash.put(axis, axis);
		}
	}
	
	private static boolean isBraceBegin(gitem gitem) {
		if(gitem.ruleType == 'B' ) {
			List<Object> list = gitem.getWordList();
			if(((String)list.get(0)).equals("(")) {
				return true;
			}
		}
		return false;
	}
	private static boolean isBraceEnd(gitem gitem) {
		if(gitem.ruleType == 'B' ) {
			List<Object> list = gitem.getWordList();
			if(((String)list.get(0)).equals(")")) {
				return true;
			}
		}
		return false;
	}
	
	// 사전에 알고있는 정보를 통해 분리하고 Rule에 대한 기본 구조를 정함
	// ( ) , AND OR 
	private static void makeRule(List<gitem> itemList) {
		List<gitem> braceItemList = new ArrayList<gitem>();
		int brace_cnt = 0;
		for(gitem gitem : itemList) {
			if(isBraceBegin(gitem)) {
				if(brace_cnt == 0) {
					braceItemList = new ArrayList<gitem>();
				}
				brace_cnt++;
			} else if(isBraceEnd(gitem)) {
				brace_cnt--;
			} else {
				if(brace_cnt > 0) {
					braceItemList.add(gitem);
				} else {
					// 
					String axis = gitem.axis;
					switch(axis) {
						case "SELECT" :
						case "FROM" :
						case "ORDER" :
						case "GROUP" :
							
							break;
						case "WHERE" :
							break;
					}
					
				}
			}
		}
	}
	
	public static void main(String[] args) {

		make_axis_hash();
		
		String axis = "";
		List<gitem> itemList = new ArrayList<gitem>();
		
		try {		
			// 1. Lexcer process
			Lexer.parseRegex("C:\\work\\testdata\\Sql1.xml");

			// 2. 전처리
			for(int i=0; i<Lexer.tokens.size(); i++) {
				Token token = Lexer.tokens.get(i);
				if(axisHash.containsKey(token.getStmt().toUpperCase())) {
					axis =  token.getStmt().toUpperCase();
				}
				gitem gitem = new gitem();
				gitem.axis = axis;
				gitem.tokenType = token.getType();
				gitem.ruleType = 'B';
				gitem.addWord(token.getStmt());
				
				itemList.add(gitem);
			}

			// 3. 규칙 만들기
			makeRule(itemList);
		
//		} catch (SQLException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private static void loadMetaData() {
		
		
		HashMap<String, String> meta = new HashMap<String, String>();
		meta.put("SELECT", "SELECT");
		meta.put("FROM", "FROM");
		meta.put("WHERE", "WHERE");
		meta.put("TABLE_NAME", "column_name");
		meta.put("TABLE_NAME", "column");
		meta.put("CONV_TABLE", "table_name");
		meta.put("SCHEMA_NAME", "column_name");
		MetaData.setMetaHash(meta);
	}
}
