package com.processor.util.decorator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.html.Option;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provide a point to tap in before and after the execution
 * The plain class itself will generate the exact same behavior as the original one
 * No information sharing between before and after, thus a bit difficult to implement functionality
 * like measuring duration
 *
 * @author dwang
 * @since 18.11.18
 */
public class BeforeOrAfterDecorator implements MethodDecorator {
    private final Predicate<ExecutableElement> shouldDecorate;

    public BeforeOrAfterDecorator(Predicate<ExecutableElement> shouldDecorate) {
        this.shouldDecorate = shouldDecorate;
    }

    @Override
    public MethodSpec decorate(final ExecutableElement executableElement) {
        return generateMethod(executableElement);
    }

    protected Optional<CodeBlock> before(List<String> paramNames, List<TypeName> paramTypes) {
        return Optional.empty();
    }

    protected Optional<CodeBlock> after(List<String> paramNames, List<TypeName> paramTypes) {
        return Optional.empty();
    }

    private MethodSpec generateMethod(ExecutableElement method) {
        MethodSpec.Builder mBuilder = MethodSpec
                .methodBuilder(method.getSimpleName().toString())
                .addModifiers(method.getModifiers());

        List<? extends TypeMirror> params = ((ExecutableType) method.asType()).getParameterTypes();
        List<String> paramNames = IntStream.range(0, params.size())
                .boxed()
                .map(i -> "_var" + String.valueOf(i))
                .collect(Collectors.toList());
        List<TypeName> paramTypes = IntStream.range(0, params.size())
                .boxed()
                .map(i -> TypeName.get(params.get(i)))
                .collect(Collectors.toList());

        for (int i = 0; i < params.size(); ++i) {
            mBuilder.addParameter(paramTypes.get(i), paramNames.get(i));
        }

        if (shouldDecorate.test(method)) {
            before(paramNames, paramTypes).ifPresent(mBuilder::addCode);
        }

        TypeName returnType = TypeName.get(((ExecutableType) method.asType()).getReturnType());
        mBuilder.returns(returnType);
        if (returnType == TypeName.VOID) {
            mBuilder.addStatement("original.$L($L)",
                    method.getSimpleName().toString(),
                    paramNames.stream().collect(Collectors.joining(", ")));
            if (shouldDecorate.test(method)) {
                after(paramNames, paramTypes).ifPresent(mBuilder::addCode);
            }
        } else {
            mBuilder.addStatement("$T result = original.$L($L)",
                    returnType,
                    method.getSimpleName().toString(),
                    paramNames.stream().collect(Collectors.joining(", ")));
            paramNames.add("result");
            paramTypes.add(returnType);

            if (shouldDecorate.test(method)) {
                after(paramNames, paramTypes).ifPresent(mBuilder::addCode);
            }

            mBuilder.addStatement("return result");
        }

        return mBuilder.build();
    }
}
