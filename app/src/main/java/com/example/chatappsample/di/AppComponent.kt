package com.example.chatappsample.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DatabaseModule::class,
        DispatchModule::class,
        RepositoryModule::class,
        WorkerModule::class
    ]
)

interface AppComponent {
}