package com.xiaogong.csestudy.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.ui.donate.DonateScreen
import com.xiaogong.csestudy.ui.home.HomeScreen
import com.xiaogong.csestudy.ui.home.HomeViewModel
import com.xiaogong.csestudy.ui.home.HomeViewModelFactory
import com.xiaogong.csestudy.ui.levelselection.LevelSelectionScreen
import com.xiaogong.csestudy.ui.main.ExamLevelState
import com.xiaogong.csestudy.ui.material.MaterialListScreen
import com.xiaogong.csestudy.ui.material.PdfReaderScreen
import com.xiaogong.csestudy.ui.main.MainViewModel
import com.xiaogong.csestudy.ui.main.MainViewModelFactory
import com.xiaogong.csestudy.ui.main.ProfileState
import com.xiaogong.csestudy.ui.profile.ProfileScreen
import com.xiaogong.csestudy.ui.profile.ProfileViewModel
import com.xiaogong.csestudy.ui.profile.ProfileViewModelFactory
import com.xiaogong.csestudy.ui.profilesetup.UserProfileSetupScreen
import com.xiaogong.csestudy.ui.quiz.*
import com.xiaogong.csestudy.ui.study.ChapterSubjectSelectScreen
import com.xiaogong.csestudy.ui.study.StudyViewModel
import com.xiaogong.csestudy.ui.study.StudyViewModelFactory
import com.xiaogong.csestudy.ui.study.SubjectDetailScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "首页", Icons.Default.Home),
    BottomNavItem(Screen.Quiz, "刷题", Icons.Default.Quiz),
    BottomNavItem(Screen.MaterialList, "资料", Icons.Default.MenuBook),
    BottomNavItem(Screen.Profile, "我的", Icons.Default.Person)
)

@Composable
fun AppNavigation(application: CseApplication) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(application))
    val examLevelState by mainVm.examLevelState.collectAsStateWithLifecycle()
    val profileState by mainVm.profileState.collectAsStateWithLifecycle()

    val showBottomBar = bottomNavItems.any { currentRoute == it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "init",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── 启动路由：根据是否已选级别决定跳转目标 ──────
            composable("init") {
                LaunchedEffect(profileState, examLevelState) {
                    when {
                        profileState is ProfileState.Loading || examLevelState is ExamLevelState.Loading -> Unit
                        profileState is ProfileState.NotSet -> navController.navigate(Screen.UserProfileSetup.route) {
                            popUpTo("init") { inclusive = true }
                        }
                        examLevelState is ExamLevelState.NotSelected -> navController.navigate(Screen.LevelSelection.route) {
                            popUpTo("init") { inclusive = true }
                        }
                        examLevelState is ExamLevelState.Selected -> navController.navigate(Screen.Home.route) {
                            popUpTo("init") { inclusive = true }
                        }
                    }
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // ── 用户资料设置（首次启动）──────────────────────
            composable(Screen.UserProfileSetup.route) {
                UserProfileSetupScreen(onComplete = { nickname, avatarUri ->
                    mainVm.saveProfile(nickname, avatarUri)
                    navController.navigate(Screen.LevelSelection.route) {
                        popUpTo(Screen.UserProfileSetup.route) { inclusive = true }
                    }
                })
            }

            // ── 级别选择页 ──────────────────────────────────
            composable(Screen.LevelSelection.route) {
                LevelSelectionScreen(onLevelSelected = { level ->
                    mainVm.saveLevel(level)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.LevelSelection.route) { inclusive = true }
                    }
                })
            }

            // ── 底部导航页面 ──────────────────────────────
            composable(Screen.Home.route) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(application))
                HomeScreen(vm = vm, onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Quiz.route) {
                val vm: QuizModeViewModel = viewModel(factory = QuizModeViewModelFactory(application))
                val quizModeUiState by vm.uiState.collectAsStateWithLifecycle()
                QuizModeScreen(uiState = quizModeUiState, onModeSelected = { mode, param ->
                    when (mode) {
                        QuizMode.CHAPTER -> navController.navigate(Screen.ChapterSubjectSelect.route)
                        else -> navController.navigate(Screen.QuizPlay.createRoute(mode, param))
                    }
                })
            }
            composable(Screen.Profile.route) {
                val vm: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(application))
                ProfileScreen(
                    vm = vm,
                    onLevelSelected = { level -> mainVm.saveLevel(level) },
                    onDonateClick = { navController.navigate(Screen.Donate.route) }
                )
            }

            // ── 捐赠 ──────────────────────────────────────
            composable(Screen.Donate.route) {
                DonateScreen(onBack = { navController.popBackStack() })
            }

            // ── 章节练习：选择科目和章节 ──────────────────────
            composable(Screen.ChapterSubjectSelect.route) {
                val vm: StudyViewModel = viewModel(factory = StudyViewModelFactory(application))
                ChapterSubjectSelectScreen(
                    vm = vm,
                    examLevel = (examLevelState as? ExamLevelState.Selected)?.level,
                    onSubjectClick = { subject ->
                        navController.navigate(Screen.SubjectDetail.createRoute(subject.name))
                    }
                )
            }
            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
            ) { backStack ->
                val subjectName = backStack.arguments?.getString("subjectName") ?: return@composable
                val vm: StudyViewModel = viewModel(factory = StudyViewModelFactory(application))
                SubjectDetailScreen(
                    subjectName = subjectName,
                    vm = vm,
                    onChapterClick = { chapter ->
                        navController.navigate(
                            Screen.QuizPlay.createRoute(QuizMode.CHAPTER, "$subjectName|$chapter")
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── 学习资料 ──────────────────────────────────
            composable(Screen.MaterialList.route) {
                MaterialListScreen(onDocClick = { doc ->
                    navController.navigate(Screen.PdfReader.createRoute(doc.assetName))
                })
            }
            composable(
                route = Screen.PdfReader.route,
                arguments = listOf(navArgument("assetName") { type = NavType.StringType })
            ) { backStack ->
                val assetName = java.net.URLDecoder.decode(
                    backStack.arguments?.getString("assetName") ?: "", "UTF-8"
                )
                PdfReaderScreen(assetName = assetName, onBack = { navController.popBackStack() })
            }

            // ── 刷题子页面 ────────────────────────────────
            composable(
                route = Screen.QuizPlay.route,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("param") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStack ->
                val mode = QuizMode.valueOf(backStack.arguments?.getString("mode") ?: "RANDOM")
                val param = java.net.URLDecoder.decode(
                    backStack.arguments?.getString("param") ?: "", "UTF-8"
                )
                val vm: QuizViewModel = viewModel(
                    factory = QuizViewModelFactory(application, mode, param)
                )
                QuizScreen(
                    vm = vm,
                    onFinish = { correct, total ->
                        navController.navigate(Screen.QuizResult.createRoute(correct, total)) {
                            popUpTo(Screen.QuizPlay.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.QuizResult.route,
                arguments = listOf(
                    navArgument("correct") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType }
                )
            ) { backStack ->
                val correct = backStack.arguments?.getInt("correct") ?: 0
                val total = backStack.arguments?.getInt("total") ?: 0
                QuizResultScreen(
                    correctCount = correct,
                    totalCount = total,
                    onRetry = { navController.popBackStack() },
                    onHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
