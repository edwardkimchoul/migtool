package sqlparser.recognition;

import java.util.ArrayList;
import java.util.List;

public class item {
	public String axis;
	public String type;   /* I : Iteration, S : Selection */
	public List<String> wordList;
	
	public item() {
		wordList = new ArrayList<String>();
	}
}
