package migtool.schman;

import java.sql.*;
import java.util.*;

public final class ConnectionPoolStg {
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    private ArrayList<Connection> free;
    private ArrayList<Connection> used;

    private final static String url = "";
    private final static String user = "";
    private final static String password = "";
    private final static int initialCons = 0;
    private final static int maxCons = 0;

    private int numCons = 0;
    private static ConnectionPoolStg cp;

    // ConnectionPool 객체 리턴
    public static ConnectionPoolStg getInstance()  { // (String url, String user, String password, int initialCons, int maxCons) {
        try {
            if (cp == null) {
                synchronized (ConnectionPoolStg.class) {
                    cp = new ConnectionPoolStg(url, user,
                            password, initialCons, maxCons);
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return cp;
    }

    private ConnectionPoolStg(String url, String user, String password, int initialCons, int maxCons) throws SQLException {

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
        Connection _con = free.get(free.size() - 1);
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
}