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
package consulo.metal.cpp.fileType;

import consulo.cpp.api.icon.CppApiIconGroup;
import consulo.cpp.lang.CLanguage;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.metal.cpp.localize.MetalCppLocalize;
import consulo.ui.image.Image;

import java.util.List;

/**
 * The headers of Metal without an extension, {@code metal_stdlib} and {@code metal_types} this plugin bundles: claimed
 * by their exact names, so the C/C++ plugin parses them as Metal, as it parses no extensionless file on its own.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class MetalHeaderFileType extends LanguageFileType {
    public static final MetalHeaderFileType INSTANCE = new MetalHeaderFileType();

    public static final String STDLIB = "metal_stdlib";
    public static final String TYPES = "metal_types";
    public static final List<String> FILE_NAMES = List.of(STDLIB, TYPES);

    private MetalHeaderFileType() {
        super(CLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "MetalHeader";
    }

    @Override
    public LocalizeValue getDescription() {
        return MetalCppLocalize.metalHeaderFileTypeDescription();
    }

    @Override
    public String getDefaultExtension() {
        return "";
    }

    @Override
    public Image getIcon() {
        return CppApiIconGroup.filetypeHpp();
    }
}
