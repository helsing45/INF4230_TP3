package main.java.controller;

import main.java.model.Link;
import main.java.model.Move;
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
import java.util.TreeSet;

/**
 * Created by j-c9 on 2017-04-15.
 */
public class Controller {
    static private final int NO_OF_MOVES = 23;
    static private final int NO_OF_DETECTIVES = 5;
    static private final int CHECK_POINTS = 5;
    static public final int INF = 100;
    static public final int WIN = 200;
    static public final int LOSE = -200;
    static public final int NBEST = 1000000000;
    static private final int BLACK_TICKETS = 5;

    static public int DEPTH = 2;
    static public int[][] shortestDistance;
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

    public Controller(Controller board) {
        this.currentMoves = board.currentMoves;
        detectives = new Seeker[board.detectives.length];
        for (int i = 0; i < detectives.length; i++) {
            detectives[i] = new Seeker(board.detectives[i]);
        }
        Node n = board.MrX.getPosition();
        MrX = new Hider(n);
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

    public Hider getMrX() {
        return MrX;
    }

    public boolean isNeedToBeReveal(){
        return isNeedToBeReveal(currentMoves);
    }

    public boolean isNeedToBeReveal(int move) {
        for (int checkPoint : checkPoints) {
            if (move == checkPoint) {
                return true;
            }
        }
        return false;
    }

    public Move moveDetectives(int detectiveId){
        TreeSet<Move> moves = getDetectivePossibleMoves(detectiveId);
        return moves == null ? null : moves.first();
    }

    public TreeSet<Move> getDetectivePossibleMoves(int i) {
        return detectives[i].getPossibleMoves(this);
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

    public boolean isHiderAsWin() {
        boolean noneCanMove = true;
        for (int i = 0; i < NO_OF_DETECTIVES; i++)
            if (detectives[i].canMove(this))
                noneCanMove = false;
        return ((currentMoves == NO_OF_MOVES) || noneCanMove);
    }

    public boolean isSeekerAsWin() {
        Link[] xLinks = MrX.getPosition().getLinks();
        boolean isBlocked = true;
        for (int i = 0; i < xLinks.length; i++) {
            Node xNode = xLinks[i].getToNode();
            boolean thisIsOccupied = false;
            for (int j = 0; j < NO_OF_DETECTIVES; j++) {
                Node dNode = detectives[j].getPosition();
                if (dNode.equals(xNode))
                    thisIsOccupied = true;
            }
            if (!thisIsOccupied)
                isBlocked = false;
        }
        boolean isCaptured = false;
        for (int i = 0; i < NO_OF_DETECTIVES; i++) {
            Node detNode = detectives[i].getPosition();
            if (detNode.equals(MrX.getPosition()))
                isCaptured = true;
        }
        return (isBlocked || isCaptured);
    }

    public Move moveHider() {
        Move move = getMrX().getNextMove(this);
        currentMoves++;
        return move;
    }

    public void changeDetectivePosition(int detectiveId, Move move) {
        detectives[detectiveId].changePosition(nodes[move.getNode()], move.getType());
    }

    public static Node[] getNodes() {
        return nodes;
    }
}
