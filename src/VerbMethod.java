package utility;

import java.util.Objects;

public class VerbMethod {
    private String verb_name;
    private String method_name; 

    public VerbMethod(String verb_name, String method_name) {
        this.setMethodName(method_name);
        this.setVerbName(verb_name);
    }

    public String getVerbName() {
        return verb_name;
    }

    public void setVerbName(String verb_name) {
        this.verb_name = verb_name;
    }

    public String getMethodName() {
        return method_name;
    }

    public void setMethodName(String method_name) {
        this.method_name = method_name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VerbMethod that = (VerbMethod) obj;
        return Objects.equals(this.verb_name, that.verb_name) && Objects.equals(this.method_name, that.method_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verb_name, method_name);
    }
}
