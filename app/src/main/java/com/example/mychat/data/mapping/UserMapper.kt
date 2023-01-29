package com.example.mychat.data.mapping

import com.example.mychat.data.models.UserFirestore
import com.example.mychat.domain.models.User

class UserMapper(
    private val imageMapper: ImageMapper
): Mapper<UserFirestore, User> {
    override fun transform(data: UserFirestore): User {
        return User(
            id = data.id!!,
            image = imageMapper.transform(data.image!!),
            name = data.name!!,
            email = data.email!!,
            password = ""
        )
    }

}