package com.banking.merchantservice;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.banking.merchantservice")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule respect_layered_architecture = layeredArchitecture()
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
            slices().matching("com.banking.merchantservice.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule entities_must_stay_internal =
            classes()
                    .that().haveSimpleNameEndingWith("Entity")
                    .should().onlyBeAccessed().byAnyPackage(
                            "..service..",
                            "..repository..",
                            "..mapper..",
                            "..model.."
                    )
                    .because("Controller shpuld only use DTOs. Entities should be internal.");

    @ArchTest
    static final ArchRule controllers_should_return_response_entity =
            methods()
                    .that().arePublic()
                    .and().areDeclaredInClassesThat().resideInAPackage("..controller..")
                    .and().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().haveRawReturnType(ResponseEntity.class);

    @ArchTest
    static final ArchRule kafka_listeners_should_be_in_right_place =
            methods()
                    .that().areAnnotatedWith("org.springframework.kafka.annotation.KafkaListener")
                    .should().beDeclaredInClassesThat().resideInAPackage("..listener..")
                    .andShould().beDeclaredInClassesThat().haveSimpleNameEndingWith("Listener");

    @ArchTest
    static final ArchRule entities_must_be_annotated_with_entity =
            classes()
                    .that().haveSimpleNameEndingWith("Entity")
                    .should().beAnnotatedWith(Entity.class);

    @ArchTest
    static final ArchRule services_must_be_annotated =
            classes()
                    .that().resideInAPackage("..service..")
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule repositories_must_be_interfaces =
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().beInterfaces();

    @ArchTest
    static final ArchRule controllers_must_not_have_transactional =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                    .because("Service should not have transactional");

    @ArchTest
    static final ArchRule services_must_not_be_web_controllers =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Controller")
                    .because("Services should not be web controller with HTTP response.");

    @ArchTest
    static final ArchRule entities_must_not_have_json_annotations =
            noClasses()
                    .that().resideInAPackage("..model..")
                    .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                    .because("Entities should not have json annotations.");

    @ArchTest
    static final ArchRule no_generic_exceptions = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static final ArchRule no_system_out = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule no_java_util_logging = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    static final ArchRule no_java_util_date =
            noClasses()
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Date")
                    .orShould().dependOnClassesThat().haveFullyQualifiedName("java.sql.Date")
                    .because("Use java.time (LocalDateTime) instead of java.util.Date");

    @ArchTest
    static final ArchRule events_must_not_depend_on_entities =
            noClasses()
                    .that().resideInAPackage("..event..")
                    .should().dependOnClassesThat(annotatedWith(Entity.class))
                    .allowEmptyShould(true)
                    .because("Events must be decoupled from persistence entities to ensure schema evolution independence.");

    @ArchTest
    static final ArchRule event_sourcing_write_protection =
            noClasses()
                    .that().haveSimpleNameNotEndingWith("EventStore")
                    .should().accessClassesThat().haveSimpleNameEndingWith("EventRepository")
                    .because("Only EventStore classes should access EventRepository classes to ensure ledger consistency.");

    @ArchTest
    static final ArchRule event_store_must_be_transactional =
            methods()
                    .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("EventStore")
                    .and().arePublic()
                    .and().haveNameNotStartingWith("get")
                    .and().haveNameNotStartingWith("find")
                    .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
                    .because("Event sourcing operations must be transactional to guarantee atomicity.");

    @ArchTest
    static final ArchRule listeners_should_delegate_to_services =
            classes()
                    .that().resideInAPackage("..listener..")
                    .should().onlyAccessClassesThat().resideInAnyPackage(
                            "..listener..",
                            "..service..",
                            "..event..",
                            "com.banking.core..",
                            "java..", "org.slf4j..", "org.springframework.."
                    )
                    .because("Listeners should only delegate to Services and not access Repositories or Models directly.");

    @ArchTest
    static final ArchRule model_should_not_depend_on_events =
            noClasses()
                    .that().resideInAPackage("..model..")
                    .should().dependOnClassesThat().resideInAPackage("..event..")
                    .because("Domain models should not depend on Events. The flow must be: Service -> changes Model -> generates Event.");
}
