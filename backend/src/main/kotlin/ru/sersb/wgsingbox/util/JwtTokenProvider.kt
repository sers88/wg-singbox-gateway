package ru.sersb.wgsingbox.util

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import ru.sersb.wgsingbox.model.entity.User
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${spring.security.jwt.secret}")
    private val secret: String,

    @Value("\${spring.security.jwt.expiration}")
    private val expiration: Long,

    @Value("\${spring.security.jwt.refresh-expiration}")
    private val refreshExpiration: Long
) {
    private val logger = KotlinLogging.logger {}

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val parser: JwtParser by lazy {
        Jwts.parser().verifyWith(key).build()
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("username", user.username)
            .claim("role", user.role.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshExpiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims = parser.parseSignedClaims(token).payload
            claims.subject?.toLongOrNull()
        } catch (e: Exception) {
            logger.error { "Failed to get user ID from token: ${e.message}" }
            null
        }
    }

    fun getUsernameFromToken(token: String): String? {
        return try {
            val claims = parser.parseSignedClaims(token).payload
            claims["username"] as? String
        } catch (e: Exception) {
            logger.error { "Failed to get username from token: ${e.message}" }
            null
        }
    }

    fun getRoleFromToken(token: String): String? {
        return try {
            val claims = parser.parseSignedClaims(token).payload
            claims["role"] as? String
        } catch (e: Exception) {
            logger.error { "Failed to get role from token: ${e.message}" }
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            parser.parseSignedClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            logger.warn { "JWT token is expired: ${e.message}" }
            false
        } catch (e: UnsupportedJwtException) {
            logger.warn { "JWT token is unsupported: ${e.message}" }
            false
        } catch (e: MalformedJwtException) {
            logger.warn { "JWT token is malformed: ${e.message}" }
            false
        } catch (e: IllegalArgumentException) {
            logger.warn { "JWT token claims string is empty: ${e.message}" }
            false
        }
    }
}
