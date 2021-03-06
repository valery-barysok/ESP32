package it.achdjian.plugin.esp32.setting

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.components.installFileCompletionAndBrowseDialog
import it.achdjian.plugin.esp32.ui.BaudRateComboBox
import it.achdjian.plugin.esp32.ui.GridLayout2
import it.achdjian.plugin.esp32.ui.SerialPortListComboBox
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

fun DocumentEvent.getText(): String = document.getText(0, document.length)

class SDKPathDocumentListener(var path: String) : DocumentListener {
    var component: JComponent?
        get() = _component
        set(value) {
            _component = value
            _border = value?.border
            changeBorder()
        }

    private var _component: JComponent? = null
    private var _border: Border? = null


    override fun insertUpdate(p: DocumentEvent) {
        path = p.getText()
        changeBorder()

    }

    override fun removeUpdate(p: DocumentEvent) {
        path = p.getText()
        changeBorder()
    }

    override fun changedUpdate(p: DocumentEvent) {
        path = p.getText()
        changeBorder()
    }

    private fun changeBorder() {
        _component?.let {
            if (!validSDKPath(path)) {
                it.border = BorderFactory.createLineBorder(Color.RED)
            } else {
                it.border = _border
            }
        }
    }

}


class GccPathDocumentListener(var path: String) : DocumentListener {
    override fun insertUpdate(p: DocumentEvent) {
        path = p.getText()
    }

    override fun removeUpdate(p: DocumentEvent) {
        path = p.getText()
    }

    override fun changedUpdate(p: DocumentEvent) {
        path = p.getText()
    }

}

class Esp32PathDocumentListener(var path: String) : DocumentListener {
    override fun insertUpdate(p: DocumentEvent) {
        path = p.getText()
    }

    override fun removeUpdate(p: DocumentEvent) {
        path = p.getText()
    }

    override fun changedUpdate(p: DocumentEvent) {
        path = p.getText()
    }

}


class ESP32Setting : SearchableConfigurable {
    private val espToolBaudRate = BaudRateComboBox{ESP32SettingState.serialPortBaud = it}
    private val espToolPy = SerialPortListComboBox{ESP32SettingState.serialPortName = it}
    private var esp32Sdk = SDKPathDocumentListener(ESP32SettingState.sdkPath)
    private var crosscompiler = GccPathDocumentListener(ESP32SettingState.crosscompilerPath)
    private var esp32OpenOcdListener = Esp32PathDocumentListener(ESP32SettingState.esp32OpenOcdLocation)

    init {
        espToolBaudRate.maximumSize = Dimension(10000, 30)
        espToolPy.maximumSize = Dimension(10000, 30)
    }

    /**
     * Indicates whether the Swing form was modified or not.
     * This method is called very often, so it should not take a long time.
     *
     * @return `true` if the settings were modified, `false` otherwise
     */
    override fun isModified(): Boolean {
        return esp32Sdk.path != ESP32SettingState.sdkPath
                || crosscompiler.path != ESP32SettingState.crosscompilerPath
                || espToolPy.isModified()
                || espToolBaudRate.selectedItem != ESP32SettingState.serialPortBaud
                || esp32OpenOcdListener.path != ESP32SettingState.esp32OpenOcdLocation
    }

    /**
     * Returns the visible name of the configurable component.
     * Note, that this method must return the display name
     * that is equal to the display name declared in XML
     * to avoid unexpected errors.
     *
     * @return the visible name of the configurable component
     */
    override fun getDisplayName(): String = "ESP32 config"

    /**
     * Stores the settings from the Swing form to the configurable component.
     * This method is called on EDT upon user's request.
     *
     */
    override fun apply() {
        ESP32SettingState.sdkPath = esp32Sdk.path
        ESP32SettingState.crosscompilerPath = crosscompiler.path
        ESP32SettingState.esp32OpenOcdLocation = esp32OpenOcdListener.path
    }

    override fun getId(): String =  "it.achdjian.plugin.esp32"

    override fun getHelpTopic(): String = "it.achdjian.plugin.ESP32.setup"

    /**
     * Creates new Swing form that enables user to configure the settings.
     * Usually this method is called on the EDT, so it should not take a long time.
     *
     * Also this place is designed to allocate resources (subscriptions/listeners etc.)
     * @see .disposeUIResources
     *
     *
     * @return new Swing form to show, or `null` if it cannot be created
     */
    override fun createComponent(): JComponent {
        val panel = DialogPanel()
        panel.layout = GridLayout2(5, 2)
        panel.name = "Tools path"

        val sdkPathLabel = JLabel("ESP32 Espressif SDK Path")
        val crossCompilerLabel = JLabel("Crosscompiler Path: ")
        val serialPortLabel = JLabel("Default Serial Port: ")
        val baudRateLabel = JLabel("Default Serial Flashing Baud Rate")
        val esp32OpenOcdLabel = JLabel("ESP32 Openocd Path")

        panel.add(sdkPathLabel)
        panel.add(sdkPathComponent())
        panel.add(crossCompilerLabel)
        panel.add(sdkCrossCompilerPath())
        panel.add(serialPortLabel)
        panel.add(espToolPy)
        panel.add(baudRateLabel)
        espToolBaudRate.selectedItem = ESP32SettingState.serialPortBaud
        panel.add(espToolBaudRate)
        panel.add(esp32OpenOcdLabel)
        panel.add(esp32OpenOcdPathComponent())
        return panel
    }

    private fun sdkCrossCompilerPath(): Component {
        val component = TextFieldWithHistoryWithBrowseButton()
        val editor = component.childComponent.textEditor
        editor.text = ESP32SettingState.crosscompilerPath
        editor.document.addDocumentListener(crosscompiler)
        installFileCompletionAndBrowseDialog(
            null,
            component,
            editor,
            "ESP32 crosscompilerPath path",
            FileChooserDescriptorFactory.createSingleFileDescriptor(),
            TextComponentAccessor.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
        ) {
            crosscompiler.path = it.path
            crosscompiler.path
        }
        return component
    }

    private fun sdkPathComponent(): Component {
        val component = TextFieldWithHistoryWithBrowseButton()
        val editor = component.childComponent.textEditor
        editor.text = ESP32SettingState.sdkPath
        editor.document.addDocumentListener(esp32Sdk)
        esp32Sdk.component = component.childComponent
        installFileCompletionAndBrowseDialog(
            null,
            component,
            editor,
            "ESP32 sdk path",
            FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            TextComponentAccessor.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
        ) {
            esp32Sdk.path = it.path
            esp32Sdk.path
        }
        return component
    }

    private fun esp32OpenOcdPathComponent(): Component {
        val component = TextFieldWithHistoryWithBrowseButton()
        val editor = component.childComponent.textEditor
        editor.text = ESP32SettingState.esp32OpenOcdLocation
        editor.document.addDocumentListener(esp32OpenOcdListener)
        installFileCompletionAndBrowseDialog(
            null,
            component,
            editor,
            "ESP32 OpenOCD  path",
            FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
            TextComponentAccessor.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
        ) {
            esp32OpenOcdListener.path = it.path
            esp32OpenOcdListener.path
        }
        return component
    }
}