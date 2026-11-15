/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.metal.cpp;

import consulo.annotation.access.RequiredReadAction;
import consulo.cpp.lang.version.Cpp17LanguageVersion;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.language.psi.stub.IndexingDataKeys;
import consulo.language.version.LanguageVersion;
import consulo.metal.cpp.fileType.MetalFileType;
import consulo.metal.cpp.fileType.MetalHeaderFileType;
import consulo.metal.cpp.version.Metal3LanguageVersion;
import consulo.metal.cpp.version.Metal4LanguageVersion;
import consulo.test.junit.impl.language.SimpleParsingTest;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.fileType.FileType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Metal sources and the bundled headers parse without errors in the Metal versions: the address spaces, function
 * qualifiers and attributes of Metal are syntax, not errors. A test named {@code ...Metal3} parses with Metal 3.2,
 * {@code ...PlainCpp} with C++17 and must fail, the rest with Metal 4.0.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class MetalParsingTest extends SimpleParsingTest<Object> {
    private static final Map<String, String> BUNDLED = Map.of(
        "MetalStdlib", MetalHeaderFileType.STDLIB,
        "MetalTypes", MetalHeaderFileType.TYPES
    );

    public MetalParsingTest() {
        super("parsing/metal", "metal");
    }

    @Test
    public void testSimpleKernel(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRenderPipeline(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRenderPipelineMetal3(Context context) throws Exception {
        doTest(context, null);
    }

    /**
     * The control: plain C++17, without Metal's words, does not parse a kernel.
     */
    @Test
    public void testSimpleKernelPlainCpp(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMetalStdlib(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMetalTypes(Context context) throws Exception {
        doTest(context, null);
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return MetalFileType.INSTANCE;
    }

    /**
     * As the C/C++ plugin's own parsing tests: a light file is never indexed, so its (empty) options are attached up
     * front.
     */
    @RequiredReadAction
    @Override
    protected PsiFile createFile(Context context, @Nullable Object testContext, String fileName, FileType fileType, String text) {
        LanguageVersion languageVersion = resolveLanguageVersion(context, testContext, fileType);
        PsiFile file = Objects.requireNonNull(context.psiFileFactory().createFileFromText(fileName, languageVersion, text, true, false, false));
        for (PsiFile root : file.getViewProvider().getAllFiles()) {
            root.putUserData(IndexingDataKeys.INDEX_OPTIONS, Map.of());
        }
        return file;
    }

    @RequiredReadAction
    @Override
    protected LanguageVersion resolveLanguageVersion(Context context, @Nullable Object testContext, FileType fileType) {
        String name = testName(context);
        if (name.endsWith("PlainCpp")) {
            return new Cpp17LanguageVersion();
        }
        return name.endsWith("Metal3") ? new Metal3LanguageVersion() : new Metal4LanguageVersion();
    }

    /**
     * {@code RenderPipelineMetal3} reads {@code RenderPipeline.metal}, {@code SimpleKernelPlainCpp} {@code SimpleKernel.metal}; the header tests read the bundled headers.
     */
    @Override
    protected String loadText(Context context, Object testContext, String extension) throws Exception {
        String name = testName(context);
        String bundled = BUNDLED.get(name);
        String path = bundled != null
            ? "/consulo/metal/cpp/include/" + bundled
            : "/parsing/metal/" + name.replace("Metal3", "").replace("PlainCpp", "") + "." + extension;
        InputStream stream = getClass().getResourceAsStream(path);
        Assertions.assertNotNull(stream, path);
        return FileUtil.loadTextAndClose(stream, true);
    }

    @Override
    protected void checkResult(Context context, @Nullable Object testContext, PsiFile file) {
        List<String> errors = new ArrayList<>();
        file.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PsiErrorElement error) {
                    errors.add(file.getText().substring(0, error.getTextOffset()).split("\n", -1).length + ": "
                        + error.getErrorDescription());
                }
                super.visitElement(element);
            }
        });
        if (testName(context).endsWith("PlainCpp")) {
            Assertions.assertFalse(errors.isEmpty(), "plain C++ parsed Metal without errors");
        }
        else {
            Assertions.assertEquals(List.of(), errors);
        }
    }

    private static String testName(Context context) {
        return context.testInfo().getTestMethod().orElseThrow().getName().substring("test".length());
    }
}
