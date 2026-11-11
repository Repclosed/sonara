package echo.music.iad1tya.listentogether

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class ListenTogetherServer(
  val name: String,
  val url: String,
  val location: String,
  val operator: String,
)

/**
 * Sonara no longer polls upstream Echo Music announcement feeds. The curated
 * list below is static and can be extended by the user at runtime.
 */
object ListenTogetherServers {
  private val _servers =
    MutableStateFlow(
      listOf(
        ListenTogetherServer(
          name = "Metrolist Server",
          url = "wss://metroserverx.meowery.eu/ws",
          location = "Poland",
          operator = "Metrolist",
        )
      )
    )

  val serversFlow: StateFlow<List<ListenTogetherServer>> = _servers

  val servers: List<ListenTogetherServer>
    get() = _servers.value

  val defaultServerUrl: String
    get() = servers.first().url

  fun findByUrl(url: String): ListenTogetherServer? = servers.firstOrNull { it.url == url }
}
