import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.SystemUtils
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.w3c.dom.CharacterData
import java.awt.datatransfer.Clipboard
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue
import kotlin.reflect.KProperty
import kotlin.time.*

class HeadlessChrome {
    operator fun getValue(owner: Any, prop: KProperty<*>): Chrome {
        return Chrome(ChromeOptions().setHeadless(true))
    }
}

class Chrome constructor(
    override val coroutineContext: CoroutineContext = Executors.newFixedThreadPool(1)
        .asCoroutineDispatcher(),
    opts: ChromeOptions? = null,
) : Driver {
    constructor(chromeOptions: ChromeOptions?) : this(opts = chromeOptions)

    private val driveFile: File = when {
        SystemUtils.IS_OS_WINDOWS -> {
            File(javaClass.getResource("chromedriver.exe").file)
        }
        SystemUtils.IS_OS_LINUX -> {
            File(javaClass.getResource("chromedriver").file)
        }
        else -> {
            File(javaClass.getResource("chromedrivermac").file)
        }

    }

    override val driver: ChromeDriver

    private fun initDriver(options: ChromeOptions = ChromeOptions()): ChromeDriver {
        System.setProperty("webdriver.chrome.driver", driveFile.absolutePath)
        return ChromeDriver(options)
    }


    init {
        if (opts != null) {
            this.driver = initDriver(opts)
        } else {
            this.driver = initDriver()
        }
        addShutdownHook()
    }

    private fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                runBlocking {
                    println("Shutting down driver. . .")
                    driver.quit()
                }
            }
        })
    }

    fun newTab(url: String = "about:blank") {
        driver.executeScript("window.open('${url}','_blank');")
    }


    /**
     * Send's keyboard actions to an input element with the type=text that is found on the page either by it's text or by the text of it's
     * label.
     * @param input - the string that you want to input to the element
     * @param elementText - the text of the input field or of the label for it
     * @return true if the input element was found and typed into, false otherwise
     */
    @ExperimentalTime
    fun typeTo(input: String, elementText: String): Boolean {
        return runBlocking {
            awaitAll(
                async {
                    driver.findElementsByTagName("label").filter { it.text == elementText }
                },
                async {
                    driver.findElementsByTagName("input").filter { it.text == elementText }
                })
                .flatten()
                .let {
                    if (it.isNullOrEmpty()) return@runBlocking false
                    it
                }
                .first()
                .run {
                    click()
//                    if (tagName == "label") {
//                        val labelFor = getAttribute("for")
//                        if (labelFor == null) {
//                            if (!driver.findElementsByName(elementText).isNullOrEmpty()) {
//                                driver.findElementsByName(elementText).first().sendKeys(input)
//                                return@runBlocking true
//                            }
//                            val byName = driver.findElementsByTagName("input")
//                                .filter { it.size.width > 0 || it.size.height > 0 }
//                                .sortedWith(Comparator<WebElement> { o1, o2 -> o1.location.distance(o2) })
//
//                            if (byName.first().text == "") {
//                                byName.first().sendKeys(input)
//                                return@runBlocking true
//                            }
//                            else {
//                                byName[1].sendKeys(input)
//                                return@runBlocking true
//                            }
//                        }
//                        driver.findElementByName(labelFor).sendKeys(input)
//                        return@runBlocking true
//                    } else
                        sendKeys(input)
                    return@runBlocking true
                }
        }
    }

    override fun get(text: String): WebElement? {
        return driver.findElementByXPath("//*[contains(text(),'${text}')]")
    }

    /**
     * @param text: the
     */
    override fun click(text: String): Boolean {
        return runBlocking {
            with(driver.findElementsByPartialLinkText(text).firstOrNull { it.isEnabled }) {
                if (this != null) {
                    this.click()
                    delay(1.seconds)
                    return@runBlocking true
                }
                if (get(text) != null) {
                    get(text)?.click()
                    delay(1.seconds)
                    return@runBlocking true
                }
                return@runBlocking false
            }
        }
    }
}

fun cortTest() {
    val chrome = Chrome()
    var email = ""
    chrome.run {
        go("https://twitter.com")
        click("Sign up")
        click("Use email instead")
        newTab("https://10minutemail.net/")
        switchToTab(2)
        click("Copy to clipboard")
        switchToTab(1)


        typeTo("Bobby dole", "Name")
        typeTo(Keys.chord(Keys.LEFT_CONTROL, "v"), "Email")
        select("March")
        select(RandomUtils.nextInt(1, 28).toString())
        select(RandomUtils.nextInt(1900, 2000).toString())
        click("Next")
        click("Next")
        click("Sign up")
        switchToTab(2)
        wait(2.minutes)
        val verificationCode = linkText("verification code").replace(" is your Twitter verification code", "")
        switchToTab(1)
        println(verificationCode)
        typeTo(verificationCode, "Verification code")
        click("Next")
        wait(5.minutes)



    }

}

@ExperimentalTime
fun main() {
    cortTest()
}

fun WebElement.below(element: WebElement): Boolean = this.location.y < element.location.y

fun Point.distance(element: WebElement): Int = (this.getX() - element.location.x).absoluteValue +
        (this.getY() - element.location.y).absoluteValue

@ExperimentalTime
fun WebElement.getForm(chrome: Chrome): WebElement {
    val script = "document.getElementsByName('${this}')[0]"
    return chrome.driver.executeScript(script) as WebElement
}

fun WebElement.isLabel(): Boolean = this.tagName.contains("label")

@ExperimentalTime
fun WebElement.labelTarget(chrome: Chrome): WebElement? {
    val labelFor = getAttribute("for") ?: return null
    return chrome.driver.findElementByName(labelFor)
}

val eventBus = BroadcastChannel<Event>(DEFAULT_BUFFER_SIZE)