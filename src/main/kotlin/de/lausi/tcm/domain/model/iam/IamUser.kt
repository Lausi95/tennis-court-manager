package de.lausi.tcm.domain.model.iam

data class IamUserId(val value: String)
data class IamUserUsername(val value: String)
data class IamUserFirstname(val value: String)
data class IamUserLastname(val value: String)

data class IamUser(
  val id: IamUserId,
  val username: IamUserUsername,
  val firstname: IamUserFirstname,
  val lastname: IamUserLastname,
)

interface IamUserRepository {

  fun findAll(): List<IamUser>
}
