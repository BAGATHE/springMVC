package utility;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
public class FormData {
    private HttpServletRequest request;

  

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }



    public FormData() {
    }



    public FormData(HttpServletRequest request) {
        this.request = request;
    }

    public String getValue(String name){
        return this.request.getParameter(name);
    }

    public String[] getValues(String name){
        return this.request.getParameterValues(name);
    }

    public Object getAttribute(String name){
        return this.request.getAttribute(name);
    }

    
}