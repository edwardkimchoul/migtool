package tunesql.trace;

import java.util.List;

public class TraceData {
	List<PlanData> planlist;
	List<PredicateData> predicatelist;
	
	public List<PlanData> getPlanlist() {
		return planlist;
	}
	public void setPlanlist(List<PlanData> planlist) {
		this.planlist = planlist;
	}
	public List<PredicateData> getPredicatelist() {
		return predicatelist;
	}
	public void setPredicatelist(List<PredicateData> predicatelist) {
		this.predicatelist = predicatelist;
	}
	
}
