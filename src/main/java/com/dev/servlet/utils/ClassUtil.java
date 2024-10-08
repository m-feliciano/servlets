package com.dev.servlet.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class ClassUtil {

    private ClassUtil() {
    }

    /**
     * Load the classes in the package
     *
     * @param packageName
     * @return
     * @throws Exception
     */
    public static List<Class<?>> loadClasses(String packageName) throws Exception {
        return loadClasses(packageName, null);
    }

    /**
     * Load the classes in the package, with the specified annotations
     *
     * @param packageName
     * @param annotations
     * @return
     * @throws Exception
     */
    public static List<Class<?>> loadClasses(String packageName, Class<? extends Annotation>[] annotations) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        // Get the files in the package
        File[] files = getFiles(packageName);

        for (File file : files) {
            // Check if the file is a directory
            if (file.isDirectory()) {
                // Load the classes in the subdirectory
                classes.addAll(loadClasses(packageName + "." + file.getName(), annotations));
            } else if (file.getName().endsWith(".class")) {
                // Get the class name
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);

                // Load the class
                Class<?> clazz = Class.forName(className);

                if (annotations == null) {
                    classes.add(clazz);
                } else {
                    for (var annotation : annotations) {
                        if (clazz.isAnnotationPresent(annotation)) {
                            classes.add(clazz);
                            break;
                        }
                    }
                }
            }
        }

        return classes;
    }

    /**
     * Get the files in the package
     *
     * @param packageName
     * @return
     * @throws Exception
     */
    private static File[] getFiles(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new Exception("Class loader is null");
        }

        // Get the package path
        String path = packageName.replace('.', File.separatorChar);

        // Get the package directory
        File directory = new File(classLoader.getResource(path).getFile());

        if (!directory.exists()) {
            throw new Exception("Directory does not exist");
        }

        // Get the list of files in the directory
        File[] files = directory.listFiles();

        if (files == null) {
            throw new Exception("No files found in the directory");
        }
        return files;
    }
}
