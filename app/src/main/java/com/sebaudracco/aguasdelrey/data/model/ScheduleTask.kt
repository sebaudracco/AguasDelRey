package com.sebaudracco.aguasdelrey.data.model

data class ScheduleTask (
    var id: String,
    var taskId: String,
    var addressDescription: String,
    var clientDescription: String,
    var inProgress: Boolean,
    var hasHours: Boolean,
    var idleTask: Boolean,
    var peopleCount: Int,
    var startTime: String,
    var progressive: Boolean,
    var lunch: Boolean
)