package com.example.galaxy.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.galaxy.data.local.GalaxyDatabase
import com.example.galaxy.data.model.NoticeCategory
import com.example.galaxy.data.repository.AuthRepository
import com.example.galaxy.ui.navigation.Route
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository(context) }
    val db = remember { GalaxyDatabase.getInstance(context) }

    var isLoggedIn by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoggedIn = authRepo.isLoggedIn()
        userName = authRepo.getUserName()
        userId = authRepo.getUserId()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
    ) {
        Text("설정", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(24.dp))

        // Account section
        SectionTitle("계정")
        SettingsCard {
            if (isLoggedIn) {
                SettingsItem(Icons.Default.Person, userName ?: "사용자", "학번: ${userId ?: ""}") {}
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                SettingsItem(Icons.Default.Logout, "로그아웃", "토큰 삭제") {
                    scope.launch {
                        authRepo.logout()
                        isLoggedIn = false
                        userName = null
                        userId = null
                    }
                }
            } else {
                SettingsItem(Icons.Default.Login, "JUIS 로그인", "학사일정, 도서관 등 이용") {
                    navController.navigate(Route.Login)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionTitle("정보")
        SettingsCard {
            SettingsItem(Icons.Default.School, "전주대학교", "www.jj.ac.kr") {}
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            SettingsItem(Icons.Default.Code, "앱 버전", "1.0.0") {}
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            SettingsItem(Icons.Default.Info, "오픈소스 라이선스", "Jsoup, Retrofit, Room 등") {}
        }

        Spacer(Modifier.height(16.dp))

        SectionTitle("데이터")
        SettingsCard {
            SettingsItem(Icons.Default.Delete, "캐시 삭제", "학식, 공지 캐시 초기화") {
                scope.launch {
                    db.mealDao().deleteAll()
                    NoticeCategory.entries.forEach { db.noticeDao().deleteByCategory(it.name) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            SettingsItem(Icons.Default.Delete, "시간표 초기화", "저장된 시간표 전체 삭제") {
                scope.launch { db.timetableDao().deleteAll() }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            SettingsItem(Icons.Default.Notifications, "알림 설정", "준비 중") {}
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.5.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
