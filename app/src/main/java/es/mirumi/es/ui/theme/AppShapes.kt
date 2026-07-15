package es.mirumi.es.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Central corner-radius tokens. Every `Button.shape` / `Card.shape` /
 * `OutlinedTextField.shape` should reference one of these.
 *
 * - [ButtonShape] — every primary and secondary button (12 dp).
 * - [CardShape]   — every list-item Card, balance card, tarjeta blanca (15 dp).
 * - [InputShape]  — every text-field container (15 dp, matches Card so
 *                    forms sit visually flush with adjacent cards).
 */
val ButtonShape = RoundedCornerShape(12.dp)
val CardShape = RoundedCornerShape(15.dp)
val InputShape = RoundedCornerShape(15.dp)
