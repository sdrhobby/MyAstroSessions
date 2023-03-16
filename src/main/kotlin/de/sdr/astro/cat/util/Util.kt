package de.sdr.astro.cat.util

import de.sdr.astro.cat.config.Config
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

class Util {
    companion object {
        private val months = arrayOf(
            Config.getInstance().l10n.getString("month_january"),
            Config.getInstance().l10n.getString("month_february"),
            Config.getInstance().l10n.getString("month_march"),
            Config.getInstance().l10n.getString("month_april"),
            Config.getInstance().l10n.getString("month_may"),
            Config.getInstance().l10n.getString("month_june"),
            Config.getInstance().l10n.getString("month_july"),
            Config.getInstance().l10n.getString("month_august"),
            Config.getInstance().l10n.getString("month_september"),
            Config.getInstance().l10n.getString("month_october"),
            Config.getInstance().l10n.getString("month_november"),
            Config.getInstance().l10n.getString("month_dezember")
        )

        @JvmStatic
        fun formatExposure(exp: Double?): String {
            if (exp == null)
                return "0.0"
            var s = exp.toString()
            if (exp >= 10) {
                s = DecimalFormat("#,##0").format(exp)
            } else if (exp < 10 && exp >= 1) {
                s = DecimalFormat("#,##0.00").format(exp)
            } else if (exp < 1 && exp > 0.01) {
                s = DecimalFormat("#,##0.000").format(exp)
            } else if (exp <= 0.01) {
                s = DecimalFormat("#,##0.00000").format(exp)
            }
            return s
        }

        /**
         * check if the given String starts with a date pattern ("yyyy-MM-dd")
         */
        @JvmStatic
        fun startsWithDate(s: String?): Boolean {
            if (s == null || s.length < 10)
                return false
            val sub = s.substring(0, 10)
            return (sub.indexOf("-") == 4) && (sub.lastIndexOf("-") == 7) && (sub.filter { it.isDigit() }.length == 8)
        }

        fun removeLeadingSlash(s: String): String {
            if (s.startsWith("/"))
                return if (s.length > 1) s.substring(1) else ""
            return s
        }

        // credits to: https://www.techiedelight.com/find-similarities-between-two-strings-in-kotlin/
        private fun getLevenshteinDistance(x: String, y: String): Int {
            val m = x.length
            val n = y.length
            val t = Array(m + 1) { IntArray(n + 1) }
            for (i in 1..m) {
                t[i][0] = i
            }
            for (j in 1..n) {
                t[0][j] = j
            }
            var cost: Int
            for (i in 1..m) {
                for (j in 1..n) {
                    cost = if (x[i - 1] == y[j - 1]) 0 else 1
                    t[i][j] = min(
                        min(t[i - 1][j] + 1, t[i][j - 1] + 1),
                        t[i - 1][j - 1] + cost
                    )
                }
            }
            return t[m][n]
        }

        fun findSimilarity(x: String?, y: String?): Double {
            require(!(x == null || y == null)) { "Strings should not be null" }

            val maxLength = max(x.length, y.length)
            return if (maxLength > 0) {
                (maxLength * 1.0 - getLevenshteinDistance(x, y)) / maxLength * 1.0
            } else 1.0
        }

        @JvmStatic
        fun getMonthName(m: Int): String {
            return months[m - 1]
        }

        @JvmStatic
        fun isFits(path : String): Boolean {
            val ext = path.substring(path.lastIndexOf('.') + 1)
            return arrayOf("fit", "fits").contains(ext.lowercase())
        }
        @JvmStatic
        fun isJpegTiffPng(path: String): Boolean {
            val ext = path.substring(path.lastIndexOf('.') + 1)
            return arrayOf("jpg", "jpeg", "tiff", "tif", "png").contains(ext.lowercase())
        }

    }
}

