package main.java.search;

import main.java.model.Action;
import main.java.model.State;
import main.java.players.Player;

import java.util.Collections;
import java.util.List;

public class SearchTree {

    private static final double NO_EXPLORATION = 0;

    private final int numberOfIterations;
    private double explorationParameter;

    public static SearchTree initializeIterations(int numberOfIterations) {
        return new SearchTree(numberOfIterations);
    }

    private SearchTree(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public Action uctSearchWithExploration(State state, double explorationParameter) {
        setExplorationForSearch(explorationParameter);
        SearchTreeNode rootNode = new SearchTreeNode(state);
        for (int i = 0; i < numberOfIterations; i++) {
            performMctsIteration(rootNode, state.getCurrentAgent());
        }
        return getNodesMostPromisingAction(rootNode);
    }

    private void setExplorationForSearch(double explorationParameter) {
        this.explorationParameter = explorationParameter;
    }

    private void performMctsIteration(SearchTreeNode rootNode, Player agentInvoking) {
        SearchTreeNode selectedChildNode = treePolicy(rootNode);
        State terminalState = getTerminalStateFromDefaultPolicy(selectedChildNode, agentInvoking);
        backPropagate(selectedChildNode, terminalState);
    }

    private SearchTreeNode treePolicy(SearchTreeNode node) {
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


    private SearchTreeNode expandWithoutAction(SearchTreeNode node) {
        return node.addNewChildWithoutAction();
    }

    private SearchTreeNode expandWithAction(SearchTreeNode node) {
        Action randomUntriedAction = getRandomActionFromNodesUntriedActions(node);
        return node.addNewChildFromAction(randomUntriedAction);
    }

    private Action getRandomActionFromNodesUntriedActions(SearchTreeNode node) {
        List<Action> untriedActions = node.getUntriedActionsForCurrentAgent();
        Collections.shuffle(untriedActions);
        return untriedActions.get(0);
    }

    private SearchTreeNode getNodesBestChild(SearchTreeNode node) {
        //validateBestChildComputable(node);
        return getNodesBestChildConfidentlyWithExploration(node, explorationParameter);
    }

    /*private void validateBestChildComputable(SearchTreeNode node) {
        if (!node.hasChildNodes())
            throw new UnsupportedOperationException("Error: operation not supported if child nodes empty");
        else if (!node.isFullyExpanded())
            throw new UnsupportedOperationException("Error: operation not supported if node not fully expanded");
        else if (node.hasUnvisitedChild())
            throw new UnsupportedOperationException(
                    "Error: operation not supported if node contains an unvisited child");
    }*/

    private Action getNodesMostPromisingAction(SearchTreeNode node) {
        //validateBestChildComputable(node);
        SearchTreeNode bestChildWithoutExploration =
                getNodesBestChildConfidentlyWithExploration(node, NO_EXPLORATION);
        return bestChildWithoutExploration.getIncomingAction();
    }

    private SearchTreeNode getNodesBestChildConfidentlyWithExploration(
            SearchTreeNode node, double explorationParameter) {
        return node.getChildNodes().stream()
                .max((node1, node2) -> Double.compare(
                        calculateUctValue(node1, explorationParameter),
                        calculateUctValue(node2, explorationParameter))).get();
    }

    private double calculateUctValue(SearchTreeNode node, double explorationParameter) {
        return node.getDomainTheoreticValue()
                + explorationParameter
                * (Math.sqrt((2 * Math.log(node.getParentsVisitCount())) / node.getVisitCount()));
    }

    private State getTerminalStateFromDefaultPolicy(
            SearchTreeNode node, Player agentInvoking) {
        State nodesStateClone = node.getCopyOfRepresentedState();
        return agentInvoking.getTerminalStateByPerformingSimulationFromState(nodesStateClone);
    }

    private void backPropagate(SearchTreeNode node, State terminalState) {
        while (node != null) {
            updateNodesDomainTheoreticValue(node, terminalState);
            node = node.getParentNode();
        }
    }

    private void updateNodesDomainTheoreticValue(SearchTreeNode node, State terminalState) {
        // violation of the law of demeter
        Player parentsStatesCurrentAgent = node.getRepresentedStatesPreviousAgent();
        double reward = parentsStatesCurrentAgent.getRewardFromTerminalState(terminalState);
        node.updateDomainTheoreticValue(reward);
    }
}