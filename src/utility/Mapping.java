package utility;
import java.util.HashSet;
public class Mapping {
   private String className;
    private HashSet<VerbMethod> listMethodVerb; 

    public Mapping(String className,HashSet<VerbMethod> listMethodVerb) {
        this.setClassName(className);
        this.setListMethodVerb(listMethodVerb);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public HashSet<VerbMethod> getListMethodVerb() {
        return listMethodVerb;
    }

    public void setListMethodVerb(HashSet<VerbMethod> listMethodVerb) {
        this.listMethodVerb = listMethodVerb;
    }
    
}
