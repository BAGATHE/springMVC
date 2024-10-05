package utility;
public class Mapping {
  private String className;
  private String methodName;
  private String verbe;
  
 
  // Constructeur pour initialiser les attributs
public Mapping (){ 
}

  public Mapping(String className, String methodName,String verbe) {

        this.setClassName(className);
        this.setMethodName(methodName);
        this.setVerbe(verbe);
  }
// Getters et setters pour les attributs    
  public String getClassName() {
        return className;
  }
    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getVerbe(){
      return verbe;
    }

    public void setVerbe(String verbe){
      this.verbe = verbe;
    }


    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
    
}
