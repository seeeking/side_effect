package com.processor;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dwang
 * @since 02.11.18
 */
public class LoggedProcessor extends AbstractProcessor {
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
        elements.forEach(e -> {
            try {
                generateSource(e);
            } catch (Exception e1) {
                e1.printStackTrace();
                throw new RuntimeException("failed");
            }
        });

        System.out.println(Arrays.toString(elements.toArray()));

        return false;
    }

    private void generateSource(Element element) throws IOException, ClassNotFoundException {
        Set<Element> methods = element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD
                        && e.getModifiers().contains(Modifier.PUBLIC))
                .collect(Collectors.toSet());

        String className = element.getSimpleName() + "Logged";

        System.out.println("Generating " + className);
        PackageElement pkg = elementUtils.getPackageOf(element);
        String packageName = pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString();

        TypeSpec.Builder specBuilder = TypeSpec.classBuilder(className);

        specBuilder.addField(FieldSpec.builder(TypeName.get(element.asType()), "original", Modifier.PRIVATE)
                .build());
        specBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(element.asType()), "original")
                .addStatement("this.original = original")
                .build());

        for (Element method : methods) {
            System.out.println((ExecutableType) method.asType());
            MethodSpec.Builder mBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());

            List<? extends TypeMirror> params = ((ExecutableType) method.asType()).getParameterTypes();
            List<String> paramNames = new ArrayList<>();
            for (int i = 0; i < params.size(); ++i) {
                String paramName = "var" + String.valueOf(i);
                paramNames.add(paramName);
                mBuilder.addParameter(TypeName.get(params.get(i)), paramName);
                mBuilder.addStatement("System.out.println(String.valueOf($L))", paramName);
            }
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
                mBuilder.addStatement("System.out.println(String.valueOf(result))");
                mBuilder.addStatement("return result");
            }


            specBuilder.addMethod(mBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(packageName, specBuilder.build())
                .addFileComment("Generated class to log and call method")
                .build();
        // Write package
        JavaFileObject jfo = filer.createSourceFile(className);
        Writer writer = jfo.openWriter();
        javaFile.writeTo(writer);
        writer.close();
    }


//
//    private void error(Element e, String msg, Object... args) {
//        messager.printMessage(
//                Diagnostic.Kind.ERROR,
//                String.format(msg, args),
//                e);
//    }
}

