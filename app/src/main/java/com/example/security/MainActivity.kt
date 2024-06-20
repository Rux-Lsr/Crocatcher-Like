package com.example.security

import AskPermissions
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.security.sampledata.MailSender
import com.example.security.ui.theme.SecurityTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity :  ComponentActivity() {
    private val REQUEST_CODE_ENABLE_ADMIN = 1

    private var devicePolicyManager: DevicePolicyManager? = null
    private var componentName: ComponentName? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SecurityTheme {
                devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.oip_removebg_preview), modifier = Modifier.size(300.dp),contentDescription = "")
                    Button( onClick = {
                        if (!devicePolicyManager!!.isAdminActive(componentName!!)) {
                            // If not, request device administrator privileges
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Cette app a besoin des paramètre admin pour fonctionner")
                            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
                        } else {
                            Toast.makeText(applicationContext, "déjà un admin", Toast.LENGTH_SHORT).show()
                        }

                    }){
                        Text(text = "Activer l'admin")
                    }
                    AskPermissions()
                }

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                // Device admin privileges granted
                // You can handle this case as needed
            } else {
                // Device admin privileges not granted
                // You can handle this case as needed
            }
        }
    }
}