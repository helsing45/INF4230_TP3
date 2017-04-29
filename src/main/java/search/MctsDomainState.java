package main.java.search;

import java.util.List;

public interface MctsDomainState<ActionT, AgentT extends MctsDomainAgent> {

    boolean isTerminal();
    AgentT getCurrentAgent();
    AgentT getPreviousAgent();
    int getNumberOfAvailableActionsForCurrentAgent();
    List<ActionT> getAvailableActionsForCurrentAgent();
    MctsDomainState performActionForCurrentAgent(ActionT action);
    MctsDomainState skipCurrentAgent();
}