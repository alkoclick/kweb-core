package io.kweb.demos.multipleSelect

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.button
import io.kweb.dom.element.creation.tags.select
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin

fun main(args: Array<String>) {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin), buildPage = {
        doc.body.new {
            select(listOf(
                "it1" to "Item 1",
                "it2" to "Item 2",
                "it1" to "Item 3"
            ), attributes = fomantic.ui.search.selection.dropdown).setAttributeRaw("multiple", "")

            execute("${'$'}('.ui.dropdown').dropdown();")

            button().text("Click me after selecting some items!").on.click {
                evaluate("${'$'}('.ui.dropdown').dropdown('get value');")
            }
        }
    })
}