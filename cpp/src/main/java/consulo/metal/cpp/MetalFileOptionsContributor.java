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

import consulo.annotation.component.ExtensionImpl;
import consulo.cpp.lang.version.Cpp17LanguageVersion;
import consulo.cpp.moduleAware.CFileOptions;
import consulo.cpp.moduleAware.CFileOptionsContributor;
import consulo.metal.cpp.fileType.MetalFileType;
import consulo.metal.cpp.fileType.MetalHeaderFileType;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * The options of {@code .metal} files: the headers this plugin bundles, {@code <metal_stdlib>} among them, are searched
 * after the module's, {@code metal_types} comes before the first line - the Metal compiler knows {@code float4} and
 * {@code half} without any include - and a module which configures no C++ standard gets Metal 4.
 * <p>
 * Apple's own headers are not used: they are part of Xcode, and only on macOS. The bundled ones are written from the
 * Metal Shading Language Specification and declare what completion and resolve need, not how Apple implements it.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
@ExtensionImpl
public class MetalFileOptionsContributor implements CFileOptionsContributor {
    private static final String INCLUDE_DIRECTORY = "/consulo/metal/cpp/include";

    @Override
    public @Nullable CFileOptions getOptions(Module module, VirtualFile file) {
        if (file.getFileType() != MetalFileType.INSTANCE) {
            return null;
        }
        String includeUrl = bundledIncludeUrl();
        if (includeUrl == null) {
            return new CFileOptions(Map.of(), List.of(), List.of(), Cpp17LanguageVersion.ID);
        }
        return new CFileOptions(Map.of(), List.of(includeUrl), List.of(includeUrl + "/" + MetalHeaderFileType.TYPES), Cpp17LanguageVersion.ID);
    }

    /**
     * The URL of the bundled headers in the file system of the plugin's jar - {@code zip://.../consulo.metal-cpp.jar!/consulo/metal/cpp/include}:
     * the platform has no {@code jar} file system, archives are {@code zip}.
     */
    private static @Nullable String bundledIncludeUrl() {
        // a file, not the directory: a named module finds no directory entry
        URL url = MetalFileOptionsContributor.class.getResource(INCLUDE_DIRECTORY + "/" + MetalHeaderFileType.STDLIB);
        if (url == null) {
            return null;
        }
        String fileUrl = VirtualFileUtil.convertFromUrl(url, VirtualFileManager.getInstance());
        return fileUrl.substring(0, fileUrl.length() - MetalHeaderFileType.STDLIB.length() - 1);
    }
}
