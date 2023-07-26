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

public class recognition {
	private static List<item> resultset;
	private static String axis = "WITH|SELECT|FROM|WHERE|ORDER|GROUP";
	
	private static List<item> iteration(String str) {

		HashMap<String, Integer> wordmap = new HashMap<String, Integer>(); 
		String whitespaceMetaChar = "\\s";
		String[] splited = str.split(whitespaceMetaChar);
		
		// 1. 단어별 빈도수 구하기
		for(int i=0; i<splited.length; i++) {
			if(wordmap.containsKey(splited[i])) {
				int cnt = wordmap.get(splited[i]);
				wordmap.put(splited[i], cnt+1);
			} else {
				wordmap.put(splited[i], 1);
			}
		}
		// 2. 최다 빈도수 구하기(Sort)
		List<Map.Entry<String, Integer>> entries = new ArrayList<>(wordmap.entrySet());
		entries.sort((v1, v2) -> v1.getValue().compareTo(v2.getValue()));
		
		
		
		
		return new ArrayList<item>();
	}

	private static void selection() {
		
		
	}
	
	public static void main(String[] args) {
		String inputLine;
		String whitespaceMetaChar = "\\s";
		try {		
			// 1. file 읽기 
			String filename = "c:/work/s_org.sql";
			BufferedReader dataReader = new BufferedReader(new FileReader(filename));
			while ((inputLine = dataReader.readLine()) != null) {
				String[] splited = inputLine.split(whitespaceMetaChar);
			}	
		
			// 2. 개별 데이터 일기 
			
			
			// 3. 규칙 만들기
		
//		} catch (SQLException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	
}
