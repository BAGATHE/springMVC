package utility;
public class Mapping {
  private String className;
  private String methodName;
  
 // Constructeur pour initialiser les attributs
  public Mapping(String className, String methodName) {

        this.setClassName(className);
        this.setMethodName(methodName);
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
    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
    
}
