package com.processor.uniquename;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Set;

/**
 * @author dwang
 * @since 07.11.18
 */
@AutoService(UniqueName.class)
public class UniqueNameProcessor extends AbstractProcessor {

    private Messager messager;
    private GroupedNameContext groupedNameContext;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        if (groupedNameContext == null) {
            groupedNameContext = new GroupedNameContext();
        } else {
            groupedNameContext.clear();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(UniqueName.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(UniqueName.class)) {
            if (!element.getKind().equals(ElementKind.CLASS)) {
                error(element, "Element should be a class");
                return true;
            }
            try {
                String group = element.getAnnotation(UniqueName.class)
                        .scope();
                groupedNameContext.insertClassOrThrow(group, element);
            } catch (GroupedNameContext.DuplicateGroupNameException e) {
                error(element, e.getMessage());
                return true;
            }
        }

        return false;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}
