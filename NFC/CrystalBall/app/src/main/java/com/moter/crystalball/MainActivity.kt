package com.moter.crystalball

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.moter.crystalball.ui.theme.CrystalBallTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.experimental.and

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        initNfcConfigurations()
        intent?.handleDiscoveredTag()
        setContent {
            CrystalBallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScreenContent()
                }
            }
        }
    }

    private fun initNfcConfigurations() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            finish()
        }

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_MUTABLE
        )

        intentFiltersArray = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.handleDiscoveredTag()
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            null,
        )
    }

    private fun Intent.handleDiscoveredTag() {
        println("Scan handleDiscoveredTag ${this.action}")
        if (NfcAdapter.ACTION_TAG_DISCOVERED == this.action || NfcAdapter.ACTION_NDEF_DISCOVERED == this.action) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                this.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            tag?.id?.let {
                println("Scan tagId $it")
                val tagValue = it.toHexString()
                viewModel.handleEvent(MainScreenEvent.NfcScanned(tagValue))
                Toast.makeText(this@MainActivity, "NFC tag detected: $tagValue", Toast.LENGTH_SHORT)
                    .show()
            }

            parseNDefMessage(this)
        }
    }


    private fun parseNDefMessage(intent: Intent) {
        val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val ndefMessages = mutableListOf<NdefMessage>()
        if (parcelables != null) {
            for (i in parcelables.indices) {
                ndefMessages.add(i, parcelables[i] as NdefMessage)
            }
            buildTagViews(ndefMessages.toTypedArray())
        }
    }

    private fun buildTagViews(ndefMessages: Array<NdefMessage>) {
        if (ndefMessages.isEmpty()) return
        var text = ""
        val payload = ndefMessages[0].records[0].payload
        val textEncoding: Charset =
            if ((payload[0] and 128.toByte()).toInt() == 0) Charsets.UTF_8 else Charsets.UTF_16 // Get the Text Encoding
        val languageCodeLength: Int =
            (payload[0] and 51).toInt() // Get the Language Code, e.g. "en"
        try {
            // Get the Text
            text = String(
                payload,
                languageCodeLength + 1,
                payload.size - languageCodeLength - 1,
                textEncoding
            )
        } catch (e: UnsupportedEncodingException) {
            Log.e("UnsupportedEncoding", e.toString())
        }
        Toast.makeText(this@MainActivity, "NFC message: $text", Toast.LENGTH_SHORT)
            .show()
    }


    private fun ByteArray.toHexString(): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(size * 2)

        map { byte ->
            val value = byte.toInt()
            val hexChar1 = hexChars[value shr 4 and 0x0F]
            val hexChar2 = hexChars[value and 0x0F]
            result.append(hexChar1)
            result.append(hexChar2)
        }

        return result.toString()
    }


}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CrystalBallTheme {
        ScreenContent()
    }
}