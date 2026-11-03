package com.mmatthews.process

import android.app.Application
import com.mmatthews.process.data.AppDatabase
import com.mmatthews.process.data.EntryStore

class ProcessApp : Application() {
    val store: EntryStore by lazy { EntryStore(AppDatabase.create(this).entryDao()) }
}
