package main.java.controller;

import main.java.model.Link;
import main.java.model.Node;
import main.java.model.Transport;
import main.java.model.player.Hider;
import main.java.model.player.Seeker;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

/**
 * Created by j-c9 on 2017-04-15.
 */
public class Controller {
    static private final int NO_OF_MOVES = 23;
    static private final int NO_OF_DETECTIVES = 5;
    static private final int CHECK_POINTS = 5;
    static private final int INF = 100;
    static private final int WIN = 200;
    static private final int LOSE = -200;
    static private final int NBEST = 1000000000;
    static private final int BLACK_TICKETS = 5;

    static private int DEPTH = 2;
    static private int[][] shortestDistance;
    static private Node[] nodes;
    static private Point[] nodePositions;
    static private int[] checkPoints;
    static private int noOfNodes;

    private int currentMoves;
    private Seeker[] detectives;
    private Hider MrX;

    public Controller() {
        currentMoves = 0;
        checkPoints = new int[CHECK_POINTS];
        for (int i = 0; i < CHECK_POINTS; i++)
            checkPoints[i] = 3 + 5 * i;
        readFile();
        readPosFile();
        detectives = new Seeker[NO_OF_DETECTIVES];
        int partition = nodes.length / NO_OF_DETECTIVES - 1;
        int part = 1;
        for (int i = 0; i < NO_OF_DETECTIVES; i++) {
            int rnd = (int) (partition * Math.random());
            int pos = rnd + part;
            detectives[i] = new Seeker(nodes[pos]);
            part += partition;
        }
        int xPos = 0;
        boolean done = true;
        do {
            xPos = 1 + (int) (noOfNodes * Math.random());
            for (int i = 0; i < NO_OF_DETECTIVES; i++)
                if (xPos == detectives[i].getPosition().getPosition())
                    done = false;

        } while (!done);
        MrX = new Hider(nodes[xPos]);
        MrX.setBlackTickets(BLACK_TICKETS);
        shortestDistance = new int[nodes.length][nodes.length];
        for (int i = 0; i < nodes.length; i++)
            for (int j = 0; j < nodes.length; j++)
                shortestDistance[i][j] = weight(nodes[i], nodes[j]);
        shortestDistance = getShortestDistanceMatrix(shortestDistance, 1);
    }

    /**
     * This method reads the text file which contains the map
     */
    private void readFile() {
        String fileName = "/res/SCOTMAP.TXT";
        try {
            File f = new File(getClass().getResource(fileName).getPath());
            if (!f.exists())
                throw new IOException();
            RandomAccessFile map = new RandomAccessFile(f, "r");
            String buffer = map.readLine();
            StringTokenizer token;
            token = new StringTokenizer(buffer);
            noOfNodes = Integer.parseInt(token.nextToken());
            nodes = new Node[noOfNodes];
            for (int i = 0; i < nodes.length; i++)
                nodes[i] = new Node(i);
            int lks = Integer.parseInt(token.nextToken());
            for (int i = 0; i < lks; i++) {
                buffer = map.readLine();
                token = new StringTokenizer(buffer);
                int node1 = Integer.parseInt(token.nextToken());
                int node2 = Integer.parseInt(token.nextToken());

                Transport type = Transport.findByAbbreviation(token.nextToken());
                nodes[node1].addLink(nodes[node2], type);
                nodes[node2].addLink(nodes[node1], type);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, String.format("Error opening file %s. Exiting!", fileName), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * This method reads the text file which contains the positions of the nodes
     * on the map
     */
    private void readPosFile() {
        String fileName = "/res/SCOTPOS.TXT";
        try {
            File f = new File(getClass().getResource(fileName).getPath());
            if (!f.exists())
                throw new IOException();
            RandomAccessFile map = new RandomAccessFile(f, "r");
            String buffer = map.readLine();
            StringTokenizer token = new StringTokenizer(buffer);
            noOfNodes = Integer.parseInt(token.nextToken());
            nodePositions = new Point[noOfNodes];

            for (int i = 0; i < noOfNodes; i++) {
                buffer = map.readLine();
                token = new StringTokenizer(buffer);
                int node = Integer.parseInt(token.nextToken());
                int x = Integer.parseInt(token.nextToken());
                int y = Integer.parseInt(token.nextToken());

                nodePositions[node] = new Point(x, y);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, String.format("Error opening file %s. Exiting!", fileName), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static int getNumberOfDetectives() {
        return Controller.NO_OF_DETECTIVES;
    }

    public Seeker[] getDetectives() {
        return detectives;
    }

    public Hider getMrX(){
        return MrX;
    }

    public boolean isCheckPoint(int move) {
        for (int checkPoint : checkPoints) {
            if(move == checkPoint){
                return true;
            }
        }
        return false;
    }

    public int getCurrentMoves() {
        return currentMoves;
    }

    private int weight(Node x, Node y) {
        if (x.equals(y))
            return 0;
        int weight = INF;
        Link[] lk = x.getLinks();
        for (int i = 0; i < lk.length; i++) {
            Node n = lk[i].getToNode();
            if (n.equals(y))
                weight = lk[i].getType().getId();
        }
        if (weight != INF && weight != Transport.FERRY.getId())
            weight = 1;
        return weight;
    }

    /**
     * This method evaluates the shortest distance between all the possible
     * nodes. It uses the Floyd-Warshall's Algorithm
     */
    private int[][] getShortestDistanceMatrix(int[][] mat, int k) {
        if (k == nodes.length - 1)
            return mat;
        int newMat[][] = new int[nodes.length][nodes.length];
        {
            for (int i = 0; i < nodes.length; i++)
                for (int j = 0; j < nodes.length; j++)
                    newMat[i][j] = Math.min(mat[i][j], mat[i][k] + mat[k][j]);
        }
        k = k + 1;
        return getShortestDistanceMatrix(newMat, k);
    }

    public Point getPoint(int nodeIndex) {
        return nodePositions[nodeIndex];
    }
}
