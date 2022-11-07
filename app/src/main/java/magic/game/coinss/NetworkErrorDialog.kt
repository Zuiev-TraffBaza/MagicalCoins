package magic.game.coinss

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import magic.game.coinss.databinding.DialogArletBinding
import magic.game.coinss.utils.hasInternetConnection

class NetworkErrorDialog(
    private val activity: Activity,
    private val onClickTryAgain: () -> Unit,
    private val onClickDemoMode: () -> Unit,
) : Dialog(activity) {
    private var btnTryAgain: Button? = null
    private var btnSetting: Button? = null
    lateinit var binding: DialogArletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogArletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)

        btnTryAgain = findViewById(R.id.btn_try_again)
        btnSetting = findViewById(R.id.btn_setting)

        binding.btnSetting.setOnClickListener(View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val settingPanelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                activity.startActivityForResult(settingPanelIntent, 3)
            } else {
                val settingWifiIntent = Intent("android.settings.WIFI_SETTINGS")
                context.startActivity(settingWifiIntent)
            }
        })

        binding.btnTryAgain.setOnClickListener(View.OnClickListener {
            onClickTryAgain()
            if (hasInternetConnection(context)) {
                dismiss()
            } else {
                show()
            }
        })

        binding.btnDemoMode.setOnClickListener { onClickDemoMode() }
    }

    fun hideDl() {
        binding.btnTryAgain.performClick()
    }


}