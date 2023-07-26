package tunesql.trace;

public class PredicateData {
	int id;
	String operation;
	String condition_str;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getCondition_str() {
		return condition_str;
	}
	public void setCondition_str(String condition_str) {
		this.condition_str = condition_str;
	}
	@Override
	public String toString() {
		return "PredicateData [id=" + id + ", operation=" + operation + ", condition_str=" + condition_str + "]";
	}
}

