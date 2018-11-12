package com.processor.log;

import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author dwang
 * @since 12.11.18
 */
public class MethodWrapper {
    private final String classPrefix;

    public MethodWrapper(String classPrefix) {
        this.classPrefix = classPrefix;
    }

    public JavaFile generateSource(TypeElement original, String packageName, Predicate<? super ExecutableElement> shouldDecorate) {
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
        MethodSpec helper = privateHelper();
        specBuilder.addMethod(helper);

        original.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD
                        && e.getModifiers().contains(Modifier.PUBLIC))
                .map(m -> (ExecutableElement) m)
                .forEach(method ->
                    specBuilder.addMethod(generateMethod(method, helper, shouldDecorate.test(method)))
                );

        return JavaFile.builder(packageName, specBuilder.build())
                .addFileComment("Generated class to log and call method")
                .build();
    }

    private MethodSpec generateMethod(ExecutableElement method, MethodSpec helper, boolean shouldDecorate) {
        MethodSpec.Builder mBuilder = MethodSpec
                .methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC);

        List<? extends TypeMirror> params = ((ExecutableType) method.asType()).getParameterTypes();
        List<String> paramNames = IntStream.range(0, params.size())
                .boxed()
                .map(i -> "_var" + String.valueOf(i))
                .collect(Collectors.toList());

        for (int i = 0; i < params.size(); ++i) {
            mBuilder.addParameter(TypeName.get(params.get(i)), paramNames.get(i));
        }

        if (shouldDecorate) {
            mBuilder.addCode(callHelper(helper, paramNames));
        }

        mBuilder.returns(TypeName.get(((ExecutableType) method.asType()).getReturnType()));
        if (TypeName.get(((ExecutableType) method.asType()).getReturnType()) == TypeName.VOID) {
            mBuilder.addStatement("original.$L($L)",
                    method.getSimpleName().toString(),
                    paramNames.stream().collect(Collectors.joining(", ")));

        } else {
            mBuilder.addStatement("$T result = original.$L($L)",
                    TypeName.get(((ExecutableType) method.asType()).getReturnType()),
                    method.getSimpleName().toString(),
                    paramNames.stream().collect(Collectors.joining(", ")));

            if (shouldDecorate) {
                mBuilder.addCode(callHelper(helper, Collections.singletonList("result")));
            }

            mBuilder.addStatement("return result");
        }

        return mBuilder.build();
    }

    // it's interesting that it produces generated code in modularized way
    // it should not be encouraged, especially when modularization hurts functionality
    private MethodSpec privateHelper() {
        return MethodSpec.methodBuilder("_generated_helper")
                .addModifiers(Modifier.PRIVATE)
                .returns(TypeName.VOID)
                .addParameter(TypeName.OBJECT, "param")
                .addStatement("System.out.println(String.valueOf(param))")
                .build();
    }

    private CodeBlock callHelper(MethodSpec helper, List<String> paramNames) {
        CodeBlock.Builder builder = CodeBlock.builder();

        paramNames.forEach(pn -> builder.addStatement("$N($L)", helper, pn));
        return builder.build();
    }
}
