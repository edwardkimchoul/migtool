package test;

import java.util.Arrays;
import java.util.List;

public class simpleSpliter2 {

	public static void main(String[] args) {
		String  line = "   4 - filter((\"E\".\"REGISTRATIONDATE\">='20180101' AND \"E\".\"ACQUSTATUS\"='E' AND INTERNAL_FUNCTION(\"E\".\"LABDEPT\") AND \"E\".\"REGISTRATIONDATE\"<='20221231')) ";
		
		List<String> list = Arrays.asList(line.split("-"));
		
		int i = 1;
		for(String s : list) {
			System.out.println( i + " : " + s);
			i++;
		}
		
		String str = list.get(1);
		
		String str1 = str.substring(0, str.indexOf("("));
		
		System.out.println("str1 ----> " + str1);

	
	}
	
}
