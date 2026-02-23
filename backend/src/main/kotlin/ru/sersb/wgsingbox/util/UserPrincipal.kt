package ru.sersb.wgsingbox.util

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.sersb.wgsingbox.model.entity.User

class UserPrincipal(
    val id: Long?,
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    constructor(user: User) : this(
        id = user.id,
        username = user.username,
        password = user.password,
        authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    )

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
