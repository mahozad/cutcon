package ir.mahozad.cutcon.component

import com.sun.jna.NativeLibrary
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.assetsPath
import ir.mahozad.cutcon.getCurrentOs
import ir.mahozad.cutcon.model.OS
import uk.co.caprica.vlcj.binding.lib.LibC
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil
import uk.co.caprica.vlcj.factory.discovery.strategy.BaseNativeDiscoveryStrategy
import kotlin.io.path.absolutePathString
import kotlin.io.path.div

// Adapted from uk.co.caprica.vlcj.factory.discovery.strategy.OsxNativeDiscoveryStrategy
// and uk.co.caprica.vlcj.factory.discovery.provider.DirectoryProviderDiscoveryStrategy
class MacOsVlcDiscoverer() : BaseNativeDiscoveryStrategy(
    arrayOf("libvlc\\.dylib", "libvlccore\\.dylib"),
    arrayOf("%s/plugins")
) {

    override fun supported() = getCurrentOs() == OS.MAC

    override fun discoveryDirectories() = listOf(
        (assetsPath / BuildConfig.VLC_DIRECTORY_NAME).absolutePathString()
    )

    override fun onFound(path: String): Boolean {
        forceLoadLibVlcCore(path)
        return true
    }

    override fun setPluginPath(pluginPath: String?) =
        LibC.INSTANCE.setenv(PLUGIN_ENV_NAME, pluginPath, 1) == 0

    /**
     * On later versions of OSX, it is necessary to force-load libvlccore before libvlc.
     * Otherwise, libvlc will fail to load.
     */
    private fun forceLoadLibVlcCore(path: String) {
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), path)
        NativeLibrary.getInstance(RuntimeUtil.getLibVlcCoreLibraryName())
    }
}
