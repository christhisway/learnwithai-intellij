package edu.unc.learnwithai

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLifeSpanHandlerAdapter
import java.awt.FlowLayout
import java.awt.GridBagLayout
import java.net.URI
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants

const val HOME_URL = "https://learnwithai.unc.edu/"

/**
 * Sidebar that shows learnwithai.unc.edu in the IDE's embedded Chromium (JCEF).
 * Deliberately not a browser: no address bar, and the only controls are Home,
 * Reload, and Open in Browser in the tool window's title bar.
 */
class LearnWithAIToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (!JBCefApp.isSupported()) {
            addContent(toolWindow, unsupportedPanel())
            return
        }

        val browser = JBCefBrowser(HOME_URL)
        browser.jbCefClient.addLifeSpanHandler(PopupHandler(), browser.cefBrowser)

        val content = addContent(toolWindow, browser.component)
        content.setDisposer(browser)

        toolWindow.setTitleActions(listOf(
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
        ))
    }

    private fun addContent(toolWindow: ToolWindow, component: JComponent) =
        ContentFactory.getInstance().createContent(component, "", false).also {
            toolWindow.contentManager.addContent(it)
        }

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

    private fun unsupportedPanel(): JComponent {
        val inner = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JBLabel(
                "<html><div style='text-align:center'>This IDE can't display web pages in a sidebar.<br>" +
                    "Open LearnWithAI in your browser instead.</div></html>",
                SwingConstants.CENTER,
            ))
            add(JPanel(FlowLayout(FlowLayout.CENTER)).apply {
                add(JButton("Open learnwithai.unc.edu").apply {
                    addActionListener { BrowserUtil.browse(HOME_URL) }
                })
            })
            border = JBUI.Borders.empty(16)
        }
        return JPanel(GridBagLayout()).apply { add(inner) }
    }
}

internal fun isUncUrl(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    val host = uri.host?.lowercase() ?: return false
    return uri.scheme == "https" && (host == "unc.edu" || host.endsWith(".unc.edu"))
}
