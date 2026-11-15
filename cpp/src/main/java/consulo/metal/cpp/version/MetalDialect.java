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

import consulo.cpp.lang.version.CDialectKeyword;

import java.util.Map;

/**
 * What every version of the Metal Shading Language adds to the C++ it is built on.
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public final class MetalDialect {
    /**
     * The address spaces and function qualifiers of MSL. Mesh, object, intersection and visible functions are marked
     * by attributes ({@code [[mesh]]}), not words, so {@code mesh} and {@code object} stay names.
     */
    public static final Map<String, CDialectKeyword> KEYWORDS = Map.ofEntries(
        Map.entry("device", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("constant", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("thread", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("threadgroup", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("threadgroup_imageblock", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("ray_data", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("object_data", CDialectKeyword.ADDRESS_SPACE),
        Map.entry("kernel", CDialectKeyword.FUNCTION_QUALIFIER),
        Map.entry("vertex", CDialectKeyword.FUNCTION_QUALIFIER),
        Map.entry("fragment", CDialectKeyword.FUNCTION_QUALIFIER)
    );

    private MetalDialect() {
    }

    /**
     * The macros of the Metal compiler: {@code __METAL_VERSION__} is the language version as {@code MajorMinorPatch}
     * (320 for 3.2). The compiler defines no macro for the target platform.
     */
    static void fillPredefinedMacros(Map<String, String> macros, String metalVersion) {
        macros.put("__METAL__", "1");
        macros.put("__METAL_VERSION__", metalVersion);
        macros.put("__APPLE__", "1");
    }
}
