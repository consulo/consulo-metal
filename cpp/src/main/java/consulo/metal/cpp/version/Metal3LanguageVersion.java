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
package consulo.metal.cpp.version;

import consulo.annotation.component.ExtensionImpl;
import consulo.cpp.lang.version.CDialectKeyword;
import consulo.cpp.lang.version.CLanguageVersion;
import consulo.cpp.lang.version.CLanguageVersions;
import consulo.cpp.lang.version.Cpp14LanguageVersion;
import consulo.metal.cpp.fileType.MetalFileTypeFactory;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Metal Shading Language 3.2, built on C++14: its address spaces, function qualifiers and the macros of the Metal
 * compiler. The version of {@code .metal} files in a module on C++14 or older.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
@ExtensionImpl
public class Metal3LanguageVersion extends Cpp14LanguageVersion {
    public static final String ID = "METAL32";

    @Inject
    public Metal3LanguageVersion() {
        super(ID, "Metal 3.2");
    }

    @Override
    public @Nullable CLanguageVersion getBaseVersion() {
        return CLanguageVersions.findById(Cpp14LanguageVersion.ID);
    }

    @Override
    public boolean isDialectFileType(FileType fileType) {
        return MetalFileTypeFactory.isMetal(fileType);
    }

    @Override
    public Map<String, CDialectKeyword> getDialectKeywords() {
        return MetalDialect.KEYWORDS;
    }

    @Override
    protected void fillPredefinedMacros(Map<String, String> macros) {
        super.fillPredefinedMacros(macros);
        MetalDialect.fillPredefinedMacros(macros, "320");
    }
}
