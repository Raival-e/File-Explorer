package com.raival.fileexplorer.activity.editor.language.xml

import com.raival.fileexplorer.App
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.InputStreamReader
import java.io.Reader

class XmlLanguage(
    iThemeSource: IThemeSource,
    iGrammarSource: IGrammarSource = IGrammarSource.fromInputStream(
        App.appContext.assets.open("textmate/xml/syntax/xml.tmLanguage.json"),
        "xml.tmLanguage.json",
        null
    ),
    languageConfiguration: Reader = InputStreamReader(App.appContext.assets.open("textmate/xml/language-configuration.json")),
    createIdentifiers: Boolean = true
) : TextMateLanguage(iGrammarSource, languageConfiguration, iThemeSource, createIdentifiers) {

}