package com.banking.notificationservice;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.banking.notificationservice")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule respect_layered_architecture = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Listener").definedBy("..listener..")
            .layer("Notifier").definedBy("..notifier..")

            .whereLayer("Service").mayOnlyBeAccessedByLayers("Listener")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Notifier").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule notifiers_must_reside_in_notifier_package =
            classes()
                    .that().haveSimpleNameEndingWith("Notifier")
                    .should().resideInAPackage("..notifier..");

    @ArchTest
    static final ArchRule kafka_listeners_must_be_in_right_place =
            methods()
                    .that().areAnnotatedWith("org.springframework.kafka.annotation.KafkaListener")
                    .should().beDeclaredInClassesThat().resideInAPackage("..listener..")
                    .andShould().beDeclaredInClassesThat().haveSimpleNameEndingWith("Consumer");

    @ArchTest
    static final ArchRule services_must_be_annotated =
            classes()
                    .that().resideInAPackage("..service..")
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule no_cycle_between_packages =
            slices().matching("com.banking.notificationservice.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule entities_must_be_annotated_with_entity =
            classes()
                    .that().haveSimpleNameEndingWith("Entity")
                    .should().beAnnotatedWith(Entity.class);

    @ArchTest
    static final ArchRule entities_must_not_have_json_annotations =
            noClasses()
                    .that().resideInAPackage("..model..")
                    .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                    .because("Domain Entities must not have serialization dependencies (JSON).");

    @ArchTest
    static final ArchRule services_must_not_be_web_controllers =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().beAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
                    .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .because("Notification Service is asynchronous and should not have REST endpoints.");

    @ArchTest
    static final ArchRule repositories_must_be_interfaces =
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().beInterfaces()
                    .because("Spring JPA repositories should be interfaces");

    @ArchTest
    static final ArchRule no_generic_exceptions = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
            .because("Throw specific business exceptions instead of generic RuntimeException.");

    @ArchTest
    static final ArchRule no_system_out = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
            .because("Use Logger (SLF4J) instead of System.out");

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
}
