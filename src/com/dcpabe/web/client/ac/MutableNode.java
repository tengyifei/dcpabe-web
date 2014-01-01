package com.dcpabe.web.client.ac;

public class MutableNode extends InternalNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int type=0;
	
	public MutableNode(){}
	
	public int getType() {
		return type;
	}

	public void setType(int _type) {
		type=_type;
	}
	
	public MutableNode getRight() {
		return (MutableNode) right;
	}
	
	public MutableNode getLeft() {
		return (MutableNode) left;
	}

	@Override
	String getName() {
		return null;
	}
}
