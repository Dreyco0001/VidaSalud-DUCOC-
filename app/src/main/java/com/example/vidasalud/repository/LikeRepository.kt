package com.example.vidasalud.repository


import com.example.vidasalud.model.Like
import kotlinx.coroutines.flow.Flow

interface LikeRepository {
    fun getLikesByPost(postId: String): Flow<List<Like>>
    suspend fun addLike(like: Like)
    suspend fun removeLike(likeId: String)
    fun userLikeOnPost(userId: String, postId: String): Flow<Like?>
}