package dev.airyy.AiryLib.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class Annotations {

    public static <T extends Annotation> boolean hasAnnotation(Class<?> clazz, Class<T> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    public static <T extends Annotation> boolean hasAnnotation(Method method, Class<T> annotation) {
        return method.isAnnotationPresent(annotation);
    }
}
