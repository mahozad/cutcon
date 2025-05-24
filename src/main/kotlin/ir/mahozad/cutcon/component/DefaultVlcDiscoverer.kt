package ir.mahozad.cutcon.component

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.assetsPath
import ir.mahozad.cutcon.getCurrentOs
import ir.mahozad.cutcon.model.OS
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy
import kotlin.io.path.absolutePathString
import kotlin.io.path.div

class DefaultVlcDiscoverer : NativeDiscoveryStrategy {

    private val logger = logger(name = javaClass.simpleName)

    override fun supported() = getCurrentOs() != OS.MAC
    override fun discover() = (assetsPath / BuildConfig.VLC_DIRECTORY_NAME).absolutePathString()
    override fun onFound(path: String): Boolean {
        logger.debug { "Found native VLC libraries in $path" }
        return true
    }
    override fun onSetPluginPath(path: String) = true
}
