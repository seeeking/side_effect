package com.processor.log;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author dwang
 * @since 02.11.18
 */
@AutoService(Logged.class)
public class LoggedProcessor extends AbstractProcessor {
    private static final String suffix = "logged";
    private Elements elementUtils;
    private Filer filer;
    private MethodWrapper methodWrapper = new MethodWrapper(suffix);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
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
        System.out.println(Arrays.toString(annotations.toArray()));
        System.out.println(roundEnv);
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Logged.class);
        elements.forEach(this::generateSource);

        return false;
    }

    private void generateSource(Element element) {
        try {
            PackageElement pkg = elementUtils.getPackageOf(element);
            methodWrapper.generateSource(element, pkg).writeTo(filer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

