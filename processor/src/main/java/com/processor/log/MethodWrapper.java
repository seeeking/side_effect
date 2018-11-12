package com.processor.log;

import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author dwang
 * @since 12.11.18
 */
public class MethodWrapper {
    private final String classSuffix;

    public MethodWrapper(String classSuffix) {
        this.classSuffix = classSuffix;
    }

    public JavaFile generateSource(Element element, PackageElement pkg) {
        Set<Element> methods = element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD
                        && e.getModifiers().contains(Modifier.PUBLIC))
                .collect(Collectors.toSet());

        String className = element.getSimpleName() + classSuffix;

        String packageName = pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString();

        TypeSpec.Builder specBuilder = TypeSpec.classBuilder(className).superclass(TypeName.get(element.asType()));

        specBuilder.addField(FieldSpec.builder(TypeName.get(element.asType()), "original", Modifier.PRIVATE)
                .build());
        specBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(element.asType()), "original")
                .addStatement("this.original = original")
                .build());
        MethodSpec helper = privateHelper();
        specBuilder.addMethod(helper);

        for (Element method : methods) {
            specBuilder.addMethod(generateMethod((ExecutableElement) method, helper));
        }

        return JavaFile.builder(packageName, specBuilder.build())
                .addFileComment("Generated class to log and call method")
                .build();
    }

    private MethodSpec generateMethod(ExecutableElement method, MethodSpec helper) {
        MethodSpec.Builder mBuilder = MethodSpec
                .methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC);

        List<? extends TypeMirror> params = ((ExecutableType) method.asType()).getParameterTypes();
        List<String> paramNames = IntStream.range(0, params.size())
                .boxed()
                .map(i -> "var" + String.valueOf(i))
                .collect(Collectors.toList());

        for (int i = 0; i < params.size(); ++i) {
            mBuilder.addParameter(TypeName.get(params.get(i)), paramNames.get(i));
        }

        mBuilder.addCode(callHelper(helper, paramNames));

        mBuilder.returns(TypeName.get(((ExecutableType) method.asType()).getReturnType()));
        if (TypeName.get(((ExecutableType) method.asType()).getReturnType()) == TypeName.VOID) {
            mBuilder.addStatement("original.$L($L)",
                    method.getSimpleName().toString(),
                    paramNames.stream().collect(Collectors.joining(", ")));

        } else {
            mBuilder.addStatement("$L result = original.$L($L)",
                    TypeName.get(((ExecutableType) method.asType()).getReturnType()),
                    method.getSimpleName().toString(),
                    paramNames.stream().collect(Collectors.joining(", ")));

            mBuilder.addCode(callHelper(helper, Collections.singletonList("result")));
            mBuilder.addStatement("return result");
        }

        return mBuilder.build();
    }

    // it's interesting that it produces generated code in modularized way
    // it should not be encouraged, especially when modularization hurts functionality
    private MethodSpec privateHelper() {
        return MethodSpec.methodBuilder("generatedHelper")
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
