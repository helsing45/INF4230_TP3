package main.java.model;

public class Node {
	private int position;
	private Link[] links;

	public Node(int pos) {
		position = pos;
	}

	public void addLink(Node n, Transport t) {
		if (links == null) {
			links = new Link[1];
			links[0] = new Link(this, t);
			links[0].setToNode(n);
		} else {
			Link[] temp = new Link[links.length + 1];
			for (int i = 0; i < links.length; i++)
				temp[i] = links[i];
			Link now = new Link(this, t);
			now.setToNode(n);
			temp[links.length] = now;
			links = temp;
		}
	}

	public int getPosition() {
		return position;
	}

	public Link[] getLinks() {
		return links;
	}
}
