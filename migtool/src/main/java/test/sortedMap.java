package test;

import java.util.SortedMap;
import java.util.TreeMap;

public class sortedMap {

	public static void main(String[] args) {
	    SortedMap<String, Integer> map = new TreeMap<>((s1, s2) -> s1.length() - s2.length());
	    map.put("banana", 50);
	    map.put("cocoa", 90);
	    map.put("avocado", 10);
	    map.put("cocoa", 40);
	    
	    map.keySet().forEach(key -> {
	        System.out.println(key + " -> " + map.get(key));
	    });
	}
}
