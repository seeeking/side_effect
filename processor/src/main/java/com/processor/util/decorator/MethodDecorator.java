package com.processor.util.decorator;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.ExecutableElement;

/**
 * @author dwang
 * @since 18.11.18
 */
public interface MethodDecorator {
    MethodSpec decorate(final ExecutableElement executableElement);
}
