package com.example.factory_compiler;


public class IdAlreadyUsedException extends RuntimeException {
    private FactoryAnnotatedClass factoryAnnotatedClass;
    public IdAlreadyUsedException(FactoryAnnotatedClass factoryAnnotatedClass) {
        this.factoryAnnotatedClass = factoryAnnotatedClass;
    }

    public FactoryAnnotatedClass getExisting() {
        return factoryAnnotatedClass;
    }

}
