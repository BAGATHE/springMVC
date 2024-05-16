package controller;

import javax.servlet.http.*;  
import javax.servlet.*;  
import java.io.*;
<<<<<<< HEAD
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import annotation.*;
=======

>>>>>>> 25f3bc6cd8171259407b539fa513c1df5c1a9c39
/**
 *
 * @author Pc
 */
public class FrontController extends HttpServlet {
<<<<<<< HEAD
    private List<Class<?>> listController;
=======

>>>>>>> 25f3bc6cd8171259407b539fa513c1df5c1a9c39
    /**
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
<<<<<<< HEAD
    private void  getController()throws Exception{
        String packageToScan = getServletConfig().getInitParameter("packageController");
        listController = new ArrayList<>();
                String path = getClass().getClassLoader().getResource(packageToScan.replace('.', '/')).getPath();
                String decodedPath = URLDecoder.decode(path, "UTF-8");
                File packageDir = new File(decodedPath);
            // Parcourir tous les fichiers dans le répertoire du package
            File[] files = packageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = packageToScan + "." + file.getName().replace(".class", "");
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(Controller.class)) {
                            listController.add(clazz);
                        }
                    }
                }
            }
        
        }  
        public void init() throws ServletException{
                try {
                        getController();
                } catch (Exception e) {
                        e.getStackTrace();
                }
        }
=======
>>>>>>> 25f3bc6cd8171259407b539fa513c1df5c1a9c39
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
<<<<<<< HEAD
            out.print("ato");
              for (Class<?> class1 : listController) {
                out.println("<h2> voici "+class1.getSimpleName()+"</h2>");    
        }
=======
          
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FrontController</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet FrontController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
>>>>>>> 25f3bc6cd8171259407b539fa513c1df5c1a9c39
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
