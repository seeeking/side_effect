package com.processor.util.decorator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author dwang
 * @since 18.11.18
 */
public class LogParamDecorator extends BeforeOrAfterDecorator {
    public LogParamDecorator(Predicate<ExecutableElement> shouldDecorate) {
        super(shouldDecorate);
    }

    @Override
    protected Optional<CodeBlock> before(List<String> paramNames, List<TypeName> paramTypes) {
        CodeBlock.Builder builder = CodeBlock.builder();

        paramNames.forEach(pn -> builder.addStatement("System.out.println($L)", pn));

        return Optional.of(builder.build());
    }

    @Override
    protected Optional<CodeBlock> after(List<String> paramNames, List<TypeName> paramTypes) {
        CodeBlock.Builder builder = CodeBlock.builder();

        if (paramNames.size() > 0) {
            builder.addStatement("System.out.println($L)", paramNames.get(paramNames.size()-1));
        }

        return Optional.of(builder.build());
    }
}
