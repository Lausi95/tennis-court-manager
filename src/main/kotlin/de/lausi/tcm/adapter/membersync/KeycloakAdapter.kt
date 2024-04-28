package de.lausi.tcm.adapter.membersync

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

data class KeycloakUser(
  val username: String,
  val firstName: String,
  val lastName: String,
  val emailVerified: Boolean,
)

data class TokenEndpointResponse(val access_token: String)

interface KeycloakAdapter {

  fun getKeycloakUsers(): List<KeycloakUser>
}

private interface KeycloakApi {

  @GET("/admin/realms/borussia-friedrichsfelde/users?max=1000")
  fun getKeycloakUsers(
    @Header("Authorization") token: String): Call<List<KeycloakUser>>

  @POST("/realms/master/protocol/openid-connect/token")
  @Headers("Accept: application/json")
  @FormUrlEncoded
  fun getToken(
    @Field("grant_type") grantType: String,
    @Field("client_id") clientId: String,
    @Field("client_secret") clientSecret: String): Call<TokenEndpointResponse>
}

@ConfigurationProperties(prefix = "keycloak")
private data class KeycloakProperties(
  val host: String,
  val clientId: String,
  val clientSecret: String,
)

@Configuration
@EnableConfigurationProperties(KeycloakProperties::class)
private class KeycloakConfiguration {

  @Bean
  fun keycloakApi(properties: KeycloakProperties, objectMapper: ObjectMapper) = Retrofit.Builder()
    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
    .baseUrl(properties.host)
    .build()
    .create(KeycloakApi::class.java)

  @Bean
  fun keycloakAdapter(keycloakApi: KeycloakApi, properties: KeycloakProperties): KeycloakAdapter = KeycloakAdapterImpl(
    properties.clientId,
    properties.clientSecret,
    keycloakApi)
}

private class KeycloakAdapterImpl(
  private val clientId: String,
  private val clientSecret: String,
  private val keycloakApi: KeycloakApi,
): KeycloakAdapter {

  override fun getKeycloakUsers(): List<KeycloakUser> {
    val accessToken = keycloakApi
      .getToken("client_credentials", clientId, clientSecret)
      .execute()
      .body()?.access_token ?: error("Could not obtain access token")

    val keycloakUsersResponse = keycloakApi
      .getKeycloakUsers("Bearer $accessToken")
      .execute()

    if (!keycloakUsersResponse.isSuccessful) {
      error(keycloakUsersResponse.errorBody()?.string() ?: "Failed to fetch keycloak users")
    }

    return keycloakUsersResponse.body() ?: error("Response body is empty")
  }
}
