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
        PrintWriter out = response.getWriter();
        try{
            //url apres nom de domaine
            String requestURI = request.getRequestURI();
            //chemin du context
            String contextPath = request.getContextPath();
            //url apres le context
            String pathInfo = requestURI.substring(contextPath.length());
            Mapping map = Util.findMappingAssociateUrl(myHashMap,pathInfo);
            Object result = Util.executeMethod(map.getClassName(),map.getMethodName());
                if(result instanceof ModelView){
                    ModelView modelview = (ModelView) result;
                    HashMap<String, Object> dataInHashmap = modelview.getData();
                    for (String keyInData : dataInHashmap.keySet()) {
                        request.setAttribute(keyInData,dataInHashmap.get(keyInData));
                    }
                    String redirection = modelview.getUrl();
                            RequestDispatcher dispatcher = request.getRequestDispatcher("/"+redirection);
                            dispatcher.forward(request,response);
                
            }else{
             out.print((String)result);
            }
    }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
