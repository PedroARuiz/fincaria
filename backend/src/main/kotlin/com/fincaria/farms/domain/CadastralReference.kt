package com.fincaria.farms.domain

@JvmInline
value class CadastralReference(val value: String) {

    init {
        require(FORMAT_REGEX.matches(value)) { "Invalid cadastral reference format: $value" }
        require(hasValidCheckDigits(value)) { "Invalid cadastral reference checksum: $value" }
    }

    companion object {
        private val FORMAT_REGEX = Regex("^[A-ZÑ0-9]{20}$")
        private const val ALPHABET = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ0123456789"
        private const val CHECK_DIGIT_ALPHABET = "MQWERTYUIOPASDFGHJKLBZX"
        private val WEIGHTS = intArrayOf(13, 15, 12, 5, 4, 17, 9, 21, 3, 7, 1)

        private fun characterValue(char: Char): Int =
            if (char.isDigit()) char.digitToInt() else ALPHABET.indexOf(char) + 1

        private fun checkDigit(fragment: String): Char {
            val weightedSum = fragment.withIndex().sumOf { (index, char) -> WEIGHTS[index] * characterValue(char) }
            return CHECK_DIGIT_ALPHABET[weightedSum % 23]
        }

        private fun hasValidCheckDigits(reference: String): Boolean {
            val firstCheckDigit = checkDigit(reference.substring(0, 7) + reference.substring(14, 18))
            val secondCheckDigit = checkDigit(reference.substring(7, 14) + reference.substring(14, 18))
            return "$firstCheckDigit$secondCheckDigit" == reference.substring(18, 20)
        }
    }
}
