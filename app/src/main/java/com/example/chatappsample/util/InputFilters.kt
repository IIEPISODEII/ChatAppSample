package com.example.chatappsample.util

import android.text.InputFilter
import android.text.Spanned

class LetterDigitsInputFilter: InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        for (i in start until end) {
            if (!Character.isLetterOrDigit(source!![i])) return ""
        }
        return source!!
    }
}

class CharLengthInputFilter(private val endLimit: Int): InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        return if (source!!.length + dend > endLimit) ""
        else source
    }
}