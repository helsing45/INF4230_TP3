package main.java.search;

import main.java.cloning.Cloner;
import main.java.game.Action;
import main.java.game.State;
import main.java.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MctsTreeNode {

    private final MctsTreeNode parentNode;
    private final Action incomingAction;
    private final State representedState;
    private int visitCount;
    private double totalReward;
    private List<MctsTreeNode> childNodes;
    private final Cloner cloner;

    protected MctsTreeNode(State representedState, Cloner cloner) {
        this(null, null, representedState, cloner);
    }

    private MctsTreeNode(MctsTreeNode parentNode, Action incomingAction,
                         State representedState, Cloner cloner) {
        this.parentNode = parentNode;
        this.incomingAction = incomingAction;
        this.representedState = representedState;
        this.visitCount = 0;
        this.totalReward = 0.0;
        this.childNodes = new ArrayList<>();
        this.cloner = cloner;
    }

    protected MctsTreeNode getParentNode() {
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

    protected List<MctsTreeNode> getChildNodes() {
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
                .anyMatch(MctsTreeNode::isUnvisited);
    }

    private boolean isUnvisited() {
        return visitCount == 0;
    }

    protected MctsTreeNode addNewChildWithoutAction() {
        State childNodeState = getDeepCloneOfRepresentedState();
        childNodeState.skipCurrentAgent();
        return appendNewChildInstance(childNodeState, null);
    }

    protected MctsTreeNode addNewChildFromAction(Action action) {
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
                .map(MctsTreeNode::getIncomingAction)
                .collect(Collectors.toList());
    }

    private MctsTreeNode addNewChildFromUntriedAction(Action incomingAction) {
        State childNodeState = getNewStateFromAction(incomingAction);
        return appendNewChildInstance(childNodeState, incomingAction);
    }

    private State getNewStateFromAction(Action action) {
        State representedStateClone = getDeepCloneOfRepresentedState();
        representedStateClone.performActionForCurrentAgent(action);
        return representedStateClone;
    }

    protected State getDeepCloneOfRepresentedState() {
        return cloner.deepClone(representedState);
    }

    private MctsTreeNode appendNewChildInstance(
            State representedState, Action incomingAction) {
        MctsTreeNode childNode = new MctsTreeNode(this, incomingAction, representedState, cloner);
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