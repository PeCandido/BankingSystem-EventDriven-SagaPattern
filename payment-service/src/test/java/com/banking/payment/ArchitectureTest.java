package com.banking.payment;

import com.banking.core.event.BaseEvent;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

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
            .layer("Listener").definedBy("..listener..")
            .layer("Mapper").definedBy("..mapper..")
            .layer("Exception").definedBy("..exception..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Listener")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Mapper").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule no_cicle_between_packages =
            slices().matching("com.banking.payment.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule controllers_must_not_expose_entities =
            noMethods()
                    .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                    .should().haveRawReturnType(resideInAPackage("..model.."))
                    .orShould().haveRawReturnType(resideInAPackage("..model.."))
                    .because("Banking entities must not leak to the API. Use DTOs.");

    @ArchTest
    static final ArchRule controllers_must_response_entity =
            methods()
                    .that().arePublic()
                    .and().areDeclaredInClassesThat().resideInAPackage("..controller..")
                    .and().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().haveRawReturnType(ResponseEntity.class);

    @ArchTest
    static final ArchRule kafka_listeners_must_be_in_right_place =
            methods()
                    .that().areAnnotatedWith("org.springframework.kafka.annotation.KafkaListener")
                    .should().beDeclaredInClassesThat().resideInAPackage("..listener..")
                    .andShould().beDeclaredInClassesThat().haveSimpleNameEndingWith("Listener");

    @ArchTest
    static final ArchRule controllers_must_not_have_transactional =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                    .because("Transactions should be managed in the Service layer.");

    @ArchTest
    static final ArchRule services_must_not_be_web_controllers =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().beAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Controller")
                    .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .because("Services should not handle HTTP requests directly");

    @ArchTest
    static final ArchRule dtos_must_not_be_entities =
            noClasses()
                    .that().resideInAPackage("..dto..")
                    .should().beAnnotatedWith(Entity.class)
                    .because("DTOS should not be database tables");

    @ArchTest
    static final ArchRule entities_must_not_have_json_annotations =
            noClasses()
                    .that().resideInAPackage("..model..")
                    .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                    .because("Domain Entities must not have serialization dependencies (JSON). Use DTOs/Mappers.");

    @ArchTest
    static final ArchRule repositories_must_be_interfaces =
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().beInterfaces()
                    .because("Spring JPA repositories should be interfaces");

    @ArchTest
    static final ArchRule no_access_to_standard_streams = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule no_java_util_logging = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    static final ArchRule events_must_not_depend_on_entities =
        noClasses()
                .that().resideInAPackage("..event..")
                .should().dependOnClassesThat(annotatedWith(Entity.class))
                .allowEmptyShould(true)
                .because("Events must be decoupled from persistence entities to ensure schema evolution independence.");

    @ArchTest
    static final ArchRule event_sourcing_integrity_check =
            noClasses()
                    .that().haveSimpleNameNotEndingWith("EventStore")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("EventRepository");

    @ArchTest
    static final ArchRule saga_must_be_services =
            classes()
                    .that().haveSimpleNameEndingWith("Saga")
                    .should().beAnnotatedWith(Service.class)
                    .andShould().resideInAPackage("..service..");

}
