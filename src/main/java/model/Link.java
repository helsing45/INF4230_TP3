package main.java.model;

public class Link {
	@SuppressWarnings("unused")
	private Node from;
	private Node to;
	private Transport type;

	Link(Node n, Transport t) {
		from = n;
		type = t;
	}

	public void setToNode(Node x) {
		to = x;
	}

	public Node getToNode() {
		return to;
	}

	public Transport getType() {
		return type;
	}

}
