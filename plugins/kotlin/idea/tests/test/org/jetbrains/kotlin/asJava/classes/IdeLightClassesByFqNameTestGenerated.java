// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.asJava.classes;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.idea.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.idea.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.jetbrains.kotlin.idea.base.test.TestRoot;
import org.junit.runner.RunWith;
import static org.jetbrains.kotlin.idea.base.plugin.artifacts.TestKotlinArtifacts.compilerTestData;

/**
 * This class is generated by {@link org.jetbrains.kotlin.testGenerator.generator.TestGenerator}.
 * DO NOT MODIFY MANUALLY.
 */
@SuppressWarnings("all")
@TestRoot("idea/tests")
@TestDataPath("$CONTENT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
@TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName")
public abstract class IdeLightClassesByFqNameTestGenerated extends AbstractIdeLightClassesByFqNameTest {
    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors")
    public static class CompilationErrors extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors");
            super.setUp();
        }

        @TestMetadata("ActualClass.kt")
        public void testActualClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ActualClass.kt"));
        }

        @TestMetadata("ActualTypeAlias.kt")
        public void testActualTypeAlias() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ActualTypeAlias.kt"));
        }

        @TestMetadata("ActualTypeAliasCustomJvmPackageName.kt")
        public void testActualTypeAliasCustomJvmPackageName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ActualTypeAliasCustomJvmPackageName.kt"));
        }

        @TestMetadata("AllInlineOnly.kt")
        public void testAllInlineOnly() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/AllInlineOnly.kt"));
        }

        @TestMetadata("AnnotationModifiers.kt")
        public void testAnnotationModifiers() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/AnnotationModifiers.kt"));
        }

        @TestMetadata("EnumNameOverride.kt")
        public void testEnumNameOverride() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/EnumNameOverride.kt"));
        }

        @TestMetadata("ExpectClass.kt")
        public void testExpectClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ExpectClass.kt"));
        }

        @TestMetadata("ExpectObject.kt")
        public void testExpectObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ExpectObject.kt"));
        }

        @TestMetadata("ExpectedNestedClass.kt")
        public void testExpectedNestedClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ExpectedNestedClass.kt"));
        }

        @TestMetadata("ExpectedNestedClassInObject.kt")
        public void testExpectedNestedClassInObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/ExpectedNestedClassInObject.kt"));
        }

        @TestMetadata("FunctionWithoutName.kt")
        public void testFunctionWithoutName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/FunctionWithoutName.kt"));
        }

        @TestMetadata("JvmPackageName.kt")
        public void testJvmPackageName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/JvmPackageName.kt"));
        }

        @TestMetadata("LocalInAnnotation.kt")
        public void testLocalInAnnotation() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/LocalInAnnotation.kt"));
        }

        @TestMetadata("PrivateInTrait.kt")
        public void testPrivateInTrait() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/PrivateInTrait.kt"));
        }

        @TestMetadata("PropertyWithoutName.kt")
        public void testPropertyWithoutName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/PropertyWithoutName.kt"));
        }

        @TestMetadata("RepetableAnnotations.kt")
        public void testRepetableAnnotations() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/RepetableAnnotations.kt"));
        }

        @TestMetadata("SameName.kt")
        public void testSameName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/SameName.kt"));
        }

        @TestMetadata("TopLevelDestructuring.kt")
        public void testTopLevelDestructuring() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/TopLevelDestructuring.kt"));
        }

        @TestMetadata("TraitClassObjectField.kt")
        public void testTraitClassObjectField() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/TraitClassObjectField.kt"));
        }

        @TestMetadata("TwoOverrides.kt")
        public void testTwoOverrides() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/TwoOverrides.kt"));
        }

        @TestMetadata("unresolvedQuialifierInAnnotation.kt")
        public void testUnresolvedQuialifierInAnnotation() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/unresolvedQuialifierInAnnotation.kt"));
        }

        @TestMetadata("WrongAnnotations.kt")
        public void testWrongAnnotations() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/compilationErrors/WrongAnnotations.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName/delegation")
    public static class Delegation extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/delegation");
            super.setUp();
        }

        @TestMetadata("Function.kt")
        public void testFunction() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/delegation/Function.kt"));
        }

        @TestMetadata("Property.kt")
        public void testProperty() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/delegation/Property.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName/facades")
    public static class Facades extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/facades");
            super.setUp();
        }

        @TestMetadata("AllPrivate.kt")
        public void testAllPrivate() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/facades/AllPrivate.kt"));
        }

        @TestMetadata("InternalFacadeClass.kt")
        public void testInternalFacadeClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/facades/InternalFacadeClass.kt"));
        }

        @TestMetadata("MultiFile.kt")
        public void testMultiFile() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/facades/MultiFile.kt"));
        }

        @TestMetadata("SingleFile.kt")
        public void testSingleFile() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/facades/SingleFile.kt"));
        }

        @TestMetadata("SingleJvmClassName.kt")
        public void testSingleJvmClassName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/facades/SingleJvmClassName.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression")
    public static class IdeRegression extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression");
            super.setUp();
        }

        @TestMetadata("AllOpenAnnotatedClasses.kt")
        public void testAllOpenAnnotatedClasses() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/AllOpenAnnotatedClasses.kt"));
        }

        @TestMetadata("ImplementingCharSequenceAndNumber.kt")
        public void testImplementingCharSequenceAndNumber() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/ImplementingCharSequenceAndNumber.kt"));
        }

        @TestMetadata("ImplementingMap.kt")
        public void testImplementingMap() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/ImplementingMap.kt"));
        }

        @TestMetadata("ImplementingMutableSet.kt")
        public void testImplementingMutableSet() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/ImplementingMutableSet.kt"));
        }

        @TestMetadata("InheritingInterfaceDefaultImpls.kt")
        public void testInheritingInterfaceDefaultImpls() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/InheritingInterfaceDefaultImpls.kt"));
        }

        @TestMetadata("OverridingFinalInternal.kt")
        public void testOverridingFinalInternal() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/OverridingFinalInternal.kt"));
        }

        @TestMetadata("OverridingInternal.kt")
        public void testOverridingInternal() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/OverridingInternal.kt"));
        }

        @TestMetadata("OverridingProtected.kt")
        public void testOverridingProtected() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/OverridingProtected.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations")
    public static class NullabilityAnnotations extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations");
            super.setUp();
        }

        @TestMetadata("Class.kt")
        public void testClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/Class.kt"));
        }

        @TestMetadata("ClassObjectField.kt")
        public void testClassObjectField() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/ClassObjectField.kt"));
        }

        @TestMetadata("ClassWithConstructor.kt")
        public void testClassWithConstructor() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/ClassWithConstructor.kt"));
        }

        @TestMetadata("ClassWithConstructorAndProperties.kt")
        public void testClassWithConstructorAndProperties() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/ClassWithConstructorAndProperties.kt"));
        }

        @TestMetadata("FileFacade.kt")
        public void testFileFacade() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/FileFacade.kt"));
        }

        @TestMetadata("Generic.kt")
        public void testGeneric() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/Generic.kt"));
        }

        @TestMetadata("IntOverridesAny.kt")
        public void testIntOverridesAny() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/IntOverridesAny.kt"));
        }

        @TestMetadata("JvmOverloads.kt")
        public void testJvmOverloads() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/JvmOverloads.kt"));
        }

        @TestMetadata("NullableUnitReturn.kt")
        public void testNullableUnitReturn() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/NullableUnitReturn.kt"));
        }

        @TestMetadata("OverrideAnyWithUnit.kt")
        public void testOverrideAnyWithUnit() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/OverrideAnyWithUnit.kt"));
        }

        @TestMetadata("PlatformTypes.kt")
        public void testPlatformTypes() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/PlatformTypes.kt"));
        }

        @TestMetadata("Primitives.kt")
        public void testPrimitives() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/Primitives.kt"));
        }

        @TestMetadata("PrivateInClass.kt")
        public void testPrivateInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/PrivateInClass.kt"));
        }

        @TestMetadata("Synthetic.kt")
        public void testSynthetic() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/Synthetic.kt"));
        }

        @TestMetadata("Trait.kt")
        public void testTrait() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/Trait.kt"));
        }

        @TestMetadata("UnitAsGenericArgument.kt")
        public void testUnitAsGenericArgument() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/UnitAsGenericArgument.kt"));
        }

        @TestMetadata("UnitParameter.kt")
        public void testUnitParameter() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/UnitParameter.kt"));
        }

        @TestMetadata("VoidReturn.kt")
        public void testVoidReturn() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/nullabilityAnnotations/VoidReturn.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName/script")
    public static class Script extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/script");
            super.setUp();
        }

        @TestMetadata("HelloWorld.kts")
        public void testHelloWorld() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/script/HelloWorld.kts"));
        }

        @TestMetadata("InnerClasses.kts")
        public void testInnerClasses() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/script/InnerClasses.kts"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/lightClassByFqName")
    public static class Uncategorized extends AbstractIdeLightClassesByFqNameTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName");
            super.setUp();
        }

        @TestMetadata("AnnotatedParameterInEnumConstructor.kt")
        public void testAnnotatedParameterInEnumConstructor() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotatedParameterInEnumConstructor.kt"));
        }

        @TestMetadata("AnnotatedParameterInInnerClassConstructor.kt")
        public void testAnnotatedParameterInInnerClassConstructor() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotatedParameterInInnerClassConstructor.kt"));
        }

        @TestMetadata("AnnotatedPropertyWithSites.kt")
        public void testAnnotatedPropertyWithSites() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotatedPropertyWithSites.kt"));
        }

        @TestMetadata("AnnotationClass.kt")
        public void testAnnotationClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotationClass.kt"));
        }

        @TestMetadata("AnnotationJavaRepeatable.kt")
        public void testAnnotationJavaRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotationJavaRepeatable.kt"));
        }

        @TestMetadata("AnnotationJvmRepeatable.kt")
        public void testAnnotationJvmRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotationJvmRepeatable.kt"));
        }

        @TestMetadata("AnnotationKotlinAndJavaRepeatable.kt")
        public void testAnnotationKotlinAndJavaRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotationKotlinAndJavaRepeatable.kt"));
        }

        @TestMetadata("AnnotationKotlinAndJvmRepeatable.kt")
        public void testAnnotationKotlinAndJvmRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotationKotlinAndJvmRepeatable.kt"));
        }

        @TestMetadata("AnnotationRepeatable.kt")
        public void testAnnotationRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/AnnotationRepeatable.kt"));
        }

        @TestMetadata("BackingFields.kt")
        public void testBackingFields() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/BackingFields.kt"));
        }

        @TestMetadata("CompanionObject.kt")
        public void testCompanionObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/CompanionObject.kt"));
        }

        @TestMetadata("Constructors.kt")
        public void testConstructors() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/Constructors.kt"));
        }

        @TestMetadata("DataClassWithCustomImplementedMembers.kt")
        public void testDataClassWithCustomImplementedMembers() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/DataClassWithCustomImplementedMembers.kt"));
        }

        @TestMetadata("DelegatedNested.kt")
        public void testDelegatedNested() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/DelegatedNested.kt"));
        }

        @TestMetadata("Delegation.kt")
        public void testDelegation() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/Delegation.kt"));
        }

        @TestMetadata("DeprecatedEnumEntry.kt")
        public void testDeprecatedEnumEntry() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/DeprecatedEnumEntry.kt"));
        }

        @TestMetadata("DeprecatedNotHiddenInClass.kt")
        public void testDeprecatedNotHiddenInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/DeprecatedNotHiddenInClass.kt"));
        }

        @TestMetadata("DollarsInName.kt")
        public void testDollarsInName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/DollarsInName.kt"));
        }

        @TestMetadata("DollarsInNameNoPackage.kt")
        public void testDollarsInNameNoPackage() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/DollarsInNameNoPackage.kt"));
        }

        @TestMetadata("EnumClass.kt")
        public void testEnumClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/EnumClass.kt"));
        }

        @TestMetadata("EnumClassWithEnumEntries.kt")
        public void testEnumClassWithEnumEntries() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/EnumClassWithEnumEntries.kt"));
        }

        @TestMetadata("EnumEntry.kt")
        public void testEnumEntry() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/EnumEntry.kt"));
        }

        @TestMetadata("ExtendingInterfaceWithDefaultImpls.kt")
        public void testExtendingInterfaceWithDefaultImpls() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/ExtendingInterfaceWithDefaultImpls.kt"));
        }

        @TestMetadata("HiddenDeprecated.kt")
        public void testHiddenDeprecated() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/HiddenDeprecated.kt"));
        }

        @TestMetadata("HiddenDeprecatedInClass.kt")
        public void testHiddenDeprecatedInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/HiddenDeprecatedInClass.kt"));
        }

        @TestMetadata("InheritingInterfaceDefaultImpls.kt")
        public void testInheritingInterfaceDefaultImpls() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/InheritingInterfaceDefaultImpls.kt"));
        }

        @TestMetadata("InlineReified.kt")
        public void testInlineReified() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/InlineReified.kt"));
        }

        @TestMetadata("JavaBetween.kt")
        public void testJavaBetween() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/JavaBetween.kt"));
        }

        @TestMetadata("JavaClassWithAnnotation.kt")
        public void testJavaClassWithAnnotation() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/JavaClassWithAnnotation.kt"));
        }

        @TestMetadata("JvmNameOnMember.kt")
        public void testJvmNameOnMember() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/JvmNameOnMember.kt"));
        }

        @TestMetadata("JvmStatic.kt")
        public void testJvmStatic() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/JvmStatic.kt"));
        }

        @TestMetadata("LocalFunctions.kt")
        public void testLocalFunctions() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/LocalFunctions.kt"));
        }

        @TestMetadata("NestedObjects.kt")
        public void testNestedObjects() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/NestedObjects.kt"));
        }

        @TestMetadata("NonDataClassWithComponentFunctions.kt")
        public void testNonDataClassWithComponentFunctions() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/NonDataClassWithComponentFunctions.kt"));
        }

        @TestMetadata("OnlySecondaryConstructors.kt")
        public void testOnlySecondaryConstructors() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/OnlySecondaryConstructors.kt"));
        }

        @TestMetadata("PrivateObject.kt")
        public void testPrivateObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/PrivateObject.kt"));
        }

        @TestMetadata("PublishedApi.kt")
        public void testPublishedApi() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/PublishedApi.kt"));
        }

        @TestMetadata("SimpleObject.kt")
        public void testSimpleObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/SimpleObject.kt"));
        }

        @TestMetadata("SimplePublicField.kt")
        public void testSimplePublicField() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/SimplePublicField.kt"));
        }

        @TestMetadata("SpecialAnnotationsOnAnnotationClass.kt")
        public void testSpecialAnnotationsOnAnnotationClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/SpecialAnnotationsOnAnnotationClass.kt"));
        }

        @TestMetadata("StubOrderForOverloads.kt")
        public void testStubOrderForOverloads() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/StubOrderForOverloads.kt"));
        }

        @TestMetadata("Throws.kt")
        public void testThrows() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/Throws.kt"));
        }

        @TestMetadata("TypePararametersInClass.kt")
        public void testTypePararametersInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/TypePararametersInClass.kt"));
        }

        @TestMetadata("VarArgs.kt")
        public void testVarArgs() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/lightClassByFqName/VarArgs.kt"));
        }
    }
}
