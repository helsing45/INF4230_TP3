package main.java.model;

import java.util.Comparator;
import java.util.StringTokenizer;
public class Move implements Comparator<Object>, Comparable<Object> {
	int nodeIndex;
	Transport ticketType;

	public Move(int n, Transport t) {
		nodeIndex = n;
		ticketType = t;
	}

	public Move(String str) {
		StringTokenizer getFields = new StringTokenizer(str, " (");
		nodeIndex = Integer.parseInt(getFields.nextToken());
		String type = getFields.nextToken();
		ticketType = Transport.findByName(type.substring(0, type.length() - 1));
	}

	public int getScore() {
		return 10 * nodeIndex + ticketType.getId();
	}

	public int getNode() {
		return nodeIndex;
	}

	public Transport getType() {
		return ticketType;
	}

	@Override
	public String toString() {
		return String.format("%d (%s)", nodeIndex, ticketType.getName());
	}

	public int compare(Object o1, Object o2) {
		Move m1 = (Move) o1;
		Move m2 = (Move) o2;
		if (m1.getScore() < m2.getScore())
			return -1;
		else if (m1.getScore() == m2.getScore())
			return 0;
		else
			return 1;
	}

	public int compareTo(Object o) {
		Move m = (Move) o;
		return this.getScore() - m.getScore();
	}
}
