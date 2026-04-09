package es.mirumi.es.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import es.mirumi.es.BuildConfig // ¡Importante! Importa tu BuildConfig
import es.mirumi.es.data.repository.APIs.CasaAPI
import es.mirumi.es.data.repository.APIs.DatabaseAPI
import es.mirumi.es.data.repository.APIs.EventoAPI
import es.mirumi.es.data.repository.APIs.InvitacionAPI
import es.mirumi.es.data.repository.APIs.ItemAPI
import es.mirumi.es.data.repository.APIs.ListaAPI
import es.mirumi.es.data.repository.APIs.LoginAPI
import es.mirumi.es.data.repository.APIs.TareaAPI
import es.mirumi.es.data.repository.APIs.UsuarioAPI
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Objeto Singleton que gestiona la configuración de Retrofit
 * y proporciona las instancias de los servicios API.
 */
object NetworkModule {
    private val gson: Gson =
        GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()

    private val brokerUrl = "tcp://10.0.2.2:1883"

    val retrofit: Retrofit by lazy {
        Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provee la instancia del servicio DatabaseAPI.
     * Esta instancia es la que realiza las llamadas HTTP.
     */
    val databaseApiService: DatabaseAPI by lazy {
        retrofit.create(DatabaseAPI::class.java)
    }

    val loginApiService: LoginAPI by lazy {
        retrofit.create(LoginAPI::class.java)
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

    val tareaApiService: TareaAPI by lazy {
        retrofit.create(TareaAPI::class.java)
    }

    val usuarioApiService: UsuarioAPI by lazy {
        retrofit.create(UsuarioAPI::class.java)
    }

    val eventoAPIService: EventoAPI by lazy {
        retrofit.create(EventoAPI::class.java)
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
