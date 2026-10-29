package edu.unc.learnwithai

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLifeSpanHandlerAdapter
import javax.swing.JComponent

/**
 * Everything that touches JCEF lives in this file. Depending on the IDE build,
 * JCEF classes can be missing at runtime (2026.2+ moved them into a separate
 * bundled plugin that can be disabled), so [LearnWithAIToolWindowFactory] only
 * reaches this class behind a LinkageError guard and falls back to a plain
 * "open in browser" panel if it can't load.
 */
internal class BrowserPanel private constructor(private val browser: JBCefBrowser) {

    val component: JComponent get() = browser.component

    /** Disposes the embedded browser when the tool window content goes away. */
    fun disposeWith(parent: com.intellij.openapi.Disposable) = Disposer.register(parent, browser)

    fun titleActions(): List<AnAction> = listOf(
        object : DumbAwareAction("Home", "Go back to the LearnWithAI home page", AllIcons.Nodes.HomeFolder) {
            override fun actionPerformed(e: AnActionEvent) = browser.loadURL(HOME_URL)
        },
        object : DumbAwareAction("Reload", "Reload the current page", AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) = browser.cefBrowser.reload()
        },
        object : DumbAwareAction("Open in Browser", "Open this page in your web browser", AllIcons.Ide.External_link_arrow) {
            override fun actionPerformed(e: AnActionEvent) =
                BrowserUtil.browse(browser.cefBrowser.url?.takeIf { it.startsWith("http") } ?: HOME_URL)
        },
    )

    /**
     * Links that open a new window (target="_blank", window.open) can't get one
     * here. UNC pages, including sign-in, load in the sidebar itself; anything
     * else goes to the student's real browser.
     */
    private class PopupHandler : CefLifeSpanHandlerAdapter() {
        override fun onBeforePopup(
            browser: CefBrowser,
            frame: CefFrame?,
            targetUrl: String?,
            targetFrameName: String?,
        ): Boolean {
            val url = targetUrl ?: return true
            if (isUncUrl(url)) browser.loadURL(url) else BrowserUtil.browse(url)
            return true
        }
    }

    companion object {
        /** Returns null when this IDE can't host an embedded browser. */
        fun createOrNull(): BrowserPanel? {
            if (!JBCefApp.isSupported()) return null
            val browser = JBCefBrowser(HOME_URL)
            browser.jbCefClient.addLifeSpanHandler(PopupHandler(), browser.cefBrowser)
            return BrowserPanel(browser)
        }
    }
}
