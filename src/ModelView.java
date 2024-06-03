package utility;
import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    // Constructeur par défaut
    public ModelView() {
        this.data = new HashMap<>();
    }

    // Constructeur avec paramètres
    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }

    
    public String getUrl() {
        return url;
    }

 
    public void setUrl(String url) {
        this.url = url;
    }

  
    public HashMap<String, Object> getData() {
        return data;
    }

    
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

  
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

 
    public Object getData(String key) {
        return this.data.get(key);
    }

   
    public void removeData(String key) {
        this.data.remove(key);
    }

    @Override
    public String toString() {
        return "ModelView{" +
                "url='" + url + '\'' +
                ", data=" + data +
                '}';
    }

}
