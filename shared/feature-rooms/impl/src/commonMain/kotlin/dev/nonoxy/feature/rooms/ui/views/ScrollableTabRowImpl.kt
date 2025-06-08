package dev.nonoxy.feature.rooms.ui.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import dev.nonoxy.core.design.theme.padding_size_16
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScrollableTabRowImpl(
    selectedTabIndex: Int,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    scrollState: ScrollState = rememberScrollState(),
    indicator: @Composable TabIndicatorScope.() -> Unit,
    divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
    tabs: @Composable () -> Unit
) {
    Surface(
        modifier =
        modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .selectableGroup()
            .clipToBounds(),
        color = containerColor,
        contentColor = contentColor
    ) {
        val coroutineScope = rememberCoroutineScope()
        val scrollableTabData =
            remember(scrollState, coroutineScope) {
                ScrollableTabData(scrollState = scrollState, coroutineScope = coroutineScope)
            }

        val scope = remember {
            object : TabIndicatorScope, TabPositionsHolder {

                val tabPositions = mutableStateOf<(List<TabPosition>)>(listOf())

                override fun Modifier.tabIndicatorLayout(
                    measure:
                    MeasureScope.(
                        Measurable,
                        Constraints,
                        List<TabPosition>,
                    ) -> MeasureResult
                ): Modifier =
                    this.layout { measurable: Measurable, constraints: Constraints ->
                        measure(
                            measurable,
                            constraints,
                            tabPositions.value,
                        )
                    }

                override fun Modifier.tabIndicatorOffset(
                    selectedTabIndex: Int,
                    matchContentSize: Boolean
                ): Modifier =
                    this.then(
                        TabIndicatorModifier(tabPositions, selectedTabIndex, matchContentSize)
                    )

                override fun setTabPositions(positions: List<TabPosition>) {
                    tabPositions.value = positions
                }
            }
        }

        Layout(
            contents =
            listOf(
                tabs,
                divider,
                { scope.indicator() },
            )
        ) { (tabMeasurables, dividerMeasurables, indicatorMeasurables), constraints ->
            val padding = edgePadding.roundToPx()
            val tabCount = tabMeasurables.size
            val minTabWidth = ScrollableTabRowMinimumTabWidth.roundToPx()
            val layoutHeight =
                tabMeasurables.fastFold(initial = 0) { curr, measurable ->
                    maxOf(curr, measurable.maxIntrinsicHeight(Constraints.Infinity))
                }
            var layoutWidth = padding * 2
            val tabConstraints =
                constraints.copy(
                    minWidth = minTabWidth,
                    minHeight = layoutHeight,
                    maxHeight = layoutHeight,
                )

            var left = edgePadding
            val tabPlaceables = tabMeasurables.fastMap { it.measure(tabConstraints) }

            val positions =
                List(tabCount) { index ->
                    val tabWidth =
                        maxOf(ScrollableTabRowMinimumTabWidth, tabPlaceables[index].width.toDp())
                    layoutWidth += tabWidth.roundToPx()
                    // Enforce minimum touch target of 24.dp
                    val contentWidth = maxOf(tabWidth - (padding_size_16 * 2), 24.dp)
                    val tabPosition =
                        TabPosition(left = left, width = tabWidth, contentWidth = contentWidth)
                    left += tabWidth
                    tabPosition
                }
            scope.setTabPositions(positions)

            val dividerPlaceables =
                dividerMeasurables.fastMap {
                    it.measure(
                        constraints.copy(
                            minHeight = 0,
                            minWidth = layoutWidth,
                            maxWidth = layoutWidth
                        )
                    )
                }

            val indicatorPlaceables =
                indicatorMeasurables.fastMap {
                    it.measure(
                        constraints.copy(
                            minWidth = 0,
                            maxWidth = positions[selectedTabIndex].width.roundToPx(),
                            minHeight = 0,
                            maxHeight = layoutHeight
                        )
                    )
                }

            layout(layoutWidth, layoutHeight) {
                left = edgePadding
                tabPlaceables.fastForEachIndexed { index, placeable ->
                    placeable.placeRelative(left.roundToPx(), 0)
                    left += positions[index].width
                }

                dividerPlaceables.fastForEach { placeable ->
                    placeable.placeRelative(0, layoutHeight - placeable.height)
                }

                indicatorPlaceables.fastForEach {
                    val relativeOffset =
                        max(0, (positions[selectedTabIndex].width.roundToPx() - it.width) / 2)
                    it.placeRelative(relativeOffset, layoutHeight - it.height)
                }

                scrollableTabData.onLaidOut(
                    density = this@Layout,
                    edgeOffset = padding,
                    tabPositions = positions,
                    selectedTab = selectedTabIndex
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
internal interface TabIndicatorScope {

    /**
     * A layout modifier that provides tab positions, this can be used to animate and layout a
     * TabIndicator depending on size, position, and content size of each Tab.
     *
     * @sample androidx.compose.material3.samples.FancyAnimatedIndicatorWithModifier
     */
    fun Modifier.tabIndicatorLayout(
        measure:
        MeasureScope.(
            Measurable,
            Constraints,
            List<TabPosition>,
        ) -> MeasureResult
    ): Modifier

    /**
     * A Modifier that follows the default offset and animation
     *
     * @param selectedTabIndex the index of the current selected tab
     * @param matchContentSize this modifier can also animate the width of the indicator \ to match
     *   the content size of the tab.
     */
    fun Modifier.tabIndicatorOffset(
        selectedTabIndex: Int,
        matchContentSize: Boolean = false
    ): Modifier
}

internal interface TabPositionsHolder {

    fun setTabPositions(positions: List<TabPosition>)
}

@Immutable
class TabPosition internal constructor(val left: Dp, val width: Dp, val contentWidth: Dp) {

    val right: Dp
        get() = left + width

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TabPosition) return false

        if (left != other.left) return false
        if (width != other.width) return false
        if (contentWidth != other.contentWidth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + contentWidth.hashCode()
        return result
    }

    override fun toString(): String {
        return "TabPosition(left=$left, right=$right, width=$width, contentWidth=$contentWidth)"
    }
}

internal data class TabIndicatorModifier(
    val tabPositionsState: State<List<TabPosition>>,
    val selectedTabIndex: Int,
    val followContentSize: Boolean,
) : ModifierNodeElement<TabIndicatorOffsetNode>() {

    override fun create(): TabIndicatorOffsetNode {
        return TabIndicatorOffsetNode(
            tabPositionsState = tabPositionsState,
            selectedTabIndex = selectedTabIndex,
            followContentSize = followContentSize,
        )
    }

    override fun update(node: TabIndicatorOffsetNode) {
        node.tabPositionsState = tabPositionsState
        node.selectedTabIndex = selectedTabIndex
        node.followContentSize = followContentSize
    }

    override fun InspectorInfo.inspectableProperties() {
        // Show nothing in the inspector.
    }
}

internal class TabIndicatorOffsetNode(
    var tabPositionsState: State<List<TabPosition>>,
    var selectedTabIndex: Int,
    var followContentSize: Boolean
) : Modifier.Node(), LayoutModifierNode {

    private var offsetAnimatable: Animatable<Dp, AnimationVector1D>? = null
    private var widthAnimatable: Animatable<Dp, AnimationVector1D>? = null
    private var initialOffset: Dp? = null
    private var initialWidth: Dp? = null

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        if (tabPositionsState.value.isEmpty()) {
            return layout(0, 0) {}
        }

        val currentTabWidth =
            if (followContentSize) {
                tabPositionsState.value[selectedTabIndex].contentWidth
            } else {
                tabPositionsState.value[selectedTabIndex].width
            }

        if (initialWidth != null) {
            val widthAnim =
                widthAnimatable
                    ?: Animatable(initialWidth!!, Dp.VectorConverter).also { widthAnimatable = it }

            if (currentTabWidth != widthAnim.targetValue) {
                coroutineScope.launch { widthAnim.animateTo(currentTabWidth, TabRowIndicatorSpec) }
            }
        } else {
            initialWidth = currentTabWidth
        }

        val indicatorOffset = tabPositionsState.value[selectedTabIndex].left

        if (initialOffset != null) {
            val offsetAnim =
                offsetAnimatable
                    ?: Animatable(initialOffset!!, Dp.VectorConverter).also {
                        offsetAnimatable = it
                    }

            if (indicatorOffset != offsetAnim.targetValue) {
                coroutineScope.launch { offsetAnim.animateTo(indicatorOffset, TabRowIndicatorSpec) }
            }
        } else {
            initialOffset = indicatorOffset
        }

        val offset =
            if (layoutDirection == LayoutDirection.Ltr) {
                offsetAnimatable?.value ?: indicatorOffset
            } else {
                -(offsetAnimatable?.value ?: indicatorOffset)
            }

        val width = widthAnimatable?.value ?: currentTabWidth

        val placeable =
            measurable.measure(
                constraints.copy(minWidth = width.roundToPx(), maxWidth = width.roundToPx())
            )

        return layout(placeable.width, placeable.height) { placeable.place(offset.roundToPx(), 0) }
    }
}

/** Class holding onto state needed for [ScrollableTabRow] */
private class ScrollableTabData(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope
) {
    private var selectedTab: Int? = null

    fun onLaidOut(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
        selectedTab: Int
    ) {
        // Animate if the new tab is different from the old tab, or this is called for the first
        // time (i.e selectedTab is `null`).
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let {
                // Scrolls to the tab with [tabPosition], trying to place it in the center of the
                // screen or as close to the center as possible.
                val calculatedOffset = it.calculateTabOffset(density, edgeOffset, tabPositions)
                if (scrollState.value != calculatedOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            calculatedOffset,
                            animationSpec = ScrollableTabRowScrollSpec
                        )
                    }
                }
            }
        }
    }

    /**
     * @return the offset required to horizontally center the tab inside this TabRow. If the tab is
     *   at the start / end, and there is not enough space to fully centre the tab, this will just
     *   clamp to the min / max position given the max width.
     */
    private fun TabPosition.calculateTabOffset(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>
    ): Int =
        with(density) {
            val totalTabRowWidth = tabPositions.last().right.roundToPx() + edgeOffset
            val visibleWidth = totalTabRowWidth - scrollState.maxValue
            val tabOffset = left.roundToPx()
            val scrollerCenter = visibleWidth / 2
            val tabWidth = width.roundToPx()
            val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
            // How much space we have to scroll. If the visible width is <= to the total width, then
            // we have no space to scroll as everything is always visible.
            val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
            return centeredTabOffset.coerceIn(0, availableSpace)
        }
}

private val ScrollableTabRowMinimumTabWidth = 90.dp

/** [AnimationSpec] used when scrolling to a tab that is not fully visible. */
private val ScrollableTabRowScrollSpec: AnimationSpec<Float> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)

/** [AnimationSpec] used when an indicator is updating width and/or offset. */
private val TabRowIndicatorSpec: AnimationSpec<Dp> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)
