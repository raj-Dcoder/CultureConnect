package com.rajveer.cultureconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rajveer.cultureconnect.core.design.CultureConnectAppRoot
import com.rajveer.cultureconnect.features.events.EventsScreen
import com.rajveer.cultureconnect.features.explore.ExploreScreen
import com.rajveer.cultureconnect.features.profile.ProfileScreen
import com.rajveer.cultureconnect.features.travel.TravelScreen
import com.rajveer.cultureconnect.navigation.BottomNavItem
import com.rajveer.cultureconnect.ui.theme.CultureConnectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CultureConnectAppRoot()
        }
    }
}

