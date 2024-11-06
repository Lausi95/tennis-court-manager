package de.lausi.tcm.adapter.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import de.lausi.tcm.domain.model.iam.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

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
  fun getKeycloakUsers(@Header("Authorization") token: String): Call<List<KeycloakUser>>

  @POST("/realms/master/protocol/openid-connect/token")
  @Headers("Accept: application/json")
  @FormUrlEncoded
  fun getToken(
    @Field("grant_type") grantType: String,
    @Field("client_id") clientId: String,
    @Field("client_secret") clientSecret: String
  ): Call<TokenEndpointResponse>
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
  fun keycloakApi(properties: KeycloakProperties, objectMapper: ObjectMapper) =
    Retrofit.Builder()
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .baseUrl(properties.host)
      .build()
      .create(KeycloakApi::class.java)

  @Bean
  fun keycloakAdapter(keycloakApi: KeycloakApi, properties: KeycloakProperties): IamUserRepository =
    KeycloakIamUserRepository(properties.clientId, properties.clientSecret, keycloakApi)
}

private class KeycloakIamUserRepository(
  private val clientId: String,
  private val clientSecret: String,
  private val keycloakApi: KeycloakApi,
) : IamUserRepository {

  override fun findAll(): List<IamUser> {
    val accessToken = getAccessToken()
    return getKeycloakUsers(accessToken)
      .filter { it.emailVerified }
      .map {
        IamUser(
          IamUserId(it.username),
          IamUserUsername(it.username),
          IamUserFirstname(it.firstName),
          IamUserLastname(it.lastName)
        )
      }
  }

  private fun getKeycloakUsers(accessToken: String): List<KeycloakUser> {
    val bearerToken = "Bearer $accessToken"
    val response = keycloakApi.getKeycloakUsers(bearerToken).execute()
    if (!response.isSuccessful) {
      error(response.errorBody()?.string() ?: "Error getting keycloak users")
    }
    return response.body()!!
  }

  private fun getAccessToken(): String {
    return keycloakApi.getToken("client_credentials", clientId, clientSecret)
      .execute()
      .body()?.access_token ?: error("Could not obtain access token")
  }
}
