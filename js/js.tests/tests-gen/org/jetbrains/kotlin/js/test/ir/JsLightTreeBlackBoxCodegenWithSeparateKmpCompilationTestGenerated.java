/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.ir;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.GenerateJsTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/box/multiplatform/k2")
@TestDataPath("$PROJECT_ROOT")
public class JsLightTreeBlackBoxCodegenWithSeparateKmpCompilationTestGenerated extends AbstractJsLightTreeBlackBoxCodegenWithSeparateKmpCompilationTest {
  @Test
  @TestMetadata("actualInnerClassesFirMemberMapping.kt")
  public void testActualInnerClassesFirMemberMapping() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/actualInnerClassesFirMemberMapping.kt");
  }

  @Test
  @TestMetadata("aliasSuperTypeInLazy.kt")
  public void testAliasSuperTypeInLazy() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/aliasSuperTypeInLazy.kt");
  }

  @Test
  public void testAllFilesPresentInK2() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
  }

  @Test
  @TestMetadata("anonymousObjectAndSpecificImplementationInDeserializedIr.kt")
  public void testAnonymousObjectAndSpecificImplementationInDeserializedIr() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/anonymousObjectAndSpecificImplementationInDeserializedIr.kt");
  }

  @Test
  @TestMetadata("commonFakeOverridePropertyRef.kt")
  public void testCommonFakeOverridePropertyRef() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/commonFakeOverridePropertyRef.kt");
  }

  @Test
  @TestMetadata("commonInternal.kt")
  public void testCommonInternal() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/commonInternal.kt");
  }

  @Test
  @TestMetadata("covariantOverrideInActual.kt")
  public void testCovariantOverrideInActual() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/covariantOverrideInActual.kt");
  }

  @Test
  @TestMetadata("dataClassInCommonAndPlatform.kt")
  public void testDataClassInCommonAndPlatform() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/dataClassInCommonAndPlatform.kt");
  }

  @Test
  @TestMetadata("enumStaticMethods.kt")
  public void testEnumStaticMethods() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/enumStaticMethods.kt");
  }

  @Test
  @TestMetadata("expectNonExpectOverloads.kt")
  public void testExpectNonExpectOverloads() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/expectNonExpectOverloads.kt");
  }

  @Test
  @TestMetadata("expectRefinement.kt")
  public void testExpectRefinement() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/expectRefinement.kt");
  }

  @Test
  @TestMetadata("expectValInInlineClass.kt")
  public void testExpectValInInlineClass() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/expectValInInlineClass.kt");
  }

  @Test
  @TestMetadata("extensionPropertiesOverloads.kt")
  public void testExtensionPropertiesOverloads() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/extensionPropertiesOverloads.kt");
  }

  @Test
  @TestMetadata("internalOverride.kt")
  public void testInternalOverride() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/internalOverride.kt");
  }

  @Test
  @TestMetadata("internalOverride2.kt")
  public void testInternalOverride2() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/internalOverride2.kt");
  }

  @Test
  @TestMetadata("kt57391.kt")
  public void testKt57391() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/kt57391.kt");
  }

  @Test
  @TestMetadata("kt59613.kt")
  public void testKt59613() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/kt59613.kt");
  }

  @Test
  @TestMetadata("kt61166.kt")
  public void testKt61166() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/kt61166.kt");
  }

  @Test
  @TestMetadata("kt66970.kt")
  public void testKt66970() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/kt66970.kt");
  }

  @Test
  @TestMetadata("kt68801.kt")
  public void testKt68801() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/kt68801.kt");
  }

  @Test
  @TestMetadata("kt-65249.kt")
  public void testKt_65249() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/kt-65249.kt");
  }

  @Test
  @TestMetadata("mergedOverrides.kt")
  public void testMergedOverrides() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/mergedOverrides.kt");
  }

  @Test
  @TestMetadata("privateConstructorWithDefaults.kt")
  public void testPrivateConstructorWithDefaults() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/privateConstructorWithDefaults.kt");
  }

  @Test
  @TestMetadata("regularAndDeprecatedOverloads.kt")
  public void testRegularAndDeprecatedOverloads() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/regularAndDeprecatedOverloads.kt");
  }

  @Test
  @TestMetadata("starImportOfExpectEnumWithActualTypeAlias.kt")
  public void testStarImportOfExpectEnumWithActualTypeAlias() {
    runTest("compiler/testData/codegen/box/multiplatform/k2/starImportOfExpectEnumWithActualTypeAlias.kt");
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/annotations")
  @TestDataPath("$PROJECT_ROOT")
  public class Annotations {
    @Test
    public void testAllFilesPresentInAnnotations() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/annotations"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("deprecatedAnnotationOnlyOnActual_useInCommon.kt")
    public void testDeprecatedAnnotationOnlyOnActual_useInCommon() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/annotations/deprecatedAnnotationOnlyOnActual_useInCommon.kt");
    }

    @Test
    @TestMetadata("expectAnnotationCallInLibrary.kt")
    public void testExpectAnnotationCallInLibrary() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/annotations/expectAnnotationCallInLibrary.kt");
    }

    @Test
    @TestMetadata("optionalExpectation.kt")
    public void testOptionalExpectation() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/annotations/optionalExpectation.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/basic")
  @TestDataPath("$PROJECT_ROOT")
  public class Basic {
    @Test
    @TestMetadata("accessToLocalClassFromBackend.kt")
    public void testAccessToLocalClassFromBackend() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/accessToLocalClassFromBackend.kt");
    }

    @Test
    @TestMetadata("actualFunctionWithArgumentOfExpectType.kt")
    public void testActualFunctionWithArgumentOfExpectType() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/actualFunctionWithArgumentOfExpectType.kt");
    }

    @Test
    public void testAllFilesPresentInBasic() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/basic"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("anyMethodInExpect.kt")
    public void testAnyMethodInExpect() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/anyMethodInExpect.kt");
    }

    @Test
    @TestMetadata("contextInFakeOverride.kt")
    public void testContextInFakeOverride() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/contextInFakeOverride.kt");
    }

    @Test
    @TestMetadata("contextOnExpect.kt")
    public void testContextOnExpect() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/contextOnExpect.kt");
    }

    @Test
    @TestMetadata("contextOnExpectActual.kt")
    public void testContextOnExpectActual() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/contextOnExpectActual.kt");
    }

    @Test
    @TestMetadata("correctParentForTypeParameter.kt")
    public void testCorrectParentForTypeParameter() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/correctParentForTypeParameter.kt");
    }

    @Test
    @TestMetadata("delegatedByExpectExtensionProperty.kt")
    public void testDelegatedByExpectExtensionProperty() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/delegatedByExpectExtensionProperty.kt");
    }

    @Test
    @TestMetadata("enumEntryNameCall.kt")
    public void testEnumEntryNameCall() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/enumEntryNameCall.kt");
    }

    @Test
    @TestMetadata("expectActualCallableReference.kt")
    public void testExpectActualCallableReference() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualCallableReference.kt");
    }

    @Test
    @TestMetadata("expectActualDifferentExtensionReceiversOnOverloads.kt")
    public void testExpectActualDifferentExtensionReceiversOnOverloads() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualDifferentExtensionReceiversOnOverloads.kt");
    }

    @Test
    @TestMetadata("expectActualDifferentPackages.kt")
    public void testExpectActualDifferentPackages() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualDifferentPackages.kt");
    }

    @Test
    @TestMetadata("expectActualFakeOverrides.kt")
    public void testExpectActualFakeOverrides() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualFakeOverrides.kt");
    }

    @Test
    @TestMetadata("expectActualFakeOverrides2.kt")
    public void testExpectActualFakeOverrides2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualFakeOverrides2.kt");
    }

    @Test
    @TestMetadata("expectActualFakeOverrides3.kt")
    public void testExpectActualFakeOverrides3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualFakeOverrides3.kt");
    }

    @Test
    @TestMetadata("expectActualFakeOverridesWithTypeParameters.kt")
    public void testExpectActualFakeOverridesWithTypeParameters() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualFakeOverridesWithTypeParameters.kt");
    }

    @Test
    @TestMetadata("expectActualFakeOverridesWithTypeParameters2.kt")
    public void testExpectActualFakeOverridesWithTypeParameters2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualFakeOverridesWithTypeParameters2.kt");
    }

    @Test
    @TestMetadata("expectActualIntersectionOverride.kt")
    public void testExpectActualIntersectionOverride() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualIntersectionOverride.kt");
    }

    @Test
    @TestMetadata("expectActualIntersectionOverride2.kt")
    public void testExpectActualIntersectionOverride2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualIntersectionOverride2.kt");
    }

    @Test
    @TestMetadata("expectActualMultiCommon.kt")
    public void testExpectActualMultiCommon() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualMultiCommon.kt");
    }

    @Test
    @TestMetadata("expectActualNullabilityBasedOverloads.kt")
    public void testExpectActualNullabilityBasedOverloads() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualNullabilityBasedOverloads.kt");
    }

    @Test
    @TestMetadata("expectActualOverloads.kt")
    public void testExpectActualOverloads() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualOverloads.kt");
    }

    @Test
    @TestMetadata("expectActualSimple.kt")
    public void testExpectActualSimple() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualSimple.kt");
    }

    @Test
    @TestMetadata("expectActualTypeParameters.kt")
    public void testExpectActualTypeParameters() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualTypeParameters.kt");
    }

    @Test
    @TestMetadata("expectActualTypealias.kt")
    public void testExpectActualTypealias() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectActualTypealias.kt");
    }

    @Test
    @TestMetadata("expectAndCommonFunctionOverloads.kt")
    public void testExpectAndCommonFunctionOverloads() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectAndCommonFunctionOverloads.kt");
    }

    @Test
    @TestMetadata("expectInterfaceInSupertypes.kt")
    public void testExpectInterfaceInSupertypes() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectInterfaceInSupertypes.kt");
    }

    @Test
    @TestMetadata("expectInterfaceInSupertypes2.kt")
    public void testExpectInterfaceInSupertypes2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectInterfaceInSupertypes2.kt");
    }

    @Test
    @TestMetadata("expectProperty.kt")
    public void testExpectProperty() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/expectProperty.kt");
    }

    @Test
    @TestMetadata("extensionAndFakeOverride.kt")
    public void testExtensionAndFakeOverride() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionAndFakeOverride.kt");
    }

    @Test
    @TestMetadata("extensionFunction.kt")
    public void testExtensionFunction() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunction.kt");
    }

    @Test
    @TestMetadata("extensionFunctionAsASupertype.kt")
    public void testExtensionFunctionAsASupertype() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunctionAsASupertype.kt");
    }

    @Test
    @TestMetadata("extensionFunctionAsASupertypeConversion.kt")
    public void testExtensionFunctionAsASupertypeConversion() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunctionAsASupertypeConversion.kt");
    }

    @Test
    @TestMetadata("extensionFunctionAsAType.kt")
    public void testExtensionFunctionAsAType() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunctionAsAType.kt");
    }

    @Test
    @TestMetadata("extensionFunctionAsAnonymous.kt")
    public void testExtensionFunctionAsAnonymous() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunctionAsAnonymous.kt");
    }

    @Test
    @TestMetadata("extensionFunctionInDelegatedSam.kt")
    public void testExtensionFunctionInDelegatedSam() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunctionInDelegatedSam.kt");
    }

    @Test
    @TestMetadata("extensionFunctionOnExpect.kt")
    public void testExtensionFunctionOnExpect() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionFunctionOnExpect.kt");
    }

    @Test
    @TestMetadata("extensionMember.kt")
    public void testExtensionMember() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionMember.kt");
    }

    @Test
    @TestMetadata("extensionOnNestedReceiver.kt")
    public void testExtensionOnNestedReceiver() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionOnNestedReceiver.kt");
    }

    @Test
    @TestMetadata("extensionPropertyWithAnonymousExtension.kt")
    public void testExtensionPropertyWithAnonymousExtension() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/extensionPropertyWithAnonymousExtension.kt");
    }

    @Test
    @TestMetadata("fakeOverridesInPlatformModule.kt")
    public void testFakeOverridesInPlatformModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/fakeOverridesInPlatformModule.kt");
    }

    @Test
    @TestMetadata("independentCommonSourceModules.kt")
    public void testIndependentCommonSourceModules() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/independentCommonSourceModules.kt");
    }

    @Test
    @TestMetadata("interfaceMethodFromSuperTypeIsImplementedInOtherExpectSuperClass.kt")
    public void testInterfaceMethodFromSuperTypeIsImplementedInOtherExpectSuperClass() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/interfaceMethodFromSuperTypeIsImplementedInOtherExpectSuperClass.kt");
    }

    @Test
    @TestMetadata("intersectionOverrideInCommonModule.kt")
    public void testIntersectionOverrideInCommonModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/intersectionOverrideInCommonModule.kt");
    }

    @Test
    @TestMetadata("intersectionOverrideWithDefaultParameterInCommonModule.kt")
    public void testIntersectionOverrideWithDefaultParameterInCommonModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/intersectionOverrideWithDefaultParameterInCommonModule.kt");
    }

    @Test
    @TestMetadata("kt-56329.kt")
    public void testKt_56329() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/kt-56329.kt");
    }

    @Test
    @TestMetadata("localIntersectionOverrideInCommonModule.kt")
    public void testLocalIntersectionOverrideInCommonModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/localIntersectionOverrideInCommonModule.kt");
    }

    @Test
    @TestMetadata("localIntersectionOverrideWithDefaultParameterInCommonModule.kt")
    public void testLocalIntersectionOverrideWithDefaultParameterInCommonModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/localIntersectionOverrideWithDefaultParameterInCommonModule.kt");
    }

    @Test
    @TestMetadata("localSubstitutionOverrideInCommonModule.kt")
    public void testLocalSubstitutionOverrideInCommonModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/localSubstitutionOverrideInCommonModule.kt");
    }

    @Test
    @TestMetadata("nonExternalEquals.kt")
    public void testNonExternalEquals() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/nonExternalEquals.kt");
    }

    @Test
    @TestMetadata("overridesOfExpectMembers.kt")
    public void testOverridesOfExpectMembers() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/overridesOfExpectMembers.kt");
    }

    @Test
    @TestMetadata("removeExpectDeclarationsFromMetadata.kt")
    public void testRemoveExpectDeclarationsFromMetadata() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/removeExpectDeclarationsFromMetadata.kt");
    }

    @Test
    @TestMetadata("substitutionOverrideInCommonModule.kt")
    public void testSubstitutionOverrideInCommonModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/substitutionOverrideInCommonModule.kt");
    }

    @Test
    @TestMetadata("transitiveSuperclassActualization.kt")
    public void testTransitiveSuperclassActualization() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/transitiveSuperclassActualization.kt");
    }

    @Test
    @TestMetadata("widerVisibilityInActualClassifier.kt")
    public void testWiderVisibilityInActualClassifier() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/basic/widerVisibilityInActualClassifier.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/complexMatchings")
  @TestDataPath("$PROJECT_ROOT")
  public class ComplexMatchings {
    @Test
    public void testAllFilesPresentInComplexMatchings() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/complexMatchings"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("expectCtorlessFinalToActualObject.kt")
    public void testExpectCtorlessFinalToActualObject() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/complexMatchings/expectCtorlessFinalToActualObject.kt");
    }

    @Test
    @TestMetadata("expectCtorlessFinalToActualPromiseOfUnit.kt")
    public void testExpectCtorlessFinalToActualPromiseOfUnit() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/complexMatchings/expectCtorlessFinalToActualPromiseOfUnit.kt");
    }

    @Test
    @TestMetadata("expectCtorlessFinalToActualUnit.kt")
    public void testExpectCtorlessFinalToActualUnit() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/complexMatchings/expectCtorlessFinalToActualUnit.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/defaultArguments")
  @TestDataPath("$PROJECT_ROOT")
  public class DefaultArguments {
    @Test
    public void testAllFilesPresentInDefaultArguments() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/defaultArguments"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("bothInExpectAndActual.kt")
    public void testBothInExpectAndActual() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/bothInExpectAndActual.kt");
    }

    @Test
    @TestMetadata("bothInExpectAndActual2.kt")
    public void testBothInExpectAndActual2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/bothInExpectAndActual2.kt");
    }

    @Test
    @TestMetadata("constructor.kt")
    public void testConstructor() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/constructor.kt");
    }

    @Test
    @TestMetadata("defaultArgumentInDelegatedFunction.kt")
    public void testDefaultArgumentInDelegatedFunction() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/defaultArgumentInDelegatedFunction.kt");
    }

    @Test
    @TestMetadata("delegatedExpectedInterface.kt")
    public void testDelegatedExpectedInterface() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/delegatedExpectedInterface.kt");
    }

    @Test
    @TestMetadata("dispatchReceiverValue.kt")
    public void testDispatchReceiverValue() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/dispatchReceiverValue.kt");
    }

    @Test
    @TestMetadata("expectPropertyAsDefaultArgument.kt")
    public void testExpectPropertyAsDefaultArgument() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/expectPropertyAsDefaultArgument.kt");
    }

    @Test
    @TestMetadata("extensionReceiverValue.kt")
    public void testExtensionReceiverValue() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/extensionReceiverValue.kt");
    }

    @Test
    @TestMetadata("function.kt")
    public void testFunction() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/function.kt");
    }

    @Test
    @TestMetadata("functionFromOtherModule.kt")
    public void testFunctionFromOtherModule() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/functionFromOtherModule.kt");
    }

    @Test
    @TestMetadata("inheritedFromCommonClass.kt")
    public void testInheritedFromCommonClass() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inheritedFromCommonClass.kt");
    }

    @Test
    @TestMetadata("inheritedFromExpectedClass.kt")
    public void testInheritedFromExpectedClass() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inheritedFromExpectedClass.kt");
    }

    @Test
    @TestMetadata("inheritedFromExpectedInterface.kt")
    public void testInheritedFromExpectedInterface() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inheritedFromExpectedInterface.kt");
    }

    @Test
    @TestMetadata("inheritedFromExpectedMethod.kt")
    public void testInheritedFromExpectedMethod() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inheritedFromExpectedMethod.kt");
    }

    @Test
    @TestMetadata("inheritedInExpectedDeclarations.kt")
    public void testInheritedInExpectedDeclarations() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inheritedInExpectedDeclarations.kt");
    }

    @Test
    @TestMetadata("inheritedViaAnotherInterfaceIndirectly.kt")
    public void testInheritedViaAnotherInterfaceIndirectly() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inheritedViaAnotherInterfaceIndirectly.kt");
    }

    @Test
    @TestMetadata("inlineFunctionWithDefaultLambda.kt")
    public void testInlineFunctionWithDefaultLambda() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/inlineFunctionWithDefaultLambda.kt");
    }

    @Test
    @TestMetadata("kt23239.kt")
    public void testKt23239() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/kt23239.kt");
    }

    @Test
    @TestMetadata("kt23739.kt")
    public void testKt23739() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/kt23739.kt");
    }

    @Test
    @TestMetadata("kt67488.kt")
    public void testKt67488() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/kt67488.kt");
    }

    @Test
    @TestMetadata("nestedEnumEntryValue.kt")
    public void testNestedEnumEntryValue() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/nestedEnumEntryValue.kt");
    }

    @Test
    @TestMetadata("parametersInArgumentValues.kt")
    public void testParametersInArgumentValues() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/parametersInArgumentValues.kt");
    }

    @Test
    @TestMetadata("superCall.kt")
    public void testSuperCall() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/superCall.kt");
    }

    @Test
    @TestMetadata("suspend.kt")
    public void testSuspend() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/suspend.kt");
    }

    @Test
    @TestMetadata("typeAlias.kt")
    public void testTypeAlias() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/typeAlias.kt");
    }

    @Test
    @TestMetadata("typeAlias2.kt")
    public void testTypeAlias2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/typeAlias2.kt");
    }

    @Test
    @TestMetadata("withTypeParameter.kt")
    public void testWithTypeParameter() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/defaultArguments/withTypeParameter.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/delegation")
  @TestDataPath("$PROJECT_ROOT")
  public class Delegation {
    @Test
    public void testAllFilesPresentInDelegation() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/delegation"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("delegationToExpectInterfaceByExpectFun.kt")
    public void testDelegationToExpectInterfaceByExpectFun() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationToExpectInterfaceByExpectFun.kt");
    }

    @Test
    @TestMetadata("delegationToExpectInterfaceWithOverride_noNewMembers.kt")
    public void testDelegationToExpectInterfaceWithOverride_noNewMembers() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationToExpectInterfaceWithOverride_noNewMembers.kt");
    }

    @Test
    @TestMetadata("delegationToExpectInterface_noNewMembers.kt")
    public void testDelegationToExpectInterface_noNewMembers() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationToExpectInterface_noNewMembers.kt");
    }

    @Test
    @TestMetadata("delegationToExpectInterface_withNewMembers.kt")
    public void testDelegationToExpectInterface_withNewMembers() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationToExpectInterface_withNewMembers.kt");
    }

    @Test
    @TestMetadata("delegationToExpectInterface_withNewMembersSameName.kt")
    public void testDelegationToExpectInterface_withNewMembersSameName() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationToExpectInterface_withNewMembersSameName.kt");
    }

    @Test
    @TestMetadata("delegationToExpectInterface_withOverrideInDelegated.kt")
    public void testDelegationToExpectInterface_withOverrideInDelegated() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationToExpectInterface_withOverrideInDelegated.kt");
    }

    @Test
    @TestMetadata("delegationWithIntersection.kt")
    public void testDelegationWithIntersection() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/delegation/delegationWithIntersection.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/exhaustiveness")
  @TestDataPath("$PROJECT_ROOT")
  public class Exhaustiveness {
    @Test
    public void testAllFilesPresentInExhaustiveness() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/exhaustiveness"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("commonEnum.kt")
    public void testCommonEnum() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/exhaustiveness/commonEnum.kt");
    }

    @Test
    @TestMetadata("commonSealedClass.kt")
    public void testCommonSealedClass() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/exhaustiveness/commonSealedClass.kt");
    }

    @Test
    @TestMetadata("commonSealedInterface.kt")
    public void testCommonSealedInterface() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/exhaustiveness/commonSealedInterface.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/hmpp")
  @TestDataPath("$PROJECT_ROOT")
  public class Hmpp {
    @Test
    @TestMetadata("abstractExpectedActual2-2.kt")
    public void testAbstractExpectedActual2_2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/abstractExpectedActual2-2.kt");
    }

    @Test
    public void testAllFilesPresentInHmpp() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/hmpp"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("expectActualChain2-2.kt")
    public void testExpectActualChain2_2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectActualChain2-2.kt");
    }

    @Test
    @TestMetadata("expectActualChain2-3.kt")
    public void testExpectActualChain2_3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectActualChain2-3.kt");
    }

    @Test
    @TestMetadata("expectActualChain3-3.kt")
    public void testExpectActualChain3_3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectActualChain3-3.kt");
    }

    @Test
    @TestMetadata("expectActualChain3-3-error.kt")
    public void testExpectActualChain3_3_error() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectActualChain3-3-error.kt");
    }

    @Test
    @TestMetadata("expectActualChain3-4.kt")
    public void testExpectActualChain3_4() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectActualChain3-4.kt");
    }

    @Test
    @TestMetadata("expectActualChain4-4.kt")
    public void testExpectActualChain4_4() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectActualChain4-4.kt");
    }

    @Test
    @TestMetadata("expectRefinement.kt")
    public void testExpectRefinement() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/expectRefinement.kt");
    }

    @Test
    @TestMetadata("iheritanceFromExpected2-2.kt")
    public void testIheritanceFromExpected2_2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/iheritanceFromExpected2-2.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpected2-3.kt")
    public void testInheritanceFromExpected2_3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpected2-3.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpected3-3.kt")
    public void testInheritanceFromExpected3_3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpected3-3.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpected3-4.kt")
    public void testInheritanceFromExpected3_4() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpected3-4.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpected4-4.kt")
    public void testInheritanceFromExpected4_4() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpected4-4.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpectedInApp2-2.kt")
    public void testInheritanceFromExpectedInApp2_2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpectedInApp2-2.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpectedInApp2-3.kt")
    public void testInheritanceFromExpectedInApp2_3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpectedInApp2-3.kt");
    }

    @Test
    @TestMetadata("inheritanceFromExpectedInApp3-3.kt")
    public void testInheritanceFromExpectedInApp3_3() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromExpectedInApp3-3.kt");
    }

    @Test
    @TestMetadata("inheritanceFromLibraryExpectClass.kt")
    public void testInheritanceFromLibraryExpectClass() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/inheritanceFromLibraryExpectClass.kt");
    }

    @Test
    @TestMetadata("mutlipleExpectsForOneActual.kt")
    public void testMutlipleExpectsForOneActual() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/mutlipleExpectsForOneActual.kt");
    }

    @Test
    @TestMetadata("openExpectedActual2-2.kt")
    public void testOpenExpectedActual2_2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/openExpectedActual2-2.kt");
    }

    @Test
    @TestMetadata("sameLibraryInTwoEdgesOfDiamond.kt")
    public void testSameLibraryInTwoEdgesOfDiamond() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/sameLibraryInTwoEdgesOfDiamond.kt");
    }

    @Test
    @TestMetadata("simple.kt")
    public void testSimple() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/simple.kt");
    }

    @Test
    @TestMetadata("typealiasActualisation2-2.kt")
    public void testTypealiasActualisation2_2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/hmpp/typealiasActualisation2-2.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/lenientMode")
  @TestDataPath("$PROJECT_ROOT")
  public class LenientMode {
    @Test
    public void testAllFilesPresentInLenientMode() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/lenientMode"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests")
  @TestDataPath("$PROJECT_ROOT")
  public class MigratedOldTests {
    @Test
    public void testAllFilesPresentInMigratedOldTests() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }

    @Test
    @TestMetadata("mpp1.kt")
    public void testMpp1() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests/mpp1.kt");
    }

    @Test
    @TestMetadata("mpp2.kt")
    public void testMpp2() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests/mpp2.kt");
    }

    @Test
    @TestMetadata("mpp_default_args.kt")
    public void testMpp_default_args() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests/mpp_default_args.kt");
    }

    @Test
    @TestMetadata("mpp_optional_expectation.kt")
    public void testMpp_optional_expectation() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests/mpp_optional_expectation.kt");
    }

    @Test
    @TestMetadata("remap_expect_property_ref.kt")
    public void testRemap_expect_property_ref() {
      runTest("compiler/testData/codegen/box/multiplatform/k2/migratedOldTests/remap_expect_property_ref.kt");
    }
  }

  @Nested
  @TestMetadata("compiler/testData/codegen/box/multiplatform/k2/multiModule")
  @TestDataPath("$PROJECT_ROOT")
  public class MultiModule {
    @Test
    public void testAllFilesPresentInMultiModule() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/box/multiplatform/k2/multiModule"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS_IR, true);
    }
  }
}
