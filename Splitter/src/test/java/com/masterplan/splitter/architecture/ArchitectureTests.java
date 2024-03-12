package com.masterplan.splitter.architecture;

import static com.masterplan.splitter.architecture.rules.HaveExactlyOneAggregateRoot.HAVE_EXACTLY_ONE_AGGREGATE_ROOT;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.masterplan.splitter.SplitterApplication;
import com.masterplan.splitter.annotations.AggregateRoot;
import com.masterplan.splitter.annotations.Value;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@AnalyzeClasses(
    packagesOf = SplitterApplication.class,
    importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTests {

  @ArchTest
  static final ArchRule onlyAggregateRootsAndValuesArePublic =
      classes()
          .that()
          .areNotAnnotatedWith(AggregateRoot.class)
          .and()
          .areNotAnnotatedWith(Value.class)
          .and()
          .resideInAPackage("..domain.aggregates..")
          .should()
          .notBePublic()
          .allowEmptyShould(true)
          .because("the implementation of an aggregate " + "should be hidden");

  @ArchTest
  static final ArchRule oneAggregateRootPerAggregate =
      slices()
          .matching("..domain.aggregates.(*)..")
          .should(HAVE_EXACTLY_ONE_AGGREGATE_ROOT);

  @ArchTest
  static final ArchRule onion =
      onionArchitecture()
          .domainModels("com.masterplan.splitter.domain..")
          .domainServices("com.masterplan.splitter.domain.service..")
          .applicationServices("com.masterplan.splitter.application..")
          .adapter("web", "com.masterplan.splitter.web..")
          .adapter("persistence", "com.masterplan.splitter.persistence..");

  @ArchTest
  static final ArchRule noAccessToController =
      fields()
          .that()
          .areDeclaredInClassesThat()
          .areAnnotatedWith(Controller.class)
          .should()
          .bePrivate();

  @ArchTest
  static final ArchRule annotationOnService =
      classes()
          .that()
          .resideInAnyPackage("..application.service..")
          .should()
          .beAnnotatedWith(Service.class);

  @ArchTest
  static final ArchRule DependencyInjectionUsing =
      noClasses().should().beAnnotatedWith(Component.class);

  @ArchTest
  static final ArchRule domainServiceNotAnnotatedWithService =
      classes()
          .that()
          .resideInAPackage("..domain.service..")
          .should()
          .notBeAnnotatedWith(Service.class);

  // soll nur an Repo und Domain dependency haben
  @ArchTest
  static final ArchRule applicationServiceDependsOnDomainService =
      classes()
          .that()
          .resideInAPackage("..application.service..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..domain.service..")
          .orShould()
          .resideInAPackage("..application.service..");

  @ArchTest
  static final ArchRule controllerDependsOnApplicationService =
      classes()
          .that()
          .resideInAPackage("..web.controller..")
          .and()
          .areAnnotatedWith(Controller.class)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..application.service..");

  @ArchTest
  static final ArchRule formsAreRecords =
      classes()
          .that()
          .resideInAPackage("..web.forms..")
          .should()
          .beRecords();

  @ArchTest
  static final ArchRule configsAreAnnotated =
      classes()
          .that()
          .resideInAPackage("..config..")
          .should()
          .beAnnotatedWith(Configuration.class);
}
