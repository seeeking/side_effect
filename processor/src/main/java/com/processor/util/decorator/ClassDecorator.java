package com.processor.util.decorator;

import com.squareup.javapoet.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author dwang
 * @since 12.11.18
 */
public class ClassDecorator {
    private final String classPrefix;

    public ClassDecorator(String classPrefix) {
        this.classPrefix = classPrefix;
    }

    public JavaFile generateSource(TypeElement original, String packageName, MethodDecorator decorator) {
        String className = classPrefix + original.getSimpleName().toString();

        TypeSpec.Builder specBuilder = TypeSpec.classBuilder(className)
                .superclass(TypeName.get(original.asType()));

        specBuilder.addField(FieldSpec.builder(
                TypeName.get(original.asType()), "original", Modifier.PRIVATE, Modifier.FINAL)
                .build());
        specBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(original.asType()), "original")
                .addStatement("this.original = original")
                .build());

        original.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD
                        && (e.getModifiers().contains(Modifier.PUBLIC)
                        || e.getModifiers().contains(Modifier.PROTECTED)))
                .map(m -> (ExecutableElement) m)
                .forEach(method ->
                        specBuilder.addMethod(decorator.decorate(method))
                );

        return JavaFile.builder(packageName, specBuilder.build())
                .addFileComment("Generated class to log and call method")
                .build();
    }
}
