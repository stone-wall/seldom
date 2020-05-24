import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class ChromeTests {


    @Test
    fun testChomeDriver() {
        chrome.go("https://github.com/")
        assert(chrome.url == "https://github.com")
    }

    @Test
    fun testTabs() {
        chrome.newTab()
        assert(chrome.numberOfOpenTabs == 2)
        assert(chrome.currentTab == 1)
        chrome.switchToTab(2)
        assert(chrome.currentTab == 2)
    }

    @Test
    fun testLinkClicking() {
        chrome.go("https://github.com")
        assert(chrome.url == "https://github.com")
        chrome.click("business")
        assert(chrome.url == "https://github.com/enterprise")
        assertFails {chrome.click("this is a fake link")}
    }

    @Test
    fun `test getting any element based on its text`() {
        chrome.go("https://github.com")
        chrome.get("Username")?.isDisplayed?.let { assert(it) }
    }

    @Test
    fun `test getting target from label element`() {
        chrome.go("https://github.com")
        assert(chrome.url == "https://github.com")
        assert(chrome.get("Username")?.labelTarget(chrome)?.tagName == "input")
    }

    @Test
    fun `test send keyboard type type to text field`() {
        chrome.go("https://github.com")
        assert(chrome.typeTo("ITypeThisIntoUsername", "Username"))
    }

    @Test
    fun `find button test`() {
        chrome.go("https://google.com")
        assertNotNull(chrome.button("Google Search").click())
    }

    @Test
    fun `find ordered text field test`() {
        chrome.go("https://google.com")
        assertNotNull(chrome.textField(0))
    }

    companion object {
        lateinit var chrome: Chrome

        @BeforeClass
        @JvmStatic
        fun setupChromeDriver() {
            chrome = Chrome()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            chrome.driver.quit()
        }
    }
}

