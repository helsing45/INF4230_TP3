package main.java.controller;

import main.java.model.Link;
import main.java.model.Move;
import main.java.model.Node;
import main.java.model.Transport;
import main.java.model.player.Hider;
import main.java.model.player.Seeker;
import main.java.utils.BoardParser;
import main.java.utils.DistancesFileParser;

import java.awt.*;
import java.util.List;
import java.util.TreeSet;

public class Controller {

    private static final String HIDERS_DISTANCES_FILE_NAME = "src/res/hiders_distances_file.xml";
    private static final String SEEKERS_DISTANCES_FILE_NAME = "src/res/seekers_distances_file.xml";
    private static final int NO_OF_MOVES = 23;
    private static final int NO_OF_DETECTIVES = 5;
    private static final int CHECK_POINTS = 5;
    public static final int INF = 100;
    public static final int WIN = 200;
    public static final int LOSE = -200;
    public static final int NBEST = 1000000000;
    private static final int BLACK_TICKETS = 5;

    public static int DEPTH = 2;
    public static int[][] shortestDistance;
    private static Node[] nodes;

    private final List<List<Integer>> hidersDistances;
    private final List<List<Integer>> seekersDistances;
    private static int[] checkPoints;
    private static int noOfNodes;

    private int currentMoves;
    private Seeker[] detectives;
    private Hider MrX;

    public Controller() {
        currentMoves = 0;
        checkPoints = new int[CHECK_POINTS];
        for (int i = 0; i < CHECK_POINTS; i++)
            checkPoints[i] = 3 + 5 * i;
        nodes = BoardParser.getNodes();
        DistancesFileParser distancesFileParser = new DistancesFileParser(HIDERS_DISTANCES_FILE_NAME);
        hidersDistances = distancesFileParser.getParsedData();
        distancesFileParser = new DistancesFileParser(SEEKERS_DISTANCES_FILE_NAME);
        seekersDistances = distancesFileParser.getParsedData();

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
                if (xPos == detectives[i].getPosition().getId())
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

    public static int getNumberOfDetectives() {
        return Controller.NO_OF_DETECTIVES;
    }

    public Seeker[] getDetectives() {
        return detectives;
    }

    public Hider getMrX() {
        return MrX;
    }

    public boolean isNeedToBeReveal() {
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

    public Move moveDetectives(int detectiveId) {
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
        return nodes[nodeIndex].getBoardPosition();
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
