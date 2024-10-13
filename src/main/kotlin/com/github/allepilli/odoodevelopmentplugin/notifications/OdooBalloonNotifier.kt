package com.github.allepilli.odoodevelopmentplugin.notifications

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class OdooBalloonNotifier(
        private val project: Project,
) {
    fun notify(content: String, title: String? = null, type: NotificationType = NotificationType.INFORMATION) {
        val notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup("Odoo Balloon Group")

        val notification = if (title != null) notificationGroup.createNotification(title, content, type)
        else notificationGroup.createNotification(content, type)

        notification.notify(project)
    }
}