package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.HistoryLog
import com.example.data.ScheduledLink
import com.example.viewmodel.LinkViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FrostedGlassApp(viewModel: LinkViewModel) {
    val scheduledLinks by viewModel.scheduledLinks.collectAsState()
    val historyLogs by viewModel.historyLogs.collectAsState()

    var currentTab by remember { mutableStateOf("schedules") } // "schedules", "statistics", "groups"
    var showAddDialog by remember { mutableStateOf(false) }
    var linkToEdit by remember { mutableStateOf<ScheduledLink?>(null) }
    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6FF))
    ) {
        // Floating ambient glowing background circles for Frosted Glass depth
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-left soft blue glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF93C5FD).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.15f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.15f, size.height * 0.15f),
                radius = size.width * 0.7f
            )
            // Bottom-right soft indigo glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFC7D2FE).copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.85f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.85f),
                radius = size.width * 0.6f
            )
        }

        // Main content column
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HeaderSection(
                    title = when (currentTab) {
                        "schedules" -> "Hẹn giờ mở Link"
                        "statistics" -> "Thống kê"
                        "groups" -> "Nhóm Liên Kết"
                        else -> "Hẹn giờ mở Link"
                    }
                )
            },
            bottomBar = {
                FrostedBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { 
                        currentTab = it 
                        // Reset group filter when switching tab to ensure smooth flow
                        if (it != "groups") {
                            selectedGroupFilter = null
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    "schedules" -> {
                        SchedulesScreen(
                            links = scheduledLinks,
                            onToggleActive = { viewModel.toggleScheduledLink(it) },
                            onEdit = { linkToEdit = it },
                            onAddClick = { showAddDialog = true }
                        )
                    }
                    "statistics" -> {
                        StatisticsScreen(
                            historyLogs = historyLogs,
                            totalSchedules = scheduledLinks.size,
                            onClearHistory = { viewModel.clearHistory() }
                        )
                    }
                    "groups" -> {
                        GroupsScreen(
                            links = scheduledLinks,
                            selectedGroup = selectedGroupFilter,
                            onGroupSelect = { selectedGroupFilter = it },
                            onToggleActive = { viewModel.toggleScheduledLink(it) },
                            onEdit = { linkToEdit = it }
                        )
                    }
                }
            }
        }

        // Add Dialog Modal
        if (showAddDialog) {
            AddOrEditLinkDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, url, hour, minute, days, group, isInterval, intervalMins ->
                    viewModel.addScheduledLink(title, url, hour, minute, days, group, isInterval, intervalMins)
                    showAddDialog = false
                }
            )
        }

        // Edit Dialog Modal
        if (linkToEdit != null) {
            AddOrEditLinkDialog(
                existingLink = linkToEdit,
                onDismiss = { linkToEdit = null },
                onSave = { title, url, hour, minute, days, group, isInterval, intervalMins ->
                    linkToEdit?.let { old ->
                        viewModel.updateScheduledLink(
                            old.copy(
                                title = title,
                                url = url,
                                hour = hour,
                                minute = minute,
                                repeatDays = days,
                                groupName = group,
                                isIntervalMode = isInterval,
                                intervalMinutes = intervalMins
                            )
                        )
                    }
                    linkToEdit = null
                },
                onDelete = {
                    linkToEdit?.let { old ->
                        viewModel.deleteScheduledLink(old)
                    }
                    linkToEdit = null
                }
            )
        }
    }
}

@Composable
fun HeaderSection(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Tự động hóa trình duyệt",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }

        // Information badge/settings look
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.6f), CircleShape)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)), CircleShape)
                .clickable { /* No action needed, purely visual decoration matching template */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Cài đặt",
                tint = Color(0xFF475569),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FrostedBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Lịch hẹn",
                iconActive = Icons.Filled.Alarm,
                iconInactive = Icons.Outlined.Alarm,
                isActive = currentTab == "schedules",
                onClick = { onTabSelected("schedules") },
                modifier = Modifier.testTag("tab_schedules")
            )
            BottomNavItem(
                label = "Thống kê",
                iconActive = Icons.Filled.BarChart,
                iconInactive = Icons.Outlined.BarChart,
                isActive = currentTab == "statistics",
                onClick = { onTabSelected("statistics") },
                modifier = Modifier.testTag("tab_statistics")
            )
            BottomNavItem(
                label = "Nhóm",
                iconActive = Icons.Filled.Folder,
                iconInactive = Icons.Outlined.Folder,
                isActive = currentTab == "groups",
                onClick = { onTabSelected("groups") },
                modifier = Modifier.testTag("tab_groups")
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    iconActive: androidx.compose.ui.graphics.vector.ImageVector,
    iconInactive: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .background(
                    color = if (isActive) Color(0xFFDBEAFE) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActive) iconActive else iconInactive,
                contentDescription = label,
                tint = if (isActive) Color(0xFF1D4ED8) else Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color(0xFF1D4ED8) else Color(0xFF64748B),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .background(
            color = Color.White.copy(alpha = 0.45f),
            shape = RoundedCornerShape(28.dp)
        )
        .border(
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(28.dp)
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )

    Column(
        modifier = cardModifier.padding(18.dp),
        content = content
    )
}

@Composable
fun SchedulesScreen(
    links: List<ScheduledLink>,
    onToggleActive: (ScheduledLink) -> Unit,
    onEdit: (ScheduledLink) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {
            if (links.isEmpty()) {
                item {
                    EmptyStateCard(onAddClick = onAddClick)
                }
            } else {
                items(links) { link ->
                    ScheduleItemRow(
                        link = link,
                        onToggleActive = { onToggleActive(link) },
                        onEdit = { onEdit(link) }
                    )
                }

                // Dotted layout at bottom
                item {
                    DottedPlaceholderCard(onClick = onAddClick)
                }
            }
        }

        // Add Trigger Button at bottom
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 12.dp)
                .testTag("add_schedule_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF001D35),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thêm lịch mới",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ScheduleItemRow(
    link: ScheduledLink,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit
) {
    // Generate icons dynamically based on URL/group
    val isWork = link.groupName.lowercase().contains("việc") || link.groupName.lowercase().contains("work")
    val isSocial = link.url.contains("facebook") || link.url.contains("fb") || link.url.contains("zalo") || link.url.contains("youtube")
    val isNews = link.groupName.lowercase().contains("tin") || link.url.contains("vnexpress") || link.url.contains("news")

    val (iconBgColor, iconColor, iconText) = when {
        isWork -> Triple(Color(0xFFEEF2FF), Color(0xFF4F46E5), "📧")
        isSocial -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), "🌐")
        isNews -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), "📰")
        else -> Triple(Color(0xFFECFDF5), Color(0xFF059669), "🔗")
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_item_${link.id}"),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle visual indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconText, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = link.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B).copy(alpha = if (link.isActive) 1f else 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (link.isIntervalMode) {
                            Text(
                                text = "Mỗi ${link.intervalMinutes} phút",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB).copy(alpha = if (link.isActive) 1f else 0.5f)
                            )
                            Text(
                                text = " • Lặp chu kỳ",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B).copy(alpha = if (link.isActive) 1f else 0.5f)
                            )
                        } else {
                            Text(
                                text = String.format("%02d:%02d", link.hour, link.minute),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B).copy(alpha = if (link.isActive) 0.8f else 0.4f)
                            )
                            Text(
                                text = " • ",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = link.getRepeatDaysText(),
                                fontSize = 13.sp,
                                color = Color(0xFF64748B).copy(alpha = if (link.isActive) 1f else 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Elegant custom toggle Switch
            Switch(
                checked = link.isActive,
                onCheckedChange = { onToggleActive() },
                modifier = Modifier.testTag("toggle_${link.id}"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2563EB),
                    uncheckedThumbColor = Color(0xFFF1F5F9),
                    uncheckedTrackColor = Color(0xFFCBD5E1)
                )
            )
        }
    }
}

@Composable
fun DottedPlaceholderCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                border = BorderStroke(2.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Lên lịch cho một liên kết mới",
            color = Color(0xFF64748B),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStateCard(onAddClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFEFF6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Không có lịch hẹn nào",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hãy thêm một lịch mới để tự động mở các trang web yêu thích theo thời gian chỉ định.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Tạo lịch ngay", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun StatisticsScreen(
    historyLogs: List<HistoryLog>,
    totalSchedules: Int,
    onClearHistory: () -> Unit
) {
    val totalTriggers = historyLogs.size
    val successTriggers = historyLogs.count { it.status == "SUCCESS" }
    val successRate = if (totalTriggers > 0) (successTriggers * 100) / totalTriggers else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Statistics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Total
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Tổng lịch", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalSchedules", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                }
            }

            // Card 2: Executions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Đã kích hoạt", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalTriggers", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                }
            }

            // Card 3: Success rate
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Tỉ lệ mở", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$successRate%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // History logs section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lịch sử mở Link",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )

            if (historyLogs.isNotEmpty()) {
                TextButton(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa lịch sử", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (historyLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có hoạt động mở link nào.",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(historyLogs) { log ->
                    HistoryLogItemRow(log = log)
                }
            }
        }
    }
}

@Composable
fun HistoryLogItemRow(log: HistoryLog) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val timeString = dateFormat.format(Date(log.timestamp))

    val isSuccess = log.status == "SUCCESS"

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isSuccess) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = log.linkTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = log.url,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (isSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isSuccess) "Thành công" else "Lỗi",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                }
            }
        }
    }
}

@Composable
fun GroupsScreen(
    links: List<ScheduledLink>,
    selectedGroup: String?,
    onGroupSelect: (String?) -> Unit,
    onToggleActive: (ScheduledLink) -> Unit,
    onEdit: (ScheduledLink) -> Unit
) {
    // Dynamically retrieve groups list
    val groups = remember(links) {
        val list = links.map { it.groupName }.distinct().toMutableList()
        if (!list.contains("Chung")) {
            list.add(0, "Chung")
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Group chips navigation
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = "Lọc theo danh mục",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Row-like flow for group categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GroupChip(
                        name = "Tất cả",
                        isSelected = selectedGroup == null,
                        onClick = { onGroupSelect(null) }
                    )
                    groups.forEach { groupName ->
                        val count = links.count { it.groupName == groupName }
                        GroupChip(
                            name = "$groupName ($count)",
                            isSelected = selectedGroup == groupName,
                            onClick = { onGroupSelect(groupName) }
                        )
                    }
                }
            }

            val filteredLinks = if (selectedGroup == null) {
                links
            } else {
                links.filter { it.groupName == selectedGroup }
            }

            if (filteredLinks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không tìm thấy liên kết nào trong nhóm này.",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredLinks) { link ->
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        ScheduleItemRow(
                            link = link,
                            onToggleActive = { onToggleActive(link) },
                            onEdit = { onEdit(link) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0xFF1D4ED8) else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun AddOrEditLinkDialog(
    existingLink: ScheduledLink? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, hour: Int, minute: Int, repeatDays: String, groupName: String, isIntervalMode: Boolean, intervalMinutes: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEdit = existingLink != null

    var title by remember { mutableStateOf(existingLink?.title ?: "") }
    var url by remember { mutableStateOf(existingLink?.url ?: "") }
    var hour by remember { mutableStateOf(existingLink?.hour ?: 8) }
    var minute by remember { mutableStateOf(existingLink?.minute ?: 30) }
    
    // Repeat days parsing
    val initialDaysSet = remember {
        existingLink?.repeatDays?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() }?.toMutableStateList()
            ?: mutableStateListOf<Int>()
    }

    var groupName by remember { mutableStateOf(existingLink?.groupName ?: "Chung") }
    
    var isIntervalMode by remember { mutableStateOf(existingLink?.isIntervalMode ?: false) }
    var intervalMinutes by remember { mutableStateOf(existingLink?.intervalMinutes ?: 15) }

    // Dialog layout using Frosted overlay look
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .clickable(enabled = false) {}, // Prevent clicks from dismissing
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (isEdit) "Sửa Lịch Hẹn Giờ" else "Thêm Lịch Mới",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tên gợi nhớ") },
                        placeholder = { Text("Ví dụ: Mở Facebook cá nhân") },
                        modifier = Modifier.fillMaxWidth().testTag("input_title"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // URL Input
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Đường dẫn (URL)") },
                        placeholder = { Text("Ví dụ: facebook.com") },
                        modifier = Modifier.fillMaxWidth().testTag("input_url"),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cách thức kích hoạt
                    Text(
                        text = "Cách thức kích hoạt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (!isIntervalMode) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    border = BorderStroke(1.5.dp, if (!isIntervalMode) Color(0xFF3B82F6) else Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isIntervalMode = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mốc giờ cố định",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isIntervalMode) Color(0xFF2563EB) else Color(0xFF64748B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isIntervalMode) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    border = BorderStroke(1.5.dp, if (isIntervalMode) Color(0xFF3B82F6) else Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isIntervalMode = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Lặp lại chu kỳ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIntervalMode) Color(0xFF2563EB) else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isIntervalMode) {
                        // Custom Tactile Time Selector
                        Text(
                            text = "Thời gian",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour Control
                            TimeDial(
                                value = hour,
                                range = 0..23,
                                onValueChange = { hour = it },
                                testTag = "dial_hour"
                            )
                            
                            Text(
                                text = ":",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )

                            // Minute Control
                            TimeDial(
                                value = minute,
                                range = 0..59,
                                onValueChange = { minute = it },
                                testTag = "dial_minute"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Repeat Days Selection
                        Text(
                            text = "Lặp lại",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val weekDays = listOf(
                                2 to "T2",
                                3 to "T3",
                                4 to "T4",
                                5 to "T5",
                                6 to "T6",
                                7 to "T7",
                                8 to "CN"
                            )
                            weekDays.forEach { (dayVal, label) ->
                                val isSelected = initialDaysSet.contains(dayVal)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable {
                                            if (isSelected) {
                                                initialDaysSet.remove(dayVal)
                                            } else {
                                                initialDaysSet.add(dayVal)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick select triggers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    initialDaysSet.clear()
                                    initialDaysSet.addAll(listOf(2, 3, 4, 5, 6, 7, 8))
                                }
                            ) {
                                Text("Hàng ngày", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = {
                                    initialDaysSet.clear()
                                }
                            ) {
                                Text("Một lần", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Interval-based scheduling controls
                        Text(
                            text = "Lặp lại chu kỳ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mở/Tải lại sau mỗi:",
                                fontSize = 14.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            TimeDial(
                                value = intervalMinutes,
                                range = 1..1440,
                                onValueChange = { intervalMinutes = it },
                                testTag = "dial_interval"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "phút",
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interval presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            listOf(1, 5, 15, 30, 60).forEach { mins ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (intervalMinutes == mins) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { intervalMinutes = mins }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (mins < 60) "$mins phút" else "${mins / 60} giờ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (intervalMinutes == mins) Color.White else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Group Category Input
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Nhóm (Ví dụ: Công việc, Giải trí)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_group"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEdit && onDelete != null) {
                            TextButton(
                                onClick = onDelete,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.testTag("delete_button")
                            ) {
                                Text("Xóa", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF64748B))
                        ) {
                            Text("Hủy", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank() && url.isNotBlank()) {
                                    val daysString = if (initialDaysSet.size == 7) {
                                        "daily"
                                    } else {
                                        initialDaysSet.sorted().joinToString(",")
                                    }
                                    onSave(title.trim(), url.trim(), hour, minute, daysString, groupName.trim(), isIntervalMode, intervalMinutes)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = title.isNotBlank() && url.isNotBlank(),
                            modifier = Modifier.testTag("save_button")
                        ) {
                            Text("Lưu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDial(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        IconButton(
            onClick = {
                val prev = value - 1
                onValueChange(if (prev < range.first) range.last else prev)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = Color(0xFF475569))
        }

        Text(
            text = String.format("%02d", value),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(40.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = {
                val next = value + 1
                onValueChange(if (next > range.last) range.first else next)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tăng", tint = Color(0xFF475569))
        }
    }
}
