package migtool.schman;

public class MigJob {

	private String dbName;
	private String procedureName;
	private String SqlType;
	
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
		return SqlType;
	}
	public void setSqlType(String sqlType) {
		SqlType = sqlType;
	}
	

}
