package ru.sersb.wgsingbox.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.model.entity.User
import ru.sersb.wgsingbox.repository.UserRepository
import ru.sersb.wgsingbox.util.UserPrincipal

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger = KotlinLogging.logger {}

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        if (!user.enabled) {
            throw UsernameNotFoundException("User is disabled: $username")
        }

        return UserPrincipal(user)
    }

    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User not found with id: $id") }

        return UserPrincipal(user)
    }
}
