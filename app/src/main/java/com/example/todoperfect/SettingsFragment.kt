package com.example.todoperfect

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.todoperfect.logic.SharedPreference
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpButtons()
    }

    override fun onResume() {
        super.onResume()
        emailInfoDisplay.text = TodoPerfectApplication.user!!.email
    }

    private fun setUpButtons() {
        logOutBtn.setOnClickListener { view ->
            Snackbar.make(view, "Sure to log out?", Snackbar.LENGTH_SHORT)
                .setAction("YES") {
                    SharedPreference.removeUser()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                }.show()
        }
        askForIgnoreBatteryOptimization()
    }
    @SuppressLint("BatteryLife")
    private fun askForIgnoreBatteryOptimization() {
        enableNotificationBtn.setOnClickListener { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                val packageName = requireActivity().packageName
                val pm = requireActivity().getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Snackbar.make(view, "Enable exact notification?", Snackbar.LENGTH_SHORT)
                        .setAction("SURE") {
                            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }.show()
                } else {
                    Snackbar.make(view, "You've already enabled notification!",
                        Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}