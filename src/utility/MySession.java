package utility;
import jakarta.servlet.http.HttpSession;
public class MySession {
    private HttpSession session;

    // Constructeur vide
    public MySession() {}

    // Constructeur avec HttpSession
    public MySession(HttpSession session) {
        this.session = session;
    }

    // Getters et setters
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    // Méthode get(String key)
    public Object get(String key) {
        return session.getAttribute(key);
    }

    // Méthode add(String key, Object objet)
    public void add(String key, Object objet) {
        session.setAttribute(key, objet);
    }

    // Méthode delete(String key)
    public void delete(String key) {
        session.removeAttribute(key);
    }

}
