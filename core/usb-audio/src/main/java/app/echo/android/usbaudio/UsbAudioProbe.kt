package app.echo.android.usbaudio

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbAudioProbe(context: Context) {
    private val appContext = context.applicationContext
    private val usbManager = appContext.getSystemService(Context.USB_SERVICE) as UsbManager
    private val descriptorParser = UsbAudioDescriptorParser()

    fun findBestDevice(): UsbDevice? =
        usbManager.deviceList.values
            .filter { it.isUsbAudioDevice() }
            .maxWithOrNull(
                compareBy<UsbDevice> { if (usbManager.hasPermission(it)) 1 else 0 }
                    .thenBy { it.interfaceCount },
            )

    fun snapshot(permissionPendingDeviceName: String? = null): UsbAudioDeviceSnapshot {
        val device = findBestDevice()
            ?: return UsbAudioDeviceSnapshot()
        val permissionGranted = usbManager.hasPermission(device)
        val pending = device.deviceName == permissionPendingDeviceName && !permissionGranted
        val descriptorResult = if (permissionGranted) readDescriptorInfo(device) else null

        return UsbAudioDeviceSnapshot(
            connected = true,
            deviceName = device.deviceName,
            displayName = device.getDisplayName(),
            vendorId = device.vendorId,
            productId = device.productId,
            permissionGranted = permissionGranted,
            permissionPending = pending,
            rawDescriptorAvailable = descriptorResult?.getOrNull() != null,
            descriptor = descriptorResult?.getOrNull() ?: UsbAudioDescriptorInfo(),
            descriptorError = descriptorResult?.exceptionOrNull()?.message,
        )
    }

    private fun readDescriptorInfo(device: UsbDevice): Result<UsbAudioDescriptorInfo> =
        runCatching {
            val connection = usbManager.openDevice(device)
                ?: error("Unable to open USB audio device")
            try {
                descriptorParser.parse(connection.rawDescriptors ?: ByteArray(0))
            } finally {
                connection.close()
            }
        }

    private fun UsbDevice.isUsbAudioDevice(): Boolean =
        deviceClass == UsbConstants.USB_CLASS_AUDIO ||
            (0 until interfaceCount).any { index ->
                getInterface(index).interfaceClass == UsbConstants.USB_CLASS_AUDIO
            }

    private fun UsbDevice.getDisplayName(): String =
        productName?.takeIf { it.isNotBlank() }
            ?: manufacturerName?.takeIf { it.isNotBlank() }
            ?: deviceName
}
