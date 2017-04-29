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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class Controller {

    private static final int[] STARTING_LOCATION_SEEKER = new int[]{13, 26, 29, 34, 50, 53, 91, 94, 103, 112, 117, 123, 138, 141, 155, 174};
    private static final int[] STARTING_LOCATION_HIDER = new int[]{35, 45, 51, 71, 78, 104, 106, 127, 132, 146, 166, 170, 172};
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
        for (int i = 0; i < CHECK_POINTS; i++) {
            checkPoints[i] = 3 + 5 * i;
        }
        nodes = BoardParser.getNodes();
        DistancesFileParser distancesFileParser = new DistancesFileParser(HIDERS_DISTANCES_FILE_NAME);
        hidersDistances = distancesFileParser.getParsedData();
        distancesFileParser = new DistancesFileParser(SEEKERS_DISTANCES_FILE_NAME);
        seekersDistances = distancesFileParser.getParsedData();
        initPlayerPosition();

        MrX.setBlackTickets(BLACK_TICKETS);
        shortestDistance = new int[nodes.length][nodes.length];
        for (int i = 0; i < nodes.length; i++)
            for (int j = 0; j < nodes.length; j++)
                shortestDistance[i][j] = weight(nodes[i], nodes[j]);
        shortestDistance = getShortestDistanceMatrix(shortestDistance, 1);
    }

    public void initPlayerPosition() {
        detectives = new Seeker[NO_OF_DETECTIVES];
        ArrayList<Integer> startingPosition = new ArrayList<>();
        int tempPosition;

        while (startingPosition.size() < NO_OF_DETECTIVES){
            tempPosition = STARTING_LOCATION_SEEKER[new Random().nextInt(STARTING_LOCATION_SEEKER.length - 1)];
            if(!startingPosition.contains(tempPosition)){
                startingPosition.add(tempPosition);
            }
        }

        for (int index = 0; index < startingPosition.size(); index++) {
            detectives[index] = new Seeker(nodes[startingPosition.get(index)]);
        }

        startingPosition.add(tempPosition = STARTING_LOCATION_HIDER[new Random().nextInt(STARTING_LOCATION_HIDER.length - 1)]);
        MrX = new Hider(nodes[tempPosition]);

        //Pour le debuggage
        System.out.print("Starting position ");
        for (Integer integer : startingPosition) {
            System.out.print(integer + ", ");
        }
        System.out.println();
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
        //TODO do the magic
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
        detectives[detectiveId].changePosition(nodes[move.getNode() - 1], move.getType());
    }

    public static Node[] getNodes() {
        return nodes;
    }
}
