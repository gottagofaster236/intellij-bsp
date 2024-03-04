package org.jetbrains.plugins.bsp.ui.widgets.tool.window.utils

import com.intellij.icons.AllIcons
import com.intellij.ui.components.fields.ExtendableTextComponent
import javax.swing.Icon
import javax.swing.JComponent

public sealed class TextComponentExtension(
  private val tooltip: String?,
  private val beforeText: Boolean,
) : ExtendableTextComponent.Extension {
  override fun isIconBeforeText(): Boolean = beforeText

  override fun getTooltip(): String? = tooltip

  public class Indicator(
    private val trueIcon: Icon,
    private val falseIcon: Icon,
    private val predicate: () -> Boolean,
    tooltip: String? = null,
    beforeText: Boolean = false,
  ) : TextComponentExtension(tooltip, beforeText) {
    override fun getIcon(hovered: Boolean): Icon =
      if (predicate()) trueIcon else falseIcon

    override fun getActionOnClick(): Runnable? = null

    override fun isSelected(): Boolean = false
  }

  public class Switch(
    private val icon: Icon,
    private val valueGetter: () -> Boolean,
    private val valueSetter: (Boolean) -> Unit,
    private val parentComponent: JComponent,
    tooltip: String? = null,
    beforeText: Boolean = false,
  ) : TextComponentExtension(tooltip, beforeText) {
    override fun getIcon(hovered: Boolean): Icon = icon

    override fun getActionOnClick(): Runnable =
      Runnable {
        valueSetter(!valueGetter())
        parentComponent.repaint() // button selection will not update otherwise
      }

    override fun isSelected(): Boolean = valueGetter()
  }

  public class Clear(
    private val isEmpty: () -> Boolean,
    private val clearAction: () -> Unit,
    tooltip: String,
  ) : TextComponentExtension(tooltip, false) {
    override fun getIcon(hovered: Boolean): Icon? =
      when {
        isEmpty() -> null
        hovered -> AllIcons.Actions.CloseHovered
        else -> AllIcons.Actions.Close
      }

    override fun getActionOnClick(): Runnable = Runnable(clearAction)
  }
}
