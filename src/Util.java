package utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URLDecoder;
import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.*;  
import javax.servlet.*;
import java.lang.annotation.Annotation;
import annotation.Argument;
import annotation.Controller;
import annotation.Get;
import controller.FrontController;
import java.util.HashMap;
import java.lang.reflect.*;

public class Util {
    public static List<Class<?>> getListeClass(String packageController, ServletConfig servletConfig) throws Exception {
        List<Class<?>> listController = new ArrayList<>();
        String packageToScan = servletConfig.getInitParameter(packageController);
        if(Thread.currentThread().getContextClassLoader().getResource(packageToScan.replace('.', '/')) == null){
            throw new Exception("Package : " + packageController + " est null !");
        }
        String path = Thread.currentThread().getContextClassLoader().getResource(packageToScan.replace('.', '/')).getPath();
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
        return listController;
    }


    public static HashMap<String, Mapping> getListControllerWithAnnotationMethodGet(String packageController, ServletConfig servletConfig) throws Exception {
        List<Class<?>> controllers = Util.getListeClass(packageController,servletConfig);
        HashMap<String, Mapping> myHashMap = new HashMap<>();
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                // Vérification des annotations @Get sur les méthodes du contrôle
                if (method.isAnnotationPresent(Get.class)) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    String url = getAnnotation.value();
                    Mapping mapping = new Mapping(controller.getName(), method.getName());
                    myHashMap.put(url, mapping);
                }
            }
        }
        return myHashMap;
    }


    public static void isDuplicateUrlMapping(String url,String packageController, ServletConfig servletConfig)throws Exception{
        List<Class<?>> controllers = Util.getListeClass(packageController,servletConfig);
        int count = 0;
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                // Vérification des annotations @Get sur les méthodes du contrôle
                if (method.isAnnotationPresent(Get.class)) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    String urlAnnotation = getAnnotation.value();
                    if(url.equals(urlAnnotation)) count+=1;
                }
            }
        }
        if(count > 1){
            throw new Exception("Erreur : plusieurs méthodes associées à l'URL");
        }
    }

   public static Object executeMethod(String className, String methodName, HttpServletRequest request) throws Exception {
    Class<?> myClass = Class.forName(className);
    Method method = null;
    Object instance = myClass.newInstance();
    Object result = null;
    for (Method m : myClass.getMethods()) {
        if (m.getName().equals(methodName)) {
            // Vérifie si la méthode ne prend pas de paramètres
            if (m.getParameterCount() == 0) {
                method = m;
                // Méthode sans paramètres
                result = method.invoke(instance);
                break;
            } else {
                // Vérifie si les paramètres nécessaires sont disponibles dans la requête
                List<Object> methodParameters = Util.prepareParameter(m, request);
                if (methodParameters.size() == m.getParameterCount()) {
                    method = m;
                    result = method.invoke(instance, methodParameters.toArray(new Object[0]));
                    break;
                } else {
                    throw new Exception("Le nombre de paramètres est insuffisant pour la méthode " + methodName);
                }
            }
        }
    }
    if (method == null) {
        throw new NoSuchMethodException("Méthode non trouvée : " + methodName);
    }
    
    return result;
}

    public static Mapping  findMappingAssociateUrl(HashMap<String, Mapping> myHashMap,String pathInfo)throws Exception{
        Mapping map = new Mapping();
        for (String key : myHashMap.keySet()) {
            if(key.equals(pathInfo)){
               map = myHashMap.get(key);
        }
     
    }
    return map;
    }

    public static boolean isStringOrModelview(Object object){
        if(object instanceof String || object instanceof ModelView){ return true; } return false;
    }

    public static void redirectModelView(HttpServletRequest request, HttpServletResponse response,ModelView modelview)throws ServletException, IOException{
        HashMap<String, Object> dataInHashmap = modelview.getData();
        for (String keyInData : dataInHashmap.keySet()) {
            request.setAttribute(keyInData,dataInHashmap.get(keyInData));
        }
        String redirection = modelview.getUrl();
                RequestDispatcher dispatcher = request.getRequestDispatcher("/"+redirection);
                dispatcher.forward(request,response);
    }



    public static List<Object> prepareParameter(Method method, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, IOException {
        Parameter[] argument = method.getParameters();
        List<Object> result = new ArrayList<>();
        for (int i=0;i<argument.length;i++){
            // Récupère l'annotation Argument associée au paramètre couran
            Annotation arg_annotation = argument[i].getAnnotation(Argument.class);
            String name_annotation = "";
            if(arg_annotation != null){
                name_annotation = ((Argument) arg_annotation).name();
            }
            String realName = null;
            if (request.getParameter(name_annotation) != null){
                realName = name_annotation;
            }
            if (request.getParameter(argument[i].getName()) != null){
                realName = argument[i].getName();
            }
            if(realName != null){
                result.add(request.getParameter(realName));
            }

        }
        return result;
    }

}
