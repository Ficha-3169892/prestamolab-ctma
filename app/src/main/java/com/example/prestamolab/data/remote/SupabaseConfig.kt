package com.example.prestamolab.data.remote

import com.example.prestamolab.BuildConfig

object SupabaseConfig {
    val BASE_URL: String = BuildConfig.SUPABASE_URL
        .ifEmpty { "https://ngdgpnjycexjoxkoqejw.supabase.co/" }
        .let { if (it.endsWith("/")) it else "$it/" }

    val REST_URL: String = "${BASE_URL}rest/v1/"
    val STORAGE_URL: String = "${BASE_URL}storage/v1/object/public/evidencias/"

    val ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
        .ifEmpty { "sb_publishable_Im1w2TgyAPCyrXfzHPkmNQ_iiX2yyPk" }
}
