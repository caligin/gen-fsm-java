
public class Lights implements Fsm<Lights.LightsState> {
    
    private final AnHttpClientOrSomeOtherDependency totallyAnHttpClient;

    public Lights(AnHttpClientOrSomeOtherDependency totallyAnHttpClient) {
        this.totallyAnHttpClient = totallyAnHttpClient;
    }

    public NextStep<LightsState> on(Push message, LightsState state) {
        ++state.pushed;
        return new NextStep<>("off", state);
    }

    public NextStep<LightsState> on(Smash message, LightsState state) {
        ++state.pushed;
        return new NextStep<>("broken", state);
    }

    public NextStep<LightsState> off(Push message, LightsState state) {
        ++state.pushed;
        return new NextStep<>("on", state);
    }

    public NextStep<LightsState> off(Smash message, LightsState state) {
        ++state.pushed;
        return new NextStep<>("broken", state);
    }

    public NextStep<LightsState> off(Object message, LightsState state) {
        //  catchall while in off state
        totallyAnHttpClient.notifyByHttpSomeoneThatThereHasBeenAnUnexpectedSomething();
        return new NextStep<>("off", state);
    }

    public NextStep<LightsState> broken(Object message, LightsState state) {
        // broken is an entirely catchall state
        totallyAnHttpClient.notifyByHttpSomeoneThatThereHasBeenAnUnexpectedSomething();
        return new NextStep<>("broken", state);
    }

    public static class Push {

        public final Object whateverDataGoesAlongWithThisMessage;

        public Push(Object whateverDataGoesAlongWithThisMessage) {
            this.whateverDataGoesAlongWithThisMessage = whateverDataGoesAlongWithThisMessage;
        }

    }

    public static class Smash {

        public final Object whateverDataGoesAlongWithThisMessage;

        public Smash(Object whateverDataGoesAlongWithThisMessage) {
            this.whateverDataGoesAlongWithThisMessage = whateverDataGoesAlongWithThisMessage;
        }

    }

    public static class ActuallyNotARecognizedMessage {

        public final Object whateverDataGoesAlongWithThisMessage;

        public ActuallyNotARecognizedMessage(Object whateverDataGoesAlongWithThisMessage) {
            this.whateverDataGoesAlongWithThisMessage = whateverDataGoesAlongWithThisMessage;
        }

    }

    public static class LightsState {

        public int pushed = 0;
    }
}
