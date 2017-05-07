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

    public SearchTree(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public Action searchWithExploration(State state, double explorationParameter) {
        this.explorationParameter = explorationParameter;
        SearchTreeNode rootNode = new SearchTreeNode(state);
        for (int i = 0; i < numberOfIterations; i++) {
            performIteration(rootNode, state.getCurrentAgent());
        }
        return getNodesMostPromisingAction(rootNode);
    }

    private void performIteration(SearchTreeNode rootNode, Player player) {
        SearchTreeNode selectedChildNode = treePolicy(rootNode);
        State terminalState = getTerminalStateFromDefaultPolicy(selectedChildNode, player);
        backPropagate(selectedChildNode, terminalState);
    }

    private SearchTreeNode treePolicy(SearchTreeNode node) {
        while (!node.representsTerminalState()) {
            if (!node.representedStatesCurrentAgentHasAvailableActions()) return expandWithoutAction(node);
            else if (!node.isFullyExpanded()) return expandWithAction(node);
            else node = getNodesBestChild(node);
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
        return getNodesBestChildConfidentlyWithExploration(node, explorationParameter);
    }

    private Action getNodesMostPromisingAction(SearchTreeNode node) {
        SearchTreeNode bestChildWithoutExploration = getNodesBestChildConfidentlyWithExploration(node, NO_EXPLORATION);
        return bestChildWithoutExploration.getIncomingAction();
    }

    private SearchTreeNode getNodesBestChildConfidentlyWithExploration(SearchTreeNode node, double explorationParameter) {
        return node.getChildNodes().stream().max((node1, node2) -> Double.compare(calculateValue(node1, explorationParameter), calculateValue(node2, explorationParameter))).get();
    }

    private double calculateValue(SearchTreeNode node, double explorationParameter) {
        return node.getDomainTheoreticValue() + explorationParameter * (Math.sqrt((2 * Math.log(node.getParentsVisitCount())) / node.getVisitCount()));
    }

    private State getTerminalStateFromDefaultPolicy(SearchTreeNode node, Player player) {
        State nodesStateClone = node.getCopyOfRepresentedState();
        return player.getTerminalStateByPerformingSimulationFromState(nodesStateClone);
    }

    private void backPropagate(SearchTreeNode node, State terminalState) {
        while (node != null) {
            updateNodesDomainTheoreticValue(node, terminalState);
            node = node.getParentNode();
        }
    }

    private void updateNodesDomainTheoreticValue(SearchTreeNode node, State terminalState) {
        double reward =  node.getRepresentedStatesPreviousAgent().getRewardFromTerminalState(terminalState);
        node.updateDomainTheoreticValue(reward);
    }
}