package com.closetfinder.app.util

/** Generates sequential item codes like A001, A002, ... A999, B001, ... */
object CodeGenerator {

    fun next(previousCode: String?): String {
        if (previousCode == null) return "A001"

        val letters = previousCode.takeWhile { it.isLetter() }
        val number = previousCode.drop(letters.length).toIntOrNull() ?: 0

        return if (number >= 999) {
            incrementLetters(letters) + "001"
        } else {
            letters + (number + 1).toString().padStart(3, '0')
        }
    }

    private fun incrementLetters(letters: String): String {
        val chars = letters.toCharArray()
        var i = chars.size - 1
        while (i >= 0) {
            if (chars[i] == 'Z') {
                chars[i] = 'A'
                i--
            } else {
                chars[i] = chars[i] + 1
                return String(chars)
            }
        }
        return "A" + String(chars)
    }
}
