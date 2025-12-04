package com.banking.payment;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.banking.payment")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controlls_must_reside_in_controller_package =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule services_must_reside_in_service_package =
            classes()
                    .that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..service..");

    @ArchTest
    static final ArchRule controller_must_not_acess_direct_repository =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAnyPackage("..repository..");

    @ArchTest
    static final ArchRule entities_must_be_annoted_with_entity =
            classes()
                    .that().haveSimpleNameEndingWith("Entity")
                    .should().beAnnotatedWith(Entity.class);

    @ArchTest
    static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");


}
