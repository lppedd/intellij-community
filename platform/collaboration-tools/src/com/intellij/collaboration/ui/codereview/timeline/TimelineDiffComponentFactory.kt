// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.collaboration.ui.codereview.timeline

import com.intellij.collaboration.messages.CollaborationToolsBundle
import com.intellij.collaboration.ui.CollaborationToolsUIUtil
import com.intellij.collaboration.ui.codereview.comment.RoundedPanel
import com.intellij.collaboration.ui.codereview.diff.DiffLineLocation
import com.intellij.collaboration.ui.util.ActivatableCoroutineScopeProvider
import com.intellij.collaboration.ui.util.bindChild
import com.intellij.collaboration.ui.util.bindVisibility
import com.intellij.diff.util.DiffDrawUtil
import com.intellij.diff.util.TextDiffType
import com.intellij.icons.AllIcons
import com.intellij.openapi.diff.impl.patch.PatchHunk
import com.intellij.openapi.diff.impl.patch.PatchHunkUtil
import com.intellij.openapi.diff.impl.patch.PatchLine
import com.intellij.openapi.diff.impl.patch.apply.GenericPatchApplier
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.LineNumberConverter
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.LineNumberConverterAdapter
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vcs.changes.patch.AppliedTextPatch
import com.intellij.openapi.vcs.changes.patch.tool.PatchChangeBuilder
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.SideBorder
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.panels.ListLayout
import com.intellij.util.PathUtil
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.InlineIconButton
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.awt.Color
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

object TimelineDiffComponentFactory {

  fun createDiffComponent(project: Project, editorFactory: EditorFactory,
                          patchHunk: PatchHunk,
                          anchor: DiffLineLocation?,
                          anchorStart: DiffLineLocation?): JComponent {
    val truncatedHunk = if (anchor == null) patchHunk else truncateHunk(patchHunk, anchor, anchorStart)

    val anchorLineIndex = anchor?.let { PatchHunkUtil.findHunkLineIndex(truncatedHunk, it) }

    if (truncatedHunk.lines.any { it.type != PatchLine.Type.CONTEXT }) {
      val appliedSplitHunks = GenericPatchApplier.SplitHunk.read(truncatedHunk).map {
        AppliedTextPatch.AppliedSplitPatchHunk(it, -1, -1, AppliedTextPatch.HunkStatus.NOT_APPLIED)
      }

      val builder = PatchChangeBuilder()
      builder.exec(appliedSplitHunks)

      val patchContent = builder.patchContent.removeSuffix("\n")

      return createDiffComponent(project, editorFactory, patchContent) { editor ->
        editor.gutter.apply {
          setLineNumberConverter(LineNumberConverterAdapter(builder.lineConvertor1.createConvertor()),
                                 LineNumberConverterAdapter(builder.lineConvertor2.createConvertor()))
        }

        builder.hunks.forEach { hunk ->
          DiffDrawUtil.createUnifiedChunkHighlighters(editor,
                                                      hunk.patchDeletionRange,
                                                      hunk.patchInsertionRange,
                                                      null)
        }
        anchorLineIndex?.let { highlightAnchorLine(editor, it) }
      }
    }
    else {
      val patchContent = truncatedHunk.text.removeSuffix("\n")

      return createDiffComponent(project, editorFactory, patchContent) { editor ->
        editor.gutter.apply {
          setLineNumberConverter(
            LineNumberConverter.Increasing { _, line -> line + truncatedHunk.startLineBefore },
            LineNumberConverter.Increasing { _, line -> line + truncatedHunk.startLineAfter }
          )
        }
        anchorLineIndex?.let { highlightAnchorLine(editor, it) }
      }
    }
  }

  private fun highlightAnchorLine(editor: EditorEx, line: Int) {
    DiffDrawUtil.createHighlighter(editor, line, line + 1, AnchorLine, false)
  }

  object AnchorLine : TextDiffType {
    override fun getName() = "Comment Anchor Line"

    override fun getColor(editor: Editor?): Color = JBColor.namedColor(
      "Review.Timeline.Thread.Diff.AnchorLine",
      JBColor(0xFBF1D1, 0x544B2D)
    )

    override fun getIgnoredColor(editor: Editor?) = getColor(editor)
    override fun getMarkerColor(editor: Editor?) = getColor(editor)
  }

  private const val DIFF_CONTEXT_SIZE = 3

  private fun truncateHunk(hunk: PatchHunk, anchor: DiffLineLocation, anchorStart: DiffLineLocation?): PatchHunk {
    if (hunk.lines.size <= DIFF_CONTEXT_SIZE + 1) return hunk

    val hunkWithoutStart = if (anchorStart != null && anchor != anchorStart) {
      truncateHunkBefore(hunk, anchorStart)
    }
    else {
      truncateHunkBefore(hunk, anchor)
    }
    return truncateHunkAfter(hunkWithoutStart, anchor)
  }

  private fun truncateHunkBefore(hunk: PatchHunk, location: DiffLineLocation): PatchHunk {
    val lines = hunk.lines
    if (lines.size <= DIFF_CONTEXT_SIZE + 1) return hunk
    val lineIdx = PatchHunkUtil.findHunkLineIndex(hunk, location) ?: return hunk
    val startIdx = lineIdx - DIFF_CONTEXT_SIZE
    if (startIdx <= 0) return hunk

    var startLineBefore: Int = hunk.startLineBefore
    var startLineAfter: Int = hunk.startLineAfter

    for (i in 0 until startIdx) {
      val line = lines[i]
      when (line.type) {
        PatchLine.Type.CONTEXT -> {
          startLineBefore++
          startLineAfter++
        }
        PatchLine.Type.ADD -> startLineAfter++
        PatchLine.Type.REMOVE -> startLineBefore++
      }
    }
    val truncatedLines = lines.subList(startIdx, lines.size)
    return PatchHunk(startLineBefore, hunk.endLineBefore, startLineAfter, hunk.endLineAfter).apply {
      for (line in truncatedLines) {
        addLine(line)
      }
    }
  }

  private fun truncateHunkAfter(hunk: PatchHunk, location: DiffLineLocation): PatchHunk {
    val lines = hunk.lines
    if (lines.size <= DIFF_CONTEXT_SIZE + 1) return hunk
    val lineIdx = PatchHunkUtil.findHunkLineIndex(hunk, location) ?: return hunk
    val endIdx = lineIdx + DIFF_CONTEXT_SIZE
    if (endIdx > lines.size - 1) return hunk

    var endLineBefore: Int = hunk.endLineBefore
    var endLineAfter: Int = hunk.endLineAfter

    for (i in lines.size - 1 downTo endIdx) {
      val line = lines[i]
      when (line.type) {
        PatchLine.Type.CONTEXT -> {
          endLineBefore--
          endLineAfter--
        }
        PatchLine.Type.ADD -> endLineAfter--
        PatchLine.Type.REMOVE -> endLineBefore--
      }
    }
    val truncatedLines = lines.subList(0, endIdx + 1)
    return PatchHunk(hunk.startLineBefore, endLineBefore, hunk.startLineAfter, endLineAfter).apply {
      for (line in truncatedLines) {
        addLine(line)
      }
    }
  }

  fun createDiffComponent(project: Project, editorFactory: EditorFactory,
                          text: CharSequence, modifyEditor: (EditorEx) -> Unit): JComponent =
    EditorHandlerPanel.create(editorFactory) { factory ->
      val editor = createSimpleDiffEditor(project, factory, text)
      modifyEditor(editor)
      editor
    }

  private fun createSimpleDiffEditor(project: Project, editorFactory: EditorFactory, text: CharSequence): EditorEx {
    return (editorFactory.createViewer(editorFactory.createDocument(text), project, EditorKind.DIFF) as EditorEx).apply {
      gutterComponentEx.setPaintBackground(false)

      setHorizontalScrollbarVisible(true)
      setVerticalScrollbarVisible(false)
      setCaretEnabled(false)
      isEmbeddedIntoDialogWrapper = true
      contentComponent.isOpaque = false

      setBorder(JBUI.Borders.empty())

      settings.apply {
        isCaretRowShown = false
        additionalLinesCount = 0
        additionalColumnsCount = 0
        isRightMarginShown = false
        setRightMargin(-1)
        isFoldingOutlineShown = false
        isIndentGuidesShown = false
        isVirtualSpace = false
        isWheelFontChangeEnabled = false
        isAdditionalPageAtBottom = false
        lineCursorWidth = 1
      }
    }
  }

  fun createDiffWithHeader(cs: CoroutineScope,
                           collapseVm: CollapsibleTimelineItemViewModel,
                           filePath: @NlsSafe String,
                           onFileNameClick: () -> Unit,
                           diffComponentFactory: (CoroutineScope) -> JComponent): JComponent {
    val expandCollapseButton = InlineIconButton(EmptyIcon.ICON_16).apply {
      cs.launch(start = CoroutineStart.UNDISPATCHED) {
        collapseVm.collapsed.collect { collapsed ->
          icon = if (collapsed) {
            AllIcons.General.ExpandComponent
          }
          else {
            AllIcons.General.CollapseComponent
          }
          hoveredIcon = if (collapsed) {
            AllIcons.General.ExpandComponentHover
          }
          else {
            AllIcons.General.CollapseComponentHover
          }

          actionListener = ActionListener {
            collapseVm.setCollapsed(!collapsed)
          }
        }
      }
      bindVisibility(cs, collapseVm.collapsible)
    }



    return RoundedPanel(ListLayout.vertical(0), 8).apply {
      isOpaque = false
      add(createFileNameComponent(filePath, expandCollapseButton, onFileNameClick))
      CollaborationToolsUIUtil.overrideUIDependentProperty(this) {
        background = EditorColorsManager.getInstance().globalScheme.defaultBackground
      }

      bindChild(cs, collapseVm.collapsed) { cs, collapsed ->
        if (collapsed) return@bindChild null
        diffComponentFactory(cs).apply {
          border = IdeBorderFactory.createBorder(SideBorder.TOP)
        }
      }
    }
  }

  @Deprecated("deprecated in favor of a reactive solution")
  fun wrapWithHeader(diffComponent: JComponent,
                     filePath: @NonNls String,
                     collapsibleState: StateFlow<Boolean>,
                     collapsedState: MutableStateFlow<Boolean>,
                     onFileClick: () -> Unit): JComponent {
    val scopeProvider = ActivatableCoroutineScopeProvider()

    val expandCollapseButton = InlineIconButton(EmptyIcon.ICON_16).apply {
      actionListener = ActionListener {
        collapsedState.update { !it }
      }
    }

    scopeProvider.launchInScope {
      expandCollapseButton.bindVisibility(this, collapsibleState)
    }

    scopeProvider.launchInScope {
      collapsedState.collect {
        expandCollapseButton.icon = if (it) {
          AllIcons.General.ExpandComponent
        }
        else {
          AllIcons.General.CollapseComponent
        }
        expandCollapseButton.hoveredIcon = if (it) {
          AllIcons.General.ExpandComponentHover
        }
        else {
          AllIcons.General.CollapseComponentHover
        }
        //TODO: tooltip?
      }
    }

    diffComponent.border = IdeBorderFactory.createBorder(SideBorder.TOP)

    scopeProvider.launchInScope {
      diffComponent.bindVisibility(this, collapsedState.map { !it })
    }

    return RoundedPanel(ListLayout.vertical(0), 8).apply {
      isOpaque = false
      add(createFileNameComponent(filePath, expandCollapseButton, onFileClick))
      add(diffComponent)
    }.also {
      scopeProvider.activateWith(it)
    }
  }

  private fun createFileNameComponent(filePath: String, expandCollapseButton: JComponent, onFileClick: () -> Unit): JComponent {
    val name = PathUtil.getFileName(filePath)
    val path = PathUtil.getParentPath(filePath)
    val fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(name)

    val nameLabel = LinkLabel<Unit>(name, fileType.icon) { _, _ ->
      onFileClick()
    }
    return JPanel(MigLayout(LC().insets("0").gridGap("5", "0").fill().noGrid())).apply {
      isOpaque = false
      border = JBUI.Borders.empty(10)

      add(nameLabel)

      if (!path.isBlank()) add(JLabel(path).apply {
        foreground = UIUtil.getContextHelpForeground()
      }, CC().minWidth("0"))

      add(expandCollapseButton, CC().hideMode(3).gapLeft("10:push"))
    }
  }
}