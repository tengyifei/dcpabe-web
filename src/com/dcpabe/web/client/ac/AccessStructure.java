package com.dcpabe.web.client.ac;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import com.dcpabe.web.client.MatrixElement;
import com.nkdata.gwt.streamer.client.Streamable;

public class AccessStructure implements Streamable {
	
	private static final long serialVersionUID = 1L;
	private static final int MAX_INT = 2147483647;
	private Map<Integer, String> rho;
	private Vector<Vector<Integer>> A;
	private TreeNode policyTree;

	private int partsIndex;

	public AccessStructure() {
		A = new Vector<Vector<Integer>>();
		rho = new HashMap<Integer, String>();
	}

	public Vector<Integer> getRow(int row) {
		return A.get(row);
	}

	public int getL() {
		return A.get(0).size();
	}

	public int getN() {
		return A.size();
	}

	public String rho(int i) {
		return rho.get(i);
	}

	public static AccessStructure buildFromPolicy(String policy) {
		AccessStructure arho = new AccessStructure();

		arho.generateTree(policy);

		arho.generateMatrix();

		return arho;
	}

	private void generateMatrix() {
		int c = computeLabels(policyTree);

		Queue<TreeNode> queue = new LinkedList<TreeNode>();
		queue.add(policyTree);

		while (!queue.isEmpty()) {
			TreeNode node = queue.poll();

			if (node instanceof InternalNode) {
				queue.add(((InternalNode) node).getLeft());
				queue.add(((InternalNode) node).getRight());
			} else {
				rho.put(A.size(), node.getName());
				((Attribute) node).setX(A.size());
				Vector<Integer> Ax = new Vector<Integer>(c);

				for (int i = 0; i < node.getLabel().length(); i++) {
					switch (node.getLabel().charAt(i)) {
					case '0':
						Ax.add(MatrixElement.ZERO);
						break;
					case '1':
						Ax.add(MatrixElement.ONE);
						break;
					case '*':
						Ax.add(MatrixElement.MINUS_ONE);
						break;
					}
				}

				while (c > Ax.size())
					Ax.add(MatrixElement.ZERO);
				A.add(Ax);
			}
		}
	}

	private int computeLabels(TreeNode root) {
		Queue<TreeNode> queue = new LinkedList<TreeNode>();
		StringBuffer sb = new StringBuffer();
		int c = 1;
		
		root.setLabel("1");
		queue.add(root);

		while (!queue.isEmpty()) {
			TreeNode node = queue.poll();

			if (node instanceof Attribute)
				continue;
			
			if (node instanceof OrGate) {
				((OrGate) node).getLeft().setLabel(node.getLabel());
				queue.add(((OrGate) node).getLeft());
				((OrGate) node).getRight().setLabel(node.getLabel());
				queue.add(((OrGate) node).getRight());
			} else if (node instanceof AndGate) {
				sb.delete(0, sb.length());

				sb.append(node.getLabel());

				while (c > sb.length())
					sb.append('0');
				sb.append('1');
				((AndGate) node).getLeft().setLabel(sb.toString());
				queue.add(((AndGate) node).getLeft());

				sb.delete(0, sb.length());

				while (c > sb.length())
					sb.append('0');
				sb.append('*');

				((AndGate) node).getRight().setLabel(sb.toString());
				queue.add(((AndGate) node).getRight());

				c++;
			}
		}
		
		return c;
	}

	private TreeNode generateTree(String[] policyParts) {
		partsIndex++;

		TreeNode node;

		if ("and".equals(policyParts[partsIndex])) {
			node = new AndGate();
		} else if ("or".equals(policyParts[partsIndex])) {
			node = new OrGate();
		} else {
			node = new Attribute(policyParts[partsIndex]);
		}
		
		if (node instanceof InternalNode) {
			((InternalNode) node).setLeft(generateTree(policyParts).setParent(node));
			((InternalNode) node).setRight(generateTree(policyParts).setParent(node));
		}

		return node;
	}

	private void generateTree(String policy) {
		partsIndex = -1;

		String[] policyParts = policy.split(" ");	

		policyTree = generateTree(policyParts);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((A == null) ? 0 : A.hashCode());
		result = prime * result + partsIndex;
		result = prime * result
				+ ((policyTree == null) ? 0 : policyTree.hashCode());
		result = prime * result + ((rho == null) ? 0 : rho.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AccessStructure))
			return false;
		AccessStructure other = (AccessStructure) obj;
		if (A == null) {
			if (other.A != null)
				return false;
		} else if (!A.equals(other.A))
			return false;
		if (partsIndex != other.partsIndex)
			return false;
		if (policyTree == null) {
			if (other.policyTree != null)
				return false;
		} else if (!policyTree.equals(other.policyTree))
			return false;
		if (rho == null) {
			if (other.rho != null)
				return false;
		} else if (!rho.equals(other.rho))
			return false;
		return true;
	}
	
	LinkedList<TreeNode> initialize_queue(Collection<String> pKeys){
		LinkedList<TreeNode> tmp = new LinkedList<TreeNode>();
		LinkedList<TreeNode> queue = new LinkedList<TreeNode>();
		
		tmp.add(policyTree);
		while (!tmp.isEmpty()) {
			TreeNode t = tmp.poll();
			
			//initialize
			t.full_satisfied = null;
			t.satisfied_num = MAX_INT;
			
			if (t instanceof Attribute) {
				if (pKeys.contains(t.getName())){
					t.satisfied_num = 1;
					queue.offer(t);
				}
			} else	if (t instanceof InternalNode) {
				
				if (t instanceof AndGate){	//initialize
					((AndGate) t).satisfied_num_left=MAX_INT;
					((AndGate) t).satisfied_num_right=MAX_INT;
					((AndGate) t).satisfied_left = false;
					((AndGate) t).satisfied_right = false;
				}
				
				tmp.add(((InternalNode) t).getLeft());
				tmp.add(((InternalNode) t).getRight());
			}
		}
		
		return queue;
	}

	public List<Integer> getIndexesList(Collection<String> pKeys) {
		
		List<Integer> selrows = null;

		LinkedList<TreeNode> queue = initialize_queue(pKeys);
		
		boolean reached=false;
		while (!queue.isEmpty()){
			TreeNode node=queue.remove();
			
			if (node==policyTree){	//reached root
				reached=true;
				continue;
			}
			
			TreeNode parent=node.getParent();
			if (parent instanceof AndGate){
				if (((AndGate)parent).canSatisfy(node))
					queue.offer(parent);
			}else{
				if (parent instanceof OrGate){
					if (node.satisfied_num < parent.satisfied_num){
						parent.full_satisfied = node;
						parent.satisfied_num = node.satisfied_num;
						queue.offer(parent);
					}
				}
			}
		}
		
		if (reached){
			selrows=new Vector<Integer>();
			queue.clear();
			
			queue.offer(policyTree);
			while (!queue.isEmpty()){
				TreeNode node=queue.remove();
				
				if (node instanceof Attribute){
					selrows.add(((Attribute) node).getX());
					continue;
				}
				
				if (node instanceof AndGate){
					queue.offer(((InternalNode)node).getLeft());
					queue.offer(((InternalNode)node).getRight());
					continue;
				}

				if (node instanceof OrGate){
					TreeNode child_node=node.full_satisfied;
					
					if (((OrGate)node).getLeft()==child_node)
						queue.offer(((OrGate)node).getLeft());
					else
						if (((OrGate)node).getRight()==child_node)
							queue.offer(((OrGate)node).getRight());
						else
							throw new IllegalArgumentException("dangling child?!");
				}
			}
			
		}else{
			//Not satisfiable!!!
			return null;
		}
		
		return selrows;
	}
}
