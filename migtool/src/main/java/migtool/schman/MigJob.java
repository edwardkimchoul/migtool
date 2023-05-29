package migtool.schman;

public class MigJob {

	private int    procesId;
	private String dbName;
	private String procedureName;
	private String sqlType;
	
	
	public int getProcesId() {
		return procesId;
	}
	public void setProcesId(int procesId) {
		this.procesId = procesId;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getProcedureName() {
		return procedureName;
	}
	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}
	public String getSqlType() {
		return sqlType;
	}
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}
	

	

}
