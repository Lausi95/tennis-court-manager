package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.member.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component

@Document
private data class MongoMember(
  @Id val id: String,
  val firstname: String,
  val lastname: String,
  val groups: List<String>,
) {

  constructor(member: Member) : this(
    member.id.value,
    member.firstname.value,
    member.lastname.value,
    member.groups.map { it.name }
  )

  fun toMember(): Member {
    return Member(
      MemberId(this.id),
      MemberFirstname(this.firstname),
      MemberLastname(this.lastname),
      this.groups.map { MemberGroup.valueOf(it) }.toMutableSet()
    )
  }
}

private interface MongoMemberRepository : MongoRepository<MongoMember, String>

@Component
private class MemberRepositoryImpl(val mongoRepository: MongoMemberRepository) : MemberRepository {

  override fun exists(memberId: MemberId): Boolean {
    return mongoRepository.existsById(memberId.value)
  }

  override fun findById(memberId: MemberId): Member? {
    return mongoRepository.findById(memberId.value).orElse(null)?.toMember()
  }

  override fun findById(memberIds: List<MemberId>): List<Member> {
    val memberIdValues = memberIds.map { it.value }
    return mongoRepository.findAllById(memberIdValues).map { it.toMember() }
  }

  override fun findAll(): List<Member> {
    return mongoRepository.findAll().map { it.toMember() }
  }

  override fun save(member: Member) {
    mongoRepository.save(MongoMember(member))
  }

  override fun delete(member: Member) {
    mongoRepository.deleteById(member.id.value)
  }
}
