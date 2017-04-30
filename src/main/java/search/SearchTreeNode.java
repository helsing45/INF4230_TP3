package main.java.search;

import main.java.model.Action;
import main.java.model.State;
import main.java.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchTreeNode {

    private final SearchTreeNode parentNode;
    private final Action incomingAction;
    private final State representedState;
    private int visitCount;
    private double totalReward;
    private List<SearchTreeNode> childNodes;

    protected SearchTreeNode(State representedState) {
        this(null, null, representedState);
    }

    private SearchTreeNode(SearchTreeNode parentNode, Action incomingAction, State representedState) {
        this.parentNode = parentNode;
        this.incomingAction = incomingAction;
        this.representedState = representedState;
        this.visitCount = 0;
        this.totalReward = 0.0;
        this.childNodes = new ArrayList<>();
    }

    protected SearchTreeNode getParentNode() {
        return parentNode;
    }

    protected Action getIncomingAction() {
        return incomingAction;
    }

    protected int getVisitCount() {
        return visitCount;
    }

    protected int getParentsVisitCount() {
        return parentNode.getVisitCount();
    }

    protected List<SearchTreeNode> getChildNodes() {
        return childNodes;
    }

    protected boolean hasChildNodes() {
        return childNodes.size() > 0;
    }

    protected boolean representsTerminalState() {
        return representedState.isTerminal();
    }

    protected Player getRepresentedStatesPreviousAgent() {
        return representedState.getPreviousAgent();
    }

    protected boolean representedStatesCurrentAgentHasAvailableActions() {
        return representedState.getNumberOfAvailableActionsForCurrentAgent() > 0;
    }

    protected boolean isFullyExpanded() {
        return representedState.getNumberOfAvailableActionsForCurrentAgent() == childNodes.size();
    }

    protected boolean hasUnvisitedChild() {
        return childNodes.stream()
                .anyMatch(SearchTreeNode::isUnvisited);
    }

    private boolean isUnvisited() {
        return visitCount == 0;
    }

    protected SearchTreeNode addNewChildWithoutAction() {
        State childNodeState = getCopyOfRepresentedState();
        childNodeState.skipCurrentAgent();
        return appendNewChildInstance(childNodeState, null);
    }

    protected SearchTreeNode addNewChildFromAction(Action action) {
        if (!isUntriedAction(action))
            throw new IllegalArgumentException("Error: invalid action passed as function parameter");
        else
            return addNewChildFromUntriedAction(action);
    }

    private boolean isUntriedAction(Action action) {
        return getUntriedActionsForCurrentAgent().contains(action);
    }

    protected List<Action> getUntriedActionsForCurrentAgent() {
        List<Action> availableActions = representedState.getAvailableActionsForCurrentAgent();
        List<Action> untriedActions = new ArrayList<>(availableActions);
        List<Action> triedActions = getTriedActionsForCurrentAgent();
        untriedActions.removeAll(triedActions);
        return untriedActions;
    }

    private List<Action> getTriedActionsForCurrentAgent() {
        return childNodes.stream()
                .map(SearchTreeNode::getIncomingAction)
                .collect(Collectors.toList());
    }

    private SearchTreeNode addNewChildFromUntriedAction(Action incomingAction) {
        State childNodeState = getNewStateFromAction(incomingAction);
        return appendNewChildInstance(childNodeState, incomingAction);
    }

    private State getNewStateFromAction(Action action) {
        State representedStateClone = getCopyOfRepresentedState();
        representedStateClone.performActionForCurrentAgent(action);
        return representedStateClone;
    }

    protected State getCopyOfRepresentedState() {
        return representedState.copy();
    }

    private SearchTreeNode appendNewChildInstance(State representedState, Action incomingAction) {
        SearchTreeNode childNode = new SearchTreeNode(this, incomingAction, representedState);
        childNodes.add(childNode);
        return childNode;
    }

    protected void updateDomainTheoreticValue(double rewardAddend) {
        visitCount += 1;
        totalReward += rewardAddend;
    }

    protected double getDomainTheoreticValue() {
        return totalReward / visitCount;
    }
}