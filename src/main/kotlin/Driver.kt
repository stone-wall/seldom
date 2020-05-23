import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.text.FieldPosition
import kotlin.time.*

@ExperimentalTime
interface Driver : CoroutineScope {

    val driver: WebDriver

    val url: String
        get() = driver.currentUrl.trimEnd { c: Char -> c == '/' }

    val currentTab: Int
        get() = driver.windowHandles.indexOf(driver.windowHandle) + 1

    val numberOfOpenTabs: Int
        get() = driver.windowHandles.size

    fun get(text: String): WebElement?

    /**
     * @param text: the
     */
    fun click(text: String): Boolean


    fun wait(duration: Duration) {
        runBlocking {
            delay(duration)
        }
    }


    fun switchToTab(tabNumber: Int) {
        driver.switchTo().window(driver.windowHandles.toMutableList()[tabNumber - 1])
    }

    fun closeTab(tabNumber: Int) {
        driver.switchTo().window(driver.windowHandles.toMutableList()[tabNumber - 1]).close()
    }

    fun go(url: String) {
        runBlocking {
            driver.navigate().to(url)
            delay(300.milliseconds)
        }
    }

    fun button(text: String): WebElement {
        val buttonClass = driver.findElements(By.tagName("button"))
            .filter { it.isDisplayed && it.size.height > 0 && it.size.width > 0 && it.text == text }
        if (!buttonClass.isNullOrEmpty()) {
            return buttonClass.first()
        }
        val inputClass = driver.findElements(By.tagName("input"))
            .filter { it.getAttribute("type") == "submit" }
            .filter { it.isDisplayed && it.size.height > 0 && it.size.width > 0 && (it.text == text || it.getAttribute("value") == text) }
        return inputClass.first()
    }

    fun textField(position: Int): WebElement {
        val inputClass = driver.findElements(By.tagName("input"))
            .filter { it.getAttribute("type") == "text" }
            .filter { it.isDisplayed && it.size.height > 0 && it.size.width > 0 }
        return inputClass[position]
    }

    fun select(option: String) {
        val select = driver.findElements(By.tagName("select"))
            .first { webElement ->
                Select(webElement).options.map { it.text }.contains(option)
            }
        select.click()
        Select(select).selectByVisibleText(option)
    }

    fun waitFor(element: By, timeout: Duration = 3.minutes): WebElement {
        val waitInterval = 500.milliseconds
        var elapsedTime = 0.milliseconds
        return runBlocking {
            while (element.findElements(driver).isNullOrEmpty() && elapsedTime < timeout) {
                delay(waitInterval)
                elapsedTime += waitInterval
            }
            return@runBlocking element.findElements(driver).first()
        }
    }

    fun clickLink(textContains: String) {
        driver.findElements(By.tagName("a")).first { it.text.contains(textContains) }.click()
    }

    fun linkText(textContains: String): String {
        return driver.findElements(By.tagName("a")).first { it.text.contains(textContains) }.text
    }
}