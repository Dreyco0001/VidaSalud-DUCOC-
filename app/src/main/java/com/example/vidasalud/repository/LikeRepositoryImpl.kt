package com.example.vidasalud.repository

import com.example.vidasalud.model.Like
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LikeRepositoryImpl(
    private val firestore: FirebaseFirestore
) : LikeRepository {

    private val likesCollection = firestore.collection("likes")

    override fun getLikesByPost(postId: String): Flow<List<Like>> = callbackFlow {
        val listener = likesCollection
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snap, _ ->
                val lista = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Like::class.java)?.copy(id = doc.id,)
                } ?: emptyList()

                trySend(lista)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addLike(like: Like) {
        likesCollection.add(like)
    }

    override suspend fun removeLike(likeId: String) {
        likesCollection.document(likeId).delete()
    }

    override fun userLikeOnPost(userId: String, postId: String): Flow<Like?> = callbackFlow {
        val listener = likesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snap, _ ->
                val like = snap?.documents?.firstOrNull()?.let { doc ->
                    doc.toObject(Like::class.java)?.copy(id = doc.id,)
                }

                trySend(like)
            }

        awaitClose { listener.remove() }
    }
}
