package utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URLDecoder;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import jakarta.servlet.http.*;  
import jakarta.servlet.*;
import java.lang.annotation.Annotation;
import annotation.Argument;
import annotation.Controller;
import annotation.Get;
import annotation.RestApi;
import controller.FrontController;
import java.util.HashMap;
import java.lang.reflect.*;
import com.thoughtworks.paranamer.Paranamer;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class Util {
    /*implementation de singleton pour avoir qu'une seul instance de Util*/

    // Instance unique de la classe Util
    private static Util instance;

    // Constructeur privé pour empêcher l'instanciation directe
    private Util() {
        // Initialisation si nécessaire
    }

    // Méthode pour obtenir l'instance unique
    public static Util getInstance() {
        if (instance == null) {
            instance = new Util();
        }
        return instance;
    }


    private  List<Class<?>> getListeClass(String packageController, ServletConfig servletConfig) throws Exception {
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


    public  HashMap<String, Mapping> getListControllerWithAnnotationMethodGet(String packageController, ServletConfig servletConfig) throws Exception {
        List<Class<?>> controllers = this.getListeClass(packageController,servletConfig);
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


    public  void isDuplicateUrlMapping(String url,String packageController, ServletConfig servletConfig)throws Exception{
        List<Class<?>> controllers = this.getListeClass(packageController,servletConfig);
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


    private  void invokeSetter(Object instance, String fieldName, String value) throws Exception {
        Method[] methods = instance.getClass().getMethods();
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (Method method : methods) {
            if (method.getName().equals(setterName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Object convertedValue = this.convertParamPrimitiveString(value, parameterTypes[0]);
                    method.invoke(instance, convertedValue);
                    return;
                }
            }
        }
        throw new NoSuchMethodException(
                "Setter method " + setterName + " not found in class " + instance.getClass().getName());
    }

       private  Object convertParamPrimitiveString(String value, Class<?> type)throws Exception {
        Object object = null;
        if (value != null && value != "") {
            try {
                if (type == String.class) {
                    object = (value);
                } else if (type == int.class || type == Integer.class) {
                    object = (Integer.parseInt(value));
                } else if (type == boolean.class || type == Boolean.class) {
                    object = (Boolean.parseBoolean(value));
                } else if (type == long.class || type == Long.class) {
                    object = (Long.parseLong(value));
                } else if (type == double.class || type == Double.class) {
                    object = (Double.parseDouble(value));
                } else if (type == float.class || type == Float.class) {
                    object = (Float.parseFloat(value));
                } else if (type == short.class || type == Short.class) {
                    object = (Short.parseShort(value));
                } else if (type == byte.class || type == Byte.class) {
                    object = (Byte.parseByte(value));
                } else if (type == char.class || type == Character.class) {
                    object = (value.charAt(0));
                }
            } catch (Exception e) {
                throw new Exception("Type de " + value + " est invalide car c'est de type = " + type.getName());
            }
        }
        return object;
    }

    private  List<Object> prepareParameters(Method methode, HttpServletRequest request,HttpServletResponse response) throws Exception {
        Paranamer paranamer = new AdaptiveParanamer();
       
        String[] parameterNames = paranamer.lookupParameterNames(methode);
        Parameter[] arguments = methode.getParameters();
        
        List<Object> resultats = new ArrayList<>();
        
        Map<String, Object> objectInstances = new HashMap<>();
        
        for (int i = 0; i < arguments.length; i++) {
            Argument annotationArg = arguments[i].getAnnotation(Argument.class);
            String parameterName = parameterNames[i];
            if (annotationArg != null && annotationArg.name() != null) {
                 parameterName = annotationArg.name();
            }
            String parameterValue = request.getParameter(parameterName);
            Class<?> parameterType = arguments[i].getType();

            if (parameterType.isPrimitive() || parameterType.equals(String.class)) {
                resultats.add(this.convertParamPrimitiveString(parameterValue, parameterType));
            }else if(parameterType.equals(MySession.class)){
                resultats.add(new MySession(request.getSession()));
            } else {
                final String finalParameterName = parameterName;
                final Parameter finalArgument = arguments[i];

                String[] paramterFullName = request.getParameterMap().keySet().stream()
                        .filter(key -> key.startsWith(finalParameterName + "."))
                        .toArray(String[]::new);

                if (paramterFullName.length > 0) {
                    Object instance = objectInstances.computeIfAbsent(finalParameterName, key -> {
                        try {
                            return finalArgument.getType().getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Failed to create instance of " + finalArgument.getType().getName(),
                                    e);
                        }
                    });

                    for (String paramName : paramterFullName) {
                        String fieldName = paramName.substring(paramName.indexOf('.') + 1);
                        String fieldValue = request.getParameter(paramName);
                        this.invokeSetter(instance, fieldName, fieldValue);
                    }
                    resultats.add(instance);
                } else {
                    resultats.add(null);
                }
            }
        }
        return resultats;
    }

   public  Object executeMethod(Mapping map, String urlMapping, HttpServletRequest request,HttpServletResponse response) throws Exception { 
   String className =  map.getClassName();
   String methodName = map.getMethodName();
   Class<?> myClass = Class.forName(className);
    Method method = null;
    Object instance = myClass.newInstance();
    this.checkControllerContainsAttributMySession(instance,request);
    Object result = null;
    for (Method m : myClass.getMethods()) {
        if (m.getName().equals(methodName) && 
            m.isAnnotationPresent(Get.class) && 
            urlMapping.equals(((Get) m.getAnnotation(Get.class)).value())) {
            method = m;
           
            // Vérifie si la méthode ne prend pas de paramètres
            if (m.getParameterCount() == 0) {
                // Méthode sans paramètres
                result = method.invoke(instance);
            } else {
                // Vérifie si les paramètres nécessaires sont disponibles dans la requête
               
                List<Object> methodParameters = this.prepareParameters(m, request,response);
                if (methodParameters.size() == m.getParameterCount()) {
                    result = method.invoke(instance, methodParameters.toArray(new Object[0]));
                } else {
                    throw new Exception("Le nombre de paramètres est insuffisant pour la méthode " + methodName);
                }
            }

            // Si la méthode est annotée avec @RestApi, convertir le résultat en JSON
            if (this.isRestApiMethode(method)) {
                String jsonResponse = this.convertToGson(result);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
                return null;  
            }
            break;
        }
           
    }
    if (method == null) {
        throw new NoSuchMethodException("Méthode non trouvée : " + methodName);
    }
    return result;
    }



    
//verifier si la methode est un restApi
private boolean isRestApiMethode(Method m){
   return  m.isAnnotationPresent(RestApi.class);
    
}


//convertir en gson response
 public String convertToGson(Object object) {
        Gson gson = new Gson();
        String jsonResponse ="";

        if (object instanceof ModelView) {
            ModelView modelView = (ModelView) object;
            // Récupère l'attribut `data` et le transforme en JSON
            jsonResponse =  gson.toJson(modelView.getData());
        } else {
          
            jsonResponse =  gson.toJson(object);
        }
        
        return jsonResponse;  
    }






    public  Mapping  findMappingAssociateUrl(HashMap<String, Mapping> myHashMap,String pathInfo)throws Exception{
        Mapping map = new Mapping();
        for (String key : myHashMap.keySet()) {
            if(key.equals(pathInfo)){
               map = myHashMap.get(key);
        }
     
    }
    return map;
    }


    public  boolean isStringOrModelview(Object object){
        if(object instanceof String || object instanceof ModelView){ return true; } return false;
    }


    public  void redirectModelView(HttpServletRequest request, HttpServletResponse response,ModelView modelview)throws ServletException, IOException{
        HashMap<String, Object> dataInHashmap = modelview.getData();
        for (String keyInData : dataInHashmap.keySet()) {
            request.setAttribute(keyInData,dataInHashmap.get(keyInData));
        }
        String redirection = modelview.getUrl();
                RequestDispatcher dispatcher = request.getRequestDispatcher("/"+redirection);
                dispatcher.forward(request,response);
    }

    /*fonction qui verifie si une classe controller contient une attribut d'instance mySession si il en a je sette la session en attribuant une liste de valeur */
    public  void checkControllerContainsAttributMySession(Object controller, HttpServletRequest request) {
        try {
            Class<?> clazz = controller.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == MySession.class) {
                    MySession mySession = new MySession(request.getSession());
                    field.setAccessible(true); 
                    field.set(controller, mySession);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Erreur lors de l'accès au champ", e);
        }
    }


    
   
}

 



