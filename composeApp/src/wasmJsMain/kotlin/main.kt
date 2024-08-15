import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val link = document.querySelector("link[rel~='icon']")
        ?: document.createElement("link").apply {
            setAttribute("rel", "icon")
            document.head?.appendChild(this)
        }
    link.setAttribute(
        "href",
        "https://github.com/jakepurple13/TwentyFourGame/blob/master/composeApp/src/androidMain/ic_launcher-playstore.png?raw=true"
    )
    ComposeViewport(document.body!!) {
        App(remember { Settings { "" } })
    }
}