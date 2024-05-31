package controller;

import javax.servlet.http.*;  
import javax.servlet.*;  
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import annotation.*;
import utility.Util;
import utility.Mapping;
import utility.ModelView;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import java.lang.reflect.*;
/**
 *
 * @author Pc
 */
public class FrontController extends HttpServlet {
    HashMap<String, Mapping> myHashMap = new HashMap<>();


    /**
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public void initVariable() throws ServletException{
        try {
            myHashMap = Util.getListControllerWithAnnotationMethodGet("packageController", getServletConfig());
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
        //response.setContentType("text/html;charset=UTF-8");
        try{
            //url apres nom de domaine
            String requestURI = request.getRequestURI();
            //chemin du context
            String contextPath = request.getContextPath();
            //url apres le context
            String pathInfo = requestURI.substring(contextPath.length());
            for (String key : myHashMap.keySet()) {
                if(key.equals(pathInfo)){
                Mapping map = myHashMap.get(key);
                Object result = Util.executeMethod(map.getClassName(),map.getMethodName());
                if(result instanceof ModelView){
                    ModelView modelview = (ModelView) result;
                    HashMap<String, Object> dataInHashmap = modelview.getData();
                    for (String keyInData : dataInHashmap.keySet()) {
                        request.setAttribute(keyInData,dataInHashmap.get(keyInData));
                    }
                    String redirection = modelview.getUrl();
                    redirection += ".jsp";
                    if (!response.isCommitted()) {
                        try {
                            RequestDispatcher dispatcher = request.getRequestDispatcher(redirection);
                            dispatcher.forward(request, response);
                        } catch (Exception e) {
                            e.printStackTrace(); // Log l'exception dans la réponse pour le débogage
                        }
                    } else {
                        System.out.println("Response is already committed.");
                    }
                } else {
                    System.out.println("result is not instance of ModelView");
                }
                return;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
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
