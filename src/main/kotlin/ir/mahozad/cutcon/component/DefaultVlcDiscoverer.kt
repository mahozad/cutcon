package ir.mahozad.cutcon.component

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.assetsPath
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy
import kotlin.io.path.absolutePathString
import kotlin.io.path.div

class DefaultVlcDiscoverer : NativeDiscoveryStrategy {
    // See the build script for more information about assetsPath etc.
    override fun discover() = (assetsPath / BuildConfig.VLC_DIRECTORY_NAME).absolutePathString()
    override fun supported() = true
    override fun onFound(path: String) = true
    override fun onSetPluginPath(path: String) = true
}
