package es.mirumi.es.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import es.mirumi.es.BuildConfig
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.repository.APIs.CasaAPI
import es.mirumi.es.data.repository.APIs.CatalogoAPI
import es.mirumi.es.data.repository.APIs.DatabaseAPI
import es.mirumi.es.data.repository.APIs.EventoAPI
import es.mirumi.es.data.repository.APIs.InvitacionAPI
import es.mirumi.es.data.repository.APIs.ItemAPI
import es.mirumi.es.data.repository.APIs.ListaAPI
import es.mirumi.es.data.repository.APIs.LoginAPI
import es.mirumi.es.data.repository.APIs.RegistroAPI
import es.mirumi.es.data.repository.APIs.TareaAPI
import es.mirumi.es.data.repository.APIs.UsuarioAPI
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NetworkModule {
    lateinit var sessionManager: SessionManager
        private set

    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    private val gson: Gson =
        GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()

    private val client: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .addInterceptor(AuthInterceptor { sessionManager.fetchAuthToken() })
            .build()
    }

    val retrofit: Retrofit by lazy {
        Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val databaseApiService: DatabaseAPI by lazy {
        retrofit.create(DatabaseAPI::class.java)
    }

    val loginApiService: LoginAPI by lazy {
        retrofit.create(LoginAPI::class.java)
    }

    val registroApiService: RegistroAPI by lazy {
        retrofit.create(RegistroAPI::class.java)
    }

    val casaApiService: CasaAPI by lazy {
        retrofit.create(CasaAPI::class.java)
    }

    val invitacionApiService: InvitacionAPI by lazy {
        retrofit.create(InvitacionAPI::class.java)
    }

    val listaApiService: ListaAPI by lazy {
        retrofit.create(ListaAPI::class.java)
    }

    val itemApiService: ItemAPI by lazy {
        retrofit.create(ItemAPI::class.java)
    }

    val catalogoApiService: CatalogoAPI by lazy {
        retrofit.create(CatalogoAPI::class.java)
    }

    val tareaApiService: TareaAPI by lazy {
        retrofit.create(TareaAPI::class.java)
    }

    val usuarioApiService: UsuarioAPI by lazy {
        retrofit.create(UsuarioAPI::class.java)
    }

    val eventoAPIService: EventoAPI by lazy {
        retrofit.create(EventoAPI::class.java)
    }

    private class AuthInterceptor(
        private val tokenProvider: () -> String?,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val token = tokenProvider()
            if (token != null && originalRequest.header("Authorization") == null) {
                return chain.proceed(
                    originalRequest
                        .newBuilder()
                        .header("Authorization", token)
                        .build(),
                )
            }
            return chain.proceed(originalRequest)
        }
    }

    class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        @Throws(IOException::class)
        override fun write(
            out: JsonWriter,
            value: LocalDateTime?,
        ) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(formatter.format(value))
            }
        }

        @Throws(IOException::class)
        override fun read(reader: JsonReader): LocalDateTime? =
            try {
                val dateString = reader.nextString()
                LocalDateTime.parse(dateString, formatter)
            } catch (e: Exception) {
                null
            }
    }
}
