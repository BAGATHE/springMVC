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
    public void initVariable() throws Exception{
        try {
            myHashMap = Util.getListControllerWithAnnotationMethodGet("packageController", getServletConfig());
    } catch (Exception e) {
            throw e;
    }
    
    }

    public void init() throws ServletException{
        try {
            initVariable();
        } catch (Exception e) {
            e.printStackTrace();
            e.getStackTrace();
            System.err.println(e.getMessage());
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

            Util.isDuplicateUrlMapping(pathInfo ,"packageController", getServletConfig());
            
            Mapping map = Util.findMappingAssociateUrl(myHashMap,pathInfo);
            if (map.getClassName() == null) {
                out.print(404);
            }else{
                Object result = Util.executeMethod(map.getClassName(),map.getMethodName(),request);
                if(Util.isStringOrModelview(result)){
                    if(result instanceof ModelView){
                        ModelView modelview = (ModelView) result;
                        Util.redirectModelView(request,response,modelview);
                    }else{
                        out.print(" la methode est de type string => " + result);
                    }
            }else{
             out.print("la methode n'est pas de type string ou modelView " + result);
            }
            }
    }catch(Exception e){
            e.printStackTrace();
            e.getStackTrace();
            System.err.println(e.getMessage());
            
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
