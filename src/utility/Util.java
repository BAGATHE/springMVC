package utility;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.http.*;
import jakarta.servlet.*;

import annotation.RestApi;
import java.util.HashMap;
import com.thoughtworks.paranamer.Paranamer;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.google.gson.Gson;
import annotation.Url;
import annotation.Post;
import annotation.Controller;
import java.util.HashSet;
import annotation.Daty;
import annotation.DateTime;
import annotation.Email;
import annotation.ErrorValidation;
import annotation.Argument;
import annotation.Numeric;
import annotation.Authentified;
import annotation.UserRole;
import annotation.Required;
import java.sql.Timestamp;
import java.sql.Date;
import authentification.UserInterface;

public class Util {
    /* implementation de singleton pour avoir qu'une seul instance de Util */

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

    /**
     * Récupère une liste de classes dans un package spécifié qui sont annotées avec
     * {@Controller}.
     *
     * @param packageController le nom du paramètre d'initialisation du package à
     *                          scanner.
     * @param servletConfig     la configuration du servlet contenant le paramètre
     *                          d'initialisation.
     * @return une liste de classes trouvées dans le package spécifié qui sont
     *         annotées avec {@Controller}.
     * @throws Exception si le package spécifié n'est pas trouvé ou s'il y a une
     *                   erreur de chargement des classes
     */
    private List<Class<?>> getListeClass(String packageController, ServletConfig servletConfig) throws Exception {
        List<Class<?>> listController = new ArrayList<>();
        String packageToScan = servletConfig.getInitParameter(packageController);
        if (Thread.currentThread().getContextClassLoader().getResource(packageToScan.replace('.', '/')) == null) {
            throw new Exception("Package : " + packageController + " est null !");
        }
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

    /*
     * verifie le verbe de la method par defaut post
     * 
     * @param methode
     */
    private String getAnnotationVerbeMethod(Method method) {
        if (method.isAnnotationPresent(Post.class)) {
            return "post";
        }
        return "get";
    }

    /**
     * Recupere une liste de mappings (URL vers méthode controleur) en analysant les
     * methodes des classes controleurs
     * dans le package specifie. Seules les methodes annotees avec {@Url} sont
     * considerees.
     * 
     * @param packageController Le package dans lequel rechercher les classes
     *                          controleurs.
     * @param servletConfig     La configuration du servlet, utilisee pour obtenir
     *                          le contexte de l'application.
     * @return Un {HashMap} ou chaque cle est une URL et la valeur est un objet
     *         {Mapping} contenant
     *         les methodes associées et leurs verbes HTTP correspondants.
     * @throws Exception Si des erreurs sont détectées, comme des URL dupliquées
     *                   avec le même verbe HTTP et méthode.
     */
    public HashMap<String, Mapping> getListControllerWithAnnotationMethodUrl(String packageController,
            ServletConfig servletConfig) throws Exception {
        List<Class<?>> controllers = this.getListeClass(packageController, servletConfig);
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
                    Mapping mapping = new Mapping(controller.getName(), new HashSet<>());
                    if (!myHashMap.isEmpty() && myHashMap.containsKey(url)) {
                        boolean isAdded = myHashMap.get(url).getListMethodVerb()
                                .add(new VerbMethod(verb, method.getName()));
                        if (!isAdded) {
                            listeErreur
                                    .add("L'URL " + url + " est dupliquee dans la Class : " + controller.getName()
                                            + " , Method : " + method.getName() + ". \n");
                        }

                    } else {
                        mapping.getListMethodVerb().add(new VerbMethod(verb, method.getName()));
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

    /**
     * Vérifie s'il existe plusieurs méthodes associées à la même URL dans les
     * classes du package spécifié.
     *
     * @param url               l'URL à vérifier pour les duplications.
     * @param packageController le nom du paramètre d'initialisation du package
     *                          contenant les contrôleurs.
     * @param servletConfig     la configuration du servlet contenant le paramètre
     *                          d'initialisation.
     * @throws Exception si plusieurs méthodes sont associées à la même URL ou si
     *                   une erreur se produit lors du chargement des classes.
     */
    public void isDuplicateUrlMapping(String url, String packageController, ServletConfig servletConfig)
            throws Exception {
        List<Class<?>> controllers = this.getListeClass(packageController, servletConfig);
        int count = 0;
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                // Vérification des annotations @GURL sur les méthodes du contrôle
                if (method.isAnnotationPresent(Url.class)) {
                    Url getAnnotation = method.getAnnotation(Url.class);
                    String urlAnnotation = getAnnotation.value();
                    if (url.equals(urlAnnotation))
                        count += 1;
                }
            }
        }
        if (count > 1) {
            throw new Exception("Erreur : plusieurs méthodes associées à l'URL");
        }
    }

    /**
     * Appelle le setter correspondant au nom du champ spécifié sur une instance
     * donnée, en convertissant la valeur en type compatible.
     *
     * @param instance  l'instance de l'objet sur lequel le setter doit être appelé.
     * @param fieldName le nom du champ pour lequel le setter doit être invoqué.
     * @param value     la valeur sous forme de chaîne de caractères à affecter, qui
     *                  sera convertie en type approprié.
     * @throws Exception si le setter n'existe pas, si la conversion échoue ou si
     *                   une erreur se produit lors de l'invocation.
     */
    private void invokeSetter(Object instance, String fieldName, String value) throws Exception {
        Method[] methods = instance.getClass().getMethods();
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (Method method : methods) {
            if (method.getName().equals(setterName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Object convertedValue = this.convertParamPrimitiveString(fieldName, value, parameterTypes[0]);
                    method.invoke(instance, convertedValue);
                    return;
                }
            }
        }
        throw new NoSuchMethodException(
                "Setter method " + setterName + " not found in class " + instance.getClass().getName());
    }

    private Object convertParamPrimitiveString(String parameterName, String value, Class<?> type) throws Exception {
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
                } else if (type == Date.class || type == Timestamp.class) {
                    object = convertParamDate(parameterName, value, type);
                }
            } catch (Exception e) {
                throw new Exception("Type de " + value + " est invalide car c'est de type = " + type.getName());
            }
        } else if (value != null && type != String.class && type != Date.class && type != Timestamp.class) {
            throw new Exception("Type " + type.getName() + " ne doit pas etre null dans le parametre " + parameterName);
        }
        return object;
    }

    /**
     * Prépare et convertit les paramètres d'une méthode HTTP en fonction de leurs
     * types et annotations.
     *
     * @param methode  la méthode dont les paramètres doivent être préparés.
     * @param request  l'objet HttpServletRequest contenant les paramètres de la
     *                 requête.
     * @param response l'objet HttpServletResponse pour la réponse.
     * @return une liste d'objets représentant les paramètres préparés pour la
     *         méthode.
     * @throws Exception si une erreur survient lors de la préparation des
     *                   paramètres, y compris la validation des champs.
     */
    private List<Object> prepareParameters(Method methode, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNames = paranamer.lookupParameterNames(methode);
        Parameter[] arguments = methode.getParameters();
        List<Object> resultats = new ArrayList<>();
        String contentType = request.getContentType();
        StringBuilder validationErrors = new StringBuilder();

        for (int i = 0; i < arguments.length; i++) {
            Argument annotationArg = arguments[i].getAnnotation(Argument.class);
            String parameterName = (annotationArg != null && annotationArg.name() != null) ? annotationArg.name()
                    : parameterNames[i];
            Class<?> parameterType = arguments[i].getType();

            if (parameterType.isPrimitive() || parameterType.equals(String.class)
                    || parameterType.equals(MySession.class) || parameterType.equals(FileType.class) || parameterType.equals(FormData.class)) {
                resultats.add(processPrimitiveOrSimpleParameter(parameterType, parameterName, request, contentType));
            } else {
                resultats.add(convertParameterToObject(parameterName, arguments[i], request, validationErrors));
            }
        }
        /*
         * if (validationErrors.length() > 0) {
         * throw new Exception("Validation errors:\n" + validationErrors.toString());
         * }
         */
        return resultats;
    }

    /**
     * Traite un paramètre primitif ou simple (String, MySession, ou FileType).
     *
     * @param parameterType le type de paramètre.
     * @param parameterName le nom du paramètre dans la requête.
     * @param request       l'objet HttpServletRequest contenant les paramètres.
     * @param contentType   le type de contenu de la requête.
     * @return l'objet converti représentant le paramètre.
     * @throws Exception si le type de contenu est incorrect pour un fichier ou s'il
     *                   y a une erreur de conversion.
     */
    private Object processPrimitiveOrSimpleParameter(Class<?> parameterType, String parameterName,
            HttpServletRequest request, String contentType) throws Exception {
        String parameterValue = request.getParameter(parameterName);

        if (parameterType.isPrimitive() || parameterType.equals(String.class)) {
            return convertParamPrimitiveString(parameterName, parameterValue, parameterType);
        } else if (parameterType.equals(MySession.class)) {
            return new MySession(request.getSession());
        } else if (parameterType.equals(FormData.class)) {
            return new FormData(request);
        }else if (parameterType.equals(FileType.class)) {
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                return new FileType(request.getPart(parameterName));
            } else {
                throw new Exception("Veuillez ajouter l'attribut multipart/enctype au formulaire.");
            }
        }
        return null;
    }

    /**
     * Remplit un paramètre complexe avec des champs extraits de la requête et
     * valide les valeurs.
     *
     * @param parameterName    le nom du paramètre complexe dans la requête.
     * @param argument         le paramètre complexe avec son type.
     * @param request          l'objet HttpServletRequest contenant les paramètres.
     * @param validationErrors accumulateur pour les erreurs de validation.
     * @return une instance du paramètre complexe remplie avec des valeurs provenant
     *         de la requête.
     * @throws Exception si l'instance du paramètre ne peut pas être créée.
     */
    private Object convertParameterToObject(String parameterName, Parameter argument, HttpServletRequest request,
            StringBuilder validationErrors) throws Exception {
        Map<String, Object> objectInstances = new HashMap<>();
        String[] parameterFullNames = request.getParameterMap().keySet().stream()
                .filter(key -> key.startsWith(parameterName + "."))
                .toArray(String[]::new);

        if (parameterFullNames.length > 0) {
            Object instance = objectInstances.computeIfAbsent(parameterName, key -> {
                try {
                    return argument.getType().getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Impossible de créer une instance de " + argument.getType().getName(),
                            e);
                }
            });

            for (String paramName : parameterFullNames) {
                String fieldName = paramName.substring(paramName.indexOf('.') + 1);
                String fieldValue = request.getParameter(paramName);

                // Valide et applique la valeur du champ
                String validationMessage = checkValidation(instance, fieldName, fieldValue);
                if (!validationMessage.isEmpty()) {
                    validationErrors.append(validationMessage);
                } else {
                    invokeSetter(instance, fieldName, fieldValue);
                }
            }
            return instance;
        }
        return null;
    }

    /*
     * fonction qui verifie si une classe controller contient une attribut
     * d'instance mySession
     *
     * si il en a je sette la session en attribuant une liste de valeur
     */
    public void checkControllerContainsAttributMySession(Object controller, HttpServletRequest request) {
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

    /***
     * ------------------------------EXECUTE METHODE
     * 2.0--------------------------------------------------------------------
     */

    /**
     * Exécute la méthode correspondante au mapping URL donné en instanciant la
     * classe de
     * contrôleur et en appelant la méthode correspondante, puis traite le résultat.
     *
     * @param map        l'objet Mapping qui contient le nom de la classe et les
     *                   méthodes associées.
     * @param urlMapping l'URL demandée pour laquelle une méthode correspondante
     *                   doit être trouvée et exécutée.
     * @param request    l'objet HttpServletRequest contenant les informations de la
     *                   requête HTTP.
     * @param response   l'objet HttpServletResponse utilisé pour envoyer la réponse
     *                   au client.
     * @return l'objet résultant de l'exécution de la méthode correspondante.
     * @throws Exception si une erreur survient lors de la recherche, l'exécution de
     *                   la méthode ou le traitement du résultat.
     */

    public Object executeMethod(Mapping map, Method method, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Class<?> myClass = Class.forName(map.getClassName());
        Object instance = myClass.newInstance();
        // Sous-fonction pour vérifier et configurer l'attribut MySession
        this.checkControllerContainsAttributMySession(instance, request);
        if (method == null) {
            throw new NoSuchMethodException("Méthode non trouvée pour le mapping URL : ");
        }
        // Sous-fonction pour exécuter la méthode et retourner le résultat
        Object result = this.invokeMethod(method, instance, request, response);
        // Sous-fonction pour gérer le résultat (par exemple, convertir en JSON si
        // nécessaire)
        this.processResult(result, method, response);
        return result;
    }

    /**
     * Recherche une méthode dans une classe correspondant au mappage d'URL et au
     * verbe HTTP spécifiés.
     *
     * @param map        l'objet Mapping contenant la liste des méthodes et des
     *                   verbes HTTP associés.
     * @param urlMapping la chaîne de mappage d'URL à laquelle la méthode doit
     *                   correspondre.
     * @param request    l'objet HttpServletRequest contenant le verbe HTTP utilisé
     *                   pour la requête.
     * @param myClass    la classe dans laquelle rechercher la méthode correspondant
     *                   au mappage d'URL et au verbe HTTP.
     * @return la méthode correspondante si elle est trouvée, sinon {@code null}.
     * @throws Exception si une incompatibilité de verbe HTTP est détectée.
     */
    private Method findMatchingMethod(Mapping map, String urlMapping, HttpServletRequest request, Class<?> myClass)
            throws Exception {
        Method method = null;

        for (Method m : myClass.getMethods()) {
            for (VerbMethod verbMethod : map.getListMethodVerb()) {
                if (m.getName().equals(verbMethod.getMethodName()) &&
                        m.isAnnotationPresent(Url.class) &&
                        urlMapping.equals(((Url) m.getAnnotation(Url.class)).value()) &&
                        verbMethod.getVerbName().equalsIgnoreCase(request.getMethod())) {
                    /*
                     * if (!request.getMethod().equalsIgnoreCase(verbMethod.getVerbName())) {
                     * throw new Exception("Verbe incompatible");
                     * }
                     */
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

    /**
     * Invoque une méthode spécifique sur une instance d'objet avec des paramètres
     * optionnels basés sur la requête HTTP.
     *
     * @param method   la méthode à invoquer.
     * @param instance l'instance de l'objet sur laquelle la méthode doit être
     *                 invoquée.
     * @param request  l'objet HttpServletRequest contenant les paramètres de la
     *                 requête HTTP.
     * @param response l'objet HttpServletResponse pour la réponse HTTP.
     * @return l'objet résultant de l'invocation de la méthode.
     * @throws Exception si une erreur se produit lors de l'invocation de la
     *                   méthode, notamment si le nombre de paramètres est
     *                   insuffisant.
     */
    private Object invokeMethod(Method method, Object instance, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
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

    /**
     * Traite le résultat d'une méthode en fonction de son type et de l'annotation
     * de la méthode.
     * Si la méthode est une API REST, le résultat est converti en JSON et envoyé
     * dans la réponse HTTP.
     *
     * @param result   l'objet résultat de l'invocation de la méthode.
     * @param method   la méthode invoquée qui a produit le résultat.
     * @param response l'objet HttpServletResponse pour envoyer le résultat au
     *                 client.
     * @throws Exception si une erreur survient lors de la conversion en JSON ou de
     *                   l'écriture dans la réponse HTTP.
     */
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

    // verifier si la methode est un restApi
    private boolean isRestApiMethode(Method m) {
        return m.isAnnotationPresent(RestApi.class);

    }

    // convertir en gson response
    public String convertToGson(Object object) {
        Gson gson = new Gson();
        String jsonResponse = "";

        if (object instanceof ModelView) {
            ModelView modelView = (ModelView) object;
            // Récupère l'attribut `data` et le transforme en JSON
            jsonResponse = gson.toJson(modelView.getData());
        } else {

            jsonResponse = gson.toJson(object);
        }

        return jsonResponse;
    }

    /**
     * Recherche le mapping associé à une URL spécifique dans la table de hachage
     * fournie.
     *
     * @param myHashMap la table de hachage contenant les mappings d'URL, où la clé
     *                  est l'URL et la valeur est un objet Mapping.
     * @param pathInfo  l'URL spécifique pour laquelle un mapping associé doit être
     *                  trouvé.
     * @return l'objet Mapping associé à l'URL donnée, ou null si aucun mapping
     *         correspondant n'est trouvé.
     * @throws Exception si une erreur survient lors de la recherche du mapping.
     */
    public Mapping findMappingAssociateUrl(HashMap<String, Mapping> myHashMap, String pathInfo) throws Exception {
        Mapping map = null;
        for (String key : myHashMap.keySet()) {
            if (key.equals(pathInfo)) {
                map = myHashMap.get(key);
                break;
            }

        }
        return map;
    }

    public boolean isStringOrModelview(Object object) {
        if (object instanceof String || object instanceof ModelView) {
            return true;
        }
        return false;
    }

    public void redirectModelView(HttpServletRequest request, HttpServletResponse response, ModelView modelview)
            throws ServletException, IOException {
        HashMap<String, Object> dataInHashmap = modelview.getData();
        for (String keyInData : dataInHashmap.keySet()) {
            request.setAttribute(keyInData, dataInHashmap.get(keyInData));
        }
        String redirection = modelview.getUrl();
        RequestDispatcher dispatcher = request.getRequestDispatcher("/" + redirection);
        dispatcher.forward(request, response);
    }

    public static String generateErreurHtml(HttpServletResponse response, String message) {
        StringBuilder msg = new StringBuilder();
        msg.append("<html><body>");
        msg.append("<h1>Status : " + response.getStatus() + "</h1>");
        msg.append("<p>" + message + "</p>");
        msg.append("</html></body>");

        return msg.toString();
    }

    /**
     * Valide la valeur d'un champ spécifié dans une instance donnée selon les
     * annotations présentes sur le champ.
     * Les annotations prises en charge incluent
     * : @Required, @Numeric, @Date, @DateTime et @Email.
     * Si la validation échoue pour une annotation donnée, un message d'erreur est
     * ajouté au résultat.
     *
     * @param instance  l'objet dont le champ doit être validé.
     * @param fieldName le nom du champ à valider.
     * @param value     la valeur du champ sous forme de chaîne de caractères, à
     *                  valider.
     * @return une chaîne contenant les messages d'erreur de validation pour le
     *         champ, ou une chaîne vide si aucune erreur n'est trouvée.
     * @throws Exception si le champ spécifié n'existe pas dans la classe de
     *                   l'instance.
     */
    public static String checkValidation(Object instance, String fieldName, String value) throws Exception {
        StringBuilder etat = new StringBuilder();
        Class<?> classe = instance.getClass();
        Field field = classe.getDeclaredField(fieldName);

        // Vérification pour @Required
        if (field.isAnnotationPresent(Required.class) && (value == null || value.trim().isEmpty())) {
            etat.append(field.getName()).append(" : is required.");
        }
        // Vérification pour @Numeric
        if (field.isAnnotationPresent(Numeric.class) && !isNumeric(value)) {
            if (field.isAnnotationPresent(Required.class) || (value != null && !value.trim().isEmpty())) {
                if (!etat.isEmpty())
                    etat.append(" | ");
                etat.append(field.getName()).append(" : must be numeric.");
            }
        }
        // Vérification pour @Daty
        if (field.isAnnotationPresent(Daty.class) && !isValidFormat("yyyy-MM-dd", value)) {
            if (field.isAnnotationPresent(Required.class) || (value != null && !value.trim().isEmpty())) {
                if (!etat.isEmpty())
                    etat.append(" | ");
                etat.append(field.getName()).append(" : must be a valid date (yyyy-MM-dd).");
            }
        }
        // Vérification pour @DateTime
        if (field.isAnnotationPresent(DateTime.class) && !isValidFormat("yyyy-MM-dd HH:mm:ss", value)) {
            if (field.isAnnotationPresent(Required.class) || (value != null && !value.trim().isEmpty())) {
                if (!etat.isEmpty())
                    etat.append(" | ");
                etat.append(field.getName()).append(" : must be a valid datetime (yyyy-MM-dd HH:mm:ss).");
            }
        }
        // Vérification pour @Email
        if (field.isAnnotationPresent(Email.class) && !isEmail(value)) {
            if (field.isAnnotationPresent(Required.class) || (value != null && !value.trim().isEmpty())) {
                if (!etat.isEmpty())
                    etat.append(" | ");
                etat.append(field.getName()).append(" : must be a valid email.");
            }
        }

        return etat.toString();
    }

    // Vérifie si la chaîne est requise (non nulle et non vide)
    public static boolean isRequired(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Vérifie si la chaîne est numeric
    public static boolean isNumeric(String value) {
        return value.matches("-?\\d+(\\.\\d+)?");
    }

    // Vérifie si la chaîne peut être castée en date (yyyy-MM-dd)
    public static boolean isDate(String value) {
        return isValidFormat("yyyy-MM-dd", value);
    }

    // Vérifie si la chaîne peut être castée en date et heure (yyyy-MM-dd HH:mm:ss)
    public static boolean isDateTime(String value) {
        return isValidFormat("yyyy-MM-dd HH:mm:ss", value);
    }

    // Vérifie si la chaîne est un email valide
    public static boolean isEmail(String value) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(value).matches();
    }

    // Fonction utilitaire pour valider les formats de date et date-heure
    private static boolean isValidFormat(String format, String value) {
        if (value == null)
            return false;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // Empêche les dates non valides (par exemple, 2024-02-30)
        try {
            sdf.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * <p>
     * Cette méthode parcourt toutes les méthodes publiques d'une classe spécifiée
     * pour trouver
     * une méthode qui remplit les critères suivants :
     * - Le nom de la méthode correspond à celui spécifié dans un `MethodVerb` de la
     * liste.
     * - La méthode est annotée avec `@Url` et son URL correspond à celle fournie.
     * - Le verbe HTTP (`requestMethod`) correspond au verbe associé dans l'objet
     * `MethodVerb`.
     * </p>
     *
     * Exemple d'utilisation :
     * HashSet<MethodVerb> methods = new HashSet<>();
     * methods.add(new MethodVerb("methodName", "GET"));
     * Method method = getMethode("com.example.MyClass", methods, "/my/url", "GET");
     * }
     * </pre>
     * 
     */
    public Method getMethode(String classeName, HashSet<VerbMethod> listMethodVerbs, String url, String requestMethod)
            throws Exception {
        Class<?> classe = Class.forName(classeName);
        Method methode = null;
        for (Method mthd : classe.getMethods()) {
            for (VerbMethod methodVerb : listMethodVerbs) {
                if (mthd.getName().equals(methodVerb.getMethodName()) && mthd.isAnnotationPresent(Url.class)
                        && url.equals(((Url) mthd.getAnnotation(Url.class)).value())
                        && methodVerb.getVerbName().equalsIgnoreCase(requestMethod)) {
                    methode = mthd;
                    break;
                }
            }
        }
        if (methode == null) {
            throw new NoSuchMethodException("Methode : " + requestMethod + " non trouvee pour l'url : " + url);
        }
        return methode;
    }

    public Object convertParamDate(String parameterName, String value, Class<?> type)
            throws Exception {
        Object object = null;
        if (value != null && !"".equals(value)) {
            try {
                if (type == Date.class) {
                    object = Date.valueOf(value);
                } else if (type == Timestamp.class) {
                    object = Timestamp.valueOf(value);
                }
            } catch (Exception e) {
                throw new Exception("Type de " + value + " est invalide car c'est de type = " + type.getName());
            }
        } else if (value != null && type != String.class) {
            throw new Exception("Type " + type.getName() + " ne doit pas etre null dans le parametre " + parameterName);
        }
        return object;
    }

    /**
     * Cette méthode valide les champs d'un objet basé sur les paramètres envoyés
     * dans la requête HTTP.
     * Elle vérifie les valeurs des champs d'un objet donné en fonction de son nom
     * et applique les règles de validation associées.
     *
     * @param parameter       L'objet {@code Parameter} contenant les informations
     *                        sur le type de l'objet à valider.
     * @param parameterName   Le nom de l'objet (préfixe des champs à valider) dans
     *                        la requête.
     * @param request         L'objet {@code HttpServletRequest} contenant les
     *                        paramètres à valider.
     * @param objectInstances Une map contenant les instances des objets utilisés
     *                        pour la validation. Si une instance
     *                        n'existe pas encore, elle sera créée dynamiquement.
     * @return Une liste de tableaux de chaînes de caractères, où chaque tableau
     *         contient :
     *         <ul>
     *         <li>L'identifiant complet du champ (par exemple,
     *         "parameterName.fieldName").</li>
     *         <li>La valeur du champ extraite de la requête.</li>
     *         <li>Un message de validation pour le champ (vide si la validation est
     *         réussie).</li>
     *         </ul>
     * @throws Exception Si une erreur survient lors de la création de l'instance de
     *                   l'objet ou pendant la validation.
     */

    public static List<String[]> checkValidationObject(Parameter parameter, String parameterName,
            HttpServletRequest request, Map<String, Object> objectInstances) throws Exception {

        String[] paramterFullName = request.getParameterMap().keySet().stream()
                .filter(key -> key.startsWith(parameterName + "."))
                .toArray(String[]::new);

        List<String[]> validations = new ArrayList<>();

        if (paramterFullName.length > 0) {
            Object instance = objectInstances.computeIfAbsent(parameterName, key -> {
                try {
                    return parameter.getType().getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                        | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create instance of " + parameterName, e);
                }
            });

            for (String paramName : paramterFullName) {
                String fieldName = paramName.substring(paramName.indexOf('.') + 1);
                String fieldValue = request.getParameter(paramName);

                // Validation du champ
                String validationMessage = checkValidation(instance, fieldName, fieldValue);
                String[] elementInput = new String[3];

                elementInput[0] = parameterName + "." + fieldName;
                elementInput[1] = fieldValue;
                elementInput[2] = validationMessage;

                validations.add(elementInput);
            }
        }

        return validations;
    }

    public HashMap<String, String[]> checkValidationMethode(Method methode, HttpServletRequest request)
            throws Exception {
        Parameter[] arguments = methode.getParameters();
        Map<String, Object> objectInstances = new HashMap<>();
        String contentType = request.getContentType();

        HashMap<String, String[]> validations = new HashMap<String, String[]>();

        for (Parameter argument : arguments) {
            Object value = null;
            boolean estObjet = false;
            // Recupere l'annotation Arguments associee au parametre courant
            Argument annotationArg = argument.getAnnotation(Argument.class);
            String parameterName = argument.getName();
            if (annotationArg != null && annotationArg.name() != null) {
                parameterName = annotationArg.name();
            }
            String parameterValue = request.getParameter(parameterName);
            Class<?> parameterType = argument.getType();

            String[] elementInput = new String[3];

            elementInput[0] = parameterName;
            elementInput[1] = parameterValue;
            elementInput[2] = "";

            if (parameterType.isPrimitive() || parameterType.equals(String.class)) {
                try {
                    value = convertParamPrimitiveString(parameterName, parameterValue, parameterType);
                } catch (Exception e) {
                    if (!elementInput[2].isEmpty()) {
                        elementInput[2] += " | ";
                    }
                    elementInput[2] += e.getMessage();
                }
            } else if (parameterType.equals(Date.class) || parameterType.equals(Timestamp.class)) {
                try {
                    value = convertParamDate(parameterName, parameterValue, parameterType);
                } catch (Exception e) {
                    if (!elementInput[2].isEmpty()) {
                        elementInput[2] += " | ";
                    }
                    elementInput[2] += e.getMessage();
                }
            } else if (parameterType.equals(MySession.class)) {
                value = new MySession(request.getSession());
                estObjet = true;
            }else if (parameterType.equals(FormData.class)) {
                value = new FormData(request);
                estObjet = true;
            } else if (parameterType.equals(FileType.class)) {
                try {
                    if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                        value = new FileType(request.getPart(parameterName));
                    } else {
                        if (!elementInput[2].isEmpty()) {
                            elementInput[2] += " | ";
                        }
                        elementInput[2] += "Le formulaire doit avoir une enctype=\"multipart/form-data\"";
                    }
                } catch (Exception e) {
                    if (!elementInput[2].isEmpty()) {
                        elementInput[2] += " | ";
                    }
                    elementInput[2] += e.getMessage();
                }
            } else {
                List<String[]> validationResults = checkValidationObject(argument, parameterName, request,
                        objectInstances);

                for (String[] validationResult : validationResults) {
                    if (validationResult.length > 0) {
                        validations.put(validationResult[0], validationResult);
                    }
                }
                estObjet = true;
            }

            if (!estObjet) {
                validations.put(elementInput[0], elementInput);
            }
        }
        return validations;
    }

    public boolean estValid(HashMap<String, String[]> listValidation) {
        for (String[] array : listValidation.values()) {
            System.out.println("Array: " + Arrays.toString(array));
            if (array.length == 3) {
                if (array[2] != null && !array[2].isEmpty() && !"".equals(array[2])) {
                    System.out.println("Invalid value found: " + array[2]);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Valide les données d'une méthode et redirige vers une URL spécifique si des
     * erreurs de validation sont détectées.
     * Cette méthode vérifie les validations associées à la méthode, ajuste le verbe
     * HTTP si nécessaire,
     * et redirige l'utilisateur vers une URL d'erreur si des erreurs de validation
     * sont présentes.
     *
     * @param methode  La méthode à valider, annotée avec les informations
     *                 nécessaires pour la validation et les erreurs.
     * @param request  L'objet {@code HttpServletRequest} contenant les données de
     *                 la requête.
     * @param response L'objet {@code HttpServletResponse} utilisé pour rediriger ou
     *                 transmettre la requête.
     * @throws Exception Si aucune annotation {@code @ErrorValidation} n'est
     *                   présente sur la méthode ou si un problème
     *                   survient lors du traitement de la requête.
     */
    public void processValidationAndForward(Method methode, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HashMap<String, String[]> validationMethode = checkValidationMethode(methode, request);
        if (!estValid(validationMethode)) {
            String verbMethod = getAnnotationVerbeMethod(methode);

            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getMethod() {
                    return verbMethod.toUpperCase();
                }
            };

            for (Map.Entry<String, String[]> entry : validationMethode.entrySet()) {
                String key = entry.getKey();
                String[] value = entry.getValue();

                requestWrapper.setAttribute("error_" + key, value[2]);
            }

            String url = null;
            if (methode.isAnnotationPresent(ErrorValidation.class)) {
                ErrorValidation erreurValidation = methode.getAnnotation(ErrorValidation.class);
                url = erreurValidation.value();
            }

            if (url == null) {
                throw new Exception("Le controller doit etre annote @ErrorValidation pour les erreurs de validation");
            }
            RequestDispatcher dispatcher = requestWrapper.getRequestDispatcher(url);
            dispatcher.forward(requestWrapper, response);
        }
    }

    public boolean isExecutable(AnnotatedElement annotatedElement, MySession session) throws Exception {
        if (annotatedElement.isAnnotationPresent(Authentified.class)) {

            String userKey = (String) session.get("userKey");
            if (userKey == null) {
                throw new Exception("Acces refuse, il faut etre connectee!");
            }
            UserInterface user = (UserInterface) session.get(userKey);
            if (annotatedElement.isAnnotationPresent(UserRole.class)) {
                UserRole methodRole = annotatedElement.getAnnotation(UserRole.class);
                String[] roles = methodRole.roles();
                String[] userRoles = user.getRoles();
                for (String role : userRoles) {
                    for (String authorizedRole : roles) {
                        if (role.equalsIgnoreCase(authorizedRole)) {
                            return true;
                        }
                    }
                }
                throw new Exception("Acces refuse, methode non autorisee!");
            }
        }
        return true;
    }

}
