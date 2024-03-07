package ir.mahozad.cutcon.ui.panel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultIconSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.icon.Config
import ir.mahozad.cutcon.ui.icon.Icons
import ir.mahozad.cutcon.ui.icon.Info
import ir.mahozad.cutcon.ui.icon.Settings
import ir.mahozad.cutcon.viewModel

@Composable
fun FrameWindowScope.SidePanel() {
    val sidePanelSelectedTabIndex by viewModel.sidePanelSelectedTabIndex.collectAsState()
    Column {
        TabRow(
            selectedTabIndex = sidePanelSelectedTabIndex,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier.height(36.dp)
        ) {
            Tab(
                selected = sidePanelSelectedTabIndex == 0,
                text = {
                    Icon(
                        imageVector = Icons.Custom.Config,
                        contentDescription = Messages.ICO_DSC_PANEL_CONFIG,
                        modifier = Modifier.size(defaultIconSize)
                    )
                },
                onClick = { viewModel.setSidePanelSelectedTabIndex(0) }
            )
            Tab(
                selected = sidePanelSelectedTabIndex == 1,
                text = {
                    Icon(
                        imageVector = Icons.Custom.Settings,
                        contentDescription = Messages.ICO_DSC_PANEL_SETTINGS,
                        modifier = Modifier.size(defaultIconSize)
                    )
                },
                onClick = { viewModel.setSidePanelSelectedTabIndex(1) }
            )
            Tab(
                selected = sidePanelSelectedTabIndex == 2,
                text = {
                    Icon(
                        imageVector = Icons.Custom.Info,
                        contentDescription = Messages.ICO_DSC_PANEL_ABOUT,
                        modifier = Modifier.size(defaultIconSize)
                    )
                },
                onClick = { viewModel.setSidePanelSelectedTabIndex(2) }
            )
        }
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LocalLanguage.current.layoutDirection) {
            when (sidePanelSelectedTabIndex) {
                0 -> ConfigPanel(viewModel)
                1 -> SettingsPanel()
                2 -> AboutPanel()
            }
        }
    }
}
