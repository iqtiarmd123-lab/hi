package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CreateVmScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IsoManagerScreen
import com.example.ui.screens.SnapshotsScreen
import com.example.ui.screens.VmDisplayScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MobileVirtualOsApp()
            }
        }
    }
}

@Composable
fun MobileVirtualOsApp(
    viewModel: VmViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()
    val vms by viewModel.vms.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onCreateVmClick = { navController.navigate("create_vm") },
                        onImportIsoClick = { navController.navigate("iso_manager") },
                        onStartVmClick = { vm ->
                            navController.navigate("vm_display/${vm.id}")
                        },
                        onViewSnapshotsClick = { vm ->
                            navController.navigate("snapshots/${vm.id}")
                        }
                    )
                }

                composable("create_vm") {
                    CreateVmScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onVmCreated = { vmId ->
                            navController.navigate("vm_display/$vmId") {
                                popUpTo("home")
                            }
                        }
                    )
                }

                composable("iso_manager") {
                    IsoManagerScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onQuickBootIso = { vmId ->
                            navController.navigate("vm_display/$vmId") {
                                popUpTo("home")
                            }
                        }
                    )
                }

                composable(
                    route = "vm_display/{vmId}",
                    arguments = listOf(navArgument("vmId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val vmId = backStackEntry.arguments?.getLong("vmId") ?: 0L
                    val vm = vms.firstOrNull { it.id == vmId }
                    if (vm != null) {
                        VmDisplayScreen(
                            viewModel = viewModel,
                            vm = vm,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = "snapshots/{vmId}",
                    arguments = listOf(navArgument("vmId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val vmId = backStackEntry.arguments?.getLong("vmId") ?: 0L
                    val vm = vms.firstOrNull { it.id == vmId }
                    if (vm != null) {
                        SnapshotsScreen(
                            viewModel = viewModel,
                            vm = vm,
                            onBackClick = { navController.popBackStack() },
                            onLaunchVm = { launchedVm ->
                                navController.navigate("vm_display/${launchedVm.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}
