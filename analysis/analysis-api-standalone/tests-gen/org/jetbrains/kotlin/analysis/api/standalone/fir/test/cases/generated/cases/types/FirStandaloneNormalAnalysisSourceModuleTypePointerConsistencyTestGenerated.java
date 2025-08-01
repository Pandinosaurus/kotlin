/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.standalone.fir.test.cases.generated.cases.types;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analysis.api.standalone.fir.test.configurators.AnalysisApiFirStandaloneModeTestConfiguratorFactory;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfiguratorFactoryData;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.TestModuleKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.FrontendKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisSessionMode;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiMode;
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.types.AbstractTypePointerConsistencyTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/types/typePointers/consistency")
@TestDataPath("$PROJECT_ROOT")
public class FirStandaloneNormalAnalysisSourceModuleTypePointerConsistencyTestGenerated extends AbstractTypePointerConsistencyTest {
  @NotNull
  @Override
  public AnalysisApiTestConfigurator getConfigurator() {
    return AnalysisApiFirStandaloneModeTestConfiguratorFactory.INSTANCE.createConfigurator(
      new AnalysisApiTestConfiguratorFactoryData(
        FrontendKind.Fir,
        TestModuleKind.Source,
        AnalysisSessionMode.Normal,
        AnalysisApiMode.Standalone
      )
    );
  }

  @Test
  @TestMetadata("aliasedType.kt")
  public void testAliasedType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/aliasedType.kt");
  }

  @Test
  @TestMetadata("aliasedTypeToClass.kt")
  public void testAliasedTypeToClass() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/aliasedTypeToClass.kt");
  }

  @Test
  @TestMetadata("aliasedTypeUnrelatedModule.kt")
  public void testAliasedTypeUnrelatedModule() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/aliasedTypeUnrelatedModule.kt");
  }

  @Test
  public void testAllFilesPresentInConsistency() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/types/typePointers/consistency"), Pattern.compile("^(.+)\\.kt$"), null, true);
  }

  @Test
  @TestMetadata("annotatedType.kt")
  public void testAnnotatedType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/annotatedType.kt");
  }

  @Test
  @TestMetadata("badArgumentCount.kt")
  public void testBadArgumentCount() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/badArgumentCount.kt");
  }

  @Test
  @TestMetadata("badArgumentCount2.kt")
  public void testBadArgumentCount2() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/badArgumentCount2.kt");
  }

  @Test
  @TestMetadata("badArgumentCount3.kt")
  public void testBadArgumentCount3() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/badArgumentCount3.kt");
  }

  @Test
  @TestMetadata("classType.kt")
  public void testClassType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/classType.kt");
  }

  @Test
  @TestMetadata("classTypeToTypeAlias.kt")
  public void testClassTypeToTypeAlias() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/classTypeToTypeAlias.kt");
  }

  @Test
  @TestMetadata("classTypeUnrelatedModule.kt")
  public void testClassTypeUnrelatedModule() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/classTypeUnrelatedModule.kt");
  }

  @Test
  @TestMetadata("classTypeWithNestedDefinitelyNotNullType.kt")
  public void testClassTypeWithNestedDefinitelyNotNullType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/classTypeWithNestedDefinitelyNotNullType.kt");
  }

  @Test
  @TestMetadata("classTypeWithTypeArgumentToAlias.kt")
  public void testClassTypeWithTypeArgumentToAlias() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/classTypeWithTypeArgumentToAlias.kt");
  }

  @Test
  @TestMetadata("classTypeWithTypeArgumentUnrelatedModule.kt")
  public void testClassTypeWithTypeArgumentUnrelatedModule() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/classTypeWithTypeArgumentUnrelatedModule.kt");
  }

  @Test
  @TestMetadata("definitelyNotNullType.kt")
  public void testDefinitelyNotNullType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/definitelyNotNullType.kt");
  }

  @Test
  @TestMetadata("dynamicType.kt")
  public void testDynamicType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/dynamicType.kt");
  }

  @Test
  @TestMetadata("errorTypeAsArgument.kt")
  public void testErrorTypeAsArgument() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/errorTypeAsArgument.kt");
  }

  @Test
  @TestMetadata("errorTypeAsArgumentToClass.kt")
  public void testErrorTypeAsArgumentToClass() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/errorTypeAsArgumentToClass.kt");
  }

  @Test
  @TestMetadata("errorTypeAsArgumentUntelatedModule.kt")
  public void testErrorTypeAsArgumentUntelatedModule() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/errorTypeAsArgumentUntelatedModule.kt");
  }

  @Test
  @TestMetadata("flexibleType.kt")
  public void testFlexibleType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/flexibleType.kt");
  }

  @Test
  @TestMetadata("flexibleType2.kt")
  public void testFlexibleType2() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/flexibleType2.kt");
  }

  @Test
  @TestMetadata("flexibleTypeUnrelatedModule.kt")
  public void testFlexibleTypeUnrelatedModule() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/flexibleTypeUnrelatedModule.kt");
  }

  @Test
  @TestMetadata("functionType.kt")
  public void testFunctionType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/functionType.kt");
  }

  @Test
  @TestMetadata("functionTypeSuspend.kt")
  public void testFunctionTypeSuspend() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/functionTypeSuspend.kt");
  }

  @Test
  @TestMetadata("functionTypeWithContextParameter.kt")
  public void testFunctionTypeWithContextParameter() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/functionTypeWithContextParameter.kt");
  }

  @Test
  @TestMetadata("functionTypeWithContextParametersAndReceiver.kt")
  public void testFunctionTypeWithContextParametersAndReceiver() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/functionTypeWithContextParametersAndReceiver.kt");
  }

  @Test
  @TestMetadata("functionTypeWithReceiver.kt")
  public void testFunctionTypeWithReceiver() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/functionTypeWithReceiver.kt");
  }

  @Test
  @TestMetadata("implicitFlexibleDnnType.kt")
  public void testImplicitFlexibleDnnType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/implicitFlexibleDnnType.kt");
  }

  @Test
  @TestMetadata("intersectionType.kt")
  public void testIntersectionType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/intersectionType.kt");
  }

  @Test
  @TestMetadata("nestedDefinitelyNotNullType.kt")
  public void testNestedDefinitelyNotNullType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/nestedDefinitelyNotNullType.kt");
  }

  @Test
  @TestMetadata("nullableClassType.kt")
  public void testNullableClassType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/nullableClassType.kt");
  }

  @Test
  @TestMetadata("nullableType.kt")
  public void testNullableType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/nullableType.kt");
  }

  @Test
  @TestMetadata("qualifierNotFound.kt")
  public void testQualifierNotFound() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/qualifierNotFound.kt");
  }

  @Test
  @TestMetadata("qualifierNotFound2.kt")
  public void testQualifierNotFound2() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/qualifierNotFound2.kt");
  }

  @Test
  @TestMetadata("symbolNotFound.kt")
  public void testSymbolNotFound() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/symbolNotFound.kt");
  }

  @Test
  @TestMetadata("symbolNotFoundToClass.kt")
  public void testSymbolNotFoundToClass() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/symbolNotFoundToClass.kt");
  }

  @Test
  @TestMetadata("symbolNotFoundUnrelatedModule.kt")
  public void testSymbolNotFoundUnrelatedModule() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/symbolNotFoundUnrelatedModule.kt");
  }

  @Test
  @TestMetadata("typeParameterType.kt")
  public void testTypeParameterType() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/typeParameterType.kt");
  }

  @Test
  @TestMetadata("typeParameterType2.kt")
  public void testTypeParameterType2() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/typeParameterType2.kt");
  }

  @Test
  @TestMetadata("variance.kt")
  public void testVariance() {
    runTest("analysis/analysis-api/testData/types/typePointers/consistency/variance.kt");
  }
}
