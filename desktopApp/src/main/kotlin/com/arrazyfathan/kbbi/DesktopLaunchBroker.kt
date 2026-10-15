package com.arrazyfathan.kbbi

import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.util.Base64
import java.util.UUID
import kotlin.concurrent.thread

/** Routes repeated CLI and macOS URL launches to the running desktop process. */
internal class DesktopLaunchBroker private constructor(
    private val directory: Path,
    private val lockChannel: FileChannel,
    private val lock: java.nio.channels.FileLock,
    private val endpointFile: Path,
    private val server: ServerSocket,
    private val token: String,
) : AutoCloseable {
    init {
        val endpoint = "$token\n${server.localPort}\n"
        Files.writeString(endpointFile, endpoint)
        runCatching {
            Files.setPosixFilePermissions(
                endpointFile,
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                ),
            )
        }
        thread(name = "kbbi-launch-forwarder", isDaemon = true) {
            while (!server.isClosed) {
                runCatching { server.accept().use(::receive) }
            }
        }
    }

    private fun receive(socket: Socket) {
        socket.soTimeout = 2_000
        val input = socket.getInputStream().bufferedReader(Charsets.UTF_8)
        if (input.readLine() != token) return
        val encoded = input.readLine() ?: return
        val args =
            runCatching {
                String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8).split('\u0000')
            }.getOrNull() ?: return
        DesktopLaunchRequests.parse(args)?.let(DesktopLaunchRequests::publish)
    }

    override fun close() {
        runCatching { server.close() }
        runCatching { Files.deleteIfExists(endpointFile) }
        runCatching { lock.release() }
        runCatching { lockChannel.close() }
    }

    companion object {
        fun startOrForward(arguments: List<String>): DesktopLaunchBroker? {
            val directory = Path.of(System.getProperty("user.home"), ".kbbi-kmp")
            Files.createDirectories(directory)
            val lockPath = directory.resolve("launch.lock")
            val channel =
                FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            val lock = runCatching { channel.tryLock() }.getOrNull()
            val endpointFile = directory.resolve("launch.endpoint")
            if (lock == null) {
                runCatching {
                    val lines = Files.readAllLines(endpointFile)
                    Socket(InetAddress.getLoopbackAddress(), lines[1].toInt()).use { socket ->
                        val output = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
                        output.append(lines[0]).append('\n')
                        val encoded =
                            Base64.getUrlEncoder().withoutPadding().encodeToString(
                                arguments.joinToString("\u0000").toByteArray(Charsets.UTF_8),
                            )
                        output.append(encoded).append('\n').flush()
                    }
                }
                channel.close()
                return null
            }
            val server = ServerSocket(0, 16, InetAddress.getLoopbackAddress())
            return DesktopLaunchBroker(
                directory,
                channel,
                lock,
                endpointFile,
                server,
                UUID.randomUUID().toString(),
            )
        }
    }
}
