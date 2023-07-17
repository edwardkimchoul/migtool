package migtool.schman;

import java.sql.*;
import java.util.*;

public final class ConnectionPoolMig {
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    private ArrayList<Connection> free;
    private ArrayList<Connection> used;

//    private final static String url = "jdbc:oracle:thin:@10.90.10.22:1521:DBMGR";
//    private final static String user = "UGENS";
//    private final static String password = "ugens123";
    public final static String url = "jdbc:oracle:thin:@10.90.10.24:1521:PROD2";
    public final static String user = "UGENS";
    public final static String password = "ugens123";
    private final static int initialCons = 10;
    private final static int maxCons = 20;

    private int numCons = 0;
    private static ConnectionPoolMig cp;

    // ConnectionPool 객체 리턴
    public static ConnectionPoolMig getInstance()  { // (String url, String user, String password, int initialCons, int maxCons) {
        try {
            if (cp == null) {
                synchronized (ConnectionPoolMig.class) {
                    cp = new ConnectionPoolMig(url, user,
                            password, initialCons, maxCons);
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return cp;
    }

    private ConnectionPoolMig(String url, String user, String password, int initialCons, int maxCons) throws SQLException {

        if (initialCons < 0)
            initialCons = 5;
        if (maxCons < 0)
            maxCons = 10;

        free = new ArrayList<Connection>(initialCons);
        used = new ArrayList<Connection>(initialCons);

        while (numCons < initialCons) {

            addConnection();
        }
    }

    private void addConnection() throws SQLException {
        free.add(getNewConnection());
    }

    private Connection getNewConnection() throws SQLException {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ++numCons;
        return con;
    }

    public synchronized Connection getConnection() throws SQLException {

    	if (free.isEmpty()) {
            while (numCons < maxCons) {
                addConnection();
            }
        }
    	System.out.println("Connection pool free size --->" + free.size());
        Connection _con = free.get(free.size() - 1);

        // 0. 외부에 의해 Connection이 끊겼을때를 위한 복구코드
		if(_con == null || _con.isClosed()) {
			_con = getNewConnection();
		}
		
        free.remove(_con);
        used.add(_con);
        return _con;
    }

    public synchronized void releaseConnection(Connection _con) throws SQLException {
        boolean flag = false;
        if (used.contains(_con)) {
            used.remove(_con);
            numCons--;
            flag = true;
        } else {
            throw new SQLException();
        }
        try {
            if (flag) {
                free.add(_con);
                numCons++;
            } else {
                _con.close();
            }

        } catch (SQLException e) {

            try {
                _con.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void closeAll() {
        for (int i = 0; i < used.size(); i++) {
            Connection _con = (Connection) used.get(i);
            used.remove(i--);
            try {
                _con.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }

        for (int i = 0; i < free.size(); i++) {
            Connection _con = (Connection) free.get(i);
            free.remove(i--);
            try {
                _con.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    public int getMaxCons() {
        return maxCons;
    }

    public int getNumCons() {

        return numCons;
    }
    public int getFreeCount() {

        return free.size();
    }
}