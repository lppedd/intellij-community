// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("ReplaceGetOrSet", "OVERRIDE_DEPRECATION", "ReplacePutWithAssignment", "LeakingThis")

package com.intellij.openapi.wm.impl.status

import com.intellij.diagnostic.runActivity
import com.intellij.ide.HelpTooltipManager
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.ui.UISettings
import com.intellij.internal.statistic.service.fus.collectors.StatusBarPopupShown
import com.intellij.internal.statistic.service.fus.collectors.StatusBarWidgetClicked
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.extensions.LoadingOrder.Orderable
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.progress.ModalTaskOwner
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.TaskInfo
import com.intellij.openapi.progress.runBlockingModal
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.BalloonHandler
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.Strings
import com.intellij.openapi.wm.*
import com.intellij.openapi.wm.StatusBarWidget.*
import com.intellij.openapi.wm.WidgetPresentation
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.openapi.wm.impl.ProjectFrameHelper
import com.intellij.openapi.wm.impl.status.TextPanel.WithIconAndArrows
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsActionGroup
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import com.intellij.openapi.wm.impl.welcomeScreen.cloneableProjects.CloneableProjectsService
import com.intellij.openapi.wm.impl.welcomeScreen.cloneableProjects.CloneableProjectsService.CloneProjectListener
import com.intellij.ui.*
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.awt.RelativeRectangle
import com.intellij.ui.border.name
import com.intellij.ui.popup.AbstractPopup
import com.intellij.ui.popup.NotificationPopup
import com.intellij.ui.popup.PopupState
import com.intellij.util.EventDispatcher
import com.intellij.util.childScope
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.awt.*
import java.awt.event.MouseEvent
import java.util.function.Consumer
import java.util.function.Supplier
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.event.HyperlinkListener

private const val UI_CLASS_ID = "IdeStatusBarUI"
private val WIDGET_ID = Key.create<String>("STATUS_BAR_WIDGET_ID")
private val MIN_ICON_HEIGHT = JBUI.scale(18 + 1 + 1)

open class IdeStatusBarImpl internal constructor(
  private val frameHelper: ProjectFrameHelper,
  addToolWindowWidget: Boolean,
  private var editorProvider: () -> FileEditor? = createDefaultEditorProvider(frameHelper),
) : JComponent(), Accessible, StatusBarEx, DataProvider {
  private val infoAndProgressPanel: InfoAndProgressPanel

  internal enum class WidgetEffect {
    HOVER,
    PRESSED
  }

  private val widgetMap = LinkedHashMap<String, WidgetBean>()
  private var leftPanel: JPanel? = null

  private val rightPanelLayout = GridBagLayout()
  private val rightPanel: JPanel

  private val centerPanel: JPanel
  private var effectComponent: JComponent? = null
  private var info: @NlsContexts.StatusBarText String? = null

  private val initialEditorProvider = editorProvider

  private val children = LinkedHashSet<IdeStatusBarImpl>()
  private val listeners = EventDispatcher.create(StatusBarListener::class.java)

  companion object {
    internal val HOVERED_WIDGET_ID = DataKey.create<String>("HOVERED_WIDGET_ID")
    internal val WIDGET_EFFECT_KEY = Key.create<WidgetEffect>("TextPanel.widgetEffect")

    const val NAVBAR_WIDGET_KEY = "NavBar"
  }

  override fun findChild(c: Component): StatusBar {
    var parent: Component? = c
    while (parent != null) {
      if (parent is IdeFrame) {
        return parent.statusBar!!
      }
      parent = parent.parent
    }
    return this
  }

  private fun updateChildren(consumer: Consumer<IdeStatusBarImpl>) {
    for (child in children) {
      consumer.accept(child)
    }
  }

  override fun createChild(frame: IdeFrame, editorProvider: () -> FileEditor?): StatusBar {
    EDT.assertIsEdt()
    val bar = IdeStatusBarImpl(frameHelper = frameHelper, addToolWindowWidget = false, editorProvider = editorProvider)
    bar.isVisible = isVisible
    children.add(bar)
    Disposer.register(frameHelper) { children.remove(bar) }
    for (eachBean in widgetMap.values) {
      if (eachBean.widget is Multiframe) {
        bar.addWidget(widget = eachBean.widget.copy(), position = eachBean.position, anchor = eachBean.order)
      }
    }
    return bar
  }

  override val component: JComponent?
    get() = this

  init {
    layout = BorderLayout()
    isOpaque = true
    border = (if (ExperimentalUI.isNewUI()) {
      CompoundBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.StatusBar.BORDER_COLOR, 1, 0, 0, 0), JBUI.Borders.empty(0, 10))
    }
    else {
      JBUI.Borders.empty(1, 0, 0, 6)
    })

    centerPanel = StatusBarPanel(BorderLayout())
    centerPanel.isOpaque = false
    centerPanel.border = if (ExperimentalUI.isNewUI()) JBUI.Borders.empty() else JBUI.Borders.empty(0, 1)
    add(centerPanel, BorderLayout.CENTER)

    rightPanel = StatusBarPanel(rightPanelLayout)
    rightPanel.isOpaque = false
    rightPanel.border = JBUI.Borders.emptyLeft(1)
    add(rightPanel, BorderLayout.EAST)

    infoAndProgressPanel = InfoAndProgressPanel(UISettings.shadowInstance)
    ClientProperty.put(infoAndProgressPanel, WIDGET_ID, infoAndProgressPanel.ID())
    centerPanel.add(infoAndProgressPanel)
    widgetMap.put(infoAndProgressPanel.ID(), WidgetBean(widget = infoAndProgressPanel,
                                                        position = Position.CENTER,
                                                        component = infoAndProgressPanel,
                                                        order = LoadingOrder.ANY))
    Disposer.register(frameHelper, infoAndProgressPanel)

    registerCloneTasks()

    if (addToolWindowWidget) {
      val toolWindowWidget = ToolWindowsWidget(frameHelper, this)
      val toolWindowWidgetComponent = wrapCustomStatusBarWidget(toolWindowWidget)
      widgetMap.put(toolWindowWidget.ID(), WidgetBean(widget = toolWindowWidget,
                                                      position = Position.LEFT,
                                                      component = toolWindowWidgetComponent,
                                                      order = LoadingOrder.ANY))
      Disposer.register(frameHelper, toolWindowWidget)
      toolWindowWidgetComponent.border = if (SystemInfoRt.isMac) JBUI.Borders.empty(2, 0, 2, 4) else JBUI.Borders.empty()
      leftPanel().add(toolWindowWidgetComponent)
    }

    updateUI()

    enableEvents(AWTEvent.MOUSE_EVENT_MASK)
    enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK)
    IdeEventQueue.getInstance().addDispatcher({ e -> if (e is MouseEvent) dispatchMouseEvent(e) else false }, frameHelper)
  }

  override fun getPreferredSize(): Dimension {
    val size = super.getPreferredSize()!!
    val insets = insets
    val minHeight = insets.top + insets.bottom + MIN_ICON_HEIGHT
    return Dimension(size.width, size.height.coerceAtLeast(minHeight))
  }

  override fun getData(dataId: String): Any? {
    return when {
      CommonDataKeys.PROJECT.`is`(dataId) -> project
      PlatformDataKeys.STATUS_BAR.`is`(dataId) -> this
      HOVERED_WIDGET_ID.`is`(dataId) -> ClientProperty.get(effectComponent, WIDGET_ID)
      else -> null
    }
  }

  override fun setVisible(aFlag: Boolean) {
    super.setVisible(aFlag)

    for (child in children) {
      child.isVisible = aFlag
    }
  }

  override fun addWidget(widget: StatusBarWidget) {
    addWidget(widget, Position.RIGHT, LoadingOrder.ANY)
  }

  override fun addWidget(widget: StatusBarWidget, anchor: String) {
    val order = StatusBarWidgetsManager.anchorToOrder(anchor)
    EdtInvocationManager.invokeLaterIfNeeded { addWidget(widget, Position.RIGHT, order) }
  }

  override fun addWidget(widget: StatusBarWidget, parentDisposable: Disposable) {
    EdtInvocationManager.invokeLaterIfNeeded { addWidget(widget, Position.RIGHT, LoadingOrder.ANY) }
    val id = widget.ID()
    Disposer.register(parentDisposable) { removeWidget(id) }
  }

  override fun addWidget(widget: StatusBarWidget, anchor: String, parentDisposable: Disposable) {
    val order = StatusBarWidgetsManager.anchorToOrder(anchor)
    EdtInvocationManager.invokeLaterIfNeeded {
      addWidget(widget = widget, position = Position.RIGHT, anchor = order)
      val id = widget.ID()
      Disposer.register(parentDisposable) { removeWidget(id) }
    }
  }

  @ApiStatus.Experimental
  @RequiresEdt
  fun setCentralWidget(id: String, component: JComponent) {
    val widget = object : StatusBarWidget {
      override fun ID(): String = id
    }
    widgetMap.put(id, WidgetBean(widget = widget, position = Position.CENTER, component = component, order = LoadingOrder.ANY))
    infoAndProgressPanel.setCentralComponent(component)
    infoAndProgressPanel.revalidate()
  }

  /**
   * Adds widget to the left side of the status bar.
   * Please note there is no hover effect when the mouse is over the widget.
   * Use [addWidget] to add widget to the right side of the status bar, in this case the hover effect is on.
   * @param widget widget to add
   */
  internal suspend fun addWidgetToLeft(widget: StatusBarWidget) {
    withContext(Dispatchers.EDT) {
      addWidget(widget, Position.LEFT, LoadingOrder.ANY)
    }
  }

  @RequiresEdt
  @ApiStatus.Internal
  fun addWidget(widget: StatusBarWidget, anchor: LoadingOrder) {
    addWidget(widget, Position.RIGHT, anchor)
  }

  internal suspend fun init(project: Project, extraItems: List<kotlin.Pair<StatusBarWidget, LoadingOrder>> = emptyList()) {
    val service = project.service<StatusBarWidgetsManager>()
    val items = runActivity("status bar pre-init") {
      service.init()
    }
    runActivity("status bar init") {
      doInit(widgets = items + extraItems, parentDisposable = service)
    }
  }

  private suspend fun doInit(widgets: List<kotlin.Pair<StatusBarWidget, LoadingOrder>>, parentDisposable: Disposable) {
    val items: List<WidgetBean> = runActivity("status bar widget creating") {
      widgets.map { (widget, anchor) ->
        val component = withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
          val component = wrap(widget)
          if (component is StatusBarWidgetWrapper) {
            component.beforeUpdate()
          }
          component
        }
        val item = WidgetBean(widget = widget, position = Position.RIGHT, component = component, order = anchor)
        widget.install(this)
        Disposer.register(parentDisposable, widget)
        item
      }
    }

    withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
      runActivity("status bar widget adding") {
        for (item in items) {
          widgetMap.put(item.widget.ID(), item)
        }

        val panel = rightPanel
        for (item in items) {
          panel.add(item.component)
        }

        sortRightWidgets()
      }
    }

    if (listeners.hasListeners()) {
      withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
        for (item in items) {
          fireWidgetAdded(widget = item.widget, anchor = item.anchor)
        }
      }
    }

    withContext(Dispatchers.EDT) {
      PopupHandler.installPopupMenu(this@IdeStatusBarImpl, StatusBarWidgetsActionGroup.GROUP_ID, ActionPlaces.STATUS_BAR_PLACE)
    }
  }

  private fun sortRightWidgets() {
    val sorted = mutableListOf<Orderable>()
    widgetMap.values.filterTo(sorted) { it.position == Position.RIGHT }

    // inject not available extension to make sort correct —
    // e.g. `after PowerSaveMode` must work even if `PowerSaveMode` widget is disabled,
    // because `PowerSaveMode` can specify something like `after Encoding`
    StatusBarWidgetFactory.EP_NAME.filterableLazySequence()
      .filter { !widgetMap.containsKey(it.id) }
      .mapTo(sorted) {
        object : Orderable {
          override val orderId: String?
            get() = it.id
          override val order: LoadingOrder
            get() = it.order
        }
      }

    LoadingOrder.sort(sorted)
    for ((index, item) in sorted.withIndex()) {
      rightPanelLayout.setConstraints((item as? WidgetBean ?: continue).component, GridBagConstraints().apply {
        gridx = index
        gridy = 0
        fill = GridBagConstraints.VERTICAL
        weighty = 1.0
        weightx = 1.0
        anchor = GridBagConstraints.PAGE_END
      })
    }
  }

  @RequiresEdt
  private fun addWidget(widget: StatusBarWidget, position: Position, anchor: LoadingOrder) {
    val component = wrap(widget)

    widgetMap.put(widget.ID(), WidgetBean(widget = widget, position = position, component = component, order = anchor))
    Disposer.register(frameHelper, widget)

    widget.install(this)

    val panel = getTargetPanel(position)
    if (position == Position.LEFT && panel.componentCount == 0) {
      component.border = if (SystemInfoRt.isMac) JBUI.Borders.empty(2, 0, 2, 4) else JBUI.Borders.empty()
    }
    if (component is StatusBarWidgetWrapper) {
      component.beforeUpdate()
    }

    panel.add(component)
    if (position == Position.RIGHT) {
      sortRightWidgets()
    }
    panel.revalidate()

    if (widget is Multiframe) {
      updateChildren { child -> child.addWidget(widget = widget.copy(), position = position, anchor = anchor) }
    }

    fireWidgetAdded(widget = widget, anchor = anchor.toString())
  }

  private fun getTargetPanel(position: Position): JPanel {
    return when (position) {
      Position.RIGHT -> rightPanel
      Position.LEFT -> leftPanel()
      else -> centerPanel
    }
  }

  private fun leftPanel(): JPanel {
    var leftPanel = leftPanel
    if (leftPanel == null) {
      leftPanel = JPanel()
      this.leftPanel = leftPanel
      leftPanel.border = JBUI.Borders.empty(0, 4, 0, 1)
      leftPanel.layout = BoxLayout(leftPanel, BoxLayout.X_AXIS)
      leftPanel.isOpaque = false
      add(leftPanel, BorderLayout.WEST)
      this.leftPanel = leftPanel
    }
    return leftPanel
  }

  override fun setInfo(s: String?) {
    setInfo(s, null)
  }

  override fun setInfo(s: @Nls String?, requestor: String?) {
    UIUtil.invokeLaterIfNeeded {
      info = infoAndProgressPanel.setText(s, requestor)
    }
  }

  override fun getInfo(): @NlsContexts.StatusBarText String? = info

  override fun addProgress(indicator: ProgressIndicatorEx, info: TaskInfo) {
    infoAndProgressPanel.addProgress(indicator, info)
  }

  override fun getBackgroundProcesses(): List<Pair<TaskInfo, ProgressIndicator>> = infoAndProgressPanel.backgroundProcesses

  override fun setProcessWindowOpen(open: Boolean) {
    infoAndProgressPanel.isProcessWindowOpen = open
  }

  override fun isProcessWindowOpen(): Boolean = infoAndProgressPanel.isProcessWindowOpen

  override fun startRefreshIndication(tooltipText: @NlsContexts.Tooltip String?) {
    infoAndProgressPanel.setRefreshVisible(tooltipText)
    updateChildren { it.startRefreshIndication(tooltipText) }
  }

  override fun stopRefreshIndication() {
    infoAndProgressPanel.setRefreshHidden()
    updateChildren(IdeStatusBarImpl::stopRefreshIndication)
  }

  override fun notifyProgressByBalloon(type: MessageType, htmlBody: @NlsContexts.PopupContent String): BalloonHandler {
    return notifyProgressByBalloon(type = type, htmlBody = htmlBody, icon = null, listener = null)
  }

  override fun notifyProgressByBalloon(type: MessageType,
                                       htmlBody: @NlsContexts.PopupContent String,
                                       icon: Icon?,
                                       listener: HyperlinkListener?): BalloonHandler {
    return infoAndProgressPanel.notifyByBalloon(type, htmlBody, icon, listener)
  }

  override fun fireNotificationPopup(content: JComponent, backgroundColor: Color?) {
    NotificationPopup(this, content, backgroundColor)
  }

  private fun applyWidgetEffect(component: JComponent?, widgetEffect: WidgetEffect?) {
    if (effectComponent === component &&
        (effectComponent == null || ClientProperty.get(effectComponent, WIDGET_EFFECT_KEY) == widgetEffect)) {
      return
    }

    if (effectComponent != null) {
      ClientProperty.put(effectComponent!!, WIDGET_EFFECT_KEY, null)
      repaint(RelativeRectangle(effectComponent).getRectangleOn(this))
    }

    effectComponent = component
    val effectComponent = effectComponent ?: return
    // widgets shall not be opaque, as it may conflict with a background image, the following code can be dropped in the future
    effectComponent.background = null
    ClientProperty.put(effectComponent, WIDGET_EFFECT_KEY, widgetEffect)
    if (effectComponent.isEnabled && widgetEffect != null) {
      effectComponent.background = if (widgetEffect == WidgetEffect.HOVER) {
        JBUI.CurrentTheme.StatusBar.Widget.HOVER_BACKGROUND
      }
      else {
        JBUI.CurrentTheme.StatusBar.Widget.PRESSED_BACKGROUND
      }
    }
    repaint(RelativeRectangle(effectComponent).getRectangleOn(this))
  }

  private fun paintWidgetEffectBackground(g: Graphics) {
    val effectComponent = effectComponent ?: return
    if (!effectComponent.isEnabled || !UIUtil.isAncestor(this, effectComponent) || effectComponent is MemoryUsagePanel) {
      return
    }

    val bounds = effectComponent.bounds
    val point = RelativePoint(effectComponent.parent, bounds.location).getPoint(this)
    val widgetEffect = ClientProperty.get(effectComponent, WIDGET_EFFECT_KEY)
    val bg = if (widgetEffect == WidgetEffect.PRESSED) {
      JBUI.CurrentTheme.StatusBar.Widget.PRESSED_BACKGROUND
    }
    else {
      JBUI.CurrentTheme.StatusBar.Widget.HOVER_BACKGROUND
    }
    if (!ExperimentalUI.isNewUI() && getUI() is StatusBarUI) {
      point.y += StatusBarUI.BORDER_WIDTH.get()
      bounds.height -= StatusBarUI.BORDER_WIDTH.get()
    }
    g.color = bg
    g.fillRect(point.x, point.y, bounds.width, bounds.height)
  }

  override fun paintChildren(g: Graphics) {
    paintWidgetEffectBackground(g)
    super.paintChildren(g)
  }

  private fun dispatchMouseEvent(e: MouseEvent): Boolean {
    val rightPanel = rightPanel.takeIf { it.isVisible } ?: return false
    val component = e.component ?: return false
    if (ComponentUtil.getWindow(frameHelper.frame) !== ComponentUtil.getWindow(component)) {
      applyWidgetEffect(null, null)
      return false
    }

    val point = SwingUtilities.convertPoint(component, e.point, rightPanel)
    val widget = getVisibleChildAt(rightPanel, point)
    if (e.clickCount == 0 || e.id == MouseEvent.MOUSE_RELEASED) {
      applyWidgetEffect(if (widget !== rightPanel) widget else null, WidgetEffect.HOVER)
    }
    else if (e.clickCount == 1 && e.id == MouseEvent.MOUSE_PRESSED) {
      applyWidgetEffect(if (widget !== rightPanel) widget else null, WidgetEffect.PRESSED)
    }

    if (e.isConsumed || widget == null) {
      return false
    }

    if (e.isPopupTrigger && (e.id == MouseEvent.MOUSE_PRESSED || e.id == MouseEvent.MOUSE_RELEASED)) {
      val project = project
      if (project != null) {
        val actionManager = ActionManager.getInstance()
        val group = actionManager.getAction(StatusBarWidgetsActionGroup.GROUP_ID) as? ActionGroup
        if (group != null) {
          val menu = actionManager.createActionPopupMenu(ActionPlaces.STATUS_BAR_PLACE, group)
          menu.setTargetComponent(this)
          menu.component.show(rightPanel, point.x, point.y)
          e.consume()
          return true
        }
      }
    }
    return false
  }

  /**
   * Unlike [Container.getComponentAt] will not return invisible child.
   * Unlike [Container.findComponentAt] or [SwingUtilities.getDeepestComponentAt] will not search deeper.
   */
  private fun getVisibleChildAt(component: JComponent, point: Point): JComponent? {
    if (component.isVisible && component.contains(point)) {
      return component.components.find { child ->
        child.isVisible && child.contains(point.x - child.x, point.y - child.y)
      } as? JComponent
    }
    return null
  }

  override fun getUIClassID(): String = UI_CLASS_ID

  override fun updateUI() {
    if (UIManager.get(UI_CLASS_ID) != null) {
      setUI(UIManager.getUI(this))
    }
    else {
      setUI(StatusBarUI())
    }
    GuiUtils.iterateChildren(this, { c ->
      if (c is JComponent) {
        val newBorder = when (c.border?.name) {
          JBUI.CurrentTheme.StatusBar.Widget.borderName() -> JBUI.CurrentTheme.StatusBar.Widget.border()
          JBUI.CurrentTheme.StatusBar.Widget.iconBorderName() -> JBUI.CurrentTheme.StatusBar.Widget.iconBorder()
          else -> null
        }
        if (newBorder != null) {
          c.border = newBorder
        }
      }
    })
  }

  override fun getComponentGraphics(g: Graphics): Graphics {
    return JBSwingUtilities.runGlobalCGTransform(this, super.getComponentGraphics(g))
  }

  override fun removeWidget(id: String) {
    EdtInvocationManager.invokeLaterIfNeeded {
      val bean = widgetMap.remove(id)
      if (bean != null) {
        effectComponent?.let {
          if (UIUtil.isAncestor(bean.component, it)) {
            ClientProperty.put(it, WIDGET_EFFECT_KEY, null)
            effectComponent = null
          }
        }

        val targetPanel = getTargetPanel(bean.position)
        targetPanel.remove(bean.component)
        targetPanel.revalidate()
        Disposer.dispose(bean.widget)
        fireWidgetRemoved(id)
      }
      updateChildren { it.removeWidget(id) }
    }
  }

  override fun updateWidget(id: String) {
    EdtInvocationManager.invokeLaterIfNeeded {
      val widgetComponent = getWidgetComponent(id)
      if (widgetComponent != null) {
        if (widgetComponent is StatusBarWidgetWrapper) {
          widgetComponent.beforeUpdate()
        }
        widgetComponent.repaint()
        fireWidgetUpdated(id)
      }
      updateChildren { it.updateWidget(id) }
    }
  }

  override fun getWidget(id: String): StatusBarWidget? = widgetMap.get(id)?.widget

  override val allWidgets: Collection<StatusBarWidget>?
    get() = widgetMap.values.map { it.widget }

  override fun getWidgetAnchor(id: String): @NonNls String? = widgetMap.get(id)?.anchor

  //todo: make private after removing all external usages
  @ApiStatus.Internal
  fun getWidgetComponent(id: String): JComponent? = widgetMap.get(id)?.component

  override val project: Project?
    get() = frameHelper.project

  override val currentEditor: () -> FileEditor?
    get() = editorProvider

  @ApiStatus.Internal
  fun setEditorProvider(provider: () -> FileEditor?) {
    editorProvider = provider
  }

  @ApiStatus.Internal
  fun resetEditorProvider() {
    editorProvider = initialEditorProvider
  }

  override fun getAccessibleContext(): AccessibleContext {
    if (accessibleContext == null) {
      accessibleContext = AccessibleIdeStatusBarImpl()
    }
    return accessibleContext
  }

  override fun addListener(listener: StatusBarListener, parentDisposable: Disposable) {
    listeners.addListener(listener, parentDisposable)
  }

  private fun fireWidgetAdded(widget: StatusBarWidget, anchor: @NonNls String?) {
    listeners.multicaster.widgetAdded(widget, anchor)
  }

  private fun fireWidgetUpdated(id: @NonNls String) {
    listeners.multicaster.widgetUpdated(id)
  }

  private fun fireWidgetRemoved(id: @NonNls String) {
    listeners.multicaster.widgetRemoved(id)
  }

  private fun registerCloneTasks() {
    CloneableProjectsService.getInstance()
      .collectCloneableProjects()
      .map { it.cloneableProject }
      .forEach { addProgress(indicator = it.progressIndicator, info = it.cloneTaskInfo) }
    ApplicationManager.getApplication().messageBus.connect(frameHelper)
      .subscribe(CloneableProjectsService.TOPIC, object : CloneProjectListener {
        override fun onCloneAdded(progressIndicator: ProgressIndicatorEx, taskInfo: TaskInfo) {
          addProgress(progressIndicator, taskInfo)
        }
      })
  }

  protected inner class AccessibleIdeStatusBarImpl : AccessibleJComponent() {
    override fun getAccessibleRole(): AccessibleRole = AccessibleRole.PANEL
  }
}

private enum class Position {
  LEFT,
  RIGHT,
  CENTER
}

private class WidgetBean(
  @JvmField val widget: StatusBarWidget,
  @JvmField val position: Position,
  @JvmField val component: JComponent,
  override val order: LoadingOrder,
) : Orderable {
  val anchor: String
    get() = order.toString()

  override val orderId: String
    get() = widget.ID()

  override fun toString(): String = "Widget(id=$orderId, order=$order, position=$position)"
}

@RequiresEdt
internal fun createComponentByWidgetPresentation(presentation: WidgetPresentation, project: Project, scope: CoroutineScope): JComponent {
  val toolTipTextSupplier = { runBlockingModal(project, title = "") { presentation.getTooltipText() } }
  return when (presentation) {
    is TextWidgetPresentation -> {
      val panel = TextPanel(toolTipTextSupplier)
      panel.setTextAlignment(presentation.alignment)
      panel.border = JBUI.CurrentTheme.StatusBar.Widget.border()
      configurePresentationComponent(presentation, panel)

      scope.launch {
        presentation.text()
          .distinctUntilChanged()
          .collectLatest { text ->
            withContext(Dispatchers.EDT) {
              panel.isVisible = !text.isNullOrEmpty()
              panel.text = text
            }
          }
      }
      panel
    }
    is IconWidgetPresentation -> {
      val panel = WithIconAndArrows(toolTipTextSupplier)
      panel.border = JBUI.CurrentTheme.StatusBar.Widget.iconBorder()
      configurePresentationComponent(presentation, panel)

      scope.launch {
        presentation.icon()
          .distinctUntilChanged()
          .collectLatest { icon ->
            withContext(Dispatchers.EDT) {
              panel.icon = icon
              panel.isVisible = icon != null
              panel.repaint()
            }
          }
      }
      panel
    }
    else -> throw IllegalArgumentException("Unable to find a wrapper for presentation: ${presentation.javaClass.simpleName}")
  }
}

@RequiresEdt
private fun createComponentByWidgetPresentation(widget: StatusBarWidget): JComponent {
  val presentation = widget.getPresentation() ?: throw IllegalStateException("Widget $widget getPresentation() method must not return null")
  return when (presentation) {
    is IconPresentation -> IconPresentationComponent(presentation)
    is TextPresentation -> TextPresentationComponent(presentation)
    is MultipleTextValuesPresentation -> MultipleTextValues(presentation)
    else -> throw IllegalArgumentException("Unable to find a wrapper for presentation: ${presentation.javaClass.simpleName}")
  }
}

private fun configurePresentationComponent(presentation: WidgetPresentation, panel: JComponent) {
  presentation.getClickConsumer()?.let {
    StatusBarWidgetClickListener(it).installOn(panel, true)
  }
  ClientProperty.put(panel, HelpTooltipManager.SHORTCUT_PROPERTY,
                     Supplier { runBlockingModal(ModalTaskOwner.component(panel), title = "") { presentation.getShortcutText() } })
}

private fun wrap(widget: StatusBarWidget): JComponent {
  val result = if (widget is CustomStatusBarWidget) {
    return wrapCustomStatusBarWidget(widget)
  }
  else {
    createComponentByWidgetPresentation(widget)
  }
  ClientProperty.put(result, WIDGET_ID, widget.ID())
  result.putClientProperty(UIUtil.CENTER_TOOLTIP_DEFAULT, true)
  return result
}

private fun wrapCustomStatusBarWidget(widget: CustomStatusBarWidget): JComponent {
  val component = widget.component
  if (component.border == null) {
    component.border = if (widget is IconLikeCustomStatusBarWidget) {
      JBUI.CurrentTheme.StatusBar.Widget.iconBorder()
    }
    else {
      JBUI.CurrentTheme.StatusBar.Widget.border()
    }
  }

  // wrap with a panel, so it will fill the entire status bar height
  val result = if (component is JLabel) {
    val panel = JPanel(BorderLayout())
    panel.add(component, BorderLayout.CENTER)
    panel.isOpaque = false
    panel
  }
  else {
    component
  }
  return result
}

private fun createDefaultEditorProvider(frameHelper: ProjectFrameHelper): () -> FileEditor? {
  return p@{
    (frameHelper.project ?: return@p null).service<StatusBarWidgetsManager>().dataContext.currentFileEditor.value
  }
}

private class IconPresentationComponent(private val presentation: IconPresentation) : WithIconAndArrows(presentation::getTooltipText),
                                                                                      StatusBarWidgetWrapper {
  init {
    setTextAlignment(CENTER_ALIGNMENT)
    border = JBUI.CurrentTheme.StatusBar.Widget.iconBorder()
    presentation.getClickConsumer()?.let { clickConsumer ->
      StatusBarWidgetClickListener(clickConsumer = clickConsumer::consume).installOn(this, true)
    }
    ClientProperty.put(this, HelpTooltipManager.SHORTCUT_PROPERTY, Supplier(presentation::getShortcutText))
  }

  override fun beforeUpdate() {
    icon = presentation.getIcon()
    isVisible = hasIcon()
  }
}

private class TextPresentationComponent(
  private val presentation: TextPresentation,
) : TextPanel(presentation::getTooltipText), StatusBarWidgetWrapper {
  init {
    setTextAlignment(presentation.getAlignment())
    border = JBUI.CurrentTheme.StatusBar.Widget.border()
    presentation.getClickConsumer()?.let { clickConsumer ->
      StatusBarWidgetClickListener(clickConsumer::consume).installOn(this, true)
    }
    ClientProperty.put(this, HelpTooltipManager.SHORTCUT_PROPERTY, Supplier(presentation::getShortcutText))
  }

  override fun beforeUpdate() {
    text = presentation.getText()
    isVisible = text != null
  }
}

private class MultipleTextValues(private val presentation: MultipleTextValuesPresentation) : WithIconAndArrows(presentation::getTooltipText), StatusBarWidgetWrapper {
  init {
    isVisible = !presentation.getSelectedValue().isNullOrEmpty()
    setTextAlignment(CENTER_ALIGNMENT)
    border = JBUI.CurrentTheme.StatusBar.Widget.border()
    object : ClickListener() {
      val myPopupState = PopupState.forPopup()

      override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
        if (myPopupState.isRecentlyHidden) {
          // do not show new popup
          return false
        }

        val popup = presentation.getPopup() ?: return false
        StatusBarPopupShown.log(presentation::class.java)
        val dimension = getSizeFor(popup)
        val at = Point(0, -dimension.height)
        myPopupState.prepareToShow(popup)
        popup.show(RelativePoint(event.component, at))
        return true
      }

      private fun getSizeFor(popup: JBPopup): Dimension {
        return if (popup is AbstractPopup) popup.sizeForPositioning else popup.content.preferredSize
      }
    }.installOn(this, true)

    ClientProperty.put(this, HelpTooltipManager.SHORTCUT_PROPERTY, Supplier(presentation::getShortcutText))
  }

  override fun beforeUpdate() {
    val value = presentation.getSelectedValue()
    text = value
    icon = presentation.getIcon()
    isVisible = !Strings.isEmpty(value)
  }
}

private class StatusBarWidgetClickListener(private val clickConsumer: (MouseEvent) -> Unit) : ClickListener() {
  override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
    if (!e.isPopupTrigger && MouseEvent.BUTTON1 == e.button) {
      StatusBarWidgetClicked.log(clickConsumer.javaClass)
      clickConsumer(e)
    }
    return true
  }
}

private interface StatusBarWidgetWrapper {
  fun beforeUpdate()
}

internal fun adaptV2Widget(id: String,
                           dataContext: WidgetPresentationDataContext,
                           presentationFactory: (CoroutineScope) -> WidgetPresentation): StatusBarWidget {
  return object : StatusBarWidget, CustomStatusBarWidget {
    @Suppress("DEPRECATION")
    private val scope = dataContext.project.coroutineScope.childScope()

    override fun ID(): String = id

    override fun getComponent(): JComponent {
      return createComponentByWidgetPresentation(presentation = presentationFactory(scope), project = dataContext.project, scope = scope)
    }

    override fun dispose() {
      scope.cancel()
    }
  }
}

private class StatusBarPanel(layout: LayoutManager) : JPanel(layout) {

  init {
    updateFont()
  }

  override fun updateUI() {
    super.updateUI()
    updateFont()
  }

  private fun updateFont() {
    font = JBUI.CurrentTheme.StatusBar.font()
  }

}
