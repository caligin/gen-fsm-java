
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;

public class Dispatch {

    public static void main(String... args) throws NoSuchMethodException, IllegalAccessException, Throwable {
        final FsmState fsmState = deserializeFsmStateFromSomethingLikeMongo();

        final Object configuredFsm = getFromIoCContainerWithDependenciesSatisfied(fsmState.fsmClass);

        final Object messageIn = new Lights.Push(new Object());
        final NextStep nextState = dispatchMessage(configuredFsm, messageIn, fsmState);
        System.out.println(nextState.nextState);
        System.out.println(((Lights.LightsState) nextState.stateHolder).pushed);

        // and then serialize next state on db again
        final Object andNowTrySmashIt = new Lights.Smash(new Object());
        final NextStep afterSmashing = dispatchMessage(configuredFsm, andNowTrySmashIt, fsmState);
        System.out.println(afterSmashing.nextState);
        System.out.println(((Lights.LightsState) afterSmashing.stateHolder).pushed);

        // we can have catchalls to handle unexpected messages while we're in a specific state
        final Object whatHappensWithAnUnexpectedMessage = new Lights.ActuallyNotARecognizedMessage(new Object());
        final NextStep afterUnrecog = dispatchMessage(configuredFsm, whatHappensWithAnUnexpectedMessage, fsmState);
        System.out.println(afterUnrecog.nextState);
        System.out.println(((Lights.LightsState) afterUnrecog.stateHolder).pushed);

        // or not have catchalls and it just silently drops the message
        final FsmState forcedToOn = new FsmState() {
            {
                this.fsm = fsmState.fsm;
                this.nextState = "on";
                this.state = fsmState.state;
            }
        };
        final NextStep afterDroppingUnrecognized = dispatchMessage(configuredFsm, whatHappensWithAnUnexpectedMessage, forcedToOn);
        System.out.println(afterDroppingUnrecognized.nextState);
        System.out.println(((Lights.LightsState) afterDroppingUnrecognized.stateHolder).pushed);
    }

    public static FsmState deserializeFsmStateFromSomethingLikeMongo() throws Throwable {
        final String fsmDocumentFromMongo = "{'fsm': 'Lights', 'nextState':'off', 'state': {'pushed':9}}".replace('\'', '"');
        final String letsPretendWeCanDeserializeJustThisSubdocument = "{'pushed':9}".replace('\'', '"'); // errr don't remember how the mongo api looks like lol
        final ObjectMapper om = new ObjectMapper();
        final FsmState fsmState = om.readValue(fsmDocumentFromMongo, FsmState.class);

        // this section reflects on the generic type declared on the iface to understand how to deserialize the state
        // this can probs go in a custom jackson deserializer
        fsmState.fsmClass = Class.forName(fsmState.fsm);
        if (!Fsm.class.isAssignableFrom(fsmState.fsmClass)) {
            throw new IllegalStateException("not an fsm");
        }
        final ParameterizedType pt = ((ParameterizedType) fsmState.fsmClass.getGenericInterfaces()[0]);
        final Class<?> stateClassType = (Class<?>) pt.getActualTypeArguments()[0];
        fsmState.state = om.readValue(letsPretendWeCanDeserializeJustThisSubdocument, stateClassType);
        return fsmState;
    }

    private static NextStep dispatchMessage(Object configuredFsm, Object message, FsmState state) throws Exception, Throwable {
        try {
            final MethodHandle handleToStateCall = MethodHandles.publicLookup().findVirtual(configuredFsm.getClass(), state.nextState, MethodType.methodType(NextStep.class, new Class[]{message.getClass(), state.state.getClass()}));
            return (NextStep) handleToStateCall.invoke(configuredFsm, message, state.state);
        } catch (NoSuchMethodException notFound) {
            // try find a catchall for the state
            try {
                final MethodHandle handleToStateCall = MethodHandles.publicLookup().findVirtual(configuredFsm.getClass(), state.nextState, MethodType.methodType(NextStep.class, new Class[]{Object.class, state.state.getClass()}));
                return (NextStep) handleToStateCall.invoke(configuredFsm, message, state.state);
            } catch (NoSuchMethodException ignore) {
                // not found again then we just assume that the fsm cannot handle and does not want to catchall so it stays as it is
                return new NextStep(state.nextState, state.state);
            }
        }
    }

    private static Object getFromIoCContainerWithDependenciesSatisfied(Class<?> fsm) {
        // faking it here but either pico or hk2 would be more than happy to make and inject a new instance by classtype
        return new Lights(new AnHttpClientOrSomeOtherDependency());
    }

}
