package com.example.xarxallibres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.xarxallibres.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {

                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { userId ->
                            navController.navigate("books/$userId") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "books/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                    BooksScreen(
                        userId = userId,
                        onBackClick = {
                            navController.navigate("login") {
                                popUpTo("books/{userId}") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}