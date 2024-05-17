package controller;

import javax.servlet.http.*;  
import javax.servlet.*;  
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import annotation.*;
import utility.Util;

/**
 *
 * @author Pc
 */
public class FrontController extends HttpServlet {
    private List<Class<?>> listController;
    private boolean ischeck=false;


    /**
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public void initVariable() throws ServletException{
        try {
            listController = Util.getListeClass("packageController", getServletConfig());
    } catch (Exception e) {
            e.getStackTrace();
    }
    }
 

        public void init() throws ServletException{
                try {
                    initVariable();
                } catch (Exception e) {
                        e.getStackTrace();
                }
        }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try{
            if(!this.ischeck){
                this.initVariable();
                this.ischeck=true;
            }
           
              for (Class<?> class1 : listController) {
                out.println("<h2> voici "+class1.getSimpleName()+"</h2>");    
        }
        }catch(Exception e){

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
