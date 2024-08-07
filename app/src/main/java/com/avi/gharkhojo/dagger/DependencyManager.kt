package com.avi.gharkhojo.dagger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.avi.gharkhojo.Model.LoginViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DependencyManager {
    private val CHANNEL_ID = "message_notification_id"
    private val CHANNEL_NAME = "message_notification"
    @Provides
    @Singleton
   fun provideNotificationManager(@ActivityContext context: Context):NotificationManagerCompat {
       var notificationManager =  NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =  NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return notificationManager
    }


    @Provides
    @Singleton
    fun provideNotificationBuilder(@ActivityContext context: Context):NotificationCompat.Builder = NotificationCompat
        .Builder(context,CHANNEL_ID)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setChannelId(CHANNEL_ID)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth():FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseUser(firebaseAuth: FirebaseAuth):FirebaseUser? = firebaseAuth.currentUser


    @Provides
    @Singleton
    fun provideDatabaseReference(firebaseDatabase: FirebaseDatabase):DatabaseReference = firebaseDatabase.reference.child("users")

    @Provides
    @Singleton
    fun provideFirebaseDatabase():FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideGlide(@ApplicationContext context: Context) = Glide.with(context)


    private suspend fun reloadCurrentUser(firebaseAuth: FirebaseAuth): FirebaseUser? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return try {
            currentUser.reload().await()
            firebaseAuth.currentUser
        } catch (e: Exception) {

            null
        }
    }
}