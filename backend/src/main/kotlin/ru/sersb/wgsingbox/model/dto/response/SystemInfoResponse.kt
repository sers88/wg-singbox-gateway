package ru.sersb.wgsingbox.model.dto.response

data class SystemInfoResponse(
    val hostname: String,
    val os: String,
    val osVersion: String,
    val kernelVersion: String,
    val arch: String,
    val uptime: Long,
    val cpuCores: Int,
    val totalMemory: Long,
    val totalDisk: Long
)
