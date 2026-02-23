package ru.sersb.wgsingbox.model.exception

class ServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
