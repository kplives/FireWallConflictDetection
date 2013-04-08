package com.fwcd.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 防火墙决策树中的边类
 * 
 * @author horace
 * 
 */
public class TreeBranch {
	private Node head; // 该出边所指向的结点，头结点
	private Node tail; // 该出边所出发的结点，尾结点
	private List<Value> value; // 该出边的值

	public TreeBranch() {
		head = null;
		tail = null;
		value = null;
	}

	// 创建一条全新的边,并返回该边的路径
	public void CreateNewBranch(Rule rule, int fieldIndex, List<Value> value) {
		int rf_length = rule.getRSet().getFields().size(); // 获得该规则匹配集判定域个数
		this.value = value;
		Node nTemp = new Node();
		setHead(nTemp);
		fieldIndex++;
		while (fieldIndex < (rf_length - 1)) {// 防止越界,另外不需要取到最后一个域,即Action域
			Field fTemp = rule.getRSet().getFields().get(fieldIndex);
			TreeBranch bTemp = new TreeBranch();

			bTemp.setTail(nTemp);
			//取得该域值的一份拷贝
			List<Value> vlist = new ArrayList<Value>();
			for(Value v:fTemp.getValue()){
				Value v_temp = Value.copyValue(v);
				vlist.add(v_temp);	
			}
			bTemp.setValue(vlist);
			nTemp = new Node();
			bTemp.setHead(nTemp);

			fieldIndex++;
		}

	}

	// 复制一条已知的边
	public TreeBranch copyBranch(TreeBranch branch, Node tailNode) {

		TreeBranch bTemp = new TreeBranch();
		List<Value> vlist_temp = new ArrayList<Value>();
		for(Value v:branch.getValue()){
			vlist_temp.add(Value.copyValue(v));
		}
		bTemp.setValue(vlist_temp);
		bTemp.setTail(tailNode);

		Node nTemp = new Node();
		bTemp.setHead(nTemp);

		int branchIndex = 0;
		while (branchIndex < branch.getHead().getBranch_list().size()) { // 只要头结点的边集不为空
			// 复制子边集
			copyBranch(branch.getHead().getBranch_list().get(branchIndex),
					nTemp);
			branchIndex++;
		}
		return bTemp;
	}

	// 获取边的路径,只对新增加的边有用
	public FieldSet getPath() {
		List<Field> fListTemp = new ArrayList<Field>();
		List<Value> vlist_temp = new ArrayList<Value>();
		Field fTemp;
		Node nTemp = this.tail;
		// 回溯至根节点
		while (!nTemp.isRootNode()) {
			fTemp = new Field();
			vlist_temp = new ArrayList<Value>();
			for(Value v:nTemp.getEnterBranch().getValue()){
				vlist_temp.add(Value.copyValue(v));
			}
			fTemp.setValue(vlist_temp);
			fListTemp.add(fTemp);

			nTemp = nTemp.getEnterBranch().getTail();
		}

		FieldSet fields = new FieldSet();
		int fieldIndex = fListTemp.size() - 1; // 从最后一个开始倒着取
		while (fieldIndex >= 0) {
			fields.addField(fListTemp.get(fieldIndex));
			fieldIndex--;
		}// 完成了往前回溯

		fTemp = new Field();
		vlist_temp = new ArrayList<Value>();
		for(Value v:this.value){
			vlist_temp.add(Value.copyValue(v));
		}
		fTemp.setValue(vlist_temp); // 取得本边的值
		fields.addField(fTemp);

		// 往后遍历至叶子节点
		nTemp = head;
		while (!nTemp.isLeafNode()) {
			fTemp = new Field();
			vlist_temp = new ArrayList<Value>();
			for(Value v:nTemp.getBranch_list().get(0).getValue()){
				vlist_temp.add(Value.copyValue(v));
			}
			fTemp.setValue(vlist_temp);
			fields.addField(fTemp);
			
			nTemp = nTemp.getBranch_list().get(0).getHead();
		}

		return fields;
	}

	public Node getHead() {
		return head;
	}

	public void setHead(Node head) {
		this.head = head;
		head.setEnterBranch(this); // 在设置头结点的同时，将本边设置为结点的入边
	}

	public Node getTail() {
		return tail;
	}

	public void setTail(Node tail) {
		this.tail = tail;
		tail.addBranch(this); // 在设置为尾结点的同时，将本边加入结点的边列表
	}

	public List<Value> getValue() {
		return value;
	}

	public void setValue(List<Value> value) {
		this.value = value;
	}

}
