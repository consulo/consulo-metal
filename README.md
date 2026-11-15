# Metal support for Consulo

The Metal Shading Language in Consulo, built on the [C/C++ plugin](https://github.com/consulo/incubating-consulo-cpp):
`.metal` files are C++ files there, parsed with a Metal version of the C++ standard their module configures.

## Features

- **Language versions:** Metal 3.2 on C++14 (a module on C++14 or older) and Metal 4.0 on C++17 (C++17 or newer, and
  modules which configure no standard). Each defines `__METAL__`, `__METAL_VERSION__` (`320`, `400`) and `__APPLE__`,
  as the Metal compiler of Xcode 16 does for 3.2.
- **Keywords:** the address spaces `device`, `constant`, `thread`, `threadgroup`, `threadgroup_imageblock`,
  `ray_data`, `object_data` and the function qualifiers `kernel`, `vertex`, `fragment`. Attributes (`[[buffer(0)]]`,
  `[[thread_position_in_grid]]`, `[[mesh]]`) are C++ attributes.
- **Built-in types and standard library:** headers bundled with the plugin, written from the
  [Metal Shading Language Specification](https://developer.apple.com/metal/Metal-Shading-Language-Specification.pdf).
  `metal_types` (`half`, `float4`, `float4x4`, ...) is included before the first line of every `.metal` file, since
  the Metal compiler knows these types without any header; `#include <metal_stdlib>` resolves into the bundled
  library. Apple's headers ship only with Xcode on macOS and are not used.

Highlighting, go to declaration, find usages and completion come from the C/C++ plugin.

Not yet: swizzles (`v.xyz`) beyond single components, compiling with `xcrun metal`, `half` as a type of its own.

## Modules

| Module | Artifact | Contents |
|---|---|---|
| `cpp` | `consulo.metal-cpp` | `.metal` file type, the Metal language versions, the options of Metal files and the bundled headers |
| `plugin` | `consulo.metal` | The plugin |

## License

Apache License 2.0, see [LICENSE](LICENSE) and [NOTICE](NOTICE).
