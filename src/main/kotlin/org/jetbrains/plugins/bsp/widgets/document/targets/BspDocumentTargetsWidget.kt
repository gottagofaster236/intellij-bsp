package org.jetbrains.plugins.bsp.widgets.document.targets

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import org.jetbrains.magicmetamodel.MagicMetaModel
import org.jetbrains.plugins.bsp.services.MagicMetaModelService

// move it
private const val ID = "BSPTargets"

private class BspDocumentTargetsWidget(project: Project) : EditorBasedStatusBarPopup(project, false) {

  private val magicMetaModelService = MagicMetaModelService.getInstance(project)

  override fun ID(): String = ID

  override fun getWidgetState(file: VirtualFile?): WidgetState =
    if (file == null) getInactiveWidgetState() else getActiveWidgetState()

  private fun getInactiveWidgetState(): WidgetState {
    val state = WidgetState(BspDocumentTargetsWidgetBundle.message("widget.tooltip.text.inactive"), "", false)
    state.icon = IconLoader.getIcon("icons/buildServerProtocol.svg")

    return state
  }

  private fun getActiveWidgetState(): WidgetState {
    val state = WidgetState(BspDocumentTargetsWidgetBundle.message("widget.tooltip.text.active"), "", true)
    state.icon = IconLoader.getIcon("icons/buildServerProtocol.svg")

    return state
  }

  override fun createPopup(context: DataContext): ListPopup {
    val group = calculatePopupGroup(context)
    val mnemonics = JBPopupFactory.ActionSelectionAid.MNEMONICS
    val title = BspDocumentTargetsWidgetBundle.message("widget.title")

    return JBPopupFactory.getInstance().createActionGroupPopup(title, group, context, mnemonics, true)
  }

  private fun calculatePopupGroup(context: DataContext): ActionGroup {
    val group = DefaultActionGroup()
    group.addSeparator(BspDocumentTargetsWidgetBundle.message("widget.loaded.target.separator.title"))

    val file = CommonDataKeys.VIRTUAL_FILE.getData(context)!!
    val documentDetails =
      magicMetaModelService.magicMetaModel.getTargetsDetailsForDocument(TextDocumentIdentifier(file.url))

    val loadedTarget = documentDetails.loadedTargetId
    if (loadedTarget != null) {
      group.addAction(Action(loadedTarget, magicMetaModelService.magicMetaModel))
    }

    group.addSeparator(BspDocumentTargetsWidgetBundle.message("widget.available.targets.to.load"))
    val availableTargets = documentDetails.notLoadedTargetsIds

    availableTargets
      .map { Action(it, magicMetaModelService.magicMetaModel) }
      .forEach(group::add)

    return group
  }

  override fun createInstance(project: Project): StatusBarWidget =
    BspDocumentTargetsWidget(project)

  private class Action(
    val target: BuildTargetIdentifier,
    private val magicMetaModel: MagicMetaModel
  ) : AnAction(target.uri) {
    override fun actionPerformed(e: AnActionEvent) {
      magicMetaModel.loadTarget(target)
    }
  }
}

class BSPStatusBarWidgetFactory : StatusBarWidgetFactory {

  override fun getId(): String = ID

  override fun getDisplayName(): String =
    BspDocumentTargetsWidgetBundle.message("widget.factory.display.name")

  // TODO
  override fun isAvailable(project: Project): Boolean =
    true

  override fun createWidget(project: Project): StatusBarWidget =
    BspDocumentTargetsWidget(project)

  override fun disposeWidget(widget: StatusBarWidget) =
    Disposer.dispose(widget)

  // TODO
  override fun canBeEnabledOn(statusBar: StatusBar): Boolean =
    true
}