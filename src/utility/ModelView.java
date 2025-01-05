package utility;
import java.util.HashMap;
import com.google.gson.Gson;
import java.util.Map;

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

    public void convertToJsonData(){
       HashMap<String, Object> jsonData = new HashMap<>();
        Gson gson = new Gson();

        
        for (Map.Entry<String, Object> entry : this.getData().entrySet()) {
            String key = entry.getKey();
            Object value = gson.toJson(entry.getValue());
            jsonData.put(key, value);
        }

        this.setData(jsonData);
            }

    

}
