package tunesql.trace;

public class TraceData {
	PlanData planData;
	PredicateData predicateData;
	
	public PlanData getPlanData() {
		return planData;
	}
	public void setPlanData(PlanData planData) {
		this.planData = planData;
	}
	public PredicateData getPredicateData() {
		return predicateData;
	}
	public void setPredicateData(PredicateData predicateData) {
		this.predicateData = predicateData;
	}
}
