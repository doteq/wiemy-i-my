package me.doteq.dolinabaryczy.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.doteq.dolinabaryczy.data.db.AnswersDatabase
import me.doteq.dolinabaryczy.data.repositories.DataStoreRepository
import me.doteq.dolinabaryczy.data.repositories.DataStoreRepositoryImpl
import me.doteq.dolinabaryczy.data.repositories.MainRepository
import me.doteq.dolinabaryczy.data.repositories.MainRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnswersDatabase(app: Application): AnswersDatabase = Room.databaseBuilder(
        app, AnswersDatabase::class.java, "answers_db"
    ).build()

    @Singleton
    @Provides
    fun provideMainRepository(db: AnswersDatabase): MainRepository =
        MainRepositoryImpl(db.answersDao)

    @Singleton
    @Provides
    fun provideDatastoreRepository(
        @ApplicationContext app: Context
    ): DataStoreRepository =
        DataStoreRepositoryImpl(app)

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = LocationServices.getFusedLocationProviderClient(app)

}