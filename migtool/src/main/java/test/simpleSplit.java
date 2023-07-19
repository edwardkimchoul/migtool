package test;

import java.util.Arrays;
import java.util.List;

import tunesql.trace.PlanData;

public class simpleSplit {
	
	
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
		
		String timeStr = str.substring(0,8);
//		System.out.println("timeStr--->[" + timeStr +"]");
		
		return 0;
	}
	public static void main(String[] args) {
		String line = "|*  2 |   HASH JOIN                       |                  |      1 |     64M|     92M|00:00:53.64 |    3714K|   4686K|    590K|   146M|    15M| 6587K (1)|    3910M|";
//		String str = "a,b,c,d,e,f,g";


		List<String> list = Arrays.asList(line.split("\\|"));

		PlanData planData = new PlanData();
		int no = 0;
		for(String str : list) {
			switch(no) {
				case 1 :
					if(str.indexOf("*") >= 0) {
						planData.setFilter_yn("Y");
						planData.setId(Integer.parseInt(str.replace("*", "").trim()));   
					} else {
						planData.setFilter_yn("N");
						planData.setId(Integer.parseInt(str.trim()));
					}
					break;
				case 2 :
					planData.setOperation(str);
					planData.setDepth(countDepth(str));
					break;
				case 3 :
					planData.setName(str);
					break;
				case 4 :
					planData.setStarts(Integer.parseInt(str.trim()));
					break;
				case 5 :
					planData.setE_rows(calcValue(str));
					break;
				case 6 :
					planData.setA_rows(calcValue(str));
					break;
				case 7 :
					planData.setA_time(str);
					planData.setA_exec_sec(calcSecond(str));
					break;
				case 8 :
					planData.setBuffers(calcValue(str));
					break;
				case 9 :
					planData.setReads(calcValue(str));
					break;
				case 10 :
					planData.setWrites(calcValue(str));
					break;
				case 11 :
					planData.setMen0(calcValue(str));
					break;
				case 12 :
					planData.setMem1(calcValue(str));
					break;
				case 13 :
					planData.setUsed_mem(calcValue(str));
					break;
				case 14 :
					planData.setUsed_temp(calcValue(str));
					break;
			}
			System.out.println(no + " : [" + str + "]");
			no++;
		}
	}

}
