package es.mirumi.es.ui.activities

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class TestComposite {
    data class Message(
        val author: String,
        val body: String,
    )

    @Composable
    fun MessageCard(msg: Message) {
        Text(text = msg.author)
    }

    @Preview
    @Composable
    fun PreviewMessageCard() {
        MessageCard(
            msg = Message("Lexi", "Hey, take a look at Jetpack Compose, it's great!"),
        )
    }
}
