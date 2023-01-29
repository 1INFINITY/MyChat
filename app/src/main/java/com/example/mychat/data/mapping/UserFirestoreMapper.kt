package com.example.mychat.data.mapping

import com.example.mychat.data.models.UserFirestore
import com.example.mychat.domain.models.User

class UserFirestoreMapper(
    private val encodedImageMapper: EncodedImageMapper
): Mapper<User, UserFirestore> {
    override fun transform(data: User): UserFirestore {
        return UserFirestore(
            data.id,
            data.email,
            encodedImageMapper.transform(data.image),
            data.name
        )
    }
}