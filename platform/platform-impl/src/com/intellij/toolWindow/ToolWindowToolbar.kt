// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.toolWindow

import com.intellij.openapi.actionSystem.ActionPlaces.TOOLWINDOW_TOOLBAR_BAR
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.AbstractDroppableStripe
import com.intellij.openapi.wm.impl.IdeRootPane
import com.intellij.openapi.wm.impl.LayoutData
import com.intellij.openapi.wm.impl.SquareStripeButton
import com.intellij.ui.ComponentUtil
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JComponent
import javax.swing.border.Border

internal abstract class ToolWindowToolbar : JBPanel<ToolWindowToolbar>() {
  lateinit var defaults: List<String>

  abstract val bottomStripe: StripeV2
  abstract val topStripe: StripeV2

  protected fun init() {
    layout = BorderLayout()
    isOpaque = true
    background = JBUI.CurrentTheme.ToolWindow.background()

    val topWrapper = JBPanel<JBPanel<*>>(BorderLayout()).apply {
      border = JBUI.Borders.customLineTop(getBorderColor())
    }
    border = createBorder()
    topStripe.background = JBUI.CurrentTheme.ToolWindow.background()
    bottomStripe.background = JBUI.CurrentTheme.ToolWindow.background()
    topWrapper.background = JBUI.CurrentTheme.ToolWindow.background()

    topWrapper.add(topStripe, BorderLayout.NORTH)
    add(topWrapper, BorderLayout.NORTH)
    add(bottomStripe, BorderLayout.SOUTH)
  }

  open fun createBorder():Border = JBUI.Borders.empty()
  open fun getBorderColor(): Color? = JBUI.CurrentTheme.ToolWindow.borderColor()

  abstract fun getStripeFor(anchor: ToolWindowAnchor): AbstractDroppableStripe

  fun getButtonFor(toolWindowId: String): StripeButtonManager? {
    return topStripe.getButtons().find { it.id == toolWindowId } ?: bottomStripe.getButtons().find { it.id == toolWindowId }
  }

  open fun getStripeFor(screenPoint: Point): AbstractDroppableStripe? {
    if (!isShowing) {
      return null
    }
    if (topStripe.containsPoint(screenPoint)) {
      return topStripe
    }
    if (bottomStripe.containsPoint(screenPoint)) {
      return bottomStripe
    }
    return null
  }

  fun removeStripeButton(toolWindow: ToolWindow, anchor: ToolWindowAnchor) {
    remove(getStripeFor(anchor), toolWindow)
  }

  fun hasButtons() = topStripe.getButtons().isNotEmpty() || bottomStripe.getButtons().isNotEmpty()

  fun reset() {
    topStripe.reset()
    bottomStripe.reset()
  }

  fun startDrag() {
    revalidate()
    repaint()
  }

  fun stopDrag() = startDrag()

  fun tryDroppingOnGap(data: LayoutData, gap: Int, dropRectangle: Rectangle, doLayout: () -> Unit) {
    val sideDistance = data.eachY + gap - dropRectangle.y + dropRectangle.height
    if (sideDistance > 0) {
      data.dragInsertPosition = -1
      data.dragToSide = false
      data.dragTargetChosen = true
      doLayout()
    }
  }

  companion object {
    fun updateButtons(panel: JComponent) {
      ComponentUtil.findComponentsOfType(panel, SquareStripeButton::class.java).forEach { it.update() }
      panel.revalidate()
      panel.repaint()
    }

    fun remove(panel: AbstractDroppableStripe, toolWindow: ToolWindow) {
      val component = panel.components.firstOrNull { it is SquareStripeButton && it.toolWindow.id == toolWindow.id } ?: return
      panel.remove(component)
      panel.revalidate()
      panel.repaint()
    }
  }

  open class ToolwindowActionToolbar(val panel: JComponent) : ActionToolbarImpl(TOOLWINDOW_TOOLBAR_BAR, DefaultActionGroup(), false) {
    override fun actionsUpdated(forced: Boolean, newVisibleActions: List<AnAction>) = updateButtons(panel)
  }

  internal class StripeV2(private val toolBar: ToolWindowToolbar,
                          paneId: String,
                          override val anchor: ToolWindowAnchor,
                          override val split: Boolean = false) : AbstractDroppableStripe(paneId, VerticalFlowLayout(0, 0)) {
    override val isNewStripes: Boolean
      get() = true

    override fun getDropToSide(): Boolean {
      if (split) {
        return true
      }
      val dropToSide = super.getDropToSide()
      if (dropToSide == null) {
        return false
      }
      return dropToSide
    }

    override fun containsPoint(screenPoint: Point): Boolean {
      if (anchor == ToolWindowAnchor.LEFT || anchor == ToolWindowAnchor.RIGHT) {
        if (!toolBar.isShowing) {
          val bounds = Rectangle(rootPane.locationOnScreen, rootPane.size)
          bounds.height /= 2

          val toolWindowWidth = getFirstVisibleToolWindowSize(true)

          if (anchor == ToolWindowAnchor.RIGHT) {
            bounds.x = bounds.x + bounds.width - toolWindowWidth
          }

          bounds.width = toolWindowWidth

          return bounds.contains(screenPoint)
        }

        val bounds = Rectangle(toolBar.locationOnScreen, toolBar.size)
        bounds.height /= 2

        val toolWindowWidth = getFirstVisibleToolWindowSize(true)

        bounds.width += toolWindowWidth
        if (anchor == ToolWindowAnchor.RIGHT) {
          bounds.x -= toolWindowWidth
        }
        return bounds.contains(screenPoint)
      }
      if (anchor == ToolWindowAnchor.BOTTOM) {
        val rootBounds = Rectangle(rootPane.locationOnScreen, rootPane.size)
        val toolWindowHeight = getFirstVisibleToolWindowSize(false)
        val bounds = Rectangle(rootBounds.x, rootBounds.y + rootBounds.height - toolWindowHeight - getStatusBarHeight(),
                               rootBounds.width / 2, toolWindowHeight)
        if (split) {
          bounds.x += bounds.width
        }
        return bounds.contains(screenPoint)
      }
      return super.containsPoint(screenPoint)
    }

    private fun getFirstVisibleToolWindowSize(width: Boolean): Int {
      for (button in getButtons()) {
        if (button.toolWindow.isVisible) {
          if (width) {
            return (rootPane.width * button.windowDescriptor.weight).toInt()
          }
          return (rootPane.height * button.windowDescriptor.weight).toInt()
        }
      }

      return JBUI.scale(350)
    }

    private fun getStatusBarHeight(): Int {
      val statusBar = WindowManager.getInstance().getStatusBar(this, null)
      if (statusBar != null) {
        val component = statusBar.component
        if (component != null && component.isVisible) {
          return component.height
        }
      }
      return 0
    }

    override fun getToolWindowDropAreaScreenBounds(): Rectangle {
      val size = toolBar.size

      if (anchor == ToolWindowAnchor.LEFT) {
        val locationOnScreen = toolBar.locationOnScreen
        return Rectangle(locationOnScreen.x + size.width, locationOnScreen.y, getFirstVisibleToolWindowSize(true), size.height)
      }
      if (anchor == ToolWindowAnchor.RIGHT) {
        val locationOnScreen = toolBar.locationOnScreen
        val toolWindowSize = getFirstVisibleToolWindowSize(true)
        return Rectangle(locationOnScreen.x  - toolWindowSize, locationOnScreen.y, toolWindowSize, size.height)
      }
      if (anchor == ToolWindowAnchor.BOTTOM) {
        val rootPane = (rootPane as IdeRootPane).getToolWindowPane()
        val rootBounds = Rectangle(rootPane.locationOnScreen, rootPane.size)
        val toolWindowHeight = getFirstVisibleToolWindowSize(false)
        return Rectangle(rootBounds.x, rootBounds.y + rootBounds.height - toolWindowHeight, rootBounds.width, toolWindowHeight)
      }
      return super.getToolWindowDropAreaScreenBounds()
    }

    override fun getButtonFor(toolWindowId: String) = toolBar.getButtonFor(toolWindowId)

    override fun tryDroppingOnGap(data: LayoutData, gap: Int, insertOrder: Int) {
      toolBar.tryDroppingOnGap(data, gap, dropRectangle) {
        layoutDragButton(data, gap)
      }
    }

    override fun toString() = "StripeNewUi(anchor=$anchor)"
  }
}