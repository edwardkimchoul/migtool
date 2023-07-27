package sqlparser.recognition;

import java.util.ArrayList;
import java.util.List;

public class gitem {
	public String axis;
	public String tokenType;      
	public char ruleType;   /* B: basic,  I : Iteration, S : Selection, Q : Sequence */
	private List<Object> wordList;
	
	public gitem() {
		wordList = new ArrayList<Object>();
	}

	public List<Object> getWordList() {
		return wordList;
	}

	public void addWord(Object obj) {
		wordList.add(obj);
	}
}
