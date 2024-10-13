package controller;

import jakarta.servlet.http.*;  
import jakarta.servlet.*;  
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import annotation.*;
import utility.Util;
import utility.Mapping;
import utility.ModelView;
import java.util.HashMap;
import jakarta.servlet.annotation.WebServlet;
import java.lang.reflect.*;
import com.google.gson.Gson;
/**
 *
 * @author Pc
 */
public class FrontController extends HttpServlet {
    HashMap<String, Mapping> myHashMap = new HashMap<>();
    public void initVariable() throws Exception{
        try {
             Util util = Util.getInstance();
            myHashMap = util.getListControllerWithAnnotationMethodUrl("packageController", getServletConfig());
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
            Util util = Util.getInstance();
            //url apres nom de domaine
            String requestURI = request.getRequestURI();
            //chemin du context
            String contextPath = request.getContextPath();
            //url apres le context
            String pathInfo = requestURI.substring(contextPath.length());

           
            
            int queryIndex = pathInfo.indexOf('?');
            String urlMapping = pathInfo;
            if (queryIndex != -1) {
                urlMapping = pathInfo.substring(0, queryIndex);
            }
            //util.isDuplicateUrlMapping(urlMapping ,"packageController", getServletConfig());
            Mapping map = util.findMappingAssociateUrl(myHashMap,urlMapping);
            
            if (map.getClassName() == null) {
                out.print("pas de mapping associe a cette url");
                return ;
            }else{
                util.checkControllerContainsAttributMySession(map.getClassName(),request);
                Object result = util.executeMethod(map,urlMapping,request,response);

                if (result == null) {
                    out.print("result null");
                return; 
                }

                if(util.isStringOrModelview(result)){
                    if(result instanceof ModelView){
                        ModelView modelview = (ModelView) result;
                        util.redirectModelView(request,response,modelview);
                    }else{
                        throw new Exception(" ERREUR 500 la methode est de type string => " + result);
                    }
            }else{
                throw new Exception(" ERREUR 500 la methode n'est pas de type string ou modelView " + result);
            }
            }
    }catch(Exception e){
            e.printStackTrace();
            e.getStackTrace();
            e.getMessage();
            out.print(e.getMessage());
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
