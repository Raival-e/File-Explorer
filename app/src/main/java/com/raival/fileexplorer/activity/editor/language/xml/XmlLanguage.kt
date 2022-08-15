package com.raival.fileexplorer.activity.editor.language.xml

import com.raival.fileexplorer.App
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import org.eclipse.tm4e.core.theme.IRawTheme
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class XmlLanguage(
    theme: IRawTheme,
    grammarName: String = "xml.tmLanguage.json",
    grammarIns: InputStream = App.appContext.assets.open("textmate/xml/syntax/xml.tmLanguage.json"),
    languageConfiguration: Reader = InputStreamReader(App.appContext.assets.open("textmate/xml/language-configuration.json")),
    createIdentifiers: Boolean = true
) : TextMateLanguage(grammarName, grammarIns, languageConfiguration, theme, createIdentifiers) {

}