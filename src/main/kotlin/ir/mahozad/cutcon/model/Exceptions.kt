package ir.mahozad.cutcon.model

class FFmpegNonZeroExitCodeException(exitCode: Int?) : Exception("Exit code $exitCode")

class FFmpegProcessStartFailureException(cause: Throwable) : Exception(cause)
