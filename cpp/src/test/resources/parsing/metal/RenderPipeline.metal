#include <metal_stdlib>
using namespace metal;

struct VertexIn {
    float3 position [[attribute(0)]];
    float2 uv [[attribute(1)]];
};

struct VertexOut {
    float4 position [[position]];
    float2 uv;
};

struct Uniforms {
    float4x4 modelViewProjection;
    float time;
};

vertex VertexOut vertexMain(VertexIn in [[stage_in]],
                            constant Uniforms& uniforms [[buffer(1)]]) {
    VertexOut out;
    out.position = uniforms.modelViewProjection * float4(in.position, 1.0);
    out.uv = in.uv;
    return out;
}

constexpr sampler linearSampler(coord::normalized, address::repeat, filter::linear);

fragment half4 fragmentMain(VertexOut in [[stage_in]],
                            texture2d<half> albedo [[texture(0)]],
                            constant Uniforms& uniforms [[buffer(1)]]) {
    half4 color = albedo.sample(linearSampler, in.uv);
    return color * half(0.5 + 0.5 * sin(uniforms.time));
}

kernel void reduce(device const float* input [[buffer(0)]],
                   device atomic_float* total [[buffer(1)]],
                   threadgroup float* scratch [[threadgroup(0)]],
                   uint tid [[thread_position_in_threadgroup]],
                   uint gid [[thread_position_in_grid]],
                   uint size [[threads_per_threadgroup]]) {
    scratch[tid] = input[gid];
    threadgroup_barrier(mem_flags::mem_threadgroup);
    for (uint stride = size / 2; stride > 0; stride /= 2) {
        if (tid < stride) {
            scratch[tid] += scratch[tid + stride];
        }
        threadgroup_barrier(mem_flags::mem_threadgroup);
    }
    if (tid == 0) {
        atomic_fetch_add_explicit(total, scratch[0], memory_order_relaxed);
    }
}

kernel void names(device int* out [[buffer(0)]]) {
    int mesh = 0;
    int object = 1;
    out[0] = mesh + object;
}

#if __METAL_VERSION__ >= 300
kernel void modern(device float4* p [[buffer(0)]]) {
    half h = 1;
    p[0] = float4(h);
}
#endif
