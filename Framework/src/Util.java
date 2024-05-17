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
import controller.FrontController;

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
}
