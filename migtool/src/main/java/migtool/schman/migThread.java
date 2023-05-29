package migtool.schman;

public class migThread {
    public static class Task implements Runnable {
	    String sql;
	    String dbname;

	    public Task(String dbname, String Sql) {
	      this.dbname = dbname;
	      this.sql = sql;
	      
	    }
	    
	    @Override
	    public void run() {
//	      log.info("called!");

	      try {
	        Thread.sleep(1000);
	      } catch (Exception e) {
	        e.printStackTrace();
	      }

//	      log.info("finished!");
	    }
	  }
}
