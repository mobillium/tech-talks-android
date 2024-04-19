package com.moter.nfcwriter

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.moter.nfcwriter.ui.theme.NfcWriterTheme

class MainActivity : ComponentActivity() {
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNfcConfigurations()
        setContent {
            NfcWriterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    private fun initNfcConfigurations() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null || nfcAdapter?.isEnabled == false) {
            Toast.makeText(this, "Nfc not supported", Toast.LENGTH_SHORT).show()
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
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
        )
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            tag?.let {
                writeNdefMessage(it, "Hello Mobillium Android Team!")
//                writeNdefURL(it, "https://github.com/mobillium")
//                writeApplicationRecord(it,"com.whatsapp")
            }
        }

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


    private fun writeNdefMessage(tag: Tag, message: String) {
        val ndef = Ndef.get(tag)
        ndef.connect()

        // Create NdefMessage
        val ndefMessage = NdefMessage(
            arrayOf(
                NdefRecord.createTextRecord(null, message)
            )
        )

        // Write NdefMessage to the tag
        ndef.writeNdefMessage(ndefMessage)
        ndef.close()

        Toast.makeText(this, "Message written to NFC tag", Toast.LENGTH_LONG).show()
    }

    private fun writeNdefURL(tag: Tag, url: String) {
        val uri = Uri.parse(url)
        val uriRecord = NdefRecord.createUri(uri)

        val ndefMessage = NdefMessage(uriRecord)

        val ndef = Ndef.get(tag)
        ndef?.connect()

        ndef?.writeNdefMessage(ndefMessage)
        ndef?.close()

        Toast.makeText(this, "URL written to NFC tag", Toast.LENGTH_LONG).show()
    }


    private fun writeApplicationRecord(tag: Tag, packageName: String) {
        val aar = NdefRecord.createApplicationRecord(packageName)
        val ndefMessage = NdefMessage(aar)

        val ndef = Ndef.get(tag)
        ndef?.connect()

        ndef?.writeNdefMessage(ndefMessage)
        ndef?.close()

        Toast.makeText(this, "Application written to NFC tag", Toast.LENGTH_LONG).show()
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        NfcWriterTheme {
            Greeting("Android")
        }
    }
}