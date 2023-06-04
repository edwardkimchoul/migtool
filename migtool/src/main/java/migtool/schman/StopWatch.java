package migtool.schman;

public class StopWatch {
	long start = 0;
	
	public StopWatch() {
		reset();
	}
	
	private void reset() {
		start = System.currentTimeMillis();
	}
	
	public int getEllapsed() {
		return Math.round(System.currentTimeMillis() - start)/1000;
	}
}
