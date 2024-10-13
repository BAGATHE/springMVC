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
import annotation.Url;
import annotation.Post;
import java.util.HashSet;

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

    private String getAnnotationVerbeMethod(Method method){
        String methode = "get";
       if(method.isAnnotationPresent(Post.class)){
                    methode = "post";
                    return methode;
       }
       return methode;
    }


    public  HashMap<String, Mapping> getListControllerWithAnnotationMethodUrl(String packageController, ServletConfig servletConfig) throws Exception {
        List<Class<?>> controllers = this.getListeClass(packageController,servletConfig);
        HashMap<String, Mapping> myHashMap = new HashMap<>();
        List<String> listeErreur = new ArrayList();
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                // Vérification des annotations @Url sur les méthodes du contrôle
                if (method.isAnnotationPresent(Url.class)) {
                    String verb = "get";
                    verb = this.getAnnotationVerbeMethod(method);
                    Url annotation = method.getAnnotation(Url.class);
                    String url = annotation.value();
                    Mapping mapping = new Mapping(controller.getName(),new HashSet<>());
                    if(!myHashMap.isEmpty() && myHashMap.containsKey(url)){
                        boolean isAdded = myHashMap.get(url).getListMethodVerb().add(new VerbMethod(verb,method.getName()));
                        if (!isAdded) {
                            listeErreur
                                .add("L'URL " + url + " est dupliquee dans la Class : " + controller.getName()
                                        + " , Method : " + method.getName() + ". \n");   
                        }
                       
                    }else {
                        mapping.getListMethodVerb().add(new VerbMethod(verb,method.getName()));
                        myHashMap.put(url, mapping);
                    }
                }
            }
        }
         if (!listeErreur.isEmpty()) {
            StringBuilder erreurMessage = new StringBuilder();
            for (String erreur : listeErreur) {
                erreurMessage.append(erreur).append("\n");
            }
            throw new Exception(erreurMessage.toString());
        }
        return myHashMap;
    }


    public  void isDuplicateUrlMapping(String url,String packageController, ServletConfig servletConfig)throws Exception{
        List<Class<?>> controllers = this.getListeClass(packageController,servletConfig);
        int count = 0;
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                // Vérification des annotations @GURL sur les méthodes du contrôle
                if (method.isAnnotationPresent(Url.class)) {
                    Url getAnnotation = method.getAnnotation(Url.class);
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



  
/*
   public  Object executeMethod(Mapping map, String urlMapping, HttpServletRequest request,HttpServletResponse response) throws Exception { 
   String className =  map.getClassName();
  
   
   Class<?> myClass = Class.forName(className);
    Method method = null;
    Object instance = myClass.newInstance();
    this.checkControllerContainsAttributMySession(instance,request);
    Object result = null;
    for (Method m : myClass.getMethods()) {
        for(VerbMethod verb_method : map.getListMethodVerb()){

        if (m.getName().equals(verb_method.getMethodName()) && 
            m.isAnnotationPresent(Url.class) && 
            urlMapping.equals(((Url) m.getAnnotation(Url.class)).value()) &&
            verb_method.getVerbName().equalsIgnoreCase(request.getMethod())) {
            method = m;

             String methodName =verb_method.getMethodName();
             String verbMethod =verb_method.getVerbName();

            if(!request.getMethod().equalsIgnoreCase(verbMethod)){
                throw new Exception("verbe imcompatible");
            }

           
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
                   Gson gson = new Gson();
                   String jsonResponse ="";
                if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    modelView.convertToJsonData();
                    } else {
                        jsonResponse =  gson.toJson(result); 
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(jsonResponse);
                    return null; 
                }
                 
            }
            break;
        }
           
        }
    }
    if (method == null) {
        throw new NoSuchMethodException("Méthode non trouvée : ");
    }
    return result;
    }

*/

/***------------------------------EXECUTE METHODE 2.0-------------------------------------------------------------------- */

public Object executeMethod(Mapping map, String urlMapping, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Class<?> myClass = Class.forName(map.getClassName());
        Object instance = myClass.newInstance();
        // Sous-fonction pour vérifier et configurer l'attribut MySession
        this.checkControllerContainsAttributMySession(instance, request);
        // Sous-fonction pour obtenir la méthode à exécuter
        Method method = this.findMatchingMethod(map, urlMapping, request, myClass);
        if (method == null) {
            throw new NoSuchMethodException("Méthode non trouvée pour le mapping URL : " + urlMapping);
        }
        // Sous-fonction pour exécuter la méthode et retourner le résultat
        Object result = this.invokeMethod(method, instance, request, response);
        // Sous-fonction pour gérer le résultat (par exemple, convertir en JSON si nécessaire)
        this.processResult(result, method, response);
        return result;
    }

    // Sous-fonction 1 : Trouver la méthode correspondante
    private Method findMatchingMethod(Mapping map, String urlMapping, HttpServletRequest request, Class<?> myClass) throws Exception {
        Method method = null;

        for (Method m : myClass.getMethods()) {
            for (VerbMethod verbMethod : map.getListMethodVerb()) {
                if (m.getName().equals(verbMethod.getMethodName()) &&
                    m.isAnnotationPresent(Url.class) &&
                    urlMapping.equals(((Url) m.getAnnotation(Url.class)).value()) &&
                    verbMethod.getVerbName().equalsIgnoreCase(request.getMethod())) {
                    /*
                    if (!request.getMethod().equalsIgnoreCase(verbMethod.getVerbName())) {
                        throw new Exception("Verbe incompatible");
                    }*/
                    method = m;
                    break;
                }
            }
            if (method != null) {
                break;
            }
        }

        return method;
    }

  private Object invokeMethod(Method method, Object instance, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object result = null;

        if (method.getParameterCount() == 0) {
            // Si la méthode n'a pas de paramètres
            result = method.invoke(instance);
        } else {
            // Préparer les paramètres de la méthode
            List<Object> methodParameters = this.prepareParameters(method, request, response);
            if (methodParameters.size() == method.getParameterCount()) {
                result = method.invoke(instance, methodParameters.toArray(new Object[0]));
            } else {
                throw new Exception("Le nombre de paramètres est insuffisant pour la méthode " + method.getName());
            }
        }

        return result;
}


 // Sous-fonction 3 : Traiter le résultat de la méthode
    private void processResult(Object result, Method method, HttpServletResponse response) throws Exception {
        if (this.isRestApiMethode(method)) {
            Gson gson = new Gson();
            String jsonResponse = "";

            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                modelView.convertToJsonData();
            } else {
                jsonResponse = gson.toJson(result);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
            }
        }
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
        Mapping map = null;
        for (String key : myHashMap.keySet()) {
            if(key.equals(pathInfo)){
               map = myHashMap.get(key);
               break;
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

 



