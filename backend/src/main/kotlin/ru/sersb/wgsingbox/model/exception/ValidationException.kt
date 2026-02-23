package ru.sersb.wgsingbox.model.exception

class ValidationException(message: String, val errors: List<String>? = null) : RuntimeException(message)
