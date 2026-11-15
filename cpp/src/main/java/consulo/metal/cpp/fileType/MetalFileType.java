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

import consulo.cpp.lang.CLanguage;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.metal.cpp.icon.MetalCppIconGroup;
import consulo.metal.cpp.localize.MetalCppLocalize;
import consulo.ui.image.Image;

/**
 * {@code .metal}: a source of the Metal Shading Language. A C++ file to the C/C++ plugin, which parses it with the
 * Metal version of the standard its module configures.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class MetalFileType extends LanguageFileType {
    public static final MetalFileType INSTANCE = new MetalFileType();

    private MetalFileType() {
        super(CLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "Metal";
    }

    @Override
    public LocalizeValue getDescription() {
        return MetalCppLocalize.metalFileTypeDescription();
    }

    @Override
    public String getDefaultExtension() {
        return "metal";
    }

    @Override
    public Image getIcon() {
        return MetalCppIconGroup.filetypeMetal();
    }
}
