import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.BeforeClass
import org.junit.Test

@ExperimentalCoroutinesApi
class EventTests {
    val chrome = Chrome()
    val channel = eventBus.openSubscription()

    @Test
    fun `test chrome startup event`() {
        suspend fun subscriber(): Boolean {
            var startupEventPassed = false
            channel.consumeEach {
                if (it::class == StartupEvent::class) {
                    startupEventPassed = true
                }
            }
            return startupEventPassed
        }
        assert(
            runBlocking {
            withContext(Dispatchers.IO) {
                return@withContext subscriber()
            }
        })
    }
}