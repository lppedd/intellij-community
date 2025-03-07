// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.daemon.impl

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.ex.EditorMarkupModel
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import kotlin.math.max

private class DaemonFusReporter(private val project: Project) : DaemonCodeAnalyzer.DaemonListener {
  private var daemonStartTime = -1L

  override fun daemonStarting(fileEditors: Collection<FileEditor>) {
    daemonStartTime = System.currentTimeMillis()
  }

  override fun daemonFinished(fileEditors: Collection<FileEditor>) {
    val editor = fileEditors.filterIsInstance<TextEditor>().firstOrNull()?.editor
    val document = editor?.document

    if (document != null) {
      // Don't report 'finished' event in case of no changes in the document
      val lastReportedTimestamp = document.getUserData(lastReportedDaemonFinishedTimestamp)
      if (lastReportedTimestamp == document.modificationStamp) {
        return
      }
      document.putUserData(lastReportedDaemonFinishedTimestamp, document.modificationStamp)
    }

    val analyzer = (editor?.markupModel as? EditorMarkupModel)?.errorStripeRenderer as? TrafficLightRenderer
    val errorCounts = analyzer?.errorCounts
    val registrar = SeverityRegistrar.getSeverityRegistrar(project)
    val errorIndex = registrar.getSeverityIdx(HighlightSeverity.ERROR)
    val warningIndex = registrar.getSeverityIdx(HighlightSeverity.WARNING)
    val errorCount = errorCounts?.getOrNull(errorIndex) ?: -1
    val warningCount = errorCounts?.getOrNull(warningIndex) ?: -1
    val lines = if (document == null) -1 else roundToThreeMostSignificantBits(document.lineCount)
    val elapsedTime = System.currentTimeMillis() - daemonStartTime
    val fileType = document?.let { FileDocumentManager.getInstance().getFile(it)?.fileType }

    DaemonFusCollector.FINISHED.log(
      project,
      EventFields.DurationMs with elapsedTime,
      DaemonFusCollector.ERRORS with errorCount,
      DaemonFusCollector.WARNINGS with warningCount,
      DaemonFusCollector.LINES with lines,
      EventFields.FileType with fileType
    )
  }

  private fun roundToThreeMostSignificantBits(i: Int): Int {
    val norm = if (i == 0) 0 else max(i, 8)
    return norm and (Int.MIN_VALUE shr Integer.numberOfLeadingZeros(norm)+2)
  }

  companion object {
    val lastReportedDaemonFinishedTimestamp = Key<Long>("lastReportedDaemonFinishedTimestamp")
  }
}

class DaemonFusCollector : CounterUsagesCollector() {
  companion object {
    val GROUP = EventLogGroup("daemon", 2)
    val ERRORS = EventFields.Int("errors")
    val WARNINGS = EventFields.Int("warnings")
    val LINES = EventFields.Int("lines")
    val FINISHED = GROUP.registerVarargEvent("finished",
        EventFields.DurationMs, ERRORS, WARNINGS, LINES, EventFields.FileType)
  }

  override fun getGroup(): EventLogGroup {
    return GROUP
  }
}
