
import com.fasterxml.jackson.annotation.JsonIgnore;

public class FsmState {
    // ok this class is a mess, ideally would only need to get out the classtype, nextState and State but we need a class.forname....
    // like, if we have  a custom deserializer then we can go token by token so we don't need extra fields
    // but won't write *that* now
    public String fsm;
    @JsonIgnore
    public Class<?> fsmClass;
    public String nextState;
    // and how do we deserialize the state????
    @JsonIgnore
    public Object state;
    
}
