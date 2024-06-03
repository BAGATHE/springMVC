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
import annotation.Controller;
import annotation.Get;
import controller.FrontController;
import java.util.HashMap;
import java.lang.reflect.*;

public class Util {
    public static List<Class<?>> getListeClass(String packageController, ServletConfig servletConfig) throws Exception {
        List<Class<?>> listController = new ArrayList<>();
        String packageToScan = servletConfig.getInitParameter(packageController);
        String path = Thread.currentThread().getContextClassLoader().getResource(packageToScan.replace('.', '/'))
                .getPath();
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

    public static Object executeMethod(String className,String methodName) throws Exception{
        Class<?> myclass = Class.forName(className);
        Method method = myclass.getMethod(methodName,new Class[0]);
        Object instance = myclass.newInstance();
        Object result = (Object)method.invoke(instance,new Object[0]);
        return result;
    }

    public static Mapping  findMappingAssociateUrl(HashMap<String, Mapping> myHashMap,String pathInfo)throws Exception{
        Mapping map = null;
        for (String key : myHashMap.keySet()) {
            if(key.equals(pathInfo)){
               map = myHashMap.get(key);
               break;
        }
    }
    return map;
    }

}
