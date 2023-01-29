package com.example.mychat.data.mapping

import com.example.mychat.data.models.UserFirestore
import com.example.mychat.data.models.UserRegisteredFirestore
import com.example.mychat.domain.models.User

class UserRegisteredMapper(
    private val encodedImageMapper: EncodedImageMapper
): Mapper<User, UserRegisteredFirestore> {
    override fun transform(data: User): UserRegisteredFirestore {
        return UserRegisteredFirestore(
            data.id,
            data.email,
            encodedImageMapper.transform(data.image),
            data.name,
            data.password
        )
    }
}