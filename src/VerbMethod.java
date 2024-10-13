package utility;

public class VerbMethod{
private String verb;
private String method;


public VerbMethod(String verb,String method){
    this.setMethod(method);
    this.setVerb(verb);
}

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


}