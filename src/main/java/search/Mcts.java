package main.java.search;

import main.java.cloning.Cloner;
import main.java.game.Action;
import main.java.game.State;
import main.java.players.Player;

import java.util.Collections;
import java.util.List;

public class Mcts {

    private static final double NO_EXPLORATION = 0;

    private final int numberOfIterations;
    private double explorationParameter;
    private final Cloner cloner;

    public static Mcts initializeIterations(int numberOfIterations) {
        Cloner cloner = new Cloner();
        return new Mcts(numberOfIterations, cloner);
    }

    private Mcts(int numberOfIterations, Cloner cloner) {
        this.numberOfIterations = numberOfIterations;
        this.cloner = cloner;
    }

    public void dontClone(final Class<?>... classes) {
        cloner.dontClone(classes);
    }

    public Action uctSearchWithExploration(State state, double explorationParameter) {
        setExplorationForSearch(explorationParameter);
        MctsTreeNode rootNode = new MctsTreeNode(state, cloner);
        for (int i = 0; i < numberOfIterations; i++) {
            performMctsIteration(rootNode, state.getCurrentAgent());
        }
        return getNodesMostPromisingAction(rootNode);
    }

    private void setExplorationForSearch(double explorationParameter) {
        this.explorationParameter = explorationParameter;
    }

    private void performMctsIteration(MctsTreeNode rootNode, Player agentInvoking) {
        MctsTreeNode selectedChildNode = treePolicy(rootNode);
        State terminalState = getTerminalStateFromDefaultPolicy(selectedChildNode, agentInvoking);
        backPropagate(selectedChildNode, terminalState);
    }

    private MctsTreeNode treePolicy(MctsTreeNode node) {
        while (!node.representsTerminalState()) {
            if (!node.representedStatesCurrentAgentHasAvailableActions())
                return expandWithoutAction(node);
            else if (!node.isFullyExpanded())
                return expandWithAction(node);
            else
                node = getNodesBestChild(node);
        }
        return node;
    }


    private MctsTreeNode expandWithoutAction(MctsTreeNode node) {
        return node.addNewChildWithoutAction();
    }

    private MctsTreeNode expandWithAction(MctsTreeNode node) {
        Action randomUntriedAction = getRandomActionFromNodesUntriedActions(node);
        return node.addNewChildFromAction(randomUntriedAction);
    }

    private Action getRandomActionFromNodesUntriedActions(MctsTreeNode node) {
        List<Action> untriedActions = node.getUntriedActionsForCurrentAgent();
        Collections.shuffle(untriedActions);
        return untriedActions.get(0);
    }

    private MctsTreeNode getNodesBestChild(MctsTreeNode node) {
        validateBestChildComputable(node);
        return getNodesBestChildConfidentlyWithExploration(node, explorationParameter);
    }

    private void validateBestChildComputable(MctsTreeNode node) {
        if (!node.hasChildNodes())
            throw new UnsupportedOperationException("Error: operation not supported if child nodes empty");
        else if (!node.isFullyExpanded())
            throw new UnsupportedOperationException("Error: operation not supported if node not fully expanded");
        else if (node.hasUnvisitedChild())
            throw new UnsupportedOperationException(
                    "Error: operation not supported if node contains an unvisited child");
    }

    private Action getNodesMostPromisingAction(MctsTreeNode node) {
        validateBestChildComputable(node);
        MctsTreeNode bestChildWithoutExploration =
                getNodesBestChildConfidentlyWithExploration(node, NO_EXPLORATION);
        return bestChildWithoutExploration.getIncomingAction();
    }

    private MctsTreeNode getNodesBestChildConfidentlyWithExploration(
            MctsTreeNode node, double explorationParameter) {
        return node.getChildNodes().stream()
                .max((node1, node2) -> Double.compare(
                        calculateUctValue(node1, explorationParameter),
                        calculateUctValue(node2, explorationParameter))).get();
    }

    private double calculateUctValue(MctsTreeNode node, double explorationParameter) {
        return node.getDomainTheoreticValue()
                + explorationParameter
                * (Math.sqrt((2 * Math.log(node.getParentsVisitCount())) / node.getVisitCount()));
    }

    private State getTerminalStateFromDefaultPolicy(
            MctsTreeNode node, Player agentInvoking) {
        State nodesStateClone = node.getDeepCloneOfRepresentedState();
        return agentInvoking.getTerminalStateByPerformingSimulationFromState(nodesStateClone);
    }

    private void backPropagate(MctsTreeNode node, State terminalState) {
        while (node != null) {
            updateNodesDomainTheoreticValue(node, terminalState);
            node = node.getParentNode();
        }
    }

    private void updateNodesDomainTheoreticValue(MctsTreeNode node, State terminalState) {
        // violation of the law of demeter
        Player parentsStatesCurrentAgent = node.getRepresentedStatesPreviousAgent();
        double reward = parentsStatesCurrentAgent.getRewardFromTerminalState(terminalState);
        node.updateDomainTheoreticValue(reward);
    }
}