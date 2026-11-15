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

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileNameMatcher;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 2026-11-14
 */
@ExtensionImpl
public class MetalFileTypeFactory extends FileTypeFactory {
    private final FileNameMatcherFactory myFileNameMatcherFactory;

    @Inject
    public MetalFileTypeFactory(FileNameMatcherFactory fileNameMatcherFactory) {
        myFileNameMatcherFactory = fileNameMatcherFactory;
    }

    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(MetalFileType.INSTANCE);
        consumer.consume(MetalHeaderFileType.INSTANCE, MetalHeaderFileType.FILE_NAMES.stream()
            .map(myFileNameMatcherFactory::createExactFileNameMatcher)
            .toArray(FileNameMatcher[]::new));
    }

    /**
     * Whether a file of {@code fileType} is Metal - a source or a bundled header.
     */
    public static boolean isMetal(FileType fileType) {
        return fileType == MetalFileType.INSTANCE || fileType == MetalHeaderFileType.INSTANCE;
    }
}
