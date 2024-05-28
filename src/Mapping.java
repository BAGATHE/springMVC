package utility;
public class Mapping {
  private Object obj;
  private String className;
  private String methodName;
  
 // Constructeur pour initialiser les attributs
  public Mapping(Object ob,String className, String methodName) {
        this.setObj(ob);
        this.setClassName(className);
        this.setMethodName(methodName);
  }

// Getters et setters pour les attributs    
  public Object getObj(){
    return obj;
  }

  public void setObj(Object obj){
    this.obj = obj;
  }

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
