package ru.netology.nmedia.dto

import android.net.Uri
import java.io.File

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment?,
    val show: Boolean = true,
    val ownedByMe: Boolean = false,
    val authorId: Long,
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem

data class PhotoModel(val uri: Uri? = null, val file: File? = null)

data class Media(val id: String)
data class Attachment(
    val url: String,
    val description: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE
}