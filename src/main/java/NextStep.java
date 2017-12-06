public class NextStep<T> {
    
    public final String nextState;
    public final T stateHolder;

    public NextStep(String nextState, T stateHolder) {
        this.nextState = nextState;
        this.stateHolder = stateHolder;
    }
    
}
