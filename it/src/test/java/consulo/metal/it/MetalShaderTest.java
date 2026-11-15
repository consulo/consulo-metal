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
package consulo.metal.it;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.cpp.lang.CLanguage;
import consulo.cpp.lang.editor.highlight.CCallChecker;
import consulo.cpp.lang.version.CLanguageVersions;
import consulo.cpp.module.extension.CDefaultMutableModuleExtension;
import consulo.it.HeadlessApplicationExtension;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.metal.cpp.fileType.MetalFileType;
import consulo.metal.cpp.fileType.MetalHeaderFileType;
import consulo.metal.cpp.version.Metal4LanguageVersion;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.project.ProjectOpenContext;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A {@code .metal} file in a module with nothing configured but C/C++: it is Metal 4.0, {@code metal_types} comes
 * before its first line and {@code <metal_stdlib>} resolves into the headers bundled in the plugin's jar, so the
 * built-in types and the library resolve there.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
@ExtendWith(HeadlessApplicationExtension.class)
public class MetalShaderTest {
    private static final long TIMEOUT_SECONDS = 120;

    private static final String SHADER = """
        #include <metal_stdlib>
        using namespace metal;

        struct Uniforms {
            float4x4 transform;
            float time;
        };

        #if __METAL_VERSION__ >= 400
        struct OnlyInMetal4 {
            int value;
        };
        #endif

        kernel void reduce(device const float* input [[buffer(0)]],
                           device atomic_float* total [[buffer(1)]],
                           constant Uniforms& uniforms [[buffer(2)]],
                           threadgroup float* scratch [[threadgroup(0)]],
                           uint tid [[thread_position_in_threadgroup]]) {
            half scale = half(uniforms.time);
            float4 pixel = float4(input[tid]);
            scratch[tid] = pixel.x * scale;
            threadgroup_barrier(mem_flags::mem_threadgroup);
            OnlyInMetal4 marker;
            atomic_fetch_add_explicit(total, scratch[0], memory_order_relaxed);
        }

        fragment half4 shade(texture2d<half> albedo [[texture(0)]], sampler s [[sampler(0)]]) {
            return albedo.sample(s, float2(0.5, 0.5));
        }
        """;

    @Test
    public void shaderResolvesIntoBundledHeaders(Application application, ProjectManager projectManager) throws Exception {
        Path directory = Files.createTempDirectory("consulo-metal-it");
        Files.writeString(directory.resolve("shader.metal"), SHADER);
        Project project = openProject(application, projectManager, directory);
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory.resolve("shader.metal"));
        assertNotNull(file);
        assertEquals(MetalFileType.INSTANCE, file.getFileType(), "the file type of a .metal file");

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("Uniforms", "shader.metal");
        expected.put("float4x4", MetalHeaderFileType.TYPES);
        expected.put("atomic_float", MetalHeaderFileType.STDLIB);
        expected.put("half", MetalHeaderFileType.TYPES);
        expected.put("float4", MetalHeaderFileType.TYPES);
        expected.put("threadgroup_barrier", MetalHeaderFileType.STDLIB);
        expected.put("mem_flags", MetalHeaderFileType.STDLIB);
        expected.put("OnlyInMetal4", "shader.metal");
        expected.put("atomic_fetch_add_explicit", MetalHeaderFileType.STDLIB);
        expected.put("texture2d", MetalHeaderFileType.STDLIB);
        expected.put("sampler", MetalHeaderFileType.STDLIB);
        expected.put("float2", MetalHeaderFileType.TYPES);

        Map<String, @Nullable String> actual = new LinkedHashMap<>();
        for (String name : expected.keySet()) {
            actual.put(name, targetFileOf(project, file, name));
        }
        List<String> problems = problemsOf(project, file);
        System.out.println("Metal resolved: " + actual);
        System.out.println("Metal problems: " + problems);

        assertEquals(Metal4LanguageVersion.ID, versionOf(project, file), "the version of a .metal file");
        assertEquals(expected, actual, "where each name of the shader resolves");
        assertTrue(problems.isEmpty(), "the file parses and its calls match: " + problems);
    }

    /**
     * The bundled headers, as navigation opens them: Metal, and without errors.
     */
    @Test
    public void bundledHeadersParse(Application application, ProjectManager projectManager) throws Exception {
        Path directory = Files.createTempDirectory("consulo-metal-it-headers");
        Files.writeString(directory.resolve("shader.metal"), SHADER);
        Project project = openProject(application, projectManager, directory);
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory.resolve("shader.metal"));
        assertNotNull(file);

        for (String name : List.of("threadgroup_barrier", "float4")) {
            VirtualFile header = ReadAction.compute(() -> {
                PsiElement target = resolveFirstUse(project, file, name);
                return target == null ? null : target.getNavigationElement().getContainingFile().getVirtualFile();
            });
            assertNotNull(header, name + " resolves into a header");
            System.out.println("Metal header: " + header.getUrl());
            // zip:// inside the plugin's jar, file:// from the classes directory when tests run
            assertTrue(header.getUrl().endsWith("/consulo/metal/cpp/include/" + header.getName()), "a bundled header: " + header.getUrl());
            assertEquals(MetalHeaderFileType.INSTANCE, header.getFileType(), "the file type of " + header.getName());
            assertEquals(Metal4LanguageVersion.ID, versionOf(project, header), "the version of " + header.getName());
            List<String> problems = problemsOf(project, header);
            assertTrue(problems.isEmpty(), header.getName() + " parses: " + problems);
        }
    }

    private static @Nullable String versionOf(Project project, VirtualFile file) {
        return ReadAction.compute(() -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            PsiFile cRoot = psiFile == null ? null : psiFile.getViewProvider().getPsi(CLanguage.INSTANCE);
            return cRoot == null ? null : CLanguageVersions.of(cRoot).getId();
        });
    }

    /**
     * The file the first use of {@code name} resolves into; a description of the mismatch when navigation would not
     * land on the name.
     */
    private static @Nullable String targetFileOf(Project project, VirtualFile file, String name) {
        return ReadAction.compute(() -> {
            PsiElement target = resolveFirstUse(project, file, name);
            if (target == null) {
                return null;
            }
            PsiElement navigation = target.getNavigationElement();
            String targetText = navigation.getContainingFile().getText();
            int at = navigation.getTextOffset();
            if (!targetText.startsWith(name, at)) {
                return navigation.getContainingFile().getName() + " at offset " + at + ", not at " + name;
            }
            return navigation.getContainingFile().getName();
        });
    }

    private static @Nullable PsiElement resolveFirstUse(Project project, VirtualFile file, String name) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return null;
        }
        int offset = firstUse(psiFile.getText(), name);
        PsiReference reference = offset < 0 ? null : psiFile.findReferenceAt(offset);
        return reference == null ? null : reference.resolve();
    }

    /**
     * The first use of {@code name} as a whole word which is not where it is declared.
     */
    private static int firstUse(String text, String name) {
        int offset = -1;
        do {
            offset = text.indexOf(name, offset + 1);
        }
        while (offset >= 0 && (offset > 0 && Character.isJavaIdentifierPart(text.charAt(offset - 1))
            || offset + name.length() < text.length() && Character.isJavaIdentifierPart(text.charAt(offset + name.length()))
            || text.startsWith("struct " + name, offset - 7)
            || text.startsWith("typedef ", lineStart(text, offset)) && text.startsWith(name + ";", offset)));
        return offset;
    }

    private static int lineStart(String text, int offset) {
        return text.lastIndexOf('\n', offset) + 1;
    }

    private static List<String> problemsOf(Project project, VirtualFile file) {
        return ReadAction.compute(() -> {
            List<String> problems = new ArrayList<>();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            PsiFile cRoot = psiFile == null ? null : psiFile.getViewProvider().getPsi(CLanguage.INSTANCE);
            if (cRoot != null) {
                for (PsiErrorElement error : PsiTreeUtil.findChildrenOfType(cRoot, PsiErrorElement.class)) {
                    problems.add(error.getErrorDescription() + " at " + error.getTextRange());
                }
                collectCallErrors(cRoot, problems);
            }
            return problems;
        });
    }

    private static void collectCallErrors(PsiElement element, List<String> problems) {
        CCallChecker.check(element, (range, message) -> problems.add(message.get() + " at " + range));
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            collectCallErrors(child, problems);
        }
    }

    private static Project openProject(Application application, ProjectManager projectManager, Path directory) throws Exception {
        Project project = projectManager.openProjectAsync(directory, application.getLastUIAccess(), new ProjectOpenContext())
            .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(project);
        VirtualFile directoryFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        assertNotNull(directoryFile);
        WriteAction.run(() -> {
            ModifiableModuleModel moduleModel = ModuleManager.getInstance(project).getModifiableModel();
            Module module = moduleModel.newModule("shaders", directory.toString());
            moduleModel.commit();
            ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
            rootModel.addContentEntry(directoryFile);
            CDefaultMutableModuleExtension extension = rootModel.getExtensionWithoutCheck(CDefaultMutableModuleExtension.class);
            assertNotNull(extension);
            extension.setEnabled(true);
            rootModel.commit();
        });
        CountDownLatch smart = new CountDownLatch(1);
        DumbService.getInstance(project).runWhenSmart(smart::countDown);
        assertTrue(smart.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "project must reach smart mode");
        return project;
    }
}
