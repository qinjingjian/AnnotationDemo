package com.example.factory_compiler;

import com.example.annotation.Factory;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types mTypeUtils;
    private Messager mMessager;
    private Filer mFiler;
    private Elements mElementUtils;

    private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mTypeUtils = processingEnvironment.getTypeUtils();
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(Factory.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s",
                        Factory.class.getSimpleName());
                return true; // exit
            }
            TypeElement typeElement = (TypeElement) annotatedElement;
            try {
                FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement);

                if (!isValidClass(annotatedClass)) {
                    return true;
                }

                FactoryGroupedClasses factoryClass = factoryClasses.get(annotatedClass.getQualifiedFactoryGroupName());
                if (factoryClass == null) {
                    String qualifiedGroupName = annotatedClass.getQualifiedFactoryGroupName();
                    factoryClass = new FactoryGroupedClasses(qualifiedGroupName);
                    factoryClasses.put(qualifiedGroupName, factoryClass);
                }
                factoryClass.add(annotatedClass);

            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());

            } catch (IdAlreadyUsedException e) {
                FactoryAnnotatedClass existing = e.getExisting();
                // already exist
                error(annotatedElement,
                        "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                        typeElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        existing.getTypeElement().getQualifiedName().toString());
                return true;
            }
        }

        try {
            for (FactoryGroupedClasses factoryClassOne : factoryClasses.values()) {
                factoryClassOne.generateCode(mElementUtils, mFiler);
            }

            factoryClasses.clear();
        } catch (IOException e) {
            error(null, e.getMessage());
        }

        return true;

    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    private void info(String msg) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }



    private boolean isValidClass(FactoryAnnotatedClass item) {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            return false;
        }
        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
            return false;
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
        TypeElement superClassElement =
                mElementUtils.getTypeElement(item.getQualifiedFactoryGroupName());
        if (superClassElement.getKind() == ElementKind.INTERFACE) {
            // Check interface implemented
            if(!classElement.getInterfaces().contains(superClassElement.asType())) {
                error(classElement, "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        item.getQualifiedFactoryGroupName());
                return false;
            }
        } else {
        // Check subclassing
        TypeElement currentClass = classElement;
        while (true) {
            TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                //Basis class (java.lang.Object) reached, so exit
                error(classElement, "The class %s annotated with @%s must inherit from %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        item.getQualifiedFactoryGroupName());
                return false;
            }

            if (superClassType.toString().equals(item.getQualifiedFactoryGroupName())) {
                // Required super class found
                break;
            }

            //  Moving up in inheritance tree
            currentClass = (TypeElement) mTypeUtils.asElement(superClassType);
        }
    }

    //  Check if an empty public constructor is given
    for (Element enclosed : classElement.getEnclosedElements()) {
        if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
            ExecutableElement constructorElement = (ExecutableElement) enclosed;
            if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                    .contains(Modifier.PUBLIC)) {
                //  Found an empty constructor
                return true;
            }
        }
    }

    // No empty constructor found
    error(classElement, "The class %s must provide an public empty default constructor",
          classElement.getQualifiedName().toString());
    return false;
    }




}
