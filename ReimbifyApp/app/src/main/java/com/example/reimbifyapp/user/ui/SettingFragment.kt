package com.example.reimbifyapp.user.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.databinding.FragmentSettingUserBinding
import com.example.reimbifyapp.data.preferences.SettingPreferences
import com.example.reimbifyapp.user.factory.SettingViewModelFactory
import com.example.reimbifyapp.user.viewmodel.SettingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingViewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = SettingPreferences.getInstance(requireContext().dataStore)
        val settingViewModelFactory = SettingViewModelFactory(pref)
        settingViewModel = ViewModelProvider(this, settingViewModelFactory)[SettingViewModel::class.java]
        settingViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive ->
            binding.switchTheme.setOnCheckedChangeListener(null)
            binding.switchTheme.isChecked = isDarkModeActive

            binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
                settingViewModel.saveThemeSetting(isChecked)

                requireActivity().apply {
                    val intent = intent.apply {
                        putExtra("open_fragment", "setting")
                    }
                    finish()
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
