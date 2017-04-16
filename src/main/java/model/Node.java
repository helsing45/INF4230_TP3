package main.java.model;

import java.awt.*;

public class Node {
	private int position;
	private Point boardPosition;
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

	public int getId() {
		return position;
	}

	public Point getBoardPosition() {
		return boardPosition;
	}

	public void setBoardPosition(Point boardPosition) {
		this.boardPosition = boardPosition;
	}

	public void setBoardPosition(String x, String y){
		this.boardPosition = new Point(Integer.valueOf(x),Integer.valueOf(y));
	}

	public Link[] getLinks() {
		return links;
	}
}
