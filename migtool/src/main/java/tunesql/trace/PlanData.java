package tunesql.trace;

import java.util.ArrayList;
import java.util.List;

public class PlanData {
	int id;
	String operation;
	String name;
	int starts;
	long e_rows;
	long a_rows;
	String a_time;
	int  a_exec_sec;
	long buffers;
	long reads;
	long writes;
	long men0;
	long mem1;
	long used_mem;
	long used_temp;
	
	String filter_yn;
	int depth;
	List<Integer> leaf_list;

	public PlanData() {
		leaf_list = new ArrayList<Integer>();
	}
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getStarts() {
		return starts;
	}
	public void setStarts(int i) {
		this.starts = i;
	}
	public long getE_rows() {
		return e_rows;
	}
	public void setE_rows(long e_rows) {
		this.e_rows = e_rows;
	}
	public long getA_rows() {
		return a_rows;
	}
	public void setA_rows(long a_rows) {
		this.a_rows = a_rows;
	}
	public String getA_time() {
		return a_time;
	}
	public void setA_time(String a_time) {
		this.a_time = a_time;
	}
	public int getA_exec_sec() {
		return a_exec_sec;
	}
	public void setA_exec_sec(int a_exec_sec) {
		this.a_exec_sec = a_exec_sec;
	}
	public long getBuffers() {
		return buffers;
	}
	public void setBuffers(long buffers) {
		this.buffers = buffers;
	}
	public long getReads() {
		return reads;
	}
	public void setReads(long reads) {
		this.reads = reads;
	}
	public long getWrites() {
		return writes;
	}
	public void setWrites(long writes) {
		this.writes = writes;
	}
	public long getMen0() {
		return men0;
	}
	public void setMen0(long men0) {
		this.men0 = men0;
	}
	public long getMem1() {
		return mem1;
	}
	public void setMem1(long mem1) {
		this.mem1 = mem1;
	}
	public long getUsed_mem() {
		return used_mem;
	}
	public void setUsed_mem(long used_mem) {
		this.used_mem = used_mem;
	}
	public long getUsed_temp() {
		return used_temp;
	}
	public void setUsed_temp(long used_temp) {
		this.used_temp = used_temp;
	}
	public String getFilter_yn() {
		return filter_yn;
	}
	public void setFilter_yn(String filter_yn) {
		this.filter_yn = filter_yn;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public List<Integer> getLeaf_list() {
		return leaf_list;
	}

	public void addLeaf_list(int node_id) {
		leaf_list.add(node_id);
	}

	@Override
	public String toString() {
		return "PlanData [id=" + id + ", operation=" + operation + ", name=" + name + ", starts=" + starts + ", e_rows="
				+ e_rows + ", a_rows=" + a_rows + ", a_time=" + a_time + ", a_exec_sec=" + a_exec_sec + ", buffers="
				+ buffers + ", reads=" + reads + ", writes=" + writes + ", men0=" + men0 + ", mem1=" + mem1
				+ ", used_mem=" + used_mem + ", used_temp=" + used_temp + ", filter_yn=" + filter_yn + ", depth="
				+ depth + ", leaf_list=" + leaf_list + "]";
	}
}
