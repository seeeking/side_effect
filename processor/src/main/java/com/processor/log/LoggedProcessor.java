package com.processor.log;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author dwang
 * @since 02.11.18
 */
@AutoService(Logged.class)
public class LoggedProcessor extends AbstractProcessor {
    private static final String suffix = "Logged";
    private MethodWrapper methodWrapper = new MethodWrapper(suffix);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Logged.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Processing ... ");
        Set<TypeElement> classesToProcess = new HashSet<>();
        Set<ExecutableElement> methodToDecorate = new HashSet<>();
        try {
            roundEnv.getElementsAnnotatedWith(Logged.class)
                    .forEach(ele -> {
                        if (ele.getKind().equals(ElementKind.CLASS)) {
                            validateClassElement(ele);

                            classesToProcess.add((TypeElement) ele);
                            methodToDecorate.addAll(((TypeElement) ele).getEnclosedElements().stream()
                                    .filter(e -> e.getKind() == ElementKind.METHOD
                                            && e.getModifiers().contains(Modifier.PUBLIC))
                                    .map(e -> (ExecutableElement) e)
                                    .collect(Collectors.toSet())
                            );
                        } else if (ele.getKind() == ElementKind.METHOD) {
                            validateClassElement(ele.getEnclosingElement());

                            methodToDecorate.add((ExecutableElement) ele);
                            classesToProcess.add((TypeElement) ele.getEnclosingElement());
                        }
                    });
            classesToProcess.forEach(ele -> generateSource(ele, methodToDecorate::contains));
        } catch (RuntimeException e) {
            return true;
        }

        return false;
    }

    private void generateSource(TypeElement element, Predicate<ExecutableElement> shouldDecorate) {
        try {
            PackageElement pkg = processingEnv.getElementUtils().getPackageOf(element);
            String pkgName = pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString();
            methodWrapper.generateSource(element, pkgName, shouldDecorate).writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            error(element, "Failed to generate source: %s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateClassElement(Element ele) {
        if (!ele.getKind().equals(ElementKind.CLASS)
                || ele.getModifiers().contains(Modifier.FINAL)
                || ele.getModifiers().contains(Modifier.ABSTRACT)) {
            error(ele, "Class should be non-abstract and non-final");
            throw new RuntimeException("Annotation mis-used.");
        }
    }

    private void error(Element e, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}

