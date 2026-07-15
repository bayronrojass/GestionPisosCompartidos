package es.mirumi.es.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Central palette for MiRumi. Every screen should import from here — any inline
 * `Color(0xff...)` for one of these tokens is a drift.
 *
 * Naming reflects the *role*, not the hue: `LilaPrimary` is the brand purple used
 * for icons, chips, and secondary CTAs; `Burgundy` is the auth-flow primary CTA
 * (login/register button); `LilaDark` is the deeper accent for headers and
 * checkboxes. `Fondo` is the app background.
 */
val Fondo = Color(0xFFF8F8F8)
val LilaLight = Color(0xFFDDC1FB)
val LilaCard = Color(0xFFE8D5FC)
val LilaPrimary = Color(0xFF8061A2)
val LilaDark = Color(0xFF58337F)
val Burgundy = Color(0xFF581327)
val TextoGris = Color(0xFF6C6C6C)
val VerdeSaldo = Color(0xFF00C853)
val RojoSaldo = Color(0xFFFF1744)
