package com.processor.chain;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author dwang
 * @since 05.11.18
 */
@AutoService(Chain.class)
public class ChainProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Chain.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println(this.getClass().getCanonicalName() + " Processing ... ");
        System.out.println(Arrays.toString(annotations.toArray()));
        System.out.println(roundEnv);

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Chain.class);
        System.out.println(Arrays.toString(elements.toArray()));

        return false;
    }
}
