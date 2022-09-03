package com.raival.fileexplorer.activity.editor.language.json

import com.raival.fileexplorer.App
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.langs.java.JavaTextTokenizer
import io.github.rosemoe.sora.langs.java.Tokens
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.text.TextUtils
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.InputStreamReader
import java.io.Reader

class JsonLanguage(
    iThemeSource: IThemeSource,
    iGrammarSource: IGrammarSource = IGrammarSource.fromInputStream(
        App.appContext.assets.open("textmate/json/syntax/json.tmLanguage.json"),
        "json.tmLanguage.json",
        null
    ),
    languageConfiguration: Reader = InputStreamReader(App.appContext.assets.open("textmate/json/language-configuration.json")),
    createIdentifiers: Boolean = true
) : TextMateLanguage(iGrammarSource, languageConfiguration, iThemeSource, createIdentifiers) {

    private val jsonFormatter = JsonFormatter()
    private val newlineHandlers = arrayOf<NewlineHandler>(BraceHandler())


    override fun getFormatter(): Formatter {
        return jsonFormatter
    }

    override fun getIndentAdvance(text: ContentReference, line: Int, column: Int): Int {
        val content = text.getLine(line).substring(0, column)
        return getIndentAdvance(content)
    }

    override fun useTab(): Boolean {
        return false
    }


    private fun getIndentAdvance(content: String): Int {
        val t = JavaTextTokenizer(content)
        var token: Tokens
        var advance = 0

        while (t.nextToken().also { token = it } !== Tokens.EOF) {
            if (token === Tokens.LBRACE) {
                advance++
            }
            if (token === Tokens.LBRACK) {
                advance++
            }

            if (advance > 0) {
                if (token === Tokens.RBRACE) {
                    advance--
                }
                if (token === Tokens.RBRACE) {
                    advance--
                }
            }
        }

        advance = 0.coerceAtLeast(advance)

        if (advance > 0) return 4
        return 0
    }

    override fun getNewlineHandlers(): Array<NewlineHandler> {
        return newlineHandlers
    }

    inner class BraceHandler : NewlineHandler {
        override fun matchesRequirement(beforeText: String, afterText: String): Boolean {
            return (beforeText.endsWith("{") && afterText.startsWith("}"))
                    || (beforeText.endsWith("[") && afterText.startsWith("]"))
        }

        override fun handleNewline(
            beforeText: String,
            afterText: String,
            tabSize: Int
        ): NewlineHandleResult {
            val count: Int = TextUtils.countLeadingSpaceCount(beforeText, tabSize)
            val advanceBefore: Int = getIndentAdvance(beforeText)
            val advanceAfter: Int = getIndentAdvance(afterText)
            var text: String
            val sb: StringBuilder = StringBuilder("\n")
                .append(TextUtils.createIndent(count + advanceBefore, tabSize, useTab()))
                .append('\n')
                .append(
                    TextUtils.createIndent(count + advanceAfter, tabSize, useTab())
                        .also { text = it })
            val shiftLeft = text.length + 1
            return NewlineHandleResult(sb, shiftLeft)
        }
    }
}