package edu.unc.learnwithai

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
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
        val panel = tryCreateBrowserPanel()
        if (panel == null) {
            addContent(toolWindow, unsupportedPanel())
            return
        }

        val content = addContent(toolWindow, panel.component)
        panel.disposeWith(content)
        toolWindow.setTitleActions(panel.titleActions())
    }

    private fun addContent(toolWindow: ToolWindow, component: JComponent) =
        ContentFactory.getInstance().createContent(component, "", false).also {
            toolWindow.contentManager.addContent(it)
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

/** The embedded browser, or null if this IDE can't provide one. Never throws on missing JCEF classes. */
internal fun tryCreateBrowserPanel(): BrowserPanel? = try {
    BrowserPanel.createOrNull()
} catch (e: LinkageError) {
    // JCEF classes aren't visible to this plugin on this IDE build.
    logger<LearnWithAIToolWindowFactory>().warn("Embedded browser unavailable; showing fallback panel", e)
    null
}

internal fun isUncUrl(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    val host = uri.host?.lowercase() ?: return false
    return uri.scheme == "https" && (host == "unc.edu" || host.endsWith(".unc.edu"))
}
