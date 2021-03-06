/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.fir.frontend.api.scopes;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link GenerateNewCompilerTests.kt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/idea-frontend-fir/testData/memberScopeByFqName")
@TestDataPath("$PROJECT_ROOT")
public class MemberScopeByFqNameTestGenerated extends AbstractMemberScopeByFqNameTest {
    @Test
    public void testAllFilesPresentInMemberScopeByFqName() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/idea-frontend-fir/testData/memberScopeByFqName"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("Int.kt")
    public void testInt() throws Exception {
        runTest("idea/idea-frontend-fir/testData/memberScopeByFqName/Int.kt");
    }

    @Test
    @TestMetadata("java.lang.String.kt")
    public void testJava_lang_String() throws Exception {
        runTest("idea/idea-frontend-fir/testData/memberScopeByFqName/java.lang.String.kt");
    }

    @Test
    @TestMetadata("kotlin.Function2.kt")
    public void testKotlin_Function2() throws Exception {
        runTest("idea/idea-frontend-fir/testData/memberScopeByFqName/kotlin.Function2.kt");
    }

    @Test
    @TestMetadata("MutableList.kt")
    public void testMutableList() throws Exception {
        runTest("idea/idea-frontend-fir/testData/memberScopeByFqName/MutableList.kt");
    }
}
