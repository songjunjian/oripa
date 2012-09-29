package oripa.appstate;

import oripa.paint.EditMode;


/**
 * This class holds current state and (only one) previous state 
 * to help getting back.
 * 
 * @author koji
 *
 */
public class StateManager implements StateManagerInterface<EditMode>{
	
	//-----------------------------------------------------------------
	// singleton implementation
	private static StateManager instance = null;
	
	public static StateManager getInstance(){
		if(instance == null){
			instance = new StateManager();
		}
		
		return instance;
	}
	
	//-----------------------------------------------------------------
	// Instance implementation
	
	private ApplicationState<EditMode> current, lastInputCommand, previous;

	
	@Override
	public ApplicationState<EditMode> getCurrent() {
		return current;
	}
	
	/**
	 * push {@code s} as a new state to be held. 
	 * the current state will be dropped to previous state.
	 * @param s new state
	 */
	@Override
	public void push(ApplicationState<EditMode> s){
		// keep for popLastInputCommand()
		if(s.getGroup() == EditMode.INPUT){
			lastInputCommand = s;
		}
		previous = current;
		current = s;
	}

	/**
	 * pop previous state. It  will be set to current state.
	 * @return previous state.
	 */
	@Override
	public ApplicationState<EditMode> pop(){
		current = previous;
		return current;
	}

	/**
	 * This method accepts INPUT only.
	 * the current state will be dropped to previous state.
	 * @param group ID.
	 * @return last state of the group. 
	 * {@code null} if {@code group} is not {@code oripa.paint.EditMode.INPUT}.
	 */
	@Override
	public ApplicationState<EditMode> popLastOf(EditMode group) {
		if(group != EditMode.INPUT){
			return null;
		}

		return popLastInputCommand();
		
	}
	
	/**
	 * for the action of "input" radio button.
	 * the current state will be dropped to previous state.
	 * @return state of the last input command
	 */
	public ApplicationState<EditMode> popLastInputCommand(){
		previous = current;
		current = lastInputCommand;
		return current;
	}
	
}
