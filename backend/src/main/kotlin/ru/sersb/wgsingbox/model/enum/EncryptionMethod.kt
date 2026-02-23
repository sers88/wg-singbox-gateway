package ru.sersb.wgsingbox.model.enum

enum class EncryptionMethod(val value: String) {
    AES_128_GCM("aes-128-gcm"),
    AES_256_GCM("aes-256-gcm"),
    CHACHA20_POLY1305("chacha20-poly1305"),
    XCHACHA20_POLY1305("xchacha20-poly1305")
}
