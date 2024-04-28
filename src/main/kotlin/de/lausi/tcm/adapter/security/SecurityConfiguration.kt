package de.lausi.tcm.adapter.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

  @Bean
  fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
    return RegisterSessionAuthenticationStrategy(SessionRegistryImpl())
  }

  @Bean
  fun resouceServerFilterChain(http: HttpSecurity, logoutHandler: KeycloakLogoutHandler): SecurityFilterChain {
    // general auth settings
    http.authorizeHttpRequests { it.anyRequest().authenticated() }
    http.cors { it.disable() }
    http.csrf { it.disable() }

    // oauth2 resource server config
    http.oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }

    // OAuth2 client configuration
    http.oauth2Login(Customizer.withDefaults())
      .logout { it.addLogoutHandler(logoutHandler).logoutSuccessUrl("/") }

    return http.build()
  }
}

@Component
class KeycloakLogoutHandler : LogoutHandler {

  private val log = LoggerFactory.getLogger(KeycloakLogoutHandler::class.java)
  private val restTemplate = RestTemplate()

  override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
    val oidcUser = authentication?.principal as OidcUser
    logoutFromKeycloak(oidcUser)
  }

  private fun logoutFromKeycloak(user: OidcUser) {
    val endSessionEndpoint = "${user.issuer}/protocol/openid-connect/logout"
    val uriBuilder = UriComponentsBuilder.fromUriString(endSessionEndpoint).queryParam("id_token_hint", user.idToken.tokenValue)

    val response = restTemplate.getForEntity(uriBuilder.toUriString(), String::class.java)
    if (response.statusCode.is2xxSuccessful) {
      log.info("Logout successful")
    } else {
      log.error("Could not logout from Keycloak")
    }
  }
}
